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

/** A rotating screen drum that splits gravel by size so only the pay fraction goes on to the jigs. */
public class MTETrommel extends MTEFixedOreMultiBase<MTETrommel> {

    // Layers top to bottom, rows front to back. A round screen drum stepping down to an open discharge end at the
    // front, closed at the feed end, turning on two riding rings
    private static final String[][] STRUCTURE = new String[][] {
        { "     ", "     ", "     ", "     ", "     ", "     ", " CCC " },
        { "     ", "     ", "     ", " SSS ", " SSS ", " GGG ", "CCCCC" },
        { " CCC ", " GGG ", " SSS ", "S---S", "S---S", "G---G", "CCCCC" },
        { "C---C", "G---G", "S---S", "S---S", "S---S", "G---G", "CCCCC" },
        { "C---C", "G---G", "S---S", "S---S", "S---S", "G---G", " CCC " },
        { "C---C", "G---G", "S---S", " SSS ", " SSS ", " GGG ", " B B " },
        { " C~C ", " GGG ", " SSS ", "     ", "     ", " B B ", " B B " },
        { " B B ", " B B ", "     ", "     ", "     ", " B B ", " B B " }, };

    private static final IStructureDefinition<MTETrommel> STRUCTURE_DEFINITION = StructureDefinition
        .<MTETrommel>builder()
        .addShape(STRUCTURE_PIECE_MAIN, transpose(STRUCTURE))
        .addElement('C', buildCasing(MTETrommel.class, 1))
        .addElement('S', MoonStructure.part(Part.TrommelScreen))
        .addElement(
            'G',
            buildPart(
                Casings.BronzeGearBoxCasing,
                Casings.SteelGearBoxCasing,
                (t, tier) -> t.driveTier = tier,
                t -> t.driveTier))
        .addElement('B', buildFrame())
        .addElement('-', isAir())
        .build();

    private int driveTier = -1;

    public MTETrommel(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional, 2, 6, 0);
    }

    public MTETrommel(String aName) {
        super(aName, 2, 6, 0);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTETrommel(mName);
    }

    @Override
    public IStructureDefinition<MTETrommel> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    @Override
    protected boolean checkStructure(List<StructureError> errors) {
        driveTier = -1;
        return super.checkStructure(errors);
    }

    @Override
    protected void validateStructure(List<StructureError> errors) {
        if (driveTier != build.ordinal()) {
            errors.add(StructureErrors.of("The drive rings must match the build: Bronze or Steel Gear Box Casing"));
        }
        checkHasInputBus(errors);
        checkHasInputHatch(errors);
        checkHasOutputBus(errors);
    }

    @Override
    protected int getMinCasings() {
        return 24;
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType("Trommel");
        addBuildInfo(tt).addInfo("Screens gravel into Pay Gravel, sand and cobblestone oversize")
            .beginStructureBlock(5, 8, 7, true)
            .addController("Bottom of the open front (discharge) ring")
            .addCasingInfoMin("Bronze Plated Bricks or Solid Steel Machine Casing", 24, false)
            .addOtherStructurePart("Trommel Screen", "36, three rings of the drum")
            .addOtherStructurePart("Bronze or Steel Gear Box Casing", "2 riding rings")
            .addOtherStructurePart("Bronze or Steel Frame Box", "Legs")
            .addStructureInfo("The drum steps down one block every three slices towards the front")
            .addInputBus("Any casing", 1)
            .addInputHatch("Water, any casing", 1)
            .addOutputBus("Any casing", 1)
            .addEnergyHatch("Steel build, any casing", 1)
            .addMaintenanceHatch("Steel build, any casing", 1)
            .addOtherStructurePart("Steam Hatch", "Bronze build, any casing", 1)
            .toolTipFinisher("Moon");
        return tt;
    }

    @Override
    protected IIconContainer[] getFrontOverlays() {
        return MoonStructure.MACERATOR;
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return MoonRecipeMaps.trommelRecipes;
    }
}
