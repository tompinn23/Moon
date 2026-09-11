package org.yonside.moon.mixins.voidminer;

import bwcrossmod.galacticgreg.MTEVoidMinerBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = MTEVoidMinerBase.class, remap = false)
public interface VoidMinerBaseAccessor {

    @Accessor("TIER_MULTIPLIER")
    byte moon$getTierMultiplier();
}
