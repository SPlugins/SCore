package com.ssomar.score.features;

/**
 * Controls whether a feature writes its value to the config file when that value is equal to its default.
 * <p>
 * Each feature's level is set where its settings are declared in {@link FeatureSettingsSCore}, through
 * {@link FeatureSettingsSCore#getFeatureSettings(String, String, boolean, SavingVerbosityLevel)} or one of its overloads.
 * Features declared without a level use {@link #SAVE_ALWAYS}.
 * <p>
 * Server owners can override the level of every feature with the {@code configVerbosity} option of SCore's
 * {@code config.yml}:
 * <ul>
 *     <li>{@code NORMAL} (default): each feature uses its own level.</li>
 *     <li>{@code MIN}: every feature behaves as {@link #SAVE_ONLY_WHEN_DIFFERENT_DEFAULT}.</li>
 *     <li>{@code MAX}: every feature behaves as {@link #SAVE_ALWAYS}.</li>
 * </ul>
 * The decision is made by {@link FeatureAbstract#isSavingOnlyIfDiffDefault()}, which the {@code save} method of
 * each feature calls.
 */
public enum SavingVerbosityLevel {

    /**
     * Writes the value only when it is different from the default. When it is equal, the key is removed from the
     * config, so configs only contain what was actually changed.
     * <p>
     * For a group of features (for example a block conditions section), the whole section is removed when none of
     * its features were written.
     * <p>
     * Recommended for optional features that most configs leave unset, such as conditions.
     */
    SAVE_ONLY_WHEN_DIFFERENT_DEFAULT,

    /**
     * Always writes the value, even when it is equal to the default (for example {@code ifBlockWorld: []} or
     * {@code enabled: false}).
     * <p>
     * Use it for settings users should always see in their config. This is the level used when a feature is
     * declared without one.
     */
    SAVE_ALWAYS,
}
