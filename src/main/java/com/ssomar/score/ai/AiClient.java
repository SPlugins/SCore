package com.ssomar.score.ai;

import com.ssomar.score.SCore;
import com.ssomar.score.config.GeneralConfig;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** JSON over HTTPS to the splugins.net server API (blocking: call it from an async task). */
public final class AiClient {

    private AiClient() {
    }

    public static final class Response {
        public final int code;
        public final JSONObject body;

        Response(int code, JSONObject body) {
            this.code = code;
            this.body = body;
        }

        public boolean ok() {
            return code >= 200 && code < 300;
        }

        /** the "code" field of an error, or "http_<status>" */
        public String errorCode() {
            return body.optString("code", "http_" + code);
        }

        public String errorMessage() {
            return body.optString("error", errorCode());
        }
    }

    public static Response post(String path, JSONObject body, String token, int timeoutMs) throws Exception {
        return send("POST", path, body, token, timeoutMs);
    }

    public static Response get(String path, String token, int timeoutMs) throws Exception {
        return send("GET", path, null, token, timeoutMs);
    }

    private static Response send(String method, String path, JSONObject body, String token, int timeoutMs) throws Exception {
        String base = GeneralConfig.getInstance().getAiUrl();
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        HttpURLConnection c = (HttpURLConnection) new URL(base + path).openConnection();
        c.setRequestMethod(method);
        c.setConnectTimeout(5000);
        c.setReadTimeout(timeoutMs);
        c.setRequestProperty("Accept", "application/json");
        c.setRequestProperty("User-Agent", "SCore/" + SCore.plugin.getDescription().getVersion());
        if (token != null) c.setRequestProperty("Authorization", "Bearer " + token);
        if (body != null) {
            byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
            c.setDoOutput(true);
            c.setRequestProperty("Content-Type", "application/json");
            c.setFixedLengthStreamingMode(bytes.length);
            try (OutputStream out = c.getOutputStream()) {
                out.write(bytes);
            }
        }
        int code = c.getResponseCode();
        InputStream in = code >= 400 ? c.getErrorStream() : c.getInputStream();
        String text = "";
        if (in != null) {
            try (InputStream i = in) {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int n;
                while ((n = i.read(buf)) > 0) b.write(buf, 0, n);
                text = new String(b.toByteArray(), StandardCharsets.UTF_8);
            }
        }
        JSONObject json;
        try {
            json = text.trim().startsWith("{") ? new JSONObject(text) : new JSONObject().put("error", text.length() > 200 ? text.substring(0, 200) : text);
        } catch (Exception e) {
            json = new JSONObject().put("error", "invalid answer");
        }
        return new Response(code, json);
    }
}
