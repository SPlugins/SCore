package com.ssomar.score.ai.tools;

import com.ssomar.score.ai.AiTool;
import com.ssomar.score.ai.AiTools;
import com.ssomar.score.hub.DiagnosticReport;
import com.ssomar.score.hub.InstalledPlugin;
import com.ssomar.score.hub.LoadErrorCollector;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

/** Read tools about the server and the SPlugins plugins installed on it. */
public final class ServerTools {

    private ServerTools() {
    }

    public static List<AiTool> all() {
        return Arrays.asList(diagnostic(), plugins(), errors());
    }

    static AiTool diagnostic() {
        return new Simple("server_diagnostic",
                "Server software and version, Java, the SPlugins plugins with their version and edition (Free / Premium), and the other plugins installed.",
                AiTools.schema(new String[0][]), a -> DiagnosticReport.build());
    }

    static AiTool plugins() {
        return new Simple("list_plugins",
                "The SPlugins plugins installed: version, Free or Premium, number of loaded objects, free limit, number of load errors.",
                AiTools.schema(new String[0][]), a -> {
                    JSONArray out = new JSONArray();
                    for (InstalledPlugin ip : InstalledPlugin.scan()) {
                        JSONObject o = new JSONObject().put("plugin", ip.getKnown().getPluginName()).put("version", ip.getVersion())
                                .put("enabled", ip.isEnabled()).put("objects", ip.getObjects()).put("loadErrors", ip.getErrors());
                        if (ip.getPremium() != null) o.put("edition", ip.getPremium() ? "premium" : "free");
                        if (ip.getFreeLimit() > 0 && Boolean.FALSE.equals(ip.getPremium())) o.put("freeLimit", ip.getFreeLimit());
                        out.put(o);
                    }
                    return out;
                });
    }

    static AiTool errors() {
        return new Simple("load_errors",
                "The configuration errors printed when the plugin loaded its files (last 300 lines of the server). Empty = no error.",
                AiTools.schema(new String[][]{{"plugin", "string", "plugin name, e.g. ExecutableItems"}}, "plugin"),
                a -> {
                    List<String> lines = LoadErrorCollector.linesOf(a.getString("plugin"));
                    int from = Math.max(0, lines.size() - 60);
                    return new JSONObject().put("count", lines.size()).put("lines", new JSONArray(lines.subList(from, lines.size())));
                });
    }

    /** a tool from a lambda */
    public static final class Simple implements AiTool {
        public interface Body {
            Object run(JSONObject args) throws Exception;
        }

        private final String name, description;
        private final JSONObject schema;
        private final Body body;

        public Simple(String name, String description, JSONObject schema, Body body) {
            this.name = name;
            this.description = description;
            this.schema = schema;
            this.body = body;
        }

        @Override public String name() { return name; }
        @Override public String description() { return description; }
        @Override public JSONObject inputSchema() { return schema; }
        @Override public Object execute(JSONObject arguments) throws Exception { return body.run(arguments); }
    }
}
