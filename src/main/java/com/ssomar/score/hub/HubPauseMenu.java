package com.ssomar.score.hub;

import com.ssomar.score.SCore;
import com.ssomar.score.config.GeneralConfig;
import com.ssomar.score.languages.messages.TM;
import com.ssomar.score.languages.messages.Text;
import com.ssomar.score.utils.logging.Utils;
import com.ssomar.score.utils.strings.StringConverter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

/**
 * Opt-in "SPlugins Hub" button in the pause menu and the quick actions screen (Paper 1.21.6+ dialogs,
 * {@code hub.pause-menu-button: true}). A world data pack holds the dialog; vanilla shows it to every player (a
 * data pack can't filter by permission), and the click opens the Hub only for players with {@code score.hub}.
 * The data pack takes effect after a server restart.
 */
public final class HubPauseMenu {

    static final String PACK = "score_hub_menu";
    static final String ACTION = "score:hub/open";

    private HubPauseMenu() {
    }

    static boolean dialogsAvailable() {
        try {
            Class.forName("io.papermc.paper.dialog.Dialog");
            Class.forName("io.papermc.paper.event.player.PlayerCustomClickEvent");
            Class.forName("io.papermc.paper.connection.PlayerGameConnection");
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static File packDir() {
        World main = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
        if (main == null) return null;
        // 26.x: getWorldFolder() is world/dimensions/minecraft/overworld, the data packs stay at the level root
        File root = new File(Bukkit.getWorldContainer(), main.getName());
        if (!new File(root, "level.dat").exists()) root = main.getWorldFolder();
        return new File(new File(root, "datapacks"), PACK);
    }

    private static boolean registered;

    /** startup and /score reload */
    public static void install() {
        File dir = packDir();
        if (dir == null) return;
        if (!GeneralConfig.getInstance().isHubPauseMenuButton()) {
            if (dir.exists()) {
                delete(dir);
                Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7SPlugins Hub pause menu button removed (takes effect after a restart)");
            }
            return;
        }
        if (!dialogsAvailable()) {
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &ehub.pause-menu-button needs Paper 1.21.6+ (dialogs): ignored");
            return;
        }
        try {
            JSONObject meta = packMeta();
            if (meta == null) {
                Utils.sendConsoleMsg(SCore.NAME_COLOR + " &ehub.pause-menu-button: can't read the data pack version of this server");
                return;
            }
            boolean changed = write(new File(dir, "pack.mcmeta"), meta.toString(2));
            changed |= write(new File(dir, "data/score/dialog/hub.json"), dialog().toString(2));
            String tag = new JSONObject().put("values", new JSONArray().put("score:hub")).toString(2);
            changed |= write(new File(dir, "data/minecraft/tags/dialog/pause_screen_additions.json"), tag);
            changed |= write(new File(dir, "data/minecraft/tags/dialog/quick_actions.json"), tag);
            if (changed) Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7SPlugins Hub pause menu button written in &6" + dir.getPath() + " &7(takes effect after a restart)");
        } catch (Throwable t) {
            SCore.plugin.getLogger().warning("[SCore Hub] can't write the pause menu data pack: " + t);
            return;
        }
        if (!registered) {
            Bukkit.getPluginManager().registerEvents(new ClickListener(), SCore.plugin);
            registered = true;
        }
    }

    /** pack.mcmeta from the server jar's version.json, so it matches this server whatever SCore was compiled with */
    static JSONObject packMeta() throws Exception {
        InputStream in = Bukkit.getServer().getClass().getClassLoader().getResourceAsStream("version.json");
        if (in == null) in = ClassLoader.getSystemResourceAsStream("version.json");
        if (in == null) return null;
        JSONObject v;
        try (InputStream i = in) {
            v = new JSONObject(new String(readAll(i), StandardCharsets.UTF_8));
        }
        JSONObject pv = v.optJSONObject("pack_version");
        if (pv == null) return null;
        JSONObject pack = new JSONObject().put("description", "SCore: SPlugins Hub button in the pause menu");
        if (pv.has("data_major")) {
            JSONArray f = new JSONArray().put(pv.getInt("data_major")).put(pv.optInt("data_minor", 0));
            pack.put("min_format", f).put("max_format", f);
        } else if (pv.has("data")) {
            pack.put("pack_format", pv.getInt("data"));
        } else return null;
        return new JSONObject().put("pack", pack);
    }

    static JSONObject dialog() {
        String title = plain(TM.g(Text.HUB_TITLE));
        return new JSONObject()
                .put("type", "minecraft:multi_action")
                .put("title", new JSONObject().put("text", title))
                .put("external_title", new JSONObject().put("text", title))
                .put("body", new JSONArray().put(new JSONObject().put("type", "minecraft:plain_message")
                        .put("contents", new JSONObject().put("text", plain(TM.g(Text.HUB_PAUSE_BODY))))))
                .put("can_close_with_escape", true)
                .put("pause", false)
                .put("after_action", "close")
                .put("columns", 1)
                .put("actions", new JSONArray().put(new JSONObject()
                        .put("label", new JSONObject().put("text", plain(TM.g(Text.HUB_PAUSE_BUTTON))))
                        .put("width", 200)
                        .put("action", new JSONObject().put("type", "minecraft:dynamic/custom").put("id", ACTION))))
                .put("exit_action", new JSONObject().put("label", new JSONObject().put("translate", "gui.back")).put("width", 200));
    }

    private static String plain(String s) {
        return org.bukkit.ChatColor.stripColor(StringConverter.coloredString(s));
    }

    /** atomic write, only when the bytes differ; true if the file changed */
    private static boolean write(File f, String content) throws Exception {
        byte[] bytes = content.replace("\r\n", "\n").getBytes(StandardCharsets.UTF_8);
        if (f.exists() && Arrays.equals(Files.readAllBytes(f.toPath()), bytes)) return false;
        f.getParentFile().mkdirs();
        File tmp = new File(f.getParentFile(), f.getName() + ".tmp");
        Files.write(tmp.toPath(), bytes);
        try {
            Files.move(tmp.toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            Files.move(tmp.toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return true;
    }

    private static byte[] readAll(InputStream in) throws Exception {
        java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = in.read(buf)) > 0) b.write(buf, 0, n);
        return b.toByteArray();
    }

    /** removes only the files this class writes, then the empty folders */
    private static void delete(File dir) {
        for (String path : new String[]{"pack.mcmeta", "data/score/dialog/hub.json", "data/minecraft/tags/dialog/pause_screen_additions.json",
                "data/minecraft/tags/dialog/quick_actions.json"}) {
            File f = new File(dir, path);
            if (f.exists()) f.delete();
        }
        for (String path : new String[]{"data/score/dialog", "data/score", "data/minecraft/tags/dialog", "data/minecraft/tags", "data/minecraft", "data", ""}) {
            File f = new File(dir, path);
            String[] left = f.list();
            if (left != null && left.length == 0) f.delete();
        }
    }

    /** loaded only when the Paper dialog classes exist */
    public static final class ClickListener implements Listener {
        @EventHandler
        public void onClick(io.papermc.paper.event.player.PlayerCustomClickEvent e) {
            if (!ACTION.equals(e.getIdentifier().asString())) return;
            if (!(e.getCommonConnection() instanceof io.papermc.paper.connection.PlayerGameConnection)) return;
            Player p = ((io.papermc.paper.connection.PlayerGameConnection) e.getCommonConnection()).getPlayer();
            SCore.schedulerHook.runEntityTask(() -> {
                if (!p.isOnline()) return;
                if (!p.hasPermission("score.hub")) {
                    p.sendMessage(StringConverter.coloredString(TM.g(Text.HUB_PAUSE_DENIED)));
                    return;
                }
                SPluginsHub.open(p, null);
            }, null, p, 1L);
        }
    }
}
