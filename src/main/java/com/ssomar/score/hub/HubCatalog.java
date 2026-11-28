package com.ssomar.score.hub;

import com.ssomar.score.SCore;
import com.ssomar.score.config.GeneralConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * The public SPlugins catalog (plugins, packs, news, promotions, links), read from splugins.net with an anonymous GET.
 * Order of use: fresh copy from the site (async, 3 s max) > copy cached on disk > copy inside the jar.
 * Nothing about the server is sent.
 */
public final class HubCatalog {

    private static final long REFRESH_MS = 6L * 60 * 60 * 1000;
    private static volatile HubCatalog current;
    private static volatile long lastFetch;
    private static volatile boolean fetching;

    public final List<Entry> plugins = new ArrayList<>();
    public final List<Entry> packs = new ArrayList<>();
    /** the bundles of splugins.net/store (several products at a lower price) */
    public final List<Entry> bundles = new ArrayList<>();
    public final List<News> news = new ArrayList<>();
    public final List<News> promotions = new ArrayList<>();
    public final List<Link> links = new ArrayList<>();
    public String source = "jar";

    public static final class Entry {
        public String key, edition, name, pitch, access, currency, url, icon, banner, kind, saleLabel, latestVersion;
        /** a bundle: the names of what it contains */
        public final List<String> items = new ArrayList<>();
        /** a bundle: its id on splugins.net */
        public int id;
        /** a pack sold by a member (the community section of the store) */
        public boolean official = true;
        public double price, salePrice;
        public int salePercent, tokenCost;
        public long saleEndsAt;

        public boolean isFree() {
            return "free".equalsIgnoreCase(access) || (price <= 0 && !"subscription".equalsIgnoreCase(access));
        }

        public String priceText() {
            if ("subscription".equalsIgnoreCase(access)) return tokenCost > 0 ? tokenCost + " SToken" : "Subscribers";
            if (isFree()) return "Free";
            String cur = currency == null || currency.isEmpty() ? "€" : ("EUR".equalsIgnoreCase(currency) ? "€" : currency + " ");
            if (salePercent > 0 && salePrice > 0) return fmt(salePrice) + cur + " (-" + salePercent + "%)";
            return fmt(price) + cur;
        }

        private static String fmt(double d) {
            return String.format(Locale.ROOT, "%.2f", d);
        }
    }

    public static final class News {
        public String id, title, text, url, image;
        public long date, start, end;
        public int discount;
    }

    public static final class Link {
        public String id, label, url, icon, image;
    }

    /** The best catalog we have now (never null). Starts a refresh in the background if it is old. */
    public static HubCatalog get() {
        HubCatalog c = current;
        if (c == null) {
            c = loadLocal();
            current = c;
        }
        refreshIfOld();
        return c;
    }

    /** /score reload: forget the copy in memory, so a changed hub.catalog-url is read again at the next opening */
    public static void reset() {
        current = null;
        lastFetch = 0;
    }

    public static void refreshIfOld() {
        if (!GeneralConfig.getInstance().isHubRemoteCatalog()) return;
        if (fetching || System.currentTimeMillis() - lastFetch < REFRESH_MS) return;
        fetching = true;
        lastFetch = System.currentTimeMillis();
        SCore.schedulerHook.runAsyncTask(() -> {
            try {
                String json = download(GeneralConfig.getInstance().getHubCatalogUrl());
                HubCatalog parsed = parse(json);
                if (parsed.plugins.isEmpty() && parsed.packs.isEmpty() && parsed.links.isEmpty()) return; // error answer
                parsed.source = "splugins.net";
                current = parsed;
                try (Writer w = new OutputStreamWriter(new FileOutputStream(cacheFile()), StandardCharsets.UTF_8)) {
                    w.write(json);
                }
            } catch (Throwable ignored) {
                // offline or blocked: keep the local copy
            } finally {
                fetching = false;
            }
        }, 0);
    }

    private static File cacheFile() {
        return new File(SCore.dataFolder, "hub-catalog.json");
    }

    private static HubCatalog loadLocal() {
        File f = cacheFile();
        if (f.exists()) {
            try {
                HubCatalog c = parse(new String(readAll(new FileInputStream(f)), StandardCharsets.UTF_8));
                c.source = "cache";
                return c;
            } catch (Throwable ignored) {
            }
        }
        try (InputStream in = HubCatalog.class.getClassLoader().getResourceAsStream("hub/catalog.json")) {
            if (in != null) {
                HubCatalog c = parse(new String(readAll(in), StandardCharsets.UTF_8));
                c.source = "jar";
                return c;
            }
        } catch (Throwable ignored) {
        }
        return new HubCatalog();
    }

