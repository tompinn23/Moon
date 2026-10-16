package org.yonside.moon.metatileentity;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;

import java.util.List;

import org.yonside.moon.block.BlockMachineParts.Part;
import org.yonside.moon.metatileentity.base.MTEFixedOreMultiBase;
import org.yonside.moon.metatileentity.base.MoonStructure;
import org.yonside.moon.recipes.MoonRecipeMaps;

import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;

import gregtech.api.casing.Casings;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.structure.error.StructureError;
import gregtech.api.structure.error.StructureErrors;
import gregtech.api.util.MultiblockTooltipBuilder;

/** A vibrating deck with air blown through it: the addon's general density separator. Untiered, always sorts fully. */
public class MTEAirTable extends MTEFixedOreMultiBase<MTEAirTable> {

    // Layers top to bottom, rows front to back. A deck tilted across both axes so grains walk downhill to the front
    // corner, on a base with a blower under each deck column
    private static final String[][] STRUCTURE = new String[][] {
        { "     ", "     ", "     ", "   T ", "     " },
        { "     ", "   T ", "  TT ", " TTC ", "     " },
        { "     ", " TTC ", " TCC ", " CCC ", "     " },
        { "CC~CC", "CFFFC", "CFFFC", "CFFFC", "CCCCC" }, };

    private static final IStructureDefinition<MTEAirTable> STRUCTURE_DEFINITION = StructureDefinition
        .<MTEAirTable>builder()
        .addShape(STRUCTURE_PIECE_MAIN, transpose(STRUCTURE))
        .addElement('C', buildCasing(MTEAirTable.class, 1))
        .addElement('T', MoonStructure.part(Part.AirTableDeck))
        .addElement(
            'F',
            buildPart(
                Casings.BronzeGearBoxCasing,
                Casings.SteelGearBoxCasing,
                (t, tier) -> t.fanTier = tier,
                t -> t.fanTier))
        .build();

    private int fanTier = -1;

    public MTEAirTable(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional, 2, 3, 0);
    }

    public MTEAirTable(String aName) {
        super(aName, 2, 3, 0);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTEAirTable(mName);
    }

    @Override
    public IStructureDefinition<MTEAirTable> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    @Override
    protected boolean checkStructure(List<StructureError> errors) {
        fanTier = -1;
        return super.checkStructure(errors);
    }

    @Override
    protected void validateStructure(List<StructureError> errors) {
        if (fanTier != build.ordinal()) {
            errors.add(StructureErrors.of("The blowers must match the build: Bronze or Steel Gear Box Casing"));
        }
        checkHasInputBus(errors);
        checkHasOutputBus(errors);
    }

    @Override
    protected int getMinCasings() {
        return 18;
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType("Air Table");
        addBuildInfo(tt).addInfo("Separates grains by density on an air-fluidised deck")
            .addInfo("Splits heavy mineral, lead, copper and jig concentrates")
            .beginStructureBlock(5, 4, 5, false)
            .addController("Front center of the base")
            .addCasingInfoMin("Bronze Plated Bricks or Solid Steel Machine Casing", 18, false)
            .addOtherStructurePart("Air Table Deck", "9, a deck tilted across both axes")
            .addOtherStructurePart("Bronze or Steel Gear Box Casing", "9 blowers in the base under the deck")
            .addInputBus("Any casing", 1)
            .addOutputBus("Any casing", 1)
            .addEnergyHatch("Steel build, any casing", 1)
            .addMaintenanceHatch("Steel build, any casing", 1)
            .addOtherStructurePart("Steam Hatch", "Bronze build, any casing", 1)
            .toolTipFinisher("Moon");
        return tt;
    }

    @Override
    protected IIconContainer[] getFrontOverlays() {
        return MoonStructure.WASHER;
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return MoonRecipeMaps.airTableRecipes;
    }
}
