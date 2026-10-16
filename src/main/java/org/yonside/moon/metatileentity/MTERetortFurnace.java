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

/** Belgian zinc retorts: zinc is boiled off as vapour and condensed, losing about a third. */
public class MTERetortFurnace extends MTEPrimitiveMultiBase<MTERetortFurnace> {

    private static final String STRUCTURE_PIECE_MAIN = "main";

    // Layers top to bottom, rows front to back. An arched brick gallery with clay retorts set into both side walls and
    // a chimney at the back
    private static final String[][] STRUCTURE = new String[][] {
        { "     ", "     ", "     ", "     ", "  F  " },
        { "     ", "     ", "     ", "     ", "  F  " },
        { " FFF ", " FFF ", " FFF ", " FFF ", " FFF " },
        { "FFFFF", "F---F", "F---F", "F---F", "FFFFF" },
        { "FF~FF", "R---R", "F---F", "R---R", "FFFFF" },
        { "FFFFF", "FFFFF", "FFFFF", "FFFFF", "FFFFF" }, };

    private static final IStructureDefinition<MTERetortFurnace> STRUCTURE_DEFINITION = IStructureDefinition
        .<MTERetortFurnace>builder()
        .addShape(
            STRUCTURE_PIECE_MAIN,
            transpose(STRUCTURE))
        .addElement('F', Casings.Firebricks.asElement())
        .addElement('R', MoonStructure.part(BlockMachineParts.Part.ClayRetort))
        .addElement('-', isAir())
        .build();

    public MTERetortFurnace(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional, 2, 2);
    }

    public MTERetortFurnace(String aName) {
        super(aName, 2, 2);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTERetortFurnace(mName);
    }

    @Override
    public IStructureDefinition<MTERetortFurnace> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    @Override
    protected boolean checkStructure() {
        return checkPiece(STRUCTURE_PIECE_MAIN, 2, 4, 0);
    }

    @Override
    protected void constructStructure(ItemStack trigger, boolean hintsOnly) {
        buildPiece(STRUCTURE_PIECE_MAIN, trigger, hintsOnly, 2, 4, 0);
    }

    @Override
    protected int survivalConstructStructure(ItemStack trigger, int elementBudget, ISurvivalBuildEnvironment env) {
        return survivalBuildPiece(STRUCTURE_PIECE_MAIN, trigger, 2, 4, 0, elementBudget, env);
    }

    @Override
    protected int getPollutionPerSecond() {
        return 20;
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType("Retort Furnace")
            .addInfo("Smelts zinc calcine to zinc as a vapour and condenses it")
            .addInfo("Distils Parkes zinc crust back to Rich Lead and zinc")
            .addPollutionAmount(getPollutionPerSecond())
            .beginStructureBlock(5, 6, 5, false)
            .addController("Front wall, center, above the floor")
            .addCasingInfoExactly("Firebricks", 69, false)
            .addOtherStructurePart("Clay Retort", "4, set into both side walls of the gallery")
            .addStructureInfo("The gallery must be empty")
            .toolTipFinisher("Moon");
        return tt;
    }

    @Override
    protected Casings getCasing() {
        return Casings.Firebricks;
    }

    @Override
    protected IIconContainer[] getFrontOverlays() {
        return MoonStructure.DISTILLERY;
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return MoonRecipeMaps.retortFurnaceRecipes;
    }
}
