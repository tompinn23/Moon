package org.yonside.moon;

import java.util.Arrays;
import java.util.Objects;

import org.yonside.moon.mod.AppliedEnergistics2;

import cpw.mods.fml.common.event.*;
import gregtech.api.enums.Materials;
import gregtech.api.enums.Mods;
import gregtech.api.enums.VoltageIndex;
import gregtech.api.recipe.RecipeMaps;

public class CommonProxy {

    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());

        Moon.LOG.info(Config.greeting);
        Moon.LOG.info("I am MyMod at version " + Tags.VERSION);
    }

    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {}

    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {}

    public void loadComplete(FMLLoadCompleteEvent event) {

        RecipeEditor.of(RecipeMaps.assemblerRecipes)
            .edit("AE2 controller")
            .matching(
                r -> Arrays.stream(r.mOutputs)
                    .allMatch(i -> Objects.equals(RecipeEditor.modIdOf(i), Mods.AppliedEnergistics2.getID())))
            .swapMaterial(Materials.Titanium, Materials.Aluminium)
            .swapMaterial(Materials.HV, Materials.MV)
            .swapVoltage(VoltageIndex.HV, VoltageIndex.MV)
            .queue()
            .apply();

        if (Mods.AppliedEnergistics2.isModLoaded()) AppliedEnergistics2.loadComplete(event);

    }

    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {}
}
