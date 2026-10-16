package org.yonside.moon.metatileentity;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.isAir;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;
import static gregtech.api.util.GTStructureUtility.ofFrame;

import java.util.List;

import org.yonside.moon.metatileentity.base.MTEFixedOreMultiBase;
import org.yonside.moon.metatileentity.base.MoonStructure;
import org.yonside.moon.recipes.MoonRecipeMaps;

import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;

import gregtech.api.casing.Casings;
import gregtech.api.enums.Materials;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.structure.error.StructureError;
import gregtech.api.util.MultiblockTooltipBuilder;

/**
 * The LV Lead Hearth: same chemistry as {@link MTEPrimitiveLeadHearth}, but it captures sulfur dioxide and returns the
 * fume as an item, like a baghouse.
 */
public class MTELeadHearth extends MTEFixedOreMultiBase<MTELeadHearth> {

    // Layers top to bottom, rows front to back. A low open hearth on a firebrick floor under a hood on four frame
    // pillars, funnelling up a flue to the baghouse
    private static final String[][] STRUCTURE = new String[][] {
        { "     ", "     ", "  C  ", "     ", "     " },
        { "     ", "     ", "  P  ", "     ", "     " },
        { "     ", " CCC ", " C-C ", " CCC ", "     " },
        { " CCC ", "C---C", "C---C", "C---C", " CCC " },
        { " B B ", "     ", "     ", "     ", " B B " },
        { " C~C ", "C---C", "C---C", "C---C", " CCC " },
        { " CCC ", "CFFFC", "CFFFC", "CFFFC", " CCC " }, };

    private static final IStructureDefinition<MTELeadHearth> STRUCTURE_DEFINITION = StructureDefinition
        .<MTELeadHearth>builder()
        .addShape(STRUCTURE_PIECE_MAIN, transpose(STRUCTURE))
        .addElement('C', buildCasing(MTELeadHearth.class, 1))
        .addElement('F', Casings.Firebricks.asElement())
        .addElement('P', Casings.SteelPipeCasing.asElement())
        .addElement('B', ofFrame(Materials.Steel))
        .addElement('-', isAir())
        .build();

    public MTELeadHearth(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional, 2, 5, 0);
    }

    public MTELeadHearth(String aName) {
        super(aName, 2, 5, 0);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTELeadHearth(mName);
    }

    @Override
    public IStructureDefinition<MTELeadHearth> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    @Override
    protected boolean allowsBronze() {
        return false;
    }

    @Override
    protected int getMinRecipeTier() {
        return MoonRecipeMaps.BUILD_STEEL;
    }

    @Override
    protected void validateStructure(List<StructureError> errors) {
        checkHasInputBus(errors);
        checkHasOutputBus(errors);
        checkHasOutputHatch(errors);
    }

    @Override
    protected int getMinCasings() {
        return 30;
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType("Lead Hearth");
        addBuildInfo(tt).addInfo("Smelts galena, roasts sphalerite, cupels Rich Lead, reduces litharge and dross")
            .addInfo("Captures sulfur dioxide and returns lead fume as an item")
            .addInfo("Smelting is still charcoal-fed: carbon is the reductant")
            .beginStructureBlock(5, 7, 5, false)
            .addController("Front of the hearth wall")
            .addCasingInfoMin("Solid Steel Machine Casing", 30, false)
            .addOtherStructurePart("Firebricks", "9, the hearth floor")
            .addOtherStructurePart("Steel Frame Box", "4 pillars holding the hood")
            .addOtherStructurePart("Steel Pipe Casing", "The flue")
            .addStructureInfo("The casing capping the flue is a good place for the sulfur dioxide output hatch")
            .addInputBus("Any casing", 1)
            .addOutputBus("Any casing", 1)
            .addOutputHatch("Sulfur dioxide, any casing", 1)
            .addEnergyHatch("Any casing", 1)
            .addMaintenanceHatch("Any casing", 1)
            .toolTipFinisher("Moon");
        return tt;
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
