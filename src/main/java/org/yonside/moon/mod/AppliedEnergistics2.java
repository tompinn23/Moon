package org.yonside.moon.mod;

import appeng.api.config.PowerMultiplier;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;

public class AppliedEnergistics2 {

    public static void loadComplete(FMLLoadCompleteEvent event) {
        PowerMultiplier.CONFIG.multiplier = Math.max(0.01, PowerMultiplier.CONFIG.multiplier * 0.25);
    }
}
