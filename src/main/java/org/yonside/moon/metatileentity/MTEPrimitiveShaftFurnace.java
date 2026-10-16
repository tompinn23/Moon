package org.yonside.moon.metatileentity;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.isAir;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;

import net.minecraft.item.ItemStack;

import org.yonside.moon.block.BlockMachineParts;
import org.yonside.moon.metatileentity.base.MTEPrimitiveMultiBase;
import org.yonside.moon.metatileentity.base.MoonStructure;
import org.yonside.moon.recipes.MoonRecipeMaps;

import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;

import gregtech.api.casing.Casings;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.util.MultiblockTooltipBuilder;

/** Firebrick shaft furnace for oxide copper and copper flakes. Sulfide smelting needs the bronze or steel build. */
public class MTEPrimitiveShaftFurnace extends MTEPrimitiveMultiBase<MTEPrimitiveShaftFurnace> {

    private static final String STRUCTURE_PIECE_MAIN = "main";

    /**
     * Layers top to bottom, rows front to back. A round blast-furnace profile: a narrow throat widens to a fat
     * belly with a tuyere in each side, then pulls in to the hearth.
     */
    static final int[] OFFSET = { 3, 5, 1 };
    static final String[][] SHAPE = new String[][] {
        { "       ", "       ", "  CCC  ", "  C-C  ", "  CCC  ", "       ", "       " },
        { "       ", "       ", "  CCC  ", "  C-C  ", "  CCC  ", "       ", "       " },
        { "       ", "  CCC  ", " C---C ", " C---C ", " C---C ", "  CCC  ", "       " },
        { "  CTC  ", " C---C ", "C-----C", "T-----T", "C-----C", " C---C ", "  CTC  " },
        { "       ", "  CCC  ", " C---C ", " C---C ", " C---C ", "  CCC  ", "       " },
        { "       ", "  C~C  ", " CCCCC ", " CCCCC ", " CCCCC ", "  CCC  ", "       " }, };

    private static final IStructureDefinition<MTEPrimitiveShaftFurnace> STRUCTURE_DEFINITION = IStructureDefinition
        .<MTEPrimitiveShaftFurnace>builder()
        .addShape(STRUCTURE_PIECE_MAIN, transpose(SHAPE))
        .addElement('C', Casings.Firebricks.asElement())
        .addElement('T', MoonStructure.part(BlockMachineParts.Part.ClayTuyere))
        .addElement('-', isAir())
        .build();

    public MTEPrimitiveShaftFurnace(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional, 3, 2);
    }

    public MTEPrimitiveShaftFurnace(String aName) {
        super(aName, 3, 2);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTEPrimitiveShaftFurnace(mName);
    }

    @Override
    public IStructureDefinition<MTEPrimitiveShaftFurnace> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    @Override
    protected boolean checkStructure() {
        return checkPiece(STRUCTURE_PIECE_MAIN, OFFSET[0], OFFSET[1], OFFSET[2]);
    }

    @Override
    protected void constructStructure(ItemStack trigger, boolean hintsOnly) {
        buildPiece(STRUCTURE_PIECE_MAIN, trigger, hintsOnly, OFFSET[0], OFFSET[1], OFFSET[2]);
    }

    @Override
    protected int survivalConstructStructure(ItemStack trigger, int elementBudget, ISurvivalBuildEnvironment env) {
        return survivalBuildPiece(STRUCTURE_PIECE_MAIN, trigger, OFFSET[0], OFFSET[1], OFFSET[2], elementBudget, env);
    }

    @Override
    protected int getPollutionPerSecond() {
        return 40;
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType("Shaft Furnace")
            .addInfo("Smelts malachite concentrate to copper with charcoal")
            .addInfo("Melts metallic flakes to argentiferous copper")
            .addInfo("Matte smelting needs the bronze or steel Shaft Furnace")
            .addPollutionAmount(getPollutionPerSecond())
            .beginStructureBlock(7, 6, 7, false)
            .addController("Front of the hearth, bottom layer")
            .addCasingInfoExactly("Firebricks", 72, false)
            .addOtherStructurePart("Clay Tuyere", "4, around the belly")
            .addStructureInfo("Round shaft: narrow throat, wide belly, narrow hearth; the inside must be empty")
            .toolTipFinisher("Moon");
        return tt;
    }

    @Override
    protected Casings getCasing() {
        return Casings.Firebricks;
    }

    @Override
    protected IIconContainer[] getFrontOverlays() {
        return MoonStructure.BLAST;
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return MoonRecipeMaps.shaftFurnaceRecipes;
    }
}
