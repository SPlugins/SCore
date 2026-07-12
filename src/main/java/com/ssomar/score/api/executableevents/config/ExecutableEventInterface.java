package com.ssomar.score.api.executableevents.config;

import com.ssomar.score.sobject.SObjectWithActivators;

/**
 * An ExecutableEvent configuration.
 */
public interface ExecutableEventInterface extends SObjectWithActivators {

    /**
     * Get the id of this ExecutableEvent.
     *
     * @return the id
     */
    String getId();
}
