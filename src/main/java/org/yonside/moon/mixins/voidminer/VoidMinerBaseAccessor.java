package org.yonside.moon.mixins.voidminer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import bwcrossmod.galacticgreg.MTEVoidMinerBase;

@Mixin(value = MTEVoidMinerBase.class, remap = false)
public interface VoidMinerBaseAccessor {

    @Accessor("TIER_MULTIPLIER")
    byte moon$getTierMultiplier();
}
