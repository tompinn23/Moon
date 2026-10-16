package org.yonside.moon.metatileentity;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.isAir;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;

import net.minecraft.item.ItemStack;

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
 * Charcoal-fired ore hearth: smelts galena, roasts sphalerite, cupels rich lead and reduces litharge. Vents its sulfur
 * and fume as pollution; the steel {@link MTELeadHearth} captures them.
 */
public class MTEPrimitiveLeadHearth extends MTEPrimitiveMultiBase<MTEPrimitiveLeadHearth> {

    private static final String STRUCTURE_PIECE_MAIN = "main";

    // Layers top to bottom, rows front to back. A low open hearth under a hood on four pillars that funnels up into a
    // flue
    private static final String[][] STRUCTURE = new String[][] {
        { "     ", "     ", "  F  ", "     ", "     " },
        { "     ", "     ", "  F  ", "     ", "     " },
        { "     ", " FFF ", " F-F ", " FFF ", "     " },
        { " FFF ", "F---F", "F---F", "F---F", " FFF " },
        { " F F ", "     ", "     ", "     ", " F F " },
        { " F~F ", "F---F", "F---F", "F---F", " FFF " },
        { " FFF ", "FFFFF", "FFFFF", "FFFFF", " FFF " }, };

    private static final IStructureDefinition<MTEPrimitiveLeadHearth> STRUCTURE_DEFINITION = IStructureDefinition
        .<MTEPrimitiveLeadHearth>builder()
        .addShape(
            STRUCTURE_PIECE_MAIN,
            transpose(STRUCTURE))
        .addElement('F', Casings.Firebricks.asElement())
        .addElement('-', isAir())
        .build();

    public MTEPrimitiveLeadHearth(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional, 2, 3);
    }

    public MTEPrimitiveLeadHearth(String aName) {
        super(aName, 2, 3);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTEPrimitiveLeadHearth(mName);
    }

    @Override
    public IStructureDefinition<MTEPrimitiveLeadHearth> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    @Override
    protected boolean checkStructure() {
        return checkPiece(STRUCTURE_PIECE_MAIN, 2, 5, 0);
    }

    @Override
    protected void constructStructure(ItemStack trigger, boolean hintsOnly) {
        buildPiece(STRUCTURE_PIECE_MAIN, trigger, hintsOnly, 2, 5, 0);
    }

    @Override
    protected int survivalConstructStructure(ItemStack trigger, int elementBudget, ISurvivalBuildEnvironment env) {
        return survivalBuildPiece(STRUCTURE_PIECE_MAIN, trigger, 2, 5, 0, elementBudget, env);
    }

    @Override
    protected int getPollutionPerSecond() {
        return 40;
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType("Lead Hearth")
            .addInfo("Smelts galena to lead bullion with charcoal")
            .addInfo("Cupels Rich Lead on a Bone Ash Cupel to recover silver")
            .addInfo("Reduces litharge, lead fume and antimony dross")
            .addInfo("A quarter of the lead is lost as fume")
            .addPollutionAmount(getPollutionPerSecond())
            .beginStructureBlock(5, 7, 5, false)
            .addController("Front of the hearth wall")
            .addCasingInfoExactly("Firebricks", 58, false)
            .addStructureInfo("The hood stands on four pillars and funnels into a flue")
            .addStructureInfo("The hearth and the inside of the hood must be empty")
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
        return MoonRecipeMaps.leadHearthRecipes;
    }
}
