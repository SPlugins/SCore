package com.ssomar.score.ai;

import org.json.JSONObject;

/**
 * A tool the SPlugins assistant can call on this server (public API: other plugins register theirs with
 * {@link AiTools#register}). Tools run on the server thread; they must be quick and return little (summaries,
 * targeted fields), because every byte goes back into the model context.
 */
public interface AiTool {

    /** lower_snake_case, unique */
    String name();

    /** one or two sentences for the model: what it does and when to use it */
    String description();

    /** JSON Schema of the arguments ({"type": "object", "properties": {...}, "required": [...]}) */
    JSONObject inputSchema();

    /**
     * true if the tool changes something on the server: it is only offered to admins with the permission
     * score.ai.write, and its execute must return an {@link AiChange} (applied once the admin confirmed in the game)
     * or a result that says why nothing is proposed.
     */
    default boolean isWrite() {
        return false;
    }

    /** @return a JSONObject, a JSONArray or a String; throw to report an error to the model */
    Object execute(JSONObject arguments) throws Exception;
}
