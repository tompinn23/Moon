package org.yonside.moon.mixins.voidminer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import bwcrossmod.galacticgreg.MTEVoidMinerBase;

@Mixin(value = MTEVoidMinerBase.class, remap = false)
public abstract class MixinVoidMinerTier {

    @Final
    @Shadow(remap = false)
    protected byte TIER_MULTIPLIER;

    @Inject(method = "getMinTier", at = @At("HEAD"), cancellable = true, remap = false)
    private void moon$lowerTier(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(TIER_MULTIPLIER + 2); // HV / EV / IV instead of LuV / ZPM / UV
    }
}
