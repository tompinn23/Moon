package org.yonside.moon;

import org.yonside.moon.block.MoonBlocks;
import org.yonside.moon.metatileentity.MTEAirTable;
import org.yonside.moon.metatileentity.MTECopperConverter;
import org.yonside.moon.metatileentity.MTEElectrostaticSeparator;
import org.yonside.moon.metatileentity.MTEFlotationCellBank;
import org.yonside.moon.metatileentity.MTEJigBank;
import org.yonside.moon.metatileentity.MTELeadHearth;
import org.yonside.moon.metatileentity.MTEMagneticSeparator;
import org.yonside.moon.metatileentity.MTEPattinsonPotRow;
import org.yonside.moon.metatileentity.MTEPrimitiveLeadHearth;
import org.yonside.moon.metatileentity.MTEPrimitiveShaftFurnace;
import org.yonside.moon.metatileentity.MTERefiningKettle;
import org.yonside.moon.metatileentity.MTERetortFurnace;
import org.yonside.moon.metatileentity.MTERotaryDryer;
import org.yonside.moon.metatileentity.MTEShaftFurnace;
import org.yonside.moon.metatileentity.MTESluiceBox;
import org.yonside.moon.metatileentity.MTETrommel;
import org.yonside.moon.metatileentity.MTEWetConcentratorPlant;
import org.yonside.moon.mod.AppliedEnergistics2;
import org.yonside.moon.recipes.MoonCrafting;
import org.yonside.moon.recipes.MoonRecipes;
import org.yonside.moon.recipes.RecipeModification;

import cpw.mods.fml.common.event.*;
import gregtech.api.enums.*;

public class CommonProxy {

    // Stone age
    public static MTESluiceBox sluiceBox;
    public static MTEPrimitiveLeadHearth primitiveLeadHearth;
    public static MTEPattinsonPotRow pattinsonPotRow;
    public static MTERetortFurnace retortFurnace;
    public static MTEPrimitiveShaftFurnace primitiveShaftFurnace;

    // Bronze and steel builds
    public static MTEWetConcentratorPlant wetConcentratorPlant;
    public static MTERotaryDryer rotaryDryer;
    public static MTEMagneticSeparator magneticSeparator;
    public static MTEElectrostaticSeparator electrostaticSeparator;
    public static MTEAirTable airTable;
    public static MTERefiningKettle refiningKettle;
    public static MTEShaftFurnace shaftFurnace;
    public static MTETrommel trommel;
    public static MTEJigBank jigBank;

    // Steel only (LV)
    public static MTELeadHearth leadHearth;
    public static MTEFlotationCellBank flotationCellBank;
    public static MTECopperConverter copperConverter;

    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());

        Moon.LOG.info(Config.greeting);
        Moon.LOG.info("I am MyMod at version " + Tags.VERSION);

        MoonMaterials.init();
        // Blocks first: the multiblock structure definitions reference them when their classes load
        MoonBlocks.init();

        flotationCellBank = new MTEFlotationCellBank(22000, "moon.flotationcellbank", "Flotation Cell Bank");
        wetConcentratorPlant = new MTEWetConcentratorPlant(22001, "moon.wetconcentratorplant", "Wet Concentrator Plant");
        sluiceBox = new MTESluiceBox(22002, "moon.sluicebox", "Sluice Box");
        primitiveLeadHearth = new MTEPrimitiveLeadHearth(22003, "moon.primitiveleadhearth", "Primitive Lead Hearth");
        pattinsonPotRow = new MTEPattinsonPotRow(22004, "moon.pattinsonpotrow", "Pattinson Pot Row");
        retortFurnace = new MTERetortFurnace(22005, "moon.retortfurnace", "Retort Furnace");
        primitiveShaftFurnace = new MTEPrimitiveShaftFurnace(22006, "moon.primitiveshaftfurnace", "Primitive Shaft Furnace");
        rotaryDryer = new MTERotaryDryer(22007, "moon.rotarydryer", "Rotary Dryer");
        magneticSeparator = new MTEMagneticSeparator(22008, "moon.magneticseparator", "Magnetic Separator");
        electrostaticSeparator = new MTEElectrostaticSeparator(22009, "moon.electrostaticseparator", "Electrostatic Separator");
        airTable = new MTEAirTable(22010, "moon.airtable", "Air Table");
        leadHearth = new MTELeadHearth(22011, "moon.leadhearth", "Lead Hearth");
        refiningKettle = new MTERefiningKettle(22012, "moon.refiningkettle", "Refining Kettle");
        shaftFurnace = new MTEShaftFurnace(22013, "moon.shaftfurnace", "Shaft Furnace");
        copperConverter = new MTECopperConverter(22014, "moon.copperconverter", "Copper Converter");
        trommel = new MTETrommel(22015, "moon.trommel", "Trommel");
        jigBank = new MTEJigBank(22016, "moon.jigbank", "Jig Bank");
    }

    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {}

    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
        // Recipes go in postInit: some GT fluids (e.g. Blue Vitriol) are not assigned until GT's own init has run
        MoonRecipes.init();
        MoonCrafting.init();
    }

    public void loadComplete(FMLLoadCompleteEvent event) {
        if (Mods.AppliedEnergistics2.isModLoaded()) AppliedEnergistics2.loadComplete(event);

        RecipeModification.init();
    }

    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {}
}
