package org.yonside.moon;

import bartworks.system.material.Werkstoff;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.enums.TextureSet;
import org.apache.commons.lang3.tuple.Pair;

public class MoonMaterials {

    private static final Werkstoff.GenerationFeatures ADD_CASINGS_ONLY = new Werkstoff.GenerationFeatures().disable()
        .addPrefix(OrePrefixes.blockCasing)
        .addPrefix(OrePrefixes.blockCasingAdvanced);

    public static Werkstoff Steel = new Werkstoff(
        Materials.Steel,
        ADD_CASINGS_ONLY,
        Werkstoff.Types.MIXTURE,
        31_766 + 419);

    public static Werkstoff Titanium = new Werkstoff(
        Materials.Titanium,
        ADD_CASINGS_ONLY,
        Werkstoff.Types.MIXTURE,
        31_766 + 420);

    public static Werkstoff TungstenSteel = new Werkstoff(
        Materials.TungstenSteel,
        ADD_CASINGS_ONLY,
        Werkstoff.Types.MIXTURE,
        31_766 + 421);

    public static Werkstoff SandSlurry = new Werkstoff(
        new short[] {158, 160, 135, 255},
        "Sand Slurry",
        Werkstoff.Types.MIXTURE,
        new Werkstoff.GenerationFeatures().disable()
            .addCells(),
        31_766 + 422,
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
        31_766 + 423,
        TextureSet.SET_FLUID
    );

    public static void init() {
        Moon.LOG.info("Load Elements from GT");
    }
}
