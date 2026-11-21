package com.ssomar.score.ai;

import com.ssomar.score.ai.tools.ObjectTools;
import com.ssomar.score.ai.tools.ServerTools;
import com.ssomar.score.ai.tools.WriteTools;
import org.bukkit.plugin.Plugin;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Registry of the tools offered to the SPlugins assistant. */
public final class AiTools {

    private static final Map<String, AiTool> TOOLS = new ConcurrentHashMap<>();
    private static final Map<String, String> OWNERS = new ConcurrentHashMap<>();

    private AiTools() {
    }

    /** Add a tool (replaces a tool with the same name). */
    public static void register(Plugin owner, AiTool tool) {
        TOOLS.put(tool.name(), tool);
        OWNERS.put(tool.name(), owner == null ? "?" : owner.getName());
    }

    /** Remove every tool of a plugin (on disable). */
    public static void unregisterAll(Plugin owner) {
        for (Map.Entry<String, String> e : new ArrayList<>(OWNERS.entrySet()))
            if (owner != null && owner.getName().equals(e.getValue())) {
                TOOLS.remove(e.getKey());
                OWNERS.remove(e.getKey());
            }
    }

    public static AiTool get(String name) {
        return TOOLS.get(name);
    }

    public static List<AiTool> all() {
        return new ArrayList<>(TOOLS.values());
    }

    /** declarations sent with a question; the write tools only for an admin allowed to confirm changes */
    public static JSONArray declarations(java.util.function.Predicate<String> hasPermission) {
        JSONArray a = new JSONArray();
        for (AiTool t : TOOLS.values()) {
            String perm = t.permission();
            if (perm != null && !hasPermission.test(perm)) continue;
            a.put(new JSONObject().put("name", t.name()).put("description", t.description())
                    .put("inputSchema", t.inputSchema()).put("write", t.isWrite()));
        }
        return a;
    }

    /** the SCore tools (work for every SPlugins plugin through SCore's loaders) */
    public static void registerBuiltins(Plugin score) {
        for (AiTool t : ServerTools.all()) register(score, t);
        for (AiTool t : ObjectTools.all()) register(score, t);
        for (AiTool t : WriteTools.all()) register(score, t);
        for (AiTool t : com.ssomar.score.ai.tools.TicketTools.all()) register(score, t);
    }

    /* small helpers for schemas */
    public static JSONObject schema(String[][] props, String... required) {
        JSONObject p = new JSONObject();
        for (String[] prop : props) p.put(prop[0], new JSONObject().put("type", prop[1]).put("description", prop[2]));
        JSONObject s = new JSONObject().put("type", "object").put("properties", p);
        if (required.length > 0) s.put("required", new JSONArray(required));
        return s;
    }
}
