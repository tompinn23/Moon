package org.yonside.moon;

import bartworks.system.material.Werkstoff;
import gregtech.api.enums.MaterialIconRegistry;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.enums.TextureSet;
import org.apache.commons.lang3.tuple.Pair;

public class MoonMaterials {

    private static final int ID_BASE = 31_766;

    private static final Werkstoff.GenerationFeatures ADD_CASINGS_ONLY = new Werkstoff.GenerationFeatures().disable()
        .addPrefix(OrePrefixes.blockCasing)
        .addPrefix(OrePrefixes.blockCasingAdvanced);

    public static Werkstoff Steel = new Werkstoff(
        Materials.Steel,
        ADD_CASINGS_ONLY,
        Werkstoff.Types.MIXTURE,
        ID_BASE + 419);

    public static Werkstoff Titanium = new Werkstoff(
        Materials.Titanium,
        ADD_CASINGS_ONLY,
        Werkstoff.Types.MIXTURE,
        ID_BASE + 420);

    public static Werkstoff TungstenSteel = new Werkstoff(
        Materials.TungstenSteel,
        ADD_CASINGS_ONLY,
        Werkstoff.Types.MIXTURE,
        ID_BASE + 421);

    public static Werkstoff SandSlurry = new Werkstoff(
        new short[] {158, 160, 135, 255},
        "Sand Slurry",
        Werkstoff.Types.MIXTURE,
        new Werkstoff.GenerationFeatures().disable()
            .addCells(),
        ID_BASE + 422,
        TextureSet.SET_FLUID,
        Pair.of(Materials.QuartzSand, 1),
        Pair.of(Materials.Water, 2)
    );

    public static Werkstoff LowGradeMetalConcentrate = new Werkstoff(
        new short[] {92, 85, 67, 255},
        "Low-Grade Metal Concentrate",
        Werkstoff.Types.MIXTURE,
        new Werkstoff.GenerationFeatures().disable()
            .addCells(),
        ID_BASE + 423,
        TextureSet.SET_FLUID
    );

    public static Werkstoff NativeCopper = new Werkstoff(
        new short[] {201, 112, 58, 255},
        "Native Copper",
        noDecomposition(),
        Werkstoff.Types.MIXTURE,
        new Werkstoff.GenerationFeatures().disable()
            .onlyDust(),
        ID_BASE + 424,
        TextureSet.SET_ROUGH.withCustomTextures(
            "nativecopper",
            MaterialIconRegistry.IconType.DUST,
            MaterialIconRegistry.IconType.DUST_SMALL,
            MaterialIconRegistry.IconType.DUST_TINY)
    );

    public static Werkstoff GalenaConcentrate = dust("Galena Concentrate", 84, 84, 102, 425, TextureSet.SET_DULL);

    // ------------------------------------------------------------------ mineral sands line

    public static Werkstoff HeavyMineralSand = dust("Heavy Mineral Sand", 120, 105, 90, 426, TextureSet.SET_SAND);
    public static Werkstoff WetHeavyMineralConcentrate = dust("Wet Heavy Mineral Concentrate", 70, 62, 58, 427, TextureSet.SET_SAND);
    public static Werkstoff DryHeavyMineralConcentrate = dust("Dry Heavy Mineral Concentrate", 92, 80, 72, 428, TextureSet.SET_SAND);
    public static Werkstoff NonMagneticHeavyMineralConcentrate = dust("Non-magnetic Heavy Mineral Concentrate", 140, 120, 100, 429, TextureSet.SET_SAND);
    public static Werkstoff ConductorConcentrate = dust("Conductor Concentrate", 60, 60, 66, 430, TextureSet.SET_SAND);
    public static Werkstoff NonConductorConcentrate = dust("Non-conductor Concentrate", 190, 170, 150, 431, TextureSet.SET_SAND);
    public static Werkstoff TitaniumConcentrate = dust("Titanium Concentrate", 80, 70, 80, 432, TextureSet.SET_SAND);
    public static Werkstoff ZirconMonaziteConcentrate = dust("Zircon-Monazite Concentrate", 200, 150, 110, 433, TextureSet.SET_SAND);
    public static Werkstoff MineralSandTailings = dust("Mineral Sand Tailings", 200, 190, 160, 434, TextureSet.SET_SAND);
    public static Werkstoff WetLowGradeHeavyMineralConcentrate = dust("Wet Low-grade Heavy Mineral Concentrate", 110, 100, 90, 435, TextureSet.SET_SAND);
    public static Werkstoff DryLowGradeHeavyMineralConcentrate = dust("Dry Low-grade Heavy Mineral Concentrate", 150, 135, 115, 436, TextureSet.SET_SAND);

    // ------------------------------------------------------------------ lead and silver

