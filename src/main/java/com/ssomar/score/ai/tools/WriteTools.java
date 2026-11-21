package com.ssomar.score.ai.tools;

import com.ssomar.score.SCore;
import com.ssomar.score.ai.AiBackups;
import com.ssomar.score.ai.AiChange;
import com.ssomar.score.ai.AiProposal;
import com.ssomar.score.ai.AiTool;
import com.ssomar.score.ai.AiTools;
import com.ssomar.score.hub.InstalledPlugin;
import com.ssomar.score.hub.LoadErrorCollector;
import com.ssomar.score.sobject.SObjectWithFileLoader;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Write tools (phase 3). They never change anything themselves: they check the proposal with the real loader and
 * return an {@link AiChange}; the admin who asked sees the diff in the game, and the file is only written (with a
 * backup, then the object reloaded) once they confirmed. Offered only to admins with the permission score.ai.write.
 */
public final class WriteTools {

    static final Pattern ID = Pattern.compile("[A-Za-z0-9_\\-]{1,64}");
    static final Pattern FOLDER = Pattern.compile("[A-Za-z0-9_\\- ]{1,40}");

    private WriteTools() {
    }

    public static List<AiTool> all() {
        return Arrays.asList(write(), writeMany());
    }

    static String read(File f) throws Exception {
        return new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
    }

