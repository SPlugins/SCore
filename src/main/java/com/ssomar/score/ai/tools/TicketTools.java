package com.ssomar.score.ai.tools;

import com.ssomar.score.ai.AiChange;
import com.ssomar.score.ai.AiClient;
import com.ssomar.score.ai.AiLink;
import com.ssomar.score.ai.AiTool;
import com.ssomar.score.hub.InstalledPlugin;
import org.bukkit.Bukkit;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Tickets on the SPlugins Discord, opened by the assistant: a bug report (🚨 bugs-errors-issues) or a suggestion
 * (💡 suggestions). The admin sees the ticket in the same form as the changes and confirms it; splugins.net posts it
 * and mentions the admin (their Discord account if linked, else their splugins.net name). The server versions are
 * added here, so the ticket always carries them.
 */
public final class TicketTools {

    private TicketTools() {
    }

    public static List<AiTool> all() {
        return Arrays.asList(open());
    }

    /** server software and the SPlugins plugins with their version and edition, on one line */
    static String environment() {
        StringBuilder sb = new StringBuilder(Bukkit.getName()).append(' ').append(Bukkit.getBukkitVersion().split("-")[0]);
        for (InstalledPlugin ip : InstalledPlugin.scan()) {
            sb.append(" · ").append(ip.getKnown().getPluginName()).append(' ').append(ip.getVersion());
            if (ip.getPremium() != null) sb.append(ip.getPremium() ? " Premium" : " Free");
        }
        String s = sb.toString();
        return s.length() > 600 ? s.substring(0, 600) + "…" : s;
    }

    static AiTool open() {
        final JSONObject schema = new JSONObject().put("type", "object").put("required", new JSONArray().put("type").put("plugin").put("title").put("description"))
                .put("properties", new JSONObject()
                        .put("type", new JSONObject().put("type", "string").put("enum", new JSONArray().put("bug").put("suggestion")))
                        .put("plugin", new JSONObject().put("type", "string").put("description", "the plugin concerned, e.g. ExecutableItems"))
                        .put("title", new JSONObject().put("type", "string").put("description", "short and precise title, in English (100 characters at most)"))
                        .put("description", new JSONObject().put("type", "string").put("description",
                                "in English, markdown: for a bug what happens, what was expected, steps to reproduce and the error lines; for a suggestion the need and the proposed option. 1500 characters at most"))
                        .put("config", new JSONObject().put("type", "string").put("description", "optional: the smallest YAML that shows the problem")));
        return new AiTool() {
            @Override
            public String name() {
                return "open_ticket";
            }

            @Override
            public String description() {
                return "Open a ticket on the SPlugins Discord: a bug of a plugin (not a configuration mistake) or a suggestion (a feature that does not exist). "
                        + "The admin sees the ticket in the game and confirms it; the server versions are added automatically and the admin is mentioned. Returns the ticket link.";
            }

            @Override
            public JSONObject inputSchema() {
                return schema;
            }

            @Override
            public boolean isWrite() {
                return true;
            }

            @Override
            public String permission() {
                return "score.hub";
            }

            @Override
            public Object execute(JSONObject a) {
                String type = a.optString("type", "").toLowerCase(Locale.ROOT);
                if (!type.equals("bug") && !type.equals("suggestion")) throw new IllegalArgumentException("type must be bug or suggestion");
                String plugin = a.optString("plugin", "").trim();
                String title = a.optString("title", "").trim();
                String description = a.optString("description", "").trim();
                String config = a.optString("config", "").trim();
                if (title.isEmpty() || description.isEmpty()) throw new IllegalArgumentException("title and description are needed");
                if (title.length() > 100) title = title.substring(0, 100);
                if (description.length() > 1500) description = description.substring(0, 1500) + "…";
                if (config.length() > 1200) config = config.substring(0, 1200) + "\n…";
                String env = environment();
                StringBuilder preview = new StringBuilder().append(title).append("\n").append(plugin).append(" — ").append(env).append("\n\n").append(description);
                if (!config.isEmpty()) preview.append("\n\n").append(config);
                final JSONObject body = new JSONObject().put("type", type).put("plugin", plugin).put("title", title).put("description", description)
                        .put("config", config).put("environment", env);
                String target = "Discord › " + (type.equals("bug") ? "🚨 bugs-errors-issues" : "💡 suggestions") + " › " + plugin;
                return AiChange.text(target, title, preview.toString(), actor -> {
                    body.put("reporter", actor);
                    AiClient.Response r = AiClient.post("/ticket", body, AiLink.token(), 20_000);
                    if (!r.ok())
                        return new JSONObject().put("applied", false).put("reason", "the ticket could not be opened: " + r.errorMessage()
                                + ". Give the admin the text to post themselves on the SPlugins Discord.");
                    return new JSONObject().put("applied", true).put("url", r.body.optString("url", "")).put("mentioned", r.body.optString("mentioned", ""));
                }).offThread();
            }
        };
    }
}