    /** Crushed form is "Crushed Argentiferous Lead Ore"; the dust is the ground ore fed to flotation. */
    public static Werkstoff ArgentiferousLeadOre = crushable("Argentiferous Lead", 100, 100, 115, 437);
    public static Werkstoff SphaleriteConcentrate = dust("Sphalerite Concentrate", 160, 140, 90, 438, TextureSet.SET_DULL);
    public static Werkstoff BulkSulfideConcentrate = dust("Bulk Sulfide Concentrate", 105, 100, 95, 439, TextureSet.SET_DULL);
    public static Werkstoff WetBulkSulfideConcentrate = dust("Wet Bulk Sulfide Concentrate", 75, 72, 70, 440, TextureSet.SET_DULL);
    public static Werkstoff LeadMillTailings = dust("Lead Mill Tailings", 150, 145, 140, 441, TextureSet.SET_SAND);
    public static Werkstoff ZincCalcine = dust("Zinc Calcine", 225, 220, 200, 442, TextureSet.SET_DULL);
    public static Werkstoff LeadFume = dust("Lead Fume", 180, 180, 190, 443, TextureSet.SET_FINE);
    public static Werkstoff Litharge = dust("Litharge", 230, 170, 60, 444, TextureSet.SET_DULL);
    public static Werkstoff AntimonyDross = dust("Antimony Dross", 130, 130, 150, 445, TextureSet.SET_ROUGH);
    public static Werkstoff ZincCrust = dust("Zinc Crust", 190, 195, 205, 446, TextureSet.SET_ROUGH);
    public static Werkstoff AnodeSlime = dust("Anode Slime", 60, 55, 45, 447, TextureSet.SET_DULL);
    public static Werkstoff LeadBullion = metal("Lead Bullion", 120, 110, 140, 448);
    public static Werkstoff RichLead = metal("Rich Lead", 150, 140, 175, 449);
    public static Werkstoff SoftenedBullion = metal("Softened Bullion", 130, 120, 150, 450);

    // ------------------------------------------------------------------ copper

    public static Werkstoff OxidisedCopperOre = crushable("Oxidised Copper", 70, 140, 100, 451);
    public static Werkstoff MalachiteConcentrate = dust("Malachite Concentrate", 20, 150, 80, 452, TextureSet.SET_DULL);
    public static Werkstoff CopperTailings = dust("Copper Tailings", 160, 150, 135, 453, TextureSet.SET_SAND);
    public static Werkstoff ChalcopyriteOre = crushable("Chalcopyrite", 160, 140, 50, 454);
    public static Werkstoff CopperLeadConcentrate = dust("Copper-Lead Concentrate", 140, 115, 70, 455, TextureSet.SET_DULL);
    public static Werkstoff WetCopperLeadConcentrate = dust("Wet Copper-Lead Concentrate", 100, 85, 55, 456, TextureSet.SET_DULL);
    public static Werkstoff ChalcopyriteConcentrate = dust("Chalcopyrite Concentrate", 190, 160, 40, 457, TextureSet.SET_DULL);
    public static Werkstoff CopperMatte = dust("Copper Matte", 90, 60, 45, 458, TextureSet.SET_METALLIC);
    public static Werkstoff IronSlag = dust("Iron Slag", 70, 55, 50, 459, TextureSet.SET_ROUGH);
    public static Werkstoff BlisterCopper = metal("Blister Copper", 170, 90, 50, 460);

    // ------------------------------------------------------------------ skyblock gravel route

    public static Werkstoff PayGravel = dust("Pay Gravel", 120, 110, 105, 461, TextureSet.SET_ROUGH);
    public static Werkstoff JigConcentrate = dust("Jig Concentrate", 85, 70, 65, 462, TextureSet.SET_ROUGH);
    public static Werkstoff JigTailings = dust("Jig Tailings", 150, 140, 130, 463, TextureSet.SET_SAND);
    public static Werkstoff NonMagneticJigConcentrate = dust("Non-magnetic Jig Concentrate", 125, 95, 75, 464, TextureSet.SET_ROUGH);
    public static Werkstoff CopperGalenaConcentrate = dust("Copper-Galena Concentrate", 140, 100, 80, 465, TextureSet.SET_DULL);
    public static Werkstoff MilledJigConcentrate = dust("Milled Jig Concentrate", 130, 100, 85, 466, TextureSet.SET_FINE);
    public static Werkstoff MetallicFlakes = dust("Metallic Flakes", 210, 120, 70, 467, TextureSet.SET_SHINY);
    public static Werkstoff MineralFines = dust("Mineral Fines", 110, 100, 110, 468, TextureSet.SET_FINE);
    public static Werkstoff ArgentiferousCopper = metal("Argentiferous Copper", 205, 130, 95, 469);

    private static Werkstoff.Stats noDecomposition() {
        return new Werkstoff.Stats().setCentrifuge(false)
            .setElektrolysis(false);
    }

    /** A process intermediate: dust, small and tiny dust only, no decomposition recipes. */
    private static Werkstoff dust(String name, int r, int g, int b, int id, TextureSet texture) {
        return new Werkstoff(
            new short[] { (short) r, (short) g, (short) b, 255 },
            name,
            noDecomposition(),
            Werkstoff.Types.MIXTURE,
            new Werkstoff.GenerationFeatures().disable()
                .onlyDust(),
            ID_BASE + id,
            texture);
    }

    /** A hammered ore: "Crushed <name> Ore" plus a dust used as the ground (milled) ore. */
    private static Werkstoff crushable(String name, int r, int g, int b, int id) {
        return new Werkstoff(
            new short[] { (short) r, (short) g, (short) b, 255 },
            name,
            noDecomposition(),
            Werkstoff.Types.MIXTURE,
            new Werkstoff.GenerationFeatures().disable()
                .onlyDust()
                .addPrefix(OrePrefixes.crushed),
            ID_BASE + id,
            TextureSet.SET_ROUGH);
    }

    /** An impure metal handled as ingots (bullion, blister copper). */
    private static Werkstoff metal(String name, int r, int g, int b, int id) {
        return new Werkstoff(
            new short[] { (short) r, (short) g, (short) b, 255 },
            name,
            noDecomposition(),
            Werkstoff.Types.MIXTURE,
            new Werkstoff.GenerationFeatures().disable()
                .onlyDust()
                .addMetalItems(),
            ID_BASE + id,
            TextureSet.SET_DULL);
    }

    public static void init() {
        Moon.LOG.info("Load Elements from GT");
    }
}
