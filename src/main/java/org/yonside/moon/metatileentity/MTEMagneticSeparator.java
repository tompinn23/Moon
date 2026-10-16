package org.yonside.moon.metatileentity;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.isAir;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;

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

/** Drum magnets around a falling stream of sand. The magnet tier picks the recipe. */
public class MTEMagneticSeparator extends MTEFixedOreMultiBase<MTEMagneticSeparator> {

    // Layers top to bottom, rows front to back. One big drum magnet: a round shell turning on a geared axle, with the
    // stationary magnet arc inside under the feed chute
    private static final String[][] STRUCTURE = new String[][] {
        { "       ", " CC    ", "       " },
        { "  CCC  ", "  CCC  ", "  CCC  " },
        { " CCCCC ", " CMMMC ", " CCCCC " },
        { "CCCCCCC", "C----MC", "CCCCCCC" },
        { "CCC~CCC", "C-----C", "CCCGCCC" },
        { "CCCCCCC", "C-----C", "CCCCCCC" },
        { " CCCCC ", " C---C ", " CCCCC " },
        { "  CCC  ", "  CCC  ", "  CCC  " },
        { "  B B  ", "       ", "  B B  " }, };

    private static final IStructureDefinition<MTEMagneticSeparator> STRUCTURE_DEFINITION = StructureDefinition
        .<MTEMagneticSeparator>builder()
        .addShape(STRUCTURE_PIECE_MAIN, transpose(STRUCTURE))
        .addElement('C', buildCasing(MTEMagneticSeparator.class, 1))
        .addElement(
            'M',
            MoonStructure.tieredPart(
                new Part[] { Part.DrumMagnetIron, Part.DrumMagnetSteel, Part.DrumMagnetNeodymium,
                    Part.DrumMagnetSamarium },
                (t, tier) -> t.magnetTier = tier,
                t -> t.magnetTier))
        .addElement(
            'G',
            buildPart(
                Casings.BronzeGearBoxCasing,
                Casings.SteelGearBoxCasing,
                (t, tier) -> t.axleTier = tier,
                t -> t.axleTier))
        .addElement('B', buildFrame())
        .addElement('-', isAir())
        .build();

    private int magnetTier;
    private int axleTier = -1;

    public MTEMagneticSeparator(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional, 3, 4, 0);
    }

    public MTEMagneticSeparator(String aName) {
        super(aName, 3, 4, 0);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTEMagneticSeparator(mName);
    }

    @Override
    public IStructureDefinition<MTEMagneticSeparator> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    @Override
    protected boolean checkStructure(List<StructureError> errors) {
        magnetTier = 0;
        axleTier = -1;
        return super.checkStructure(errors);
    }

    @Override
    protected void validateStructure(List<StructureError> errors) {
        if (axleTier != build.ordinal()) {
            errors.add(StructureErrors.of("The axle must match the build: Bronze or Steel Gear Box Casing"));
        }
        checkHasInputBus(errors);
        checkHasOutputBus(errors);
    }

    @Override
    protected int getRecipeTier() {
        return magnetTier;
    }

    @Override
    protected int getMinCasings() {
        return 60;
    }

    @Override
    public String[] getInfoData() {
        String[] base = super.getInfoData();
        String[] info = new String[base.length + 1];
        System.arraycopy(base, 0, info, 0, base.length);
        info[base.length] = "Drum Magnets: " + MoonRecipeMaps.MAGNET_NAMES[magnetTier];
        return info;
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setInteger("magnetTier", magnetTier);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        magnetTier = aNBT.getInteger("magnetTier");
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType("Magnetic Separator");
        addBuildInfo(tt).addInfo("Pulls magnetic grains out of dry concentrate")
            .addInfo("The Drum Magnet tier picks the recipe: Iron, Steel, Neodymium, Samarium")
            .addInfo("Zircon-Monazite Concentrate needs Neodymium magnets or better")
            .addInfo("Unsorted material comes back as the input item")
            .beginStructureBlock(7, 9, 3, false)
            .addController("Center of the front face of the drum")
            .addCasingInfoMin("Bronze Plated Bricks or Solid Steel Machine Casing", 60, false)
            .addOtherStructurePart("Drum Magnet (all one tier)", "4, an arc inside the top of the drum")
            .addOtherStructurePart("Bronze or Steel Gear Box Casing", "The axle, center of the back face")
            .addOtherStructurePart("Bronze or Steel Frame Box", "4 legs")
            .addStructureInfo("The rest of the inside of the drum must be empty")
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
        return MoonRecipeMaps.magneticSeparatorRecipes;
    }
}
