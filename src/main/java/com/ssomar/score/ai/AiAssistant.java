package com.ssomar.score.ai;

import com.ssomar.score.SCore;
import com.ssomar.score.hub.DiagnosticReport;
import com.ssomar.score.hub.InstalledPlugin;
import com.ssomar.score.hub.KnownPlugin;
import com.ssomar.score.hub.LoadErrorCollector;
import org.bukkit.entity.Player;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * One question to the SPlugins assistant: the server context is attached, the question goes to splugins.net, and
 * while the answer is being written the server answers the tool calls of the model (read tools, run on the server
 * thread). One question at a time per admin; the conversation continues until "New conversation".
 */
public final class AiAssistant {

    static final int CHAT_TIMEOUT_MS = 300_000, POLL_TIMEOUT_MS = 35_000, TOOL_TIMEOUT_S = 10, MAX_RESULT = 100_000;

    public interface View {
        void thinking(Player p);

        void answer(Player p, String markdown);

        /** code: unlinked, quota, rate_limited, too_long, ai_error, network...; upgradeUrl may be empty */
        void error(Player p, String code, String message, String upgradeUrl);
    }

    private static final Map<UUID, String> CONVERSATIONS = new ConcurrentHashMap<>();
    private static final Set<UUID> BUSY = ConcurrentHashMap.newKeySet();

    private AiAssistant() {
    }

    public static boolean isBusy(Player p) {
        return BUSY.contains(p.getUniqueId());
    }

    public static boolean hasConversation(Player p) {
        return CONVERSATIONS.containsKey(p.getUniqueId());
    }

    public static void newConversation(Player p) {
        CONVERSATIONS.remove(p.getUniqueId());
    }

    /** the request body, built on the server thread (reads the plugins and the error lines) */
    static JSONObject request(String question, String conversationId, String focus, String turnId) {
        JSONObject ctx = new JSONObject().put("diagnostic", DiagnosticReport.build());
        JSONObject errors = new JSONObject();
        List<String> plugins = new ArrayList<>();
        KnownPlugin focused = focus == null ? null : KnownPlugin.byKey(focus);
        if (focused != null && !focused.getDocsSlug().isEmpty()) plugins.add(focused.getDocsSlug());
        for (InstalledPlugin ip : InstalledPlugin.scan()) {
            KnownPlugin k = ip.getKnown();
            List<String> lines = LoadErrorCollector.linesOf(k.getPluginName());
            if (!lines.isEmpty()) errors.put(k.getPluginName(), new JSONArray(lines.subList(Math.max(0, lines.size() - 30), lines.size())));
            if (!k.getDocsSlug().isEmpty() && !plugins.contains(k.getDocsSlug()) && plugins.size() < 3) plugins.add(k.getDocsSlug());
        }
        ctx.put("loadErrors", errors);
        return new JSONObject().put("message", question).put("conversationId", conversationId == null ? JSONObject.NULL : conversationId)
                .put("plugins", new JSONArray(plugins)).put("context", ctx).put("tools", AiTools.declarations(false)).put("turnId", turnId);
    }

    /** @return false if the player already has a question running or the server is not linked */
    public static boolean ask(Player p, String question, String focusPlugin, View view) {
        if (!AiLink.isLinked() || question == null || question.trim().isEmpty()) return false;
        if (!BUSY.add(p.getUniqueId())) return false;
        final String token = AiLink.token();
        final String turnId = UUID.randomUUID().toString();
        final JSONObject body;
        try {
            body = request(question.trim(), CONVERSATIONS.get(p.getUniqueId()), focusPlugin, turnId);
        } catch (Throwable t) {
            BUSY.remove(p.getUniqueId());
            view.error(p, "context", String.valueOf(t.getMessage()), "");
            return false;
        }
        view.thinking(p);
        final AtomicBoolean done = new AtomicBoolean(false);
        SCore.schedulerHook.runAsyncTask(() -> bridge(token, turnId, done), 0);
        SCore.schedulerHook.runAsyncTask(() -> {
            AiClient.Response r = null;
            String failure = null;
            try {
                r = AiClient.post("/chat", body, token, CHAT_TIMEOUT_MS);
            } catch (Throwable t) {
                failure = t.getMessage();
            } finally {
                done.set(true);
                BUSY.remove(p.getUniqueId());
            }
            final AiClient.Response res = r;
            final String fail = failure;
            SCore.schedulerHook.runEntityTask(() -> {
                if (!p.isOnline()) return;
                if (res == null) {
                    view.error(p, "network", fail == null ? "no answer" : fail, "");
                    return;
                }
                if (!res.ok()) {
                    if ("unlinked".equals(res.errorCode())) AiLink.forget();
                    view.error(p, res.errorCode(), res.errorMessage(), res.body.optString("upgradeUrl", ""));
                    return;
                }
                String conv = res.body.optString("conversationId", "");
                if (!conv.isEmpty()) CONVERSATIONS.put(p.getUniqueId(), conv);
                view.answer(p, res.body.optString("answer", ""));
            }, null, p, 0L);
        }, 0);
        return true;
    }

    /** answers the tool calls of the turn until the question is over */
    static void bridge(String token, String turnId, AtomicBoolean done) {
        int failures = 0;
        while (!done.get() && failures < 8 && SCore.plugin.isEnabled()) {
            try {
                AiClient.Response r = AiClient.get("/bridge/poll?turnId=" + turnId, token, POLL_TIMEOUT_MS);
                if (!r.ok()) {
                    failures++;
                    Thread.sleep(1000);
                    continue;
                }
                failures = 0;
                JSONArray calls = r.body.optJSONArray("calls");
                if (calls != null) for (int i = 0; i < calls.length(); i++) {
                    JSONObject call = calls.getJSONObject(i);
                    JSONObject result = new JSONObject().put("callId", call.getString("callId"));
                    try {
                        result.put("result", truncate(run(call.getString("name"), call.optJSONObject("arguments"))));
                    } catch (Throwable t) {
                        result.put("error", t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage());
                    }
                    AiClient.post("/bridge/result", result, token, 10_000);
                }
                if (r.body.optBoolean("done", false)) return;
            } catch (Throwable t) {
                failures++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    /** a tool, on the server thread (10 s at most) */
    static String run(String name, JSONObject args) throws Exception {
        AiTool tool = AiTools.get(name);
        if (tool == null) throw new IllegalArgumentException("unknown tool " + name);
        if (tool.isWrite()) throw new IllegalStateException("write tools are not allowed");
        CompletableFuture<Object> f = new CompletableFuture<>();
        JSONObject a = args == null ? new JSONObject() : args;
        SCore.schedulerHook.runTask(() -> {
            try {
                f.complete(tool.execute(a));
            } catch (Throwable t) {
                f.completeExceptionally(t);
            }
        }, 0);
        try {
            Object o = f.get(TOOL_TIMEOUT_S, TimeUnit.SECONDS);
            return o == null ? "" : o.toString();
        } catch (java.util.concurrent.ExecutionException e) {
            Throwable c = e.getCause() == null ? e : e.getCause();
            throw new Exception(c.getMessage() == null ? c.getClass().getSimpleName() : c.getMessage());
        }
    }

    static String truncate(String s) {
        return s.length() <= MAX_RESULT ? s : s.substring(0, MAX_RESULT) + "\n[truncated]";
    }
}
