package com.ssomar.score.sobject;


import com.ssomar.score.features.custom.activators.activator.SActivator;
import com.ssomar.score.features.custom.activators.group.ActivatorsFeature;
import org.jetbrains.annotations.Nullable;

/**
 * A SsomarDev object that owns activators (the triggers that run its commands).
 */
public interface SObjectWithActivators {

    /**
     * Get all the activators of this object.
     *
     * @return the activators group
     */
    ActivatorsFeature getActivators();

    /**
     * Get a specific activator of this object.
     *
     * @param actID the activator id
     * @return the activator, or null if there is no activator with this id
     */
    @Nullable
    SActivator getActivator(String actID);

}
