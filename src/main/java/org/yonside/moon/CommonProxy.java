package org.yonside.moon;

import bartworks.common.loaders.ItemRegistry;
import cpw.mods.fml.common.event.*;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.*;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTRecipeBuilder;
import gregtech.api.util.GTUtility;
import gregtech.loaders.postload.recipes.AssemblyLineRecipes;
import net.minecraftforge.fluids.FluidRegistry;
import org.yonside.moon.config.CoreConfig;
import org.yonside.moon.config.Feature;
import org.yonside.moon.mod.AppliedEnergistics2;

import java.util.Arrays;

import static gregtech.api.enums.Mods.GregTech;
import static org.yonside.moon.Utilities.getModItem;

public class CommonProxy {

    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());

        Moon.LOG.info(Config.greeting);
        Moon.LOG.info("I am MyMod at version " + Tags.VERSION);

        MoonMaterials.init();
    }

    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {}

    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {}

    public void loadComplete(FMLLoadCompleteEvent event) {
        if(Mods.AppliedEnergistics2.isModLoaded()) AppliedEnergistics2.loadComplete(event);
        if(CoreConfig.isEnabled(Feature.VOIDMINER)) {
            RecipeEditor.removeAssemblyLineRecipe(ItemRegistry.voidminer[0], ItemRegistry.voidminer[1], ItemRegistry.voidminer[2]);
            RecipeEditor.of(RecipeMaps.assemblylineVisualRecipes)
                    .edit("remove assembly line visuals")
                    .outputtingAny(ItemRegistry.voidminer)
                    .queueRemoval();
            RecipeEditor.of(RecipeMaps.scannerFakeRecipes)
                .edit("remove assembly line visuals")
                .outputtingAny(ItemRegistry.voidminer)
                .queueRemoval();;

            GTValues.RA.stdBuilder()
                .itemInputs(
                    getModItem(GregTech.ID, "gt.blockmachines", 1,681),
                    getModItem(GregTech.ID, "gt.blockframes", 4,305),
                    GTOreDictUnificator.get(OrePrefixes.plate, Materials.Steel, 3L),
                    GTOreDictUnificator.get(OrePrefixes.screw, Materials.Steel, 36L),
                    getModItem(GregTech.ID, "gt.metaitem.01", 4,32692),
                    getModItem(GregTech.ID, "gt.metaitem.01", 4,32672),
                    getModItem(GregTech.ID, "gt.metaitem.01", 4,32602)
                )
                .fluidInputs(FluidRegistry.getFluidStack("oxygen", 16000))
                .itemOutputs(getModItem(GregTech.ID, "gt.blockmachines", 1, 12741))
                .duration(60 * GTRecipeBuilder.SECONDS)
                .eut(TierEU.RECIPE_HV)
                .addTo(RecipeMaps.assemblerRecipes);
        }


        RecipeEditor.run();
    }

    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {}
}