    static String download(String url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setConnectTimeout(3000);
        con.setReadTimeout(3000);
        con.setRequestProperty("User-Agent", "SCore-Hub");
        con.setRequestProperty("Accept", "application/json");
        try (InputStream in = con.getInputStream()) {
            if (con.getResponseCode() != 200) throw new IOException("HTTP " + con.getResponseCode());
            return new String(readAll(in), StandardCharsets.UTF_8);
        } finally {
            con.disconnect();
        }
    }

    private static byte[] readAll(InputStream in) throws IOException {
        try (InputStream i = in) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = i.read(buf)) > 0) out.write(buf, 0, n);
            return out.toByteArray();
        }
    }

    /** Tolerant parser: unknown or missing fields are ignored. */
    public static HubCatalog parse(String json) {
        HubCatalog c = new HubCatalog();
        JSONObject root = new JSONObject(json);
        long now = System.currentTimeMillis();
        JSONArray a = root.optJSONArray("plugins");
        if (a != null) for (int i = 0; i < a.length(); i++) c.plugins.add(entry(a.optJSONObject(i)));
        a = root.optJSONArray("packs");
        if (a != null) for (int i = 0; i < a.length(); i++) c.packs.add(entry(a.optJSONObject(i)));
        a = root.optJSONArray("bundles");
        if (a != null) for (int i = 0; i < a.length(); i++) c.bundles.add(bundle(a.optJSONObject(i)));
        a = root.optJSONArray("news");
        if (a != null) for (int i = 0; i < a.length(); i++) c.news.add(news(a.optJSONObject(i)));
        a = root.optJSONArray("promotions");
        if (a != null) for (int i = 0; i < a.length(); i++) {
            News n = news(a.optJSONObject(i));
            if ((n.start == 0 || n.start <= now) && (n.end == 0 || now < n.end)) c.promotions.add(n);
        }
        a = root.optJSONArray("links");
        if (a != null) for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.optJSONObject(i);
            if (o == null) continue;
            Link l = new Link();
            l.id = o.optString("id", "");
            l.label = o.optString("label", l.id);
            l.url = o.optString("url", "");
            l.icon = o.isNull("icon") ? "" : o.optString("icon", "");
            l.image = o.isNull("image") ? "" : o.optString("image", "");
            if (!l.url.isEmpty()) c.links.add(l);
        }
        c.plugins.removeIf(Objects::isNull);
        c.packs.removeIf(Objects::isNull);
        c.bundles.removeIf(Objects::isNull);
        c.news.removeIf(Objects::isNull);
        return c;
    }

    /** a bundle of the catalog: {title, pitch, discountPercent, fullPrice, price, currency, url, resources[{name, icon}]} */
    private static Entry bundle(JSONObject o) {
        if (o == null || o.optString("url", "").isEmpty()) return null;
        Entry e = new Entry();
        e.kind = "bundle";
        e.id = o.optInt("id", 0);
        e.name = o.optString("title", "Bundle");
        e.pitch = o.optString("pitch", "");
        e.access = "paid";
        e.currency = o.optString("currency", "EUR");
        e.url = o.optString("url", "");
        double full = o.optDouble("fullPrice", 0), price = o.optDouble("price", full);
        e.price = full > 0 ? full : price;
        e.salePercent = o.optInt("discountPercent", 0);
        e.salePrice = e.salePercent > 0 ? price : 0;
        // the bundle's own pictures (content/hub.json on splugins.net), else the first icon of what it contains
        e.icon = o.isNull("icon") ? "" : o.optString("icon", "");
        e.banner = o.isNull("banner") ? "" : o.optString("banner", "");
        boolean ownIcon = !e.icon.isEmpty();
        JSONArray r = o.optJSONArray("resources");
        if (r != null) for (int i = 0; i < r.length(); i++) {
            JSONObject x = r.optJSONObject(i);
            if (x == null) continue;
            e.items.add(x.optString("name", ""));
            if (!ownIcon && e.icon.isEmpty() && !x.isNull("icon")) e.icon = x.optString("icon", "");
        }
        return e;
    }

    private static Entry entry(JSONObject o) {
        if (o == null) return null;
        Entry e = new Entry();
        e.key = o.optString("key", "");
        e.edition = o.optString("edition", "");
        e.name = o.optString("name", e.key);
        e.pitch = o.optString("pitch", "");
        e.access = o.optString("access", "");
        e.kind = o.optString("kind", "");
        e.currency = o.optString("currency", "EUR");
        e.url = o.optString("url", "");
        e.icon = o.isNull("icon") ? "" : o.optString("icon", "");
        e.banner = o.isNull("banner") ? "" : o.optString("banner", "");
        e.official = o.optBoolean("official", true) && !"community".equals(e.kind);
        if (e.key.isEmpty() || o.isNull("key")) e.key = "";
        e.price = o.optDouble("price", 0);
        e.salePrice = o.optDouble("salePrice", 0);
        e.tokenCost = o.optInt("tokenCost", 0);
        JSONObject sale = o.optJSONObject("sale");
        if (sale != null) {
            e.salePercent = sale.optInt("percent", 0);
            e.saleLabel = sale.optString("label", "");
            e.saleEndsAt = time(sale.opt("endsAt"));
            if (e.saleEndsAt > 0 && e.saleEndsAt < System.currentTimeMillis()) e.salePercent = 0;
        }
        JSONObject latest = o.optJSONObject("latestVersion");
        e.latestVersion = latest != null ? latest.optString("version", "") : o.optString("latestVersion", "");
        return e;
    }

    private static News news(JSONObject o) {
        if (o == null) return null;
        News n = new News();
        n.id = o.optString("id", "");
        n.title = o.optString("title", "");
        n.text = o.optString("text", o.optString("description", ""));
        n.url = o.isNull("url") ? "" : o.optString("url", "");
        n.image = o.isNull("image") ? "" : o.optString("image", "");
        n.date = time(o.opt("date"));
        n.start = time(o.has("startsAt") ? o.opt("startsAt") : o.opt("start"));
        n.end = time(o.has("endsAt") ? o.opt("endsAt") : o.opt("end"));
        n.discount = o.has("percent") ? o.optInt("percent", 0) : o.optInt("discount", 0);
        if (n.id.isEmpty()) n.id = Integer.toHexString((n.title + n.url).hashCode());
        return n;
    }

    /** epoch millis from a number (seconds or millis) or an ISO-8601 date; 0 if unknown */
    static long time(Object v) {
        if (v == null || v == JSONObject.NULL) return 0;
        if (v instanceof Number) {
            long l = ((Number) v).longValue();
            return l < 100_000_000_000L ? l * 1000 : l;
        }
        String s = v.toString().trim();
        if (s.isEmpty()) return 0;
        try {
            return java.time.OffsetDateTime.parse(s).toInstant().toEpochMilli();
        } catch (Throwable ignored) {
        }
        try {
            return java.time.Instant.parse(s).toEpochMilli();
        } catch (Throwable ignored) {
        }
        try {
            return java.time.LocalDate.parse(s.length() > 10 ? s.substring(0, 10) : s).atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli();
        } catch (Throwable ignored) {
            return 0;
        }
    }

    /** The catalog entry for an installed plugin (premium edition preferred if it is premium). */
    public Entry pluginEntry(KnownPlugin k, Boolean premium) {
        Entry best = null;
        for (Entry e : plugins) {
            if (!k.getKey().equalsIgnoreCase(e.key)) continue;
            if (best == null) best = e;
            if (premium != null && (premium ? "premium" : "free").equalsIgnoreCase(e.edition)) return e;
        }
        return best;
    }

    /**
     * Compare plugin versions like "7.26.11.20" (numeric parts, missing = 0).
     * @return negative if a < b
     */
    public static int compareVersions(String a, String b) {
        String[] x = a == null ? new String[0] : a.split("[^0-9]+");
        String[] y = b == null ? new String[0] : b.split("[^0-9]+");
        int n = Math.max(x.length, y.length);
        for (int i = 0; i < n; i++) {
            long xi = i < x.length && !x[i].isEmpty() ? num(x[i]) : 0;
            long yi = i < y.length && !y[i].isEmpty() ? num(y[i]) : 0;
            if (xi != yi) return xi < yi ? -1 : 1;
        }
        return 0;
    }

    private static long num(String s) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
