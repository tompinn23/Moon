package org.yonside.moon.metatileentity;

import com.gtnewhorizon.structurelib.alignment.IAlignment;
import com.gtnewhorizon.structurelib.alignment.IAlignmentLimits;
import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import gregtech.api.casing.Casings;
import gregtech.api.enums.Materials;
import gregtech.api.interfaces.ISecondaryDescribable;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.modularui.IGetTitleColor;
import gregtech.api.interfaces.tileentity.ICasingTextureProvider;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.interfaces.tileentity.RecipeMapWorkable;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipe.RecipeMap;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlockAnyMeta;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;
import static gregtech.api.util.GTStructureUtility.ofFrame;

public class MTESluiceBox extends MetaTileEntity implements IAlignment, ISurvivalConstructable, RecipeMapWorkable, IGetTitleColor, ISecondaryDescribable, ICasingTextureProvider {


    private static final IStructureDefinition<MTESluiceBox> STRUCTURE_DEFINITION = IStructureDefinition.<MTESluiceBox>builder()
        .addShape("main",
            transpose(
                new String[][]{
                    {"ABA","ADA","ADA","ADA","ADA","ACA","AAA"},
                    {"A~A","AAA","AAA","AAA","AAA","AAA","AAA"},
                }
            ))
        .addElement('A', Casings.PrimitiveWoodenCasing.asElement())
        .addElement('B', ofFrame(Materials.Wood))
        .addElement('C', ofBlockAnyMeta(Blocks.water, 1))
        .addElement('D', ofBlockAnyMeta(Blocks.flowing_water, 1))
        .build();


    public MTESluiceBox(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional, 6);
    }

    public MTESluiceBox(String aName) {
        super(aName, 6);
    }

    @Override
    public ExtendedFacing getExtendedFacing() {
        return ExtendedFacing.of(getBaseMetaTileEntity().getFrontFacing());
    }

    @Override
    public void setExtendedFacing(ExtendedFacing alignment) {
        getBaseMetaTileEntity().setFrontFacing(alignment.getDirection());
    }

    @Override
    public IAlignmentLimits getAlignmentLimits() {
        return (d, r, f) -> (d.flag & (ForgeDirection.UP.flag | ForgeDirection.DOWN.flag)) == 0 && r.isNotRotated()
            && f.isNotFlipped();
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        STRUCTURE_DEFINITION.buildOrHints(
            this,
            stackSize,
            "main",
            getBaseMetaTileEntity().getWorld(),
            getExtendedFacing(),
            getBaseMetaTileEntity().getXCoord(),
            getBaseMetaTileEntity().getYCoord(),
            getBaseMetaTileEntity().getZCoord(),
            1,
            2,
            0,
            hintsOnly);
    }

    @Override
    public String[] getStructureDescription(ItemStack stackSize) {
        return new String[0];
    }

    @Override
    public String[] getPrimaryDescription() {
        return new String[0];
    }

    @Override
    public String[] getSecondaryDescription() {
        return new String[0];
    }

    @Override
    public byte getTileEntityBaseType() {
        return 0;
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity iGregTechTileEntity) {
        return null;
    }

    @Override
    public void saveNBTData(NBTTagCompound nbtTagCompound) {

    }

    @Override
    public void loadNBTData(NBTTagCompound nbtTagCompound) {

    }

    @Override
    public boolean allowPullStack(IGregTechTileEntity iGregTechTileEntity, int i, ForgeDirection forgeDirection, ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean allowPutStack(IGregTechTileEntity iGregTechTileEntity, int i, ForgeDirection forgeDirection, ItemStack itemStack) {
        return false;
    }

    @Override
    public String[] getDescription() {
        return new String[0];
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity iGregTechTileEntity, ForgeDirection forgeDirection, ForgeDirection forgeDirection1, int i, boolean b, boolean b1) {
        return new ITexture[0];
    }

    @Override
    public ITexture getCasingTexture() {
        return null;
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return null;
    }
}
