package org.yonside.moon;

import cpw.mods.fml.common.event.*;
import gregtech.api.enums.Mods;
import gregtech.api.recipe.RecipeMaps;
import org.yonside.moon.config.CoreConfig;
import org.yonside.moon.config.Feature;
import org.yonside.moon.mod.AppliedEnergistics2;

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
            RecipeEditor.of(RecipeMaps.assemblerRecipes);
        }


        RecipeEditor.run();
    }

    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {}
}
