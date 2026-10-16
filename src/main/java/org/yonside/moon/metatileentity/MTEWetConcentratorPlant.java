package org.yonside.moon.metatileentity;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.isAir;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofChain;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;
import static gregtech.api.util.GTStructureUtility.ofAnyWater;

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

/**
 * A helical gravity spiral around a falling water column. Heavy minerals move to the inside of the spiral and light
 * quartz to the outside. The Spiral Concentrator tier picks the recipe: better spirals recover more heavy mineral.
 */
public class MTEWetConcentratorPlant extends MTEFixedOreMultiBase<MTEWetConcentratorPlant> {

    private static final String[][] STRUCTURE = new String[][] {
        { "EEEEE", "EEEEE", "EEFEE", "EEEEE", "EEEEE" },
        { "B   B", " AAA ", " ADA ", " AAA ", "B   B" },
        { "B   B", "     ", " ADA ", "     ", "B   B" },
        { "B   B", "   A ", "  D  ", " A   ", "B   B" },
        { "B   B", "  A  ", "  D  ", "  A  ", "B   B" },
        { "B   B", " A   ", "  D  ", "   A ", "B   B" },
        { "B   B", "     ", " ADA ", "     ", "B   B" },
        { "B   B", "   A ", "  D  ", " A   ", "B   B" },
        { "B   B", "  A  ", "  D  ", "  A  ", "B   B" },
        { "B   B", " A   ", "  D  ", "   A ", "B   B" },
        { "B   B", "     ", " ADA ", "     ", "B   B" },
        { "B   B", " AAA ", " ADA ", " AAA ", "B   B" },
        { "CC~CC", "CCCCC", "CCCCC", "CCCCC", "CCCCC" },
    };

    private static final IStructureDefinition<MTEWetConcentratorPlant> STRUCTURE_DEFINITION = StructureDefinition
        .<MTEWetConcentratorPlant>builder()
        .addShape(STRUCTURE_PIECE_MAIN, transpose(STRUCTURE))
        .addElement('A', MoonStructure.tieredPart(Part.SPIRALS, (t, tier) -> t.spiralTier = tier, t -> t.spiralTier))
        .addElement('B', tieredFrame(FOUR_TIER_FRAMES))
        // Output side: bottom layer with the controller
        .addElement('C', buildCasing(MTEWetConcentratorPlant.class, 1, FOUR_TIERS))
        // Feed side: the top layer
        .addElement('E', buildCasing(MTEWetConcentratorPlant.class, 2, FOUR_TIERS))
        .addElement('F', buildCasing(MTEWetConcentratorPlant.class, 2, FOUR_TIERS))
        .addElement('D', ofChain(ofAnyWater(true), isAir()))
        .build();

    private int spiralTier;

    public MTEWetConcentratorPlant(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional, 2, 12, 0);
    }

    public MTEWetConcentratorPlant(String aName) {
        super(aName, 2, 12, 0);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTEWetConcentratorPlant(mName);
    }

    @Override
    public IStructureDefinition<MTEWetConcentratorPlant> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    @Override
    protected boolean checkStructure(List<StructureError> errors) {
        spiralTier = 0;
        return super.checkStructure(errors);
    }

    @Override
    protected Casings[] getCasingTiers() {
        return FOUR_TIERS;
    }

    @Override
    protected void validateStructure(List<StructureError> errors) {
        if (spiralTier != casingTier) {
            errors.add(StructureErrors.of("The Spiral Concentrators must be the same tier as the casing"));
        }
        checkHasInputBus(errors);
        checkHasInputHatch(errors);
        checkHasOutputBus(errors);
        checkHasOutputHatch(errors);
    }

    @Override
    protected int getRecipeTier() {
        return casingTier;
    }

    @Override
    protected int getBaseParallel() {
        return build == Build.STEEL ? 8 : 4;
    }

    @Override
    protected int getMinCasings() {
        return 36;
    }

    @Override
    public String[] getInfoData() {
        String[] base = super.getInfoData();
        String[] info = new String[base.length + 1];
        System.arraycopy(base, 0, info, 0, base.length);
        info[base.length] = "Tier: " + FOUR_TIER_NAMES[Math.max(0, casingTier)];
        return info;
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setInteger("spiralTier", spiralTier);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        spiralTier = aNBT.getInteger("spiralTier");
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType("Wet Concentrator Plant");
        tt.addInfo("Gravity spirals split heavy minerals from light quartz sand");
        addTierInfo(tt, "Spiral Concentrators").addInfo("Higher tiers recover more heavy mineral per batch")
            .addInfo("Titanium tier also runs the EV tailings fines recipe")
            .addInfo("Parallels: 4 bronze, 8 for the EU tiers")
            .beginStructureBlock(5, 13, 5, false)
            .addController("Front center, bottom layer")
            .addCasingInfoMin("Tier casing", 36, false)
            .addOtherStructurePart("Tier Spiral Concentrator", "The helix")
            .addOtherStructurePart("Tier Frame Box", "The four corner columns")
            .addOtherStructurePart("Water or air", "The center column")
            .addInputBus("Top layer", 2)
            .addInputHatch("Water, top layer", 2)
            .addOutputBus("Bottom layer", 1)
            .addOutputHatch("Return water, bottom layer", 1)
            .addEnergyHatch("EU tiers, any casing", 1)
            .addMaintenanceHatch("EU tiers, any casing", 1)
            .addOtherStructurePart("Steam Hatch", "Bronze tier, any casing", 1)
            .toolTipFinisher("Moon");
        return tt;
    }

    @Override
    protected IIconContainer[] getFrontOverlays() {
        return MoonStructure.WASHER;
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return MoonRecipeMaps.wetConcentratorRecipes;
    }
}
