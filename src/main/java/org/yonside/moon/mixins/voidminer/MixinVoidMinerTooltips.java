package org.yonside.moon.mixins.voidminer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import bwcrossmod.galacticgreg.MTEVoidMiners;
import gregtech.api.util.MultiblockTooltipBuilder;

@Mixin(value = { MTEVoidMiners.VMLUV.class, MTEVoidMiners.VMUV.class, MTEVoidMiners.VMZPM.class }, remap = false)
public abstract class MixinVoidMinerTooltips {

    @Redirect(
        method = "createTooltip",
        at = @At(
            value = "INVOKE",
            target = "Lgregtech/api/util/MultiblockTooltipBuilder;addCasing(Ljava/lang/String;Ljava/lang/String;Z)Lgregtech/api/util/MultiblockTooltipBuilder;"),
        require = 4,
        remap = false)
    private MultiblockTooltipBuilder moon$renameCasing(MultiblockTooltipBuilder tt, String count, String name,
        boolean flag) {
        return tt.addCasing(count, moon$rename(name), flag);
    }

    private String moon$rename(String label) {
        return label.replace("Mining Osmiridium", "Solid Steel")
            .replace("Mining Black Plutonium", "Stable Titanium")
            .replace("Mining Neutronium", "Robust Tungsten Steel")
            .replace("Osmiridium", "Steel")
            .replace("Black Plutonium", "Titanium")
            .replace("Naquadah Alloy", "Titanium")
            .replace("Neutronium", "Tungsten Steel")
            .replace("Iridium", "Tungsten Steel")
            .replace("Adamantium", "Tungsten Steel")
            .replace("Black Plutonium", "Titanium"); // the shared pipe casing line
    }
}
