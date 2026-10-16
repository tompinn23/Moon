package org.yonside.moon.recipes;

import java.util.Collections;
import java.util.function.IntFunction;

import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMapBackend;
import gregtech.api.recipe.RecipeMapBuilder;
import gregtech.nei.formatter.INEISpecialInfoFormatter;

/**
 * One recipe map per machine. Tiered machines list one recipe per tier; the recipe's special value is the tier and the
 * machine runs the best recipe its structure allows (see MTEOreMultiBase#getRecipeTier).
 */
public class MoonRecipeMaps {

    /** Special value on shared primitive/powered maps: which build may run the recipe. */
    public static final int BUILD_PRIMITIVE = 0, BUILD_BRONZE = 1, BUILD_STEEL = 2;

    public static final String[] BUILD_NAMES = { "Primitive", "Bronze", "Steel" };
    public static final String[] MAGNET_NAMES = { "None", "Iron", "Steel", "Neodymium", "Samarium" };
    public static final String[] SPIRAL_NAMES = { "None", "Bronze", "Steel", "Stainless Steel", "Titanium" };
    public static final String[] JIG_NAMES = SPIRAL_NAMES;
    public static final String[] IMPELLER_NAMES = { "None", "Steel", "Aluminium", "Stainless Steel" };
    public static final String[] POWER_NAMES = { "Steam", "LV", "MV", "HV", "EV", "IV" };

    // ------------------------------------------------------------------ primitive

    public static final RecipeMap<RecipeMapBackend> sluiceBoxRecipes = RecipeMapBuilder.of("moon.recipes.sluicebox")
        .maxIO(1, 5, 0, 0)
        .progressBar(GTUITextures.PROGRESSBAR_SIFT)
        .build();

    public static final RecipeMap<RecipeMapBackend> pattinsonPotRecipes = RecipeMapBuilder
        .of("moon.recipes.pattinsonpots")
        .maxIO(2, 5, 0, 0)
        .progressBar(GTUITextures.PROGRESSBAR_ARROW_MULTIPLE)
        .neiSpecialInfoFormatter(special(n -> "Pots in the row: " + n))
        .build();

    public static final RecipeMap<RecipeMapBackend> retortFurnaceRecipes = RecipeMapBuilder
        .of("moon.recipes.retortfurnace")
        .maxIO(2, 2, 0, 0)
        .progressBar(GTUITextures.PROGRESSBAR_ARROW)
        .build();

    // ------------------------------------------------------------------ shared by primitive and powered builds

    public static final RecipeMap<RecipeMapBackend> leadHearthRecipes = RecipeMapBuilder.of("moon.recipes.leadhearth")
        .maxIO(2, 3, 0, 1)
        .progressBar(GTUITextures.PROGRESSBAR_ARROW)
        .neiSpecialInfoFormatter(special(MoonRecipeMaps::buildText))
        .build();

    public static final RecipeMap<RecipeMapBackend> shaftFurnaceRecipes = RecipeMapBuilder
        .of("moon.recipes.shaftfurnace")
        .maxIO(3, 2, 0, 1)
        .progressBar(GTUITextures.PROGRESSBAR_ARROW)
        .neiSpecialInfoFormatter(special(MoonRecipeMaps::buildText))
        .build();

    // ------------------------------------------------------------------ bronze and steel builds

    public static final RecipeMap<RecipeMapBackend> wetConcentratorRecipes = RecipeMapBuilder
        .of("moon.recipes.wetconcentrator")
        .maxIO(1, 3, 1, 1)
        .progressBar(GTUITextures.PROGRESSBAR_ARROW_MULTIPLE)
        .neiSpecialInfoFormatter(special(n -> "Spiral Concentrators: " + name(SPIRAL_NAMES, n)))
        .build();

    public static final RecipeMap<RecipeMapBackend> rotaryDryerRecipes = RecipeMapBuilder
        .of("moon.recipes.rotarydryer")
        .maxIO(1, 1, 0, 0)
        .progressBar(GTUITextures.PROGRESSBAR_ARROW)
        .build();

    public static final RecipeMap<RecipeMapBackend> magneticSeparatorRecipes = RecipeMapBuilder
        .of("moon.recipes.magneticseparator")
        .maxIO(1, 6, 0, 0)
        .progressBar(GTUITextures.PROGRESSBAR_MAGNET)
        .neiSpecialInfoFormatter(special(n -> "Drum Magnets: " + name(MAGNET_NAMES, n)))
        .build();

    public static final RecipeMap<RecipeMapBackend> electrostaticSeparatorRecipes = RecipeMapBuilder
        .of("moon.recipes.electrostaticseparator")
        .maxIO(1, 6, 0, 0)
        .progressBar(GTUITextures.PROGRESSBAR_ARROW_MULTIPLE)
        .neiSpecialInfoFormatter(special(n -> "Power source: " + name(POWER_NAMES, n)))
        .build();

    public static final RecipeMap<RecipeMapBackend> airTableRecipes = RecipeMapBuilder.of("moon.recipes.airtable")
        .maxIO(1, 3, 0, 0)
        .progressBar(GTUITextures.PROGRESSBAR_SIFT)
        .build();

    public static final RecipeMap<RecipeMapBackend> refiningKettleRecipes = RecipeMapBuilder
        .of("moon.recipes.refiningkettle")
        .maxIO(2, 2, 0, 0)
        .progressBar(GTUITextures.PROGRESSBAR_MIXER)
        .build();

    public static final RecipeMap<RecipeMapBackend> trommelRecipes = RecipeMapBuilder.of("moon.recipes.trommel")
        .maxIO(1, 3, 1, 0)
        .progressBar(GTUITextures.PROGRESSBAR_SIFT)
        .build();

    public static final RecipeMap<RecipeMapBackend> jigBankRecipes = RecipeMapBuilder.of("moon.recipes.jigbank")
        .maxIO(1, 3, 1, 1)
        .progressBar(GTUITextures.PROGRESSBAR_ARROW_MULTIPLE)
        .neiSpecialInfoFormatter(special(n -> "Jig Cells: " + name(JIG_NAMES, n)))
        .build();

    // ------------------------------------------------------------------ steel only

    public static final RecipeMap<RecipeMapBackend> flotationRecipes = RecipeMapBuilder.of("moon.recipes.flotation")
        .maxIO(2, 5, 2, 1)
        .progressBar(GTUITextures.PROGRESSBAR_ARROW_MULTIPLE)
        .neiSpecialInfoFormatter(special(n -> "Impeller: " + name(IMPELLER_NAMES, n)))
        .build();

    public static final RecipeMap<RecipeMapBackend> copperConverterRecipes = RecipeMapBuilder
        .of("moon.recipes.copperconverter")
        .maxIO(2, 1, 0, 1)
        .progressBar(GTUITextures.PROGRESSBAR_ARROW)
        .build();

    private static String buildText(int build) {
        return switch (build) {
            case BUILD_PRIMITIVE -> "Build: Primitive";
            case BUILD_BRONZE -> "Build: Bronze or Steel";
            default -> "Build: Steel";
        };
    }

    private static String name(String[] names, int index) {
        return index >= 0 && index < names.length ? names[index] : String.valueOf(index);
    }

    private static INEISpecialInfoFormatter special(IntFunction<String> text) {
        return info -> Collections.singletonList(text.apply(info.recipe.mSpecialValue));
    }
}
