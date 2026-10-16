package org.yonside.moon.metatileentity;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.isAir;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofChain;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.onElementPass;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;
import static gregtech.api.util.GTStructureUtility.ofCoil;

import java.util.List;
import java.util.function.BiConsumer;

import org.yonside.moon.metatileentity.base.MTEFixedOreMultiBase;
import org.yonside.moon.metatileentity.base.MoonStructure;
import org.yonside.moon.recipes.MoonRecipeMaps;

import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;

import gregtech.api.casing.Casings;
import gregtech.api.enums.HeatingCoilLevel;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.structure.error.StructureError;
import gregtech.api.structure.error.StructureErrors;
import gregtech.api.util.MultiblockTooltipBuilder;

/** A heated horizontal drum that dries wet concentrates; electrostatic separation fails on damp sand. */
public class MTERotaryDryer extends MTEFixedOreMultiBase<MTERotaryDryer> {

    private static final int HEATERS = 36;

    // Layers top to bottom, rows front to back. A round drum stepping down towards the discharge end at the front:
    // closed end caps, riding rings (gear boxes) on legs, and three heated slices in the middle
    private static final String[][] STRUCTURE = new String[][] {
        { "     ", "     ", "     ", "     ", "     ", "     ", " GGG ", " CCC ", " CCC " },
        { "     ", "     ", "     ", " HHH ", " HHH ", " HHH ", "G---G", "C---C", "CCCCC" },
        { " CCC ", " CCC ", " GGG ", "H---H", "H---H", "H---H", "G---G", "C---C", "CCCCC" },
        { "CCCCC", "C---C", "G---G", "H---H", "H---H", "H---H", "G---G", "C---C", "CCCCC" },
        { "CC~CC", "C---C", "G---G", "H---H", "H---H", "H---H", " GGG ", " CCC ", " CCC " },
        { "CCCCC", "C---C", "G---G", " HHH ", " HHH ", " HHH ", " B B ", "     ", " B B " },
        { " CCC ", " CCC ", " GGG ", "     ", "     ", "     ", " B B ", "     ", " B B " },
        { " B B ", "     ", " B B ", "     ", "     ", "     ", " B B ", "     ", " B B " }, };

    private static final IStructureDefinition<MTERotaryDryer> STRUCTURE_DEFINITION = StructureDefinition
        .<MTERotaryDryer>builder()
        .addShape(STRUCTURE_PIECE_MAIN, transpose(STRUCTURE))
        .addElement('C', buildCasing(MTERotaryDryer.class, 1))
        .addElement(
            'H',
            ofChain(
                onElementPass(t -> t.pipeHeaters++, Casings.BronzePipeCasing.asElement()),
                onElementPass(
                    t -> t.coilHeaters++,
                    ofCoil(
                        (BiConsumer<MTERotaryDryer, HeatingCoilLevel>) (t, level) -> t.coilLevel = level,
                        t -> t.coilLevel))))
        .addElement(
            'G',
            buildPart(
                Casings.BronzeGearBoxCasing,
                Casings.SteelGearBoxCasing,
                (t, tier) -> t.ringTier = tier,
                t -> t.ringTier))
        .addElement('B', buildFrame())
        .addElement('-', isAir())
        .build();

    private int pipeHeaters, coilHeaters;
    private int ringTier = -1;
    private HeatingCoilLevel coilLevel;

    public MTERotaryDryer(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional, 2, 4, 0);
    }

    public MTERotaryDryer(String aName) {
        super(aName, 2, 4, 0);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTERotaryDryer(mName);
    }

    @Override
    public IStructureDefinition<MTERotaryDryer> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    @Override
    protected boolean checkStructure(List<StructureError> errors) {
        pipeHeaters = 0;
        coilHeaters = 0;
        coilLevel = null;
        ringTier = -1;
        return super.checkStructure(errors);
    }

    @Override
    protected void validateStructure(List<StructureError> errors) {
        if (build == Build.BRONZE && pipeHeaters != HEATERS) {
            errors.add(StructureErrors.of("Bronze builds heat the drum with 36 Bronze Pipe Casings"));
        }
        if (build == Build.STEEL && (coilHeaters != HEATERS || coilLevel == null
            || coilLevel.getTier() < HeatingCoilLevel.LV.getTier())) {
            errors.add(StructureErrors.of("Steel builds heat the drum with 36 Cupronickel or better coils"));
        }
        if (ringTier != build.ordinal()) {
            errors.add(StructureErrors.of("The riding rings must match the build: Bronze or Steel Gear Box Casing"));
        }
        checkHasInputBus(errors);
        checkHasOutputBus(errors);
    }

    @Override
    protected int getMinCasings() {
        return 40;
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType("Rotary Dryer");
        addBuildInfo(tt).addInfo("Dries wet heavy mineral and flotation concentrates")
            .addInfo("Higher coils give no bonus")
            .beginStructureBlock(5, 8, 9, true)
            .addController("Center of the front (discharge) end cap")
            .addCasingInfoMin("Bronze Plated Bricks or Solid Steel Machine Casing", 40, false)
            .addOtherStructurePart("Bronze Pipe Casing (bronze) or Cupronickel+ coil (steel)", "36, three heated slices")
            .addOtherStructurePart("Bronze or Steel Gear Box Casing", "2 riding rings")
            .addOtherStructurePart("Bronze or Steel Frame Box", "Legs under the end caps and riding rings")
            .addStructureInfo("The drum steps down one block every three slices towards the front")
            .addInputBus("Feed hopper above the back cap, or the end caps", 1)
            .addOutputBus("End caps", 1)
            .addEnergyHatch("Steel build, end caps", 1)
            .addMaintenanceHatch("Steel build, end caps", 1)
            .addOtherStructurePart("Steam Hatch", "Bronze build, end caps", 1)
            .toolTipFinisher("Moon");
        return tt;
    }

    @Override
    protected IIconContainer[] getFrontOverlays() {
        return MoonStructure.FURNACE;
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return MoonRecipeMaps.rotaryDryerRecipes;
    }
}
