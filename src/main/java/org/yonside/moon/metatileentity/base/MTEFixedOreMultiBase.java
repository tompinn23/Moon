package org.yonside.moon.metatileentity.base;

import java.util.List;

import net.minecraft.item.ItemStack;

import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;

import gregtech.api.structure.error.StructureError;

/** An ore multiblock with a single fixed-shape piece. */
public abstract class MTEFixedOreMultiBase<T extends MTEFixedOreMultiBase<T>> extends MTEOreMultiBase<T> {

    protected static final String STRUCTURE_PIECE_MAIN = "main";

    private final int offsetX, offsetY, offsetZ;

    protected MTEFixedOreMultiBase(int aID, String aName, String aNameRegional, int offsetX, int offsetY,
        int offsetZ) {
        super(aID, aName, aNameRegional);
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
    }

    protected MTEFixedOreMultiBase(String aName, int offsetX, int offsetY, int offsetZ) {
        super(aName);
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
    }

    @Override
    protected boolean checkStructure(List<StructureError> errors) {
        return checkPiece(STRUCTURE_PIECE_MAIN, offsetX, offsetY, offsetZ, errors);
    }

    @Override
    public void construct(ItemStack trigger, boolean hintsOnly) {
        buildPiece(STRUCTURE_PIECE_MAIN, trigger, hintsOnly, offsetX, offsetY, offsetZ);
    }

    @Override
    public int survivalConstruct(ItemStack trigger, int elementBudget, ISurvivalBuildEnvironment env) {
        if (mMachine) return -1;
        return survivalBuildPiece(
            STRUCTURE_PIECE_MAIN,
            trigger,
            offsetX,
            offsetY,
            offsetZ,
            elementBudget,
            env,
            false,
            true);
    }
}
