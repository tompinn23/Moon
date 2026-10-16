package org.yonside.moon.metatileentity.base;

import java.util.Collections;
import java.util.List;

import gregtech.api.interfaces.IHatchElement;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.util.IGTHatchAdder;
import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.MTEHatchSteamBusInput;
import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.MTEHatchSteamBusOutput;
import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.base.MTEHatchCustomFluidBase;

/** Steam-build hatches. GT's own bus adders refuse steam buses, so these route them into the normal bus lists. */
public enum MoonHatchElement implements IHatchElement<MTEOreMultiBase<?>> {

    SteamHatch(MTEHatchCustomFluidBase.class) {

        @Override
        public IGTHatchAdder<? super MTEOreMultiBase<?>> adder() {
            return MTEOreMultiBase::addSteamHatch;
        }

        @Override
        public long count(MTEOreMultiBase<?> t) {
            return t.steamHatches.size();
        }
    },
    SteamInputBus(MTEHatchSteamBusInput.class) {

        @Override
        public IGTHatchAdder<? super MTEOreMultiBase<?>> adder() {
            return MTEOreMultiBase::addSteamBus;
        }

        @Override
        public long count(MTEOreMultiBase<?> t) {
            return t.mInputBusses.stream()
                .filter(b -> b instanceof MTEHatchSteamBusInput)
                .count();
        }
    },
    SteamOutputBus(MTEHatchSteamBusOutput.class) {

        @Override
        public IGTHatchAdder<? super MTEOreMultiBase<?>> adder() {
            return MTEOreMultiBase::addSteamBus;
        }

        @Override
        public long count(MTEOreMultiBase<?> t) {
            return t.mOutputBusses.stream()
                .filter(b -> b instanceof MTEHatchSteamBusOutput)
                .count();
        }
    };

    private final List<? extends Class<? extends IMetaTileEntity>> classes;

    MoonHatchElement(Class<? extends IMetaTileEntity> mteClass) {
        this.classes = Collections.singletonList(mteClass);
    }

    @Override
    public List<? extends Class<? extends IMetaTileEntity>> mteClasses() {
        return classes;
    }
}
