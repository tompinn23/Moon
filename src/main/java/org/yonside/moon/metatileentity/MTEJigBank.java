package org.yonside.moon.metatileentity;

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

/**
 * A staircase of pulsed-water jigs: water and pay gravel enter at the top step and cascade down, the heavy grains
 * caught on each bed. The Jig Cell tier picks the recipe.
 */
public class MTEJigBank extends MTEFixedOreMultiBase<MTEJigBank> {

    // Layers top to bottom, rows front to back
    private static final String[][] STRUCTURE = new String[][] {
        { "     ", "     ", "     ", "     ", "BCCCB" },
        { "     ", "     ", "     ", "CJJJC", "CCCCC" },
        { "     ", "     ", "CJJJC", "CCCCC", "CCCCC" },
        { "     ", "CJJJC", "CCCCC", "CCCCC", "CCCCC" },
        { "CC~CC", "CCCCC", "CCCCC", "CCCCC", "CCCCC" }, };

    private static final IStructureDefinition<MTEJigBank> STRUCTURE_DEFINITION = StructureDefinition
        .<MTEJigBank>builder()
        .addShape(STRUCTURE_PIECE_MAIN, transpose(STRUCTURE))
        .addElement('C', buildCasing(MTEJigBank.class, 1, FOUR_TIERS))
        .addElement('B', tieredFrame(FOUR_TIER_FRAMES))
        .addElement('J', MoonStructure.tieredPart(Part.JIG_CELLS, (t, tier) -> t.jigTier = tier, t -> t.jigTier))
        .build();

    private int jigTier;

    public MTEJigBank(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional, 2, 4, 0);
    }

    public MTEJigBank(String aName) {
        super(aName, 2, 4, 0);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTEJigBank(mName);
    }

    @Override
    public IStructureDefinition<MTEJigBank> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    @Override
    protected boolean checkStructure(List<StructureError> errors) {
        jigTier = 0;
        return super.checkStructure(errors);
    }

    @Override
    protected Casings[] getCasingTiers() {
        return FOUR_TIERS;
    }

    @Override
    protected void validateStructure(List<StructureError> errors) {
        if (jigTier != casingTier) {
            errors.add(StructureErrors.of("The Jig Cells must be the same tier as the casing"));
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
        return 40;
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
        aNBT.setInteger("jigTier", jigTier);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        jigTier = aNBT.getInteger("jigTier");
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType("Jig Bank");
        tt.addInfo("Concentrates Pay Gravel by density with pulsed water")
            .addInfo("Three steps of jigs; water cascades from the top step down");
        addTierInfo(tt, "Jig Cells").addInfo("Higher tiers recover more Jig Concentrate per batch")
            .addInfo("Parallels: 4 bronze, 8 for the EU tiers")
            .beginStructureBlock(5, 5, 5, false)
            .addController("Front center, bottom layer")
            .addCasingInfoMin("Tier casing", 40, false)
            .addOtherStructurePart("Tier Jig Cell", "9, three per step")
            .addOtherStructurePart("Tier Frame Box", "Ends of the water header above the top step")
            .addInputBus("Any casing", 1)
            .addInputHatch("Water, any casing", 1)
            .addOutputBus("Any casing", 1)
            .addOutputHatch("Return water, any casing", 1)
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
        return MoonRecipeMaps.jigBankRecipes;
    }
}
