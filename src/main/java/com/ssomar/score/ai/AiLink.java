package com.ssomar.score.ai;

import com.ssomar.score.SCore;
import com.ssomar.score.config.GeneralConfig;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.json.JSONObject;

import java.io.File;

/**
 * The link between this server and a splugins.net account (SCore/ai-link.yml, never config.yml): a code the admin
 * enters on splugins.net/link, then a token of this server only, revocable from the site profile.
 */
public final class AiLink {

    private static String token, account;
    private static volatile boolean linking;

    private AiLink() {
    }

    private static File file() {
        return new File(SCore.dataFolder, "ai-link.yml");
    }

    public static synchronized void load() {
        File f = file();
        if (!f.exists()) {
            token = null;
            account = null;
            return;
        }
        YamlConfiguration y = YamlConfiguration.loadConfiguration(f);
        token = y.getString("token", null);
        account = y.getString("account", "");
    }

    private static synchronized void save(String t, String a) {
        token = t;
        account = a;
        File f = file();
        if (t == null) {
            if (f.exists()) f.delete();
            return;
        }
        YamlConfiguration y = new YamlConfiguration();
        y.options().header("Link of this server to a splugins.net account (SPlugins assistant). Delete this file or use\n"
                + "Hub > Assistant > Unlink to remove it; the link can also be revoked on splugins.net.");
        y.set("token", t);
        y.set("account", a);
        try {
            y.save(f);
        } catch (Exception e) {
            SCore.plugin.getLogger().warning("[SCore AI] can't save ai-link.yml: " + e);
        }
    }

    public static boolean isLinked() {
        return token != null && !token.isEmpty();
    }

    public static String token() {
        return token;
    }

    public static String account() {
        return account == null ? "" : account;
    }

    public static boolean isLinking() {
        return linking;
    }

    /** the link was refused by the site (revoked): forget it */
    public static void forget() {
        save(null, null);
    }

    public interface Listener {
        /** the code to show, on the server thread */
        void code(String code, String url, int expiresIn);

        void linked(String account);

        void failed(String reason);
    }

    /** start a link: asks a code, shows it, then waits (every 3 s, up to its expiry) for the admin to enter it */
    public static void start(Listener l) {
        if (linking) return;
        linking = true;
        SCore.schedulerHook.runAsyncTask(() -> {
            try {
                String name = GeneralConfig.getInstance().getAiServerName();
                if (name == null || name.trim().isEmpty()) name = stripColors(Bukkit.getMotd());
                if (name == null || name.trim().isEmpty()) name = "Minecraft server";
                AiClient.Response r = AiClient.post("/link/start", new JSONObject().put("serverName", name.length() > 60 ? name.substring(0, 60) : name)
                        .put("software", Bukkit.getName()).put("version", Bukkit.getBukkitVersion()), null, 10_000);
                if (!r.ok()) {
                    main(() -> l.failed(r.errorMessage()));
                    return;
                }
                String code = r.body.getString("code"), poll = r.body.getString("pollToken"), url = r.body.optString("linkUrl", "https://splugins.net/link");
                int expires = r.body.optInt("expiresIn", 600);
                main(() -> l.code(code, url, expires));
                long end = System.currentTimeMillis() + expires * 1000L;
                while (System.currentTimeMillis() < end && SCore.plugin.isEnabled()) {
                    Thread.sleep(3000);
                    AiClient.Response p = AiClient.post("/link/poll", new JSONObject().put("pollToken", poll), null, 10_000);
                    if (p.code == 410) break;
                    if (p.ok() && "linked".equals(p.body.optString("status"))) {
                        save(p.body.getString("token"), p.body.optString("account", ""));
                        String acc = account();
                        main(() -> l.linked(acc));
                        return;
                    }
                }
                main(() -> l.failed("expired"));
            } catch (Throwable t) {
                main(() -> l.failed(String.valueOf(t.getMessage())));
            } finally {
                linking = false;
            }
        }, 0);
    }

    public static void unlink(Runnable done) {
        final String t = token;
        save(null, null);
        SCore.schedulerHook.runAsyncTask(() -> {
            try {
                if (t != null) AiClient.post("/unlink", new JSONObject(), t, 10_000);
            } catch (Throwable ignored) {
            }
            if (done != null) main(done);
        }, 0);
    }

    static void main(Runnable r) {
        SCore.schedulerHook.runTask(r, 0);
    }

    private static String stripColors(String s) {
        return s == null ? null : s.replaceAll("(?i)§[0-9a-fk-orx]", "").replace("\n", " ").trim();
    }

    /** startup and /score reload */
    public static void init() {
        load();
    }

}
