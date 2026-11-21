package com.ssomar.score.ai.tools;

import com.ssomar.score.ai.AiTool;
import com.ssomar.score.ai.AiTools;
import com.ssomar.score.hub.LoadErrorCollector;
import com.ssomar.score.sobject.SObjectWithFileLoader;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * Read tools on the objects of every SPlugins plugin (items, blocks, events, recipes, furniture...), through the
 * loaders SCore already knows: list, read, search, and validate a YAML proposal with the real loader.
 */
public final class ObjectTools {

    static final int MAX_FILE = 60_000, MAX_LIST = 300, MAX_SEARCH = 50;

    private ObjectTools() {
    }

    public static List<AiTool> all() {
        return Arrays.asList(list(), read(), search(), validate());
    }

    static final String[] PLUGIN = {"plugin", "string", "plugin name, e.g. ExecutableItems, ExecutableBlocks, ExecutableEvents, MyFurniture"};
    static final String[] TYPE = {"type", "string", "object type when the plugin has several (item, block, event, furniture, recipe...); optional"};

    /** the loader of a plugin (and type), or an exception that lists the valid ones */
    public static SObjectWithFileLoader<?> loader(JSONObject a) {
        String plugin = a.optString("plugin", "").trim();
        String type = a.optString("type", "").trim().toLowerCase(Locale.ROOT);
        List<String> known = new ArrayList<>();
        SObjectWithFileLoader<?> found = null;
        for (SObjectWithFileLoader<?> l : SObjectWithFileLoader.getLoaders()) {
            String name = l.getSPlugin().getName();
            String obj = l.getObjectNameLowerCase();
            known.add(name + "/" + obj);
            boolean pluginOk = name.equalsIgnoreCase(plugin) || l.getSPlugin().getShortName().equalsIgnoreCase(plugin);
            if (!pluginOk) continue;
            if (!type.isEmpty() && !obj.startsWith(type) && !type.startsWith(obj)) continue;
            if (found != null && type.isEmpty())
                throw new IllegalArgumentException(name + " has several object types, give \"type\": " + known);
            found = l;
        }
        if (found == null) throw new IllegalArgumentException("unknown plugin/type \"" + plugin + "/" + type + "\", installed: " + known);
        return found;
    }

    public static String relative(SObjectWithFileLoader<?> l, File f) {
        String base = new File(l.getConfigsPath()).getAbsolutePath();
        String p = f.getAbsolutePath();
        return p.startsWith(base) ? p.substring(base.length()).replace('\\', '/').replaceFirst("^/", "") : f.getName();
    }

    static AiTool list() {
        return new ServerTools.Simple("list_objects",
                "The ids of the objects (items, blocks...) of a plugin, with their file. Optional folder to narrow down.",
                AiTools.schema(new String[][]{PLUGIN, TYPE, {"folder", "string", "only this folder (name); optional"}}, "plugin"),
                a -> {
                    SObjectWithFileLoader<?> l = loader(a);
                    List<String> ids;
                    String folder = a.optString("folder", "");
                    if (!folder.isEmpty()) {
                        File f = l.searchFolder(folder);
                        if (f == null) throw new IllegalArgumentException("no folder " + folder + ", folders: " + l.getAllFoldersName());
                        ids = l.getAllObjectsOfFolder(f);
                    } else ids = l.getAllObjects();
                    Collections.sort(ids);
                    JSONArray out = new JSONArray();
                    for (String id : ids.subList(0, Math.min(MAX_LIST, ids.size()))) {
                        File f = l.searchFileOfObject(id);
                        out.put(new JSONObject().put("id", id).put("file", f == null ? "" : relative(l, f)));
                    }
                    return new JSONObject().put("total", ids.size()).put("objects", out).put("folders", new JSONArray(l.getAllFoldersName()));
                });
    }

    static AiTool read() {
        return new ServerTools.Simple("read_object",
                "The YAML configuration file of one object (item, block...) of a plugin.",
                AiTools.schema(new String[][]{PLUGIN, TYPE, {"id", "string", "object id"}}, "plugin", "id"),
                a -> {
                    SObjectWithFileLoader<?> l = loader(a);
                    File f = l.searchFileOfObject(a.getString("id"));
                    if (f == null || !f.isFile()) throw new IllegalArgumentException("no object " + a.getString("id"));
                    String yaml = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
                    boolean cut = yaml.length() > MAX_FILE;
                    return new JSONObject().put("file", relative(l, f)).put("yaml", cut ? yaml.substring(0, MAX_FILE) : yaml).put("truncated", cut);
                });
    }

    static AiTool search() {
        return new ServerTools.Simple("search_objects",
                "Ids of the objects whose YAML file contains a text (case insensitive), e.g. an activator, a command, a material.",
                AiTools.schema(new String[][]{PLUGIN, TYPE, {"text", "string", "text to find"}}, "plugin", "text"),
                a -> {
                    SObjectWithFileLoader<?> l = loader(a);
                    String needle = a.getString("text").toLowerCase(Locale.ROOT);
                    JSONArray out = new JSONArray();
                    for (String id : l.getAllObjects()) {
                        File f = l.searchFileOfObject(id);
                        if (f == null || !f.isFile() || f.length() > 2_000_000) continue;
                        String yaml = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
                        if (yaml.toLowerCase(Locale.ROOT).contains(needle)) out.put(id);
                        if (out.length() >= MAX_SEARCH) break;
                    }
                    return new JSONObject().put("ids", out);
                });
    }

    static AiTool validate() {
        return new ServerTools.Simple("validate_object",
                "Load a YAML configuration with the plugin's real loader WITHOUT saving or registering it, and return the errors it prints. Always validate a configuration before proposing it.",
                AiTools.schema(new String[][]{PLUGIN, TYPE, {"id", "string", "object id"}, {"yaml", "string", "the whole YAML of the object"}}, "plugin", "id", "yaml"),
                a -> check(loader(a), a.getString("yaml"), a.getString("id")));
    }

    /** {ok, loaded, errors[]}: the YAML loaded with the real loader, nothing saved or registered */
    public static JSONObject check(SObjectWithFileLoader<?> l, String yaml, String id) {
        final boolean[] loaded = {false};
        final String[] parse = {null};
        List<String> lines = LoadErrorCollector.capture(() -> {
            try {
                loaded[0] = l.loadDryRun(yaml, id).isPresent();
            } catch (Exception e) {
                parse[0] = e.getMessage();
            }
        });
        List<String> errors = new ArrayList<>();
        for (String line : lines) if (line.toUpperCase(Locale.ROOT).contains("ERROR") || line.toLowerCase(Locale.ROOT).contains("couldn't")) errors.add(line);
        if (parse[0] != null) errors.add(0, "YAML: " + parse[0]);
        return new JSONObject().put("ok", loaded[0] && errors.isEmpty()).put("loaded", loaded[0]).put("errors", new JSONArray(errors));
    }
}
