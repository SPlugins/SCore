package com.ssomar.score.sobject;

import com.ssomar.score.features.FeatureInterface;
import com.ssomar.score.features.FeatureSettingsInterface;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Base contract of every configurable SsomarDev object
 * (ExecutableItem, ExecutableBlock, Furniture, Trade, Recipe...).
 */
public interface SObjectInterface {

    /**
     * Get the unique id of this object (its configuration file name).
     *
     * @return the id
     */
    String getId();

    /**
     * Get all the configuration features of this object.
     *
     * @return the features
     */
    List<FeatureInterface> getFeatures();

    /**
     * Get a specific configuration feature of this object.
     *
     * @param featureSettings the settings identifying the feature
     * @return the feature, or null if this object does not have it
     */
    @Nullable
    FeatureInterface getFeature(FeatureSettingsInterface featureSettings);
}
