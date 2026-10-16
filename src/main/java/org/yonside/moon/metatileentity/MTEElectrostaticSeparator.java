package org.yonside.moon.metatileentity;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.isAir;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;
import static gregtech.api.util.GTStructureUtility.chainAllGlasses;

import java.util.List;

import org.yonside.moon.block.BlockMachineParts.Part;
import org.yonside.moon.metatileentity.base.MTEFixedOreMultiBase;
import org.yonside.moon.metatileentity.base.MoonStructure;
import org.yonside.moon.recipes.MoonRecipeMaps;

import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;

import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.structure.error.StructureError;
import gregtech.api.util.MultiblockTooltipBuilder;

/**
 * High-tension rolls that split conducting minerals from non-conductors. The best power source sets how clean the cut
 * is: steam sorts 40% of a batch, IV sorts all of it.
 */
public class MTEElectrostaticSeparator extends MTEFixedOreMultiBase<MTEElectrostaticSeparator> {

    // Layers top to bottom, rows front to back: grains cascade left-right-left-right down the rolls behind glass
    private static final String[][] STRUCTURE = new String[][] {
        { "CCCCC", "CCCCC", "CCCCC" },
        { "BGGGB", "CR--C", "BGGGB" },
        { "BGGGB", "C--RC", "BGGGB" },
        { "BGGGB", "CR--C", "BGGGB" },
        { "BGGGB", "C--RC", "BGGGB" },
        { "CC~CC", "CCCCC", "CCCCC" }, };

    private static final IStructureDefinition<MTEElectrostaticSeparator> STRUCTURE_DEFINITION = StructureDefinition
        .<MTEElectrostaticSeparator>builder()
        .addShape(STRUCTURE_PIECE_MAIN, transpose(STRUCTURE))
        .addElement('C', buildCasing(MTEElectrostaticSeparator.class, 1))
        .addElement('R', MoonStructure.part(Part.HighTensionRoll))
        .addElement('G', chainAllGlasses())
        .addElement('B', buildFrame())
        .addElement('-', isAir())
        .build();

    public MTEElectrostaticSeparator(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional, 2, 5, 0);
    }

    public MTEElectrostaticSeparator(String aName) {
        super(aName, 2, 5, 0);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTEElectrostaticSeparator(mName);
    }

    @Override
    public IStructureDefinition<MTEElectrostaticSeparator> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    @Override
    protected void validateStructure(List<StructureError> errors) {
        checkHasInputBus(errors);
        checkHasOutputBus(errors);
    }

    @Override
    protected int getRecipeTier() {
        return getPowerTier();
    }

    @Override
    protected int getMinCasings() {
        return 25;
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType("Electrostatic Separator");
        addBuildInfo(tt).addInfo("Splits conductors (ilmenite, rutile, cassiterite) from non-conductors")
            .addInfo("The best power source sets the cut: Steam 40%, LV 50%, MV 60%, HV 70%, EV 90%, IV 100%")
            .addInfo("Unsorted material comes back as Non-magnetic Heavy Mineral Concentrate")
            .beginStructureBlock(5, 6, 3, false)
            .addController("Front center, bottom layer")
            .addCasingInfoMin("Bronze Plated Bricks or Solid Steel Machine Casing", 25, false)
            .addOtherStructurePart("High-Tension Roll", "4, alternating left and right down the middle")
            .addOtherStructurePart("Any Glass", "Front and back walls")
            .addOtherStructurePart("Bronze or Steel Frame Box", "Corners")
            .addStructureInfo("The space beside each roll must be empty")
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
        return MoonStructure.CENTRIFUGE;
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return MoonRecipeMaps.electrostaticSeparatorRecipes;
    }
}
