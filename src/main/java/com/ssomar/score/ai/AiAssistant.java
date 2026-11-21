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

    static final int CHAT_TIMEOUT_MS = 600_000, POLL_TIMEOUT_MS = 35_000, TOOL_TIMEOUT_S = 10, CONFIRM_TIMEOUT_S = 180, MAX_RESULT = 100_000;

    /** the admins who may confirm the changes proposed by the assistant (write tools) */
    public static final String WRITE_PERMISSION = "score.ai.write";

    public interface View {
        void thinking(Player p);

        void answer(Player p, String markdown);

        /** code: unlinked, quota, rate_limited, too_long, ai_error, network...; upgradeUrl may be empty */
        void error(Player p, String code, String message, String upgradeUrl);

        /** show a change proposed by a write tool; decide(true) applies it, decide(false) refuses it (once) */
        void confirm(Player p, AiChange change, java.util.function.Consumer<Boolean> decide);

        /** the change was applied (result = what the tool returned) or could not be */
        void applied(Player p, AiChange change, JSONObject result);
    }

    private static final Set<UUID> BUSY = ConcurrentHashMap.newKeySet();

    private AiAssistant() {
    }

    public static boolean isBusy(Player p) {
        return BUSY.contains(p.getUniqueId());
    }

    public static boolean hasConversation(Player p) {
        return AiHistory.has(p.getUniqueId());
    }

    public static void newConversation(Player p) {
        AiHistory.clear(p.getUniqueId());
    }

    /** the request body, built on the server thread (reads the plugins and the error lines) */
    static JSONObject request(String question, String conversationId, String focus, String turnId, boolean write) {
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
                .put("plugins", new JSONArray(plugins)).put("context", ctx).put("tools", AiTools.declarations(write)).put("turnId", turnId);
    }

    /** @return false if the player already has a question running or the server is not linked */
    public static boolean ask(Player p, String question, String focusPlugin, View view) {
        if (!AiLink.isLinked() || question == null || question.trim().isEmpty()) return false;
        if (!BUSY.add(p.getUniqueId())) return false;
        final String token = AiLink.token();
        final String turnId = UUID.randomUUID().toString();
        final JSONObject body;
        try {
            body = request(question.trim(), AiHistory.conversationId(p.getUniqueId()), focusPlugin, turnId, p.hasPermission(WRITE_PERMISSION));
        } catch (Throwable t) {
            BUSY.remove(p.getUniqueId());
            view.error(p, "context", String.valueOf(t.getMessage()), "");
            return false;
        }
        view.thinking(p);
        final AtomicBoolean done = new AtomicBoolean(false);
        SCore.schedulerHook.runAsyncTask(() -> bridge(token, turnId, done, p, view), 0);
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
                    if ("unlinked".equalsIgnoreCase(res.errorCode())) AiLink.forget();
                    view.error(p, res.errorCode(), res.errorMessage(), res.body.optString("upgradeUrl", ""));
                    return;
                }
                String conv = res.body.optString("conversationId", "");
                String answer = res.body.optString("answer", "");
                AiHistory.add(p.getUniqueId(), conv, question.trim(), answer);
                view.answer(p, answer);
            }, null, p, 0L);
        }, 0);
        return true;
    }

    /** answers the tool calls of the turn until the question is over */
    static void bridge(String token, String turnId, AtomicBoolean done, Player p, View view) {
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
                        result.put("result", truncate(run(call.getString("name"), call.optJSONObject("arguments"), p, view)));
                    } catch (Throwable t) {
                        result.put("error", t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage());
                    }
                    AiClient.post("/bridge/result", result, token, 10_000);
                }
                // the site may say "done" for a turn it has not created yet (our first poll can arrive before the
                // /chat request reached it): only our own /chat answer ends the bridge
                if (r.body.optBoolean("done", false)) Thread.sleep(300);
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

    /** a tool, on the server thread (10 s at most); a write tool waits for the admin's confirmation */
    static String run(String name, JSONObject args, Player p, View view) throws Exception {
        AiTool tool = AiTools.get(name);
        if (tool == null) throw new IllegalArgumentException("unknown tool " + name);
        if (tool.isWrite()) {
            if (p == null || !p.isOnline()) throw new IllegalStateException("the admin left the game: nothing was changed");
            if (!p.hasPermission(WRITE_PERMISSION))
                throw new IllegalStateException("the admin does not have the permission " + WRITE_PERMISSION + ": give the configuration to copy instead");
        }
        JSONObject a = args == null ? new JSONObject() : args;
        Object o = onServerThread(() -> tool.execute(a));
        if (o instanceof AiChange) {
            if (!tool.isWrite()) throw new IllegalStateException("only a write tool can propose a change");
            return confirmAndApply(p, (AiChange) o, view).toString();
        }
        return o == null ? "" : o.toString();
    }

    interface Body {
        Object run() throws Exception;
    }

    static Object onServerThread(Body body) throws Exception {
        CompletableFuture<Object> f = new CompletableFuture<>();
        SCore.schedulerHook.runTask(() -> {
            try {
                f.complete(body.run());
            } catch (Throwable t) {
                f.completeExceptionally(t);
            }
        }, 0);
        try {
            return f.get(TOOL_TIMEOUT_S, TimeUnit.SECONDS);
        } catch (java.util.concurrent.ExecutionException e) {
            Throwable c = e.getCause() == null ? e : e.getCause();
            throw new Exception(c.getMessage() == null ? c.getClass().getSimpleName() : c.getMessage());
        }
    }

    /** the admin sees the change in the game; applied only on their yes (3 minutes at most) */
    static JSONObject confirmAndApply(Player p, AiChange change, View view) throws Exception {
        CompletableFuture<Boolean> decision = new CompletableFuture<>();
        java.util.function.Consumer<Boolean> decide = b -> {
            // a click after the 3 minutes (or a second click): nothing happens, the admin is told
            if (!decision.complete(b) && Boolean.TRUE.equals(b))
                view.applied(p, change, new JSONObject().put("applied", false).put("expired", true));
        };
        SCore.schedulerHook.runEntityTask(() -> view.confirm(p, change, decide), null, p, 0L);
        Boolean yes;
        try {
            yes = decision.get(CONFIRM_TIMEOUT_S, TimeUnit.SECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            decision.complete(false);
            return new JSONObject().put("applied", false).put("reason", "the admin did not answer within 3 minutes: nothing was changed");
        }
        if (!Boolean.TRUE.equals(yes))
            return new JSONObject().put("applied", false).put("reason", "the admin refused this change: nothing was changed. Do not propose it again unless they ask.");
        if (!p.isOnline()) return new JSONObject().put("applied", false).put("reason", "the admin left the game: nothing was changed");
        JSONObject result;
        try {
            Object r = onServerThread(() -> change.apply(p.getName()));
            result = r instanceof JSONObject ? (JSONObject) r : new JSONObject().put("applied", true).put("result", String.valueOf(r));
        } catch (Exception e) {
            result = new JSONObject().put("applied", false).put("reason", "could not apply the change: " + e.getMessage());
        }
        final JSONObject res = result;
        SCore.schedulerHook.runEntityTask(() -> view.applied(p, change, res), null, p, 0L);
        return result;
    }

    static String truncate(String s) {
        return s.length() <= MAX_RESULT ? s : s.substring(0, MAX_RESULT) + "\n[truncated]";
    }
}
