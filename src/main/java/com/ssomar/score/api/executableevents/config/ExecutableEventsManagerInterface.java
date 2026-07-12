package com.ssomar.score.api.executableevents.config;

import java.util.List;
import java.util.Optional;

/**
 * Manager of the ExecutableEvents configurations.
 */
public interface ExecutableEventsManagerInterface {

    /**
     * Check whether the ExecutableEvents configurations have finished loading.
     * Before that, the lookup methods return empty results.
     * Listen to {@link com.ssomar.score.api.executableevents.load.ExecutableEventsPostLoadEvent}
     * to be notified when loading completes.
     *
     * @return true once all ExecutableEvents configurations are loaded
     **/
    boolean isLoaded();

    /**
     * Verify if id is a valid ExecutableEvent ID
     *
     * @param id The ID to verify
     * @return true if it is a valid ID, false otherwise
     **/
    boolean isValidID(String id);

    /**
     * Get an ExecutableEvent from its ID
     *
     * @param id The ID of the ExecutableEvent
     * @return The ExecutableEvent or an empty optional
     **/
    Optional<? extends ExecutableEventInterface> getExecutableEvent(String id);

    /**
     * Get all ExecutableEvents Ids
     *
     * @return All ExecutableEvents ids
     **/
    List<String> getExecutableEventIdsList();

    /**
     * Get all ExecutableEvents
     *
     * @return All ExecutableEvents
     **/
    List<? extends ExecutableEventInterface> getAllExecutableEvents();

    /**
     * Get the names of the folders containing the ExecutableEvents configurations.
     *
     * @return the folder names
     **/
    List<String> getFoldersNames();

    /**
     * Get the ExecutableEvents of a specific folder.
     *
     * @param folder the folder name
     * @return the ExecutableEvents of this folder
     **/
    List<? extends ExecutableEventInterface> getExecutableEventsOfFolder(String folder);
}
