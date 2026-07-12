package com.ssomar.score.api.executablecrafting;

import com.ssomar.score.api.executablecrafting.config.ExecutableCraftingManagerInterface;
import org.jetbrains.annotations.NotNull;

/**
 * Entry point of the public ExecutableCrafting API.
 * <p>
 * The implementation is registered at runtime by the ExecutableCrafting plugin when it enables.
 * To be sure the recipes are loaded, listen to
 * {@link com.ssomar.score.api.executablecrafting.load.ExecutableCraftingPostLoadEvent} or check
 * {@link ExecutableCraftingManagerInterface#isLoaded()}.
 * <p>
 * For advanced recipe features (grids, inputs, results, groups...), use the rich API
 * shipped with the ExecutableCrafting plugin: {@code vayk.executablecrafting.api.ExecutableCraftingAPI}.
 */
public class ExecutableCraftingAPI {

    private static ExecutableCraftingManagerInterface manager;

    /**
     * Internal — called by the ExecutableCrafting plugin on enable. Do not call.
     *
     * @param managerImpl the manager implementation to expose through this API
     */
    public static void register(@NotNull ExecutableCraftingManagerInterface managerImpl) {
        manager = managerImpl;
    }

    /**
     * Check whether the ExecutableCrafting plugin is installed and enabled.
     *
     * @return true if the API is usable
     */
    public static boolean isEnabled() {
        return manager != null;
    }

    /**
     * Get the ExecutableCrafting Manager.
     * It allows you to get / retrieve the ExecutableCrafting recipes.
     *
     * @return the manager
     * @throws IllegalStateException if the ExecutableCrafting plugin is not installed/enabled yet
     */
    public static @NotNull ExecutableCraftingManagerInterface getExecutableCraftingManager() {
        if (manager == null)
            throw new IllegalStateException("ExecutableCrafting is not installed or not enabled yet (add it as a (soft)depend in your plugin.yml)");
        return manager;
    }
}
