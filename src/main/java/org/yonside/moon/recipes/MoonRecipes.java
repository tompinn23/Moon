package org.yonside.moon.recipes;

import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMapBackend;
import gregtech.api.recipe.RecipeMapBuilder;

public class MoonRecipes {

    public static final RecipeMap<RecipeMapBackend> floatationCellRecipes = RecipeMapBuilder
        .of("moon.recipes.flotation")
        .maxIO(1, 3, 3, 2)
        .build();
}
