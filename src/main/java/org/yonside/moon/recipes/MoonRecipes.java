package org.yonside.moon.recipes;

import gregtech.api.enums.Materials;
import gregtech.api.enums.TierEU;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMapBackend;
import gregtech.api.recipe.RecipeMapBuilder;
import gregtech.api.recipe.RecipeMaps;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import org.yonside.moon.MoonMaterials;

import static gregtech.api.enums.GTValues.RA;

public class MoonRecipes {

    public static final RecipeMap<RecipeMapBackend> floatationCellRecipes = RecipeMapBuilder
        .of("moon.recipes.flotation")
        .maxIO(1, 3, 3, 2)
        .build();

    public static final RecipeMap<RecipeMapBackend> washConcentratorRecipes = RecipeMapBuilder
        .of("moon.recipes.washconcentrator")
        .maxIO(0, 2, 2, 2)
        .minInputs(0, 1)
        .progressBar(GTUITextures.PROGRESSBAR_ARROW_MULTIPLE)
        .build();

    public static void init() {
        RA.stdBuilder()
            .itemInputs(new ItemStack(Blocks.sand, 2, Short.MAX_VALUE))
            .fluidInputs(Materials.Water.getFluid(1000))
            .fluidOutputs(MoonMaterials.SandSlurry.getFluidOrGas(1500))
            .duration(5 * 20)
            .eut(TierEU.RECIPE_LV)
            .addTo(RecipeMaps.mixerRecipes);

        RA.stdBuilder()
            .fluidInputs(MoonMaterials.SandSlurry.getFluidOrGas(10000))
            .fluidOutputs(MoonMaterials.SandSlurry.getFluidOrGas(15000))
            .duration(60 * 20)
            .eut(TierEU.RECIPE_LV)
            .addTo(washConcentratorRecipes);
    }
}