    static AiTool write() {
        final JSONObject schema = AiTools.schema(new String[][]{ObjectTools.PLUGIN, ObjectTools.TYPE,
                {"id", "string", "object id (file name without .yml); a new id creates the object"},
                {"yaml", "string", "the WHOLE YAML file of the object, as read_object returns it, with your change"},
                {"summary", "string", "one sentence for the admin: what the change does"},
                {"folder", "string", "for a new object only: the folder to put it in (optional)"}}, "plugin", "id", "yaml", "summary");
        return new AiTool() {
            @Override
            public String name() {
                return "write_object";
            }

            @Override
            public String description() {
                return "Create or replace the YAML file of one object (item, block, event...) on the admin's server. The admin sees the diff in the game and must confirm: "
                        + "nothing changes before. The YAML is loaded with the plugin's real loader first and refused if it has errors. A backup is kept and the admin can undo. "
                        + "Returns applied true/false and why.";
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
            public Object execute(JSONObject a) throws Exception {
                return propose(a);
            }
        };
    }

    static AiTool writeMany() {
        JSONObject item = AiTools.schema(new String[][]{ObjectTools.PLUGIN, ObjectTools.TYPE,
                {"id", "string", "object id (file name without .yml); a new id creates the object"},
                {"yaml", "string", "the WHOLE YAML file of the object, with your change"},
                {"summary", "string", "one sentence for the admin: what this change does"},
                {"folder", "string", "for a new object only: the folder to put it in (optional)"}}, "plugin", "id", "yaml", "summary");
        final JSONObject schema = new JSONObject().put("type", "object").put("required", new JSONArray().put("changes"))
                .put("properties", new JSONObject().put("changes", new JSONObject().put("type", "array").put("minItems", 1).put("maxItems", 10)
                        .put("description", "the changes, one per object (10 at most)").put("items", item)));
        return new AiTool() {
            @Override
            public String name() {
                return "write_objects";
            }

            @Override
            public String description() {
                return "Propose SEVERAL changes at once (create or replace the YAML file of each object): the admin sees them in ONE form in the game, "
                        + "ticks the ones to apply and can comment each one; nothing changes before. Each YAML is checked with the real loader first. "
                        + "Use it to fix several objects (e.g. all the load errors) in one go. Returns results[] (applied, reason, admin_comment) and not_proposed[].";
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
            public Object execute(JSONObject a) throws Exception {
                JSONArray list = a.optJSONArray("changes");
                if (list == null || list.length() == 0) throw new IllegalArgumentException("no change");
                java.util.List<AiChange> changes = new java.util.ArrayList<>();
                JSONArray not = new JSONArray();
                for (int i = 0; i < Math.min(10, list.length()); i++) {
                    JSONObject c = list.getJSONObject(i);
                    Object r;
                    try {
                        r = propose(c);
                    } catch (Exception e) {
                        r = new JSONObject().put("applied", false).put("reason", e.getMessage());
                    }
                    if (r instanceof AiChange) changes.add((AiChange) r);
                    else not.put(((JSONObject) r).put("id", c.optString("id")));
                }
                if (changes.isEmpty()) return new JSONObject().put("results", new JSONArray()).put("not_proposed", not);
                return new AiProposal(changes, not);
            }
        };
    }

    /** an AiChange for one object, or a JSONObject that says why nothing is proposed */
    static Object propose(JSONObject a) throws Exception {
        SObjectWithFileLoader<?> l = ObjectTools.loader(a);
        String id = a.getString("id").trim();
        if (!ID.matcher(id).matches()) throw new IllegalArgumentException("invalid id \"" + id + "\" (letters, digits, _ and - only)");
        String yaml = a.getString("yaml");
        String plugin = l.getSPlugin().getName(), type = l.getObjectNameLowerCase();
        File existing = l.searchFileOfObject(id);
        final File target;
        final String before;
        if (existing != null && existing.isFile()) {
            target = existing;
            before = read(existing);
            if (before.equals(yaml)) return new JSONObject().put("applied", false).put("reason", "the file is already like this, nothing to change");
        } else {
            String folder = a.optString("folder", "").trim();
            File base = new File(l.getConfigsPath());
            if (!folder.isEmpty()) {
                if (!FOLDER.matcher(folder).matches()) throw new IllegalArgumentException("invalid folder \"" + folder + "\"");
                File found = l.searchFolder(folder);
                base = found != null ? found : new File(base, folder);
            }
            target = new File(base, id + ".yml");
            before = null;
            String limit = freeLimitReached(plugin);
            if (limit != null) return new JSONObject().put("applied", false).put("reason", limit);
        }
        JSONObject check = ObjectTools.check(l, yaml, id);
        if (!check.getBoolean("ok"))
            return check.put("applied", false).put("reason", "the YAML does not load without error: nothing was proposed to the admin. Fix it and call write_object again.");
        String summary = a.optString("summary", "");
        return new AiChange(plugin + " › " + type + " › " + id, summary, before, yaml, actor -> {
            String now = target.isFile() ? read(target) : null;
            if (!Objects.equals(now, before))
                return new JSONObject().put("applied", false).put("reason", "the file was changed on the server meanwhile: read it again and propose the change again");
            String backup = AiBackups.save(actor, plugin, type, id, target, before);
            File parent = target.getParentFile();
            if (parent != null) parent.mkdirs();
            Files.write(target.toPath(), yaml.getBytes(StandardCharsets.UTF_8));
            List<String> lines = LoadErrorCollector.capture(() -> l.reloadObjectFromFile(id));
            boolean loaded = l.isObjectLoaded(id);
            SCore.plugin.getLogger().info("[AI] " + actor + " applied a change of the assistant: " + plugin + " " + type + " " + id
                    + " (" + (before == null ? "created" : "replaced") + ", backup ai-backups/" + backup + ")");
            JSONArray errors = new JSONArray();
            for (String line : lines) if (line.toUpperCase().contains("ERROR")) errors.put(line);
            return new JSONObject().put("applied", true).put("created", before == null).put("file", ObjectTools.relative(l, target))
                    .put("reloaded", loaded).put("errors", errors).put("undo", "the admin can undo it from the Hub (Assistant tab) or with /score hub ai undo");
        });
    }

    /** a message when a Free plugin already has as many objects as it allows, else null */
    static String freeLimitReached(String plugin) {
        for (InstalledPlugin ip : InstalledPlugin.scan()) {
            if (!ip.getKnown().getPluginName().equalsIgnoreCase(plugin)) continue;
            if (Boolean.FALSE.equals(ip.getPremium()) && ip.getFreeLimit() > 0 && ip.getObjects() >= ip.getFreeLimit())
                return plugin + " Free allows " + ip.getFreeLimit() + " objects and the server already has " + ip.getObjects()
                        + ": a new one would not load. Change an existing object instead, or tell the admin about Premium.";
        }
        return null;
    }
}
