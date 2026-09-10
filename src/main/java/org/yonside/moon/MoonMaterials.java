package org.yonside.moon;

import bartworks.system.material.Werkstoff;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;

public class MoonMaterials {
    private static final Werkstoff.GenerationFeatures ADD_CASINGS_ONLY = new Werkstoff.GenerationFeatures().disable()
        .addPrefix(OrePrefixes.blockCasing)
        .addPrefix(OrePrefixes.blockCasingAdvanced);

    public static Werkstoff StainlessSteel = new Werkstoff(
        Materials.StainlessSteel,
        ADD_CASINGS_ONLY,
        Werkstoff.Types.MIXTURE,
        31_766 + 419);


    public static void init() {
        Moon.LOG.info("Load Elements from GT");
    }
}
