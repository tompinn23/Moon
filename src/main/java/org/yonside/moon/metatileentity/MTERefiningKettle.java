package org.yonside.moon.metatileentity;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.isAir;
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

/** A stirred cast-iron pot: softens bullion (antimony dross) and Parkes-desilvers it with zinc. */
public class MTERefiningKettle extends MTEFixedOreMultiBase<MTERefiningKettle> {

    // Layers top to bottom, rows front to back. A round bowl narrowing down to the kettle over a fire pit, with the
    // stirrer hanging from a frame arch that rests on the rim
    private static final String[][] STRUCTURE = new String[][] {
        { "       ", "       ", "       ", "BBBGBBB", "       ", "       ", "       " },
        { "       ", "       ", "       ", "B  B  B", "       ", "       ", "       " },
        { "  CCC  ", " C---C ", "C-----C", "C--B--C", "C-----C", " C---C ", "  CCC  " },
        { "       ", "  CCC  ", " C---C ", " C---C ", " C---C ", "  CCC  ", "       " },
        { "       ", "       ", "  CCC  ", "  CKC  ", "  CCC  ", "       ", "       " },
        { "       ", "  C~C  ", " CFFFC ", " CFFFC ", " CFFFC ", "  CCC  ", "       " }, };

    private static final IStructureDefinition<MTERefiningKettle> STRUCTURE_DEFINITION = StructureDefinition
        .<MTERefiningKettle>builder()
        .addShape(STRUCTURE_PIECE_MAIN, transpose(STRUCTURE))
        .addElement('C', buildCasing(MTERefiningKettle.class, 1))
        .addElement('K', MoonStructure.part(Part.RefiningKettle))
        .addElement(
            'G',
            buildPart(
                Casings.BronzeGearBoxCasing,
                Casings.SteelGearBoxCasing,
                (t, tier) -> t.gearboxTier = tier,
                t -> t.gearboxTier))
        .addElement(
            'F',
            buildPart(
                Casings.BronzeFireboxCasing,
                Casings.SteelFireboxCasing,
                (t, tier) -> t.fireboxTier = tier,
                t -> t.fireboxTier))
        .addElement('B', buildFrame())
        .addElement('-', isAir())
        .build();

    private int gearboxTier = -1, fireboxTier = -1;

    public MTERefiningKettle(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional, 3, 5, 1);
    }

    public MTERefiningKettle(String aName) {
        super(aName, 3, 5, 1);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTERefiningKettle(mName);
    }

    @Override
    public IStructureDefinition<MTERefiningKettle> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    @Override
    protected boolean checkStructure(List<StructureError> errors) {
        gearboxTier = -1;
        fireboxTier = -1;
        return super.checkStructure(errors);
    }

    @Override
    protected void validateStructure(List<StructureError> errors) {
        if (gearboxTier != build.ordinal() || fireboxTier != build.ordinal()) {
            errors.add(StructureErrors.of("The stirrer gearbox and firebox must match the build"));
        }
        checkHasInputBus(errors);
        checkHasOutputBus(errors);
    }

    @Override
    protected int getMinCasings() {
        return 30;
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType("Refining Kettle");
        addBuildInfo(tt).addInfo("Softening: skims antimony dross off lead bullion")
            .addInfo("Parkes desilvering: zinc takes up the silver and floats as a crust")
            .beginStructureBlock(7, 6, 7, false)
            .addController("Front of the fire pit, bottom layer")
            .addCasingInfoMin("Bronze Plated Bricks or Solid Steel Machine Casing", 30, false)
            .addOtherStructurePart("Refining Kettle", "The bottom of the bowl")
            .addOtherStructurePart("Bronze or Steel Firebox Casing", "9, the fire pit")
            .addOtherStructurePart("Bronze or Steel Gear Box Casing", "Stirrer drive at the top of the arch")
            .addOtherStructurePart("Bronze or Steel Frame Box", "The arch and the stirrer shaft")
            .addStructureInfo("The inside of the bowl must be empty apart from the stirrer")
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
        return MoonStructure.ALLOY_SMELTER;
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return MoonRecipeMaps.refiningKettleRecipes;
    }
}
