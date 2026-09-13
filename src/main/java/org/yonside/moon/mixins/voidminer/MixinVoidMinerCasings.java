package org.yonside.moon.mixins.voidminer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.yonside.moon.MoonCasings;

import com.gtnewhorizon.structurelib.structure.IStructureElement;

import bwcrossmod.galacticgreg.MTEVoidMiners;
import gregtech.api.casing.Casings;
import gregtech.api.casing.ICasing;
import gregtech.api.enums.Materials;
import gregtech.api.util.GTStructureUtility;

@Mixin(value = { MTEVoidMiners.VMLUV.class, MTEVoidMiners.VMUV.class, MTEVoidMiners.VMZPM.class }, remap = false)
public abstract class MixinVoidMinerCasings {

    @Redirect(
        method = "<clinit>",
        at = @At(
            value = "INVOKE",
            target = "Lgregtech/api/util/GTStructureUtility;ofFrame(Lgregtech/api/enums/Materials;)Lcom/gtnewhorizon/structurelib/structure/IStructureElement;"),
        remap = false)
    private static IStructureElement<?> moon$swapFrame(Materials material) {
        if (material == Materials.Osmiridium) return GTStructureUtility.ofFrame(Materials.Steel);
        if (material == Materials.NaquadahAlloy) return GTStructureUtility.ofFrame(Materials.Titanium);
        if (material == Materials.Adamantium) return GTStructureUtility.ofFrame(Materials.TungstenSteel);
        return GTStructureUtility.ofFrame(material);
    }

    @Inject(method = "getControllerTextureIndex", at = @At("HEAD"), cancellable = true)
    public void getControllerTextureIndex(CallbackInfoReturnable<Integer> cir) {

        switch (((VoidMinerBaseAccessor) this).moon$getTierMultiplier()) {
            case 1:
                cir.setReturnValue(Casings.SolidSteelMachineCasing.getTextureId());
                break;
            case 2:
                cir.setReturnValue(Casings.StableTitaniumMachineCasing.getTextureId());
                break;
            default:
                cir.setReturnValue(Casings.RobustTungstenSteelMachineCasing.getTextureId());
                break;
        }

    }

    @Redirect(
        method = "<init>(Ljava/lang/String;I)V",
        at = @At(value = "INVOKE", target = "Lgregtech/api/casing/Casings;getTextureId()I"),
        require = 1)
    private int moon$casingTexture(Casings casing) {
        return moon$mapped(casing).getTextureId();
    }

    @Redirect(
        method = "<clinit>",
        at = @At(value = "INVOKE", target = "Lgregtech/api/casing/Casings;getTextureId()I"),
        require = 1,
        remap = false)
    private static int moon$textureId(Casings casing) {
        return moon$mapped(casing).getTextureId();
    }

    @Redirect(
        method = "<clinit>",
        at = @At(
            value = "INVOKE",
            target = "Lgregtech/api/casing/Casings;asElement()Lcom/gtnewhorizon/structurelib/structure/IStructureElement;"),
        require = 4,
        remap = false)
    private static IStructureElement<?> moon$element(Casings casing) {
        return moon$mapped(casing).asElement();
    }

    private static ICasing moon$mapped(ICasing casing) {
        if (casing == Casings.MiningOsmiridiumCasing) return Casings.SolidSteelMachineCasing;
        if (casing == Casings.BoltedOsmiridiumCasing) return MoonCasings.BoltedSteelCasing;
        if (casing == Casings.ReboltedOsmiridiumCasing) return MoonCasings.ReboltedSteelCasing;
        if (casing == Casings.MiningBlackPlutoniumCasing) return Casings.StableTitaniumMachineCasing;
        if (casing == Casings.BoltedNaquadahAlloyCasing) return MoonCasings.BoltedTitaniumCasing;
        if (casing == Casings.ReboltedNaquadahAlloyCasing) return MoonCasings.ReboltedTitaniumCasing;
        if (casing == Casings.MiningNeutroniumCasing) return Casings.RobustTungstenSteelMachineCasing;
        if (casing == Casings.BoltedIridiumCasing) return MoonCasings.BoltedTungstenSteelCasing;
        if (casing == Casings.ReboltedIridiumCasing) return MoonCasings.ReboltedTungstenSteelCasing;

        if (casing == Casings.BlackPlutoniumItemPipeCasing) return Casings.TitaniumPipeCasing;
        return casing;
    }
}
