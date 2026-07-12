package com.ssomar.score.api.custompiglinstrades;

import com.ssomar.score.api.custompiglinstrades.config.CustomPiglinsTradesManagerInterface;
import org.jetbrains.annotations.NotNull;

/**
 * Entry point of the public CustomPiglinsTrades API.
 * <p>
 * The implementation is registered at runtime by the CustomPiglinsTrades plugin when it enables.
 * To be sure the trades are loaded, listen to
 * {@link com.ssomar.score.api.custompiglinstrades.load.CustomPiglinsTradesPostLoadEvent} or check
 * {@link CustomPiglinsTradesManagerInterface#isLoaded()}.
 */
public class CustomPiglinsTradesAPI {

    private static CustomPiglinsTradesManagerInterface manager;

    /**
     * Internal — called by the CustomPiglinsTrades plugin on enable. Do not call.
     *
     * @param managerImpl the manager implementation to expose through this API
     */
    public static void register(@NotNull CustomPiglinsTradesManagerInterface managerImpl) {
        manager = managerImpl;
    }

    /**
     * Check whether the CustomPiglinsTrades plugin is installed and enabled.
     *
     * @return true if the API is usable
     */
    public static boolean isEnabled() {
        return manager != null;
    }

    /**
     * Get the CustomPiglinsTrades Manager.
     * It allows you to get / retrieve the trades configurations.
     *
     * @return the manager
     * @throws IllegalStateException if the CustomPiglinsTrades plugin is not installed/enabled yet
     */
    public static @NotNull CustomPiglinsTradesManagerInterface getTradesManager() {
        if (manager == null)
            throw new IllegalStateException("CustomPiglinsTrades is not installed or not enabled yet (add it as a (soft)depend in your plugin.yml)");
        return manager;
    }
}
