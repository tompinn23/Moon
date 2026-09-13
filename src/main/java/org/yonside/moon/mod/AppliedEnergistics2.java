package org.yonside.moon.mod;

import java.util.Arrays;
import java.util.Objects;

import org.yonside.moon.RecipeEditor;
import org.yonside.moon.config.CoreConfig;
import org.yonside.moon.config.Feature;

import appeng.api.config.PowerMultiplier;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import gregtech.api.enums.Materials;
import gregtech.api.enums.Mods;
import gregtech.api.enums.VoltageIndex;
import gregtech.api.recipe.RecipeMaps;

public class AppliedEnergistics2 {

    public static void loadComplete(FMLLoadCompleteEvent event) {
        if (CoreConfig.isEnabled(Feature.AE2_DOWNTIER)) {
            RecipeEditor.of(RecipeMaps.assemblerRecipes)
                .edit("AE2 controller")
                .matching(
                    r -> Arrays.stream(r.mOutputs)
                        .allMatch(i -> Objects.equals(RecipeEditor.modIdOf(i), Mods.AppliedEnergistics2.getID())))
                .swapMaterial(Materials.Titanium, Materials.Aluminium)
                .swapMaterial(Materials.HV, Materials.MV)
                .swapVoltage(VoltageIndex.HV, VoltageIndex.MV)
                .queue();

            PowerMultiplier.CONFIG.multiplier = Math.max(0.01, PowerMultiplier.CONFIG.multiplier * 0.25);
        }
    }
}
