package org.yonside.moon.metatileentity;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

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

/**
 * Pattinson crystallisation: a row of iron pots that enriches silver into Rich Lead. More pots give a better split
 * but a longer run.
 */
public class MTEPattinsonPotRow extends MTEPrimitiveMultiBase<MTEPattinsonPotRow> {

    public static final int MIN_POTS = 3, MAX_POTS = 9;

    private static final String PIECE_FRONT = "front", PIECE_POT = "pot", PIECE_FILL = "fill", PIECE_BACK = "back";

    /**
     * A brick bench stepping up one block every three pots towards the back, a pot sitting on each step, and the chimney
     * at the top end. Pieces are one row deep; layers top to bottom.
     */
    private static final IStructureDefinition<MTEPattinsonPotRow> STRUCTURE_DEFINITION = IStructureDefinition
        .<MTEPattinsonPotRow>builder()
        .addShape(PIECE_FRONT, transpose(new String[][] { { "F~F" } }))
        .addShape(PIECE_POT, transpose(new String[][] { { "FPF" }, { "FFF" } }))
        .addShape(PIECE_FILL, transpose(new String[][] { { "FFF" } }))
        .addShape(PIECE_BACK, transpose(new String[][] { { " F " }, { " F " }, { "FFF" }, { "FFF" } }))
        .addElement('F', Casings.Firebricks.asElement())
        .addElement('P', MoonStructure.part(BlockMachineParts.Part.PattinsonPot))
        .build();

    /** Height of pot {@code i} (1-based) above the controller's layer. */
    private static int rise(int i) {
        return (i - 1) / 3;
    }

    private interface PieceVisitor {

        /** @return false to stop */
        boolean visit(String piece, int horizontal, int vertical, int depth);
    }

    /** Walks every piece of an {@code n}-pot row except the front, in build order. */
    private static boolean forEachPiece(int n, PieceVisitor visitor) {
        for (int i = 1; i <= n; i++) {
            int r = rise(i);
            for (int k = 0; k < r; k++) if (!visitor.visit(PIECE_FILL, 1, -k, -i)) return false;
            if (!visitor.visit(PIECE_POT, 1, 1 - r, -i)) return false;
        }
        int r = rise(n);
        for (int k = 0; k < r; k++) if (!visitor.visit(PIECE_FILL, 1, -k, -(n + 1))) return false;
        return visitor.visit(PIECE_BACK, 1, 3 - r, -(n + 1));
    }

    private int pots;

    public MTEPattinsonPotRow(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional, 2, 2);
    }

    public MTEPattinsonPotRow(String aName) {
        super(aName, 2, 2);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTEPattinsonPotRow(mName);
    }

    @Override
    public IStructureDefinition<MTEPattinsonPotRow> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    @Override
    protected boolean checkStructure() {
        pots = 0;
        if (!checkPiece(PIECE_FRONT, 1, 0, 0)) return false;
        while (pots < MAX_POTS && checkPiece(PIECE_POT, 1, 1 - rise(pots + 1), -(pots + 1))) pots++;
        if (pots < MIN_POTS || pots % 2 == 0) return false;
        return forEachPiece(pots, this::checkPiece);
    }

    private static int potsFor(ItemStack trigger) {
        // 1 → 3 pots, 2 → 5, 3 → 7, 4+ → 9
        return Math.min(MAX_POTS, MIN_POTS + 2 * (Math.max(1, trigger.stackSize) - 1));
    }

    @Override
    protected void constructStructure(ItemStack trigger, boolean hintsOnly) {
        buildPiece(PIECE_FRONT, trigger, hintsOnly, 1, 0, 0);
        forEachPiece(potsFor(trigger), (piece, h, v, d) -> {
            buildPiece(piece, trigger, hintsOnly, h, v, d);
            return true;
        });
    }

    @Override
    protected int survivalConstructStructure(ItemStack trigger, int elementBudget, ISurvivalBuildEnvironment env) {
        int[] built = { survivalBuildPiece(PIECE_FRONT, trigger, 1, 0, 0, elementBudget, env) };
        if (built[0] >= 0) return built[0];
        forEachPiece(potsFor(trigger), (piece, h, v, d) -> {
            built[0] = survivalBuildPiece(piece, trigger, h, v, d, elementBudget, env);
            return built[0] < 0;
        });
        return built[0];
    }

    @Override
    protected int getRecipeTier() {
        return pots;
    }

    @Override
    protected int getPollutionPerSecond() {
        return 10;
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setInteger("pots", pots);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        pots = aNBT.getInteger("pots");
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType("Pattinson Pot Row")
            .addInfo("Splits Lead Bullion into Lead and silver-rich Rich Lead by crystallisation")
            .addInfo("Build 3, 5, 7 or 9 pots: more pots recover more silver")
            .addInfo("Each pot adds 10 seconds to the run")
            .addPollutionAmount(getPollutionPerSecond())
            .beginVariableStructureBlock(3, 3, 4, 6, 5, 11, false)
            .addController("Front of the bench, bottom layer")
            .addCasingInfoRange("Firebricks", 25, 88, false)
            .addOtherStructurePart("Pattinson Pot", "One on each step of the bench")
            .addStructureInfo("The bench steps up one block every three pots towards the back")
            .addStructureInfo("A chimney stands at the top end of the bench")
            .toolTipFinisher("Moon");
        return tt;
    }

    @Override
    protected Casings getCasing() {
        return Casings.Firebricks;
    }

    @Override
    protected IIconContainer[] getFrontOverlays() {
        return MoonStructure.FURNACE;
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return MoonRecipeMaps.pattinsonPotRecipes;
    }
}
