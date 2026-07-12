package com.ssomar.score.api.executableevents;

import com.ssomar.score.api.executableevents.config.ExecutableEventsManagerInterface;
import org.jetbrains.annotations.NotNull;

/**
 * Entry point of the public ExecutableEvents API.
 * <p>
 * The implementation is registered at runtime by the ExecutableEvents plugin when it enables.
 * To be sure the events are loaded, listen to
 * {@link com.ssomar.score.api.executableevents.load.ExecutableEventsPostLoadEvent} or check
 * {@link ExecutableEventsManagerInterface#isLoaded()}.
 */
public class ExecutableEventsAPI {

    private static ExecutableEventsManagerInterface manager;

    /**
     * Internal — called by the ExecutableEvents plugin on enable. Do not call.
     *
     * @param managerImpl the manager implementation to expose through this API
     */
    public static void register(@NotNull ExecutableEventsManagerInterface managerImpl) {
        manager = managerImpl;
    }

    /**
     * Check whether the ExecutableEvents plugin is installed and enabled.
     *
     * @return true if the API is usable
     */
    public static boolean isEnabled() {
        return manager != null;
    }

    /**
     * Get the ExecutableEvents Manager.
     * It allows you to get / retrieve the ExecutableEvents configurations.
     *
     * @return the manager
     * @throws IllegalStateException if the ExecutableEvents plugin is not installed/enabled yet
     */
    public static @NotNull ExecutableEventsManagerInterface getExecutableEventsManager() {
        if (manager == null)
            throw new IllegalStateException("ExecutableEvents is not installed or not enabled yet (add it as a (soft)depend in your plugin.yml)");
        return manager;
    }
}
