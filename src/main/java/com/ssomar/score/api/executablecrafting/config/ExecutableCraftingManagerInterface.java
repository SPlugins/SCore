package com.ssomar.score.api.executablecrafting.config;

import java.util.List;
import java.util.Optional;

/**
 * Manager of the ExecutableCrafting recipes.
 */
public interface ExecutableCraftingManagerInterface {

    /**
     * Check whether the ExecutableCrafting recipes have finished loading.
     * Before that, the lookup methods return empty results.
     * Listen to {@link com.ssomar.score.api.executablecrafting.load.ExecutableCraftingPostLoadEvent}
     * to be notified when loading completes.
     *
     * @return true once all recipes are loaded
     **/
    boolean isLoaded();

    /**
     * Verify if id is a valid recipe ID
     *
     * @param id The ID to verify
     * @return true if it is a valid ID, false otherwise
     **/
    boolean isValidID(String id);

    /**
     * Get a recipe from its ID
     *
     * @param id The ID of the recipe
     * @return The recipe or an empty optional
     **/
    Optional<? extends RecipeInterface> getRecipe(String id);

    /**
     * Get all recipe Ids
     *
     * @return All recipe ids
     **/
    List<String> getRecipeIdsList();

    /**
     * Get all recipes
     *
     * @return All recipes
     **/
    List<? extends RecipeInterface> getAllRecipes();
}
