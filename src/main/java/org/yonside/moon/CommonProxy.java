package org.yonside.moon;

import static gregtech.api.enums.Mods.GregTech;
import static org.yonside.moon.Utilities.getModItem;

import net.minecraftforge.fluids.FluidRegistry;

import org.yonside.moon.config.CoreConfig;
import org.yonside.moon.config.Feature;
import org.yonside.moon.metatileentity.MTEFlotationCell;
import org.yonside.moon.metatileentity.MTEWashConcentratorPlant;
import org.yonside.moon.mod.AppliedEnergistics2;

import bartworks.common.loaders.ItemRegistry;
import cpw.mods.fml.common.event.*;
import gregtech.api.enums.*;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTRecipeBuilder;
import org.yonside.moon.recipes.MoonRecipes;
import org.yonside.moon.recipes.RecipeModification;
import tectech.recipe.TecTechRecipeMaps;

public class CommonProxy {

    public static MTEFlotationCell flotationCell;
    public static MTEWashConcentratorPlant washConcentratorPlant;

    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());

        Moon.LOG.info(Config.greeting);
        Moon.LOG.info("I am MyMod at version " + Tags.VERSION);

        MoonMaterials.init();
        flotationCell = new MTEFlotationCell(22000, "moon.flotationcell", "Flotation Cell");
        washConcentratorPlant = new MTEWashConcentratorPlant(22001, "moon.washconcentratorplant", "Wash Concentrator");
    }

    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        MoonRecipes.init();
    }

    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
    }

    public void loadComplete(FMLLoadCompleteEvent event) {
        if (Mods.AppliedEnergistics2.isModLoaded()) AppliedEnergistics2.loadComplete(event);

        RecipeModification.init();
    }

    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {}
}
