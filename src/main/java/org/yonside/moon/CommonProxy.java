package org.yonside.moon;

import static gregtech.api.enums.Mods.GregTech;
import static org.yonside.moon.Utilities.getModItem;

import net.minecraftforge.fluids.FluidRegistry;

import org.yonside.moon.config.CoreConfig;
import org.yonside.moon.config.Feature;
import org.yonside.moon.metatileentity.MTEFlotationCell;
import org.yonside.moon.mod.AppliedEnergistics2;

import bartworks.common.loaders.ItemRegistry;
import cpw.mods.fml.common.event.*;
import gregtech.api.enums.*;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTRecipeBuilder;
import tectech.recipe.TecTechRecipeMaps;

public class CommonProxy {

    public static MTEFlotationCell flotationCell;

    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());

        Moon.LOG.info(Config.greeting);
        Moon.LOG.info("I am MyMod at version " + Tags.VERSION);

        MoonMaterials.init();

    }

    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        flotationCell = new MTEFlotationCell(22000, "moon.flotationcell", "Flotation Cell");
    }

    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {}

    public void loadComplete(FMLLoadCompleteEvent event) {
        if (Mods.AppliedEnergistics2.isModLoaded()) AppliedEnergistics2.loadComplete(event);
        if (CoreConfig.isEnabled(Feature.VOIDMINER)) {
            RecipeEditor.removeAssemblyLineRecipe(
                ItemRegistry.voidminer[0],
                ItemRegistry.voidminer[1],
                ItemRegistry.voidminer[2]);
            RecipeEditor.of(RecipeMaps.assemblylineVisualRecipes)
                .edit("remove assembly line visuals")
                .outputtingAny(ItemRegistry.voidminer)
                .queueRemoval();
            RecipeEditor.of(RecipeMaps.scannerFakeRecipes)
                .edit("remove assembly line visuals")
                .outputtingAny(ItemRegistry.voidminer)
                .queueRemoval();;
            RecipeEditor.of(TecTechRecipeMaps.researchStationFakeRecipes)
                .edit("remove research station")
                .outputtingAny(ItemRegistry.voidminer)
                .queueRemoval();

            GTValues.RA.stdBuilder()
                .itemInputs(
                    getModItem(GregTech.ID, "gt.blockmachines", 1, 681),
                    GTOreDictUnificator.get(OrePrefixes.frameGt, Materials.Steel, 4L),
                    GTOreDictUnificator.get(OrePrefixes.plate, Materials.Steel, 3L),
                    GTOreDictUnificator.get(OrePrefixes.screw, Materials.Steel, 36L),
                    getModItem(GregTech.ID, "gt.metaitem.01", 4, 32692),
                    getModItem(GregTech.ID, "gt.metaitem.01", 4, 32672),
                    getModItem(GregTech.ID, "gt.metaitem.01", 4, 32602))
                .fluidInputs(FluidRegistry.getFluidStack("nitrogen", 16000))
                .itemOutputs(getModItem(GregTech.ID, "gt.blockmachines", 1, 12741))
                .duration(60 * GTRecipeBuilder.SECONDS)
                .eut(TierEU.RECIPE_HV)
                .addTo(RecipeMaps.assemblerRecipes);

            GTValues.RA.stdBuilder()
                .itemInputs(
                    getModItem(GregTech.ID, "gt.blockmachines", 1, 12741),
                    GTOreDictUnificator.get(OrePrefixes.frameGt, Materials.Titanium, 4L),
                    GTOreDictUnificator.get(OrePrefixes.plate, Materials.Titanium, 3L),
                    GTOreDictUnificator.get(OrePrefixes.screw, Materials.Titanium, 36L),
                    getModItem(GregTech.ID, "gt.metaitem.01", 4, 32693),
                    getModItem(GregTech.ID, "gt.metaitem.01", 4, 32673),
                    getModItem(GregTech.ID, "gt.metaitem.01", 4, 32603))
                .fluidInputs(FluidRegistry.getFluidStack("helium", 16000))
                .itemOutputs(getModItem(GregTech.ID, "gt.blockmachines", 1, 12740))
                .duration(60 * GTRecipeBuilder.SECONDS)
                .eut(TierEU.RECIPE_EV)
                .addTo(RecipeMaps.assemblerRecipes);

            GTValues.RA.stdBuilder()
                .itemInputs(
                    getModItem(GregTech.ID, "gt.blockmachines", 1, 12740),
                    GTOreDictUnificator.get(OrePrefixes.frameGt, Materials.TungstenSteel, 4L),
                    GTOreDictUnificator.get(OrePrefixes.plate, Materials.TungstenSteel, 3L),
                    GTOreDictUnificator.get(OrePrefixes.screw, Materials.TungstenSteel, 36L),
                    getModItem(GregTech.ID, "gt.metaitem.01", 4, 32694),
                    getModItem(GregTech.ID, "gt.metaitem.01", 4, 32674),
                    getModItem(GregTech.ID, "gt.metaitem.01", 4, 32604))
                .fluidInputs(FluidRegistry.getFluidStack("helium", 16000))
                .itemOutputs(getModItem(GregTech.ID, "gt.blockmachines", 1, 12739))
                .duration(60 * GTRecipeBuilder.SECONDS)
                .eut(TierEU.RECIPE_IV)
                .addTo(RecipeMaps.assemblerRecipes);
        }

        RecipeEditor.run();
    }

    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {}
}
