package org.yonside.moon.mixins.voidminer;

import bwcrossmod.galacticgreg.MTEVoidMiners;
import com.gtnewhorizon.structurelib.structure.IStructureElement;
import gregtech.api.casing.Casings;
import gregtech.api.enums.Materials;
import gregtech.api.util.GTStructureUtility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.yonside.moon.MoonCasings;

@Mixin(value = { MTEVoidMiners.VMLUV.class}, remap = false)
public abstract class MixinVoidMinerCasings {

    @Redirect(
        method = "<clinit>",
        at = @At(value = "INVOKE",
            target = "Lgregtech/api/casing/Casings;asElement()Lcom/gtnewhorizon/structurelib/structure/IStructureElement;"),
        remap = false)
    private static IStructureElement<?> moon$swapCasing(Casings casing) {
        if (casing == Casings.MiningOsmiridiumCasing)   return Casings.CleanStainlessSteelMachineCasing.asElement();
        if (casing == Casings.BoltedOsmiridiumCasing)   return MoonCasings.BoltedStainlessSteelCasing.asElement();
        if (casing == Casings.ReboltedOsmiridiumCasing) return MoonCasings.ReboltedStainlessSteelCasing.asElement();
        return casing.asElement();
    }

    @Redirect(
        method = "<clinit>",
        at = @At(value = "INVOKE", target = "Lgregtech/api/util/GTStructureUtility;ofFrame(Lgregtech/api/enums/Materials;)Lcom/gtnewhorizon/structurelib/structure/IStructureElement;"),
        remap = false)
    private static IStructureElement<?> moon$swapFrame(Materials material) {
        return material == Materials.Osmiridium
            ? GTStructureUtility.ofFrame(Materials.StainlessSteel)
            : GTStructureUtility.ofFrame(material);
    }

    @Inject(method="getControllerTextureIndex",at= @At("HEAD"), cancellable = true)
    public void getControllerTextureIndex(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(Casings.CleanStainlessSteelMachineCasing.getTextureId());
    }

    @Redirect(
        method = "<init>",
        at = @At(value = "INVOKE", target = "Lgregtech/api/casing/Casings;getTextureId()I"),
        require = 1)
    private int moon$casingTexture(Casings casing) {
        return casing == Casings.MiningOsmiridiumCasing
            ? Casings.CleanStainlessSteelMachineCasing.getTextureId()
            : casing.getTextureId();
    }
}
