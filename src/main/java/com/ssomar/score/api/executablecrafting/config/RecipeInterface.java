package com.ssomar.score.api.executablecrafting.config;

/**
 * An ExecutableCrafting recipe configuration (crafting, furnace or anvil).
 * <p>
 * For advanced recipe features (grids, inputs, results, groups...), use the rich API
 * shipped with the ExecutableCrafting plugin: {@code vayk.executablecrafting.api.ExecutableCraftingAPI}.
 */
public interface RecipeInterface {

    /**
     * Get the id of this recipe.
     *
     * @return the id
     */
    String getId();
}
