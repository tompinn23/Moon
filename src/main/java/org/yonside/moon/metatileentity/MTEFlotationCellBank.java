package org.yonside.moon.metatileentity;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.*;
import static gregtech.api.enums.HatchElement.*;
import static gregtech.api.util.GTStructureUtility.*;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.yonside.moon.block.BlockMachineParts.Part;
import org.yonside.moon.metatileentity.base.MTEOreMultiBase;
import org.yonside.moon.metatileentity.base.MoonStructure;
import org.yonside.moon.recipes.MoonRecipeMaps;

import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;

import bartworks.API.BorosilicateGlass;
import gregtech.api.casing.Casings;
import gregtech.api.enums.Materials;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchEnergy;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.structure.error.StructureError;
import gregtech.api.structure.error.StructureErrors;
import gregtech.api.util.MultiblockTooltipBuilder;

/**
 * A glass-walled flotation tank with an impeller column down its middle. The impeller tier picks the recipe: better
 * impellers float more of the sulfides. Built to take later scheelite and PGM recipes.
 */
public class MTEFlotationCellBank extends MTEOreMultiBase<MTEFlotationCellBank> {

    private static final String STRUCTURE_PIECE_MAIN = "main";
    private static final int OFFSET_X = 2, OFFSET_Y = 3, OFFSET_Z = 0;
    private static final int MIN_CASINGS = 50;

    private static final String[][] STRUCTURE = new String[][] {
        // slice 0 -- front face, controller
        { "BCCCB", "BGGGB", "BGGGB", "BC~CB", "BCCCB", "BCCCB" },
        // slice 1
        { "CCCCC", "GDDDG", "GDDDG", "CDDDC", "CDDDC", "CCCCC" },
        // slice 2 -- centre, impeller column
        { "CCCCC", "GDADG", "GDADG", "CDADC", "CDADC", "CCCCC" },
        // slice 3
        { "CCCCC", "GDDDG", "GDDDG", "CDDDC", "CDDDC", "CCCCC" },
        // slice 4 -- back face
        { "BCCCB", "BGGGB", "BGGGB", "BCCCB", "BCCCB", "BCCCB" }, };

    private static final IStructureDefinition<MTEFlotationCellBank> STRUCTURE_DEFINITION = StructureDefinition
        .<MTEFlotationCellBank>builder()
        .addShape(STRUCTURE_PIECE_MAIN, STRUCTURE)
        .addElement(
            'A',
            MoonStructure.tieredPart(Part.IMPELLERS, (t, tier) -> t.impellerTier = tier, t -> t.impellerTier))
        .addElement('B', ofFrame(Materials.Steel))
        .addElement(
            'C',
            buildHatchAdder(MTEFlotationCellBank.class)
                .atLeast(InputBus, InputHatch, OutputBus, OutputHatch, Maintenance, Energy, Muffler)
                .casingIndex(Casings.WashPlantCasing.textureId)
                .hint(1)
                .buildAndChain(onElementPass(x -> ++x.casingCount, Casings.WashPlantCasing.asElement())))
        // Glass tier gates the energy hatch tier
        .addElement(
            'G',
            BorosilicateGlass.ofBoroGlass((byte) 0, (te, tier) -> te.glassTier = tier, te -> (byte) te.glassTier))
        // Pulp volume: filled with water, or left empty before the player floods it.
        .addElement('D', ofChain(ofAnyWater(false), isAir()))
        .build();

    private int glassTier;
    private int impellerTier;

    public MTEFlotationCellBank(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public MTEFlotationCellBank(String aName) {
        super(aName);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTEFlotationCellBank(mName);
    }

    @Override
    public IStructureDefinition<MTEFlotationCellBank> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    @Override
    protected Build getFixedBuild() {
        return Build.STEEL;
    }

    @Override
    protected Casings getBuildCasing() {
        return Casings.WashPlantCasing;
    }

    @Override
    public void construct(ItemStack trigger, boolean hintsOnly) {
        buildPiece(STRUCTURE_PIECE_MAIN, trigger, hintsOnly, OFFSET_X, OFFSET_Y, OFFSET_Z);
    }

    @Override
    public int survivalConstruct(ItemStack trigger, int elementBudget, ISurvivalBuildEnvironment env) {
        if (mMachine) return -1;
        return survivalBuildPiece(
            STRUCTURE_PIECE_MAIN,
            trigger,
            OFFSET_X,
            OFFSET_Y,
            OFFSET_Z,
            elementBudget,
            env,
            false,
            true);
    }

    @Override
    protected boolean checkStructure(List<StructureError> errors) {
        glassTier = 0;
        impellerTier = 0;
        return checkPiece(STRUCTURE_PIECE_MAIN, OFFSET_X, OFFSET_Y, OFFSET_Z, errors);
    }

    @Override
    protected void validateStructure(List<StructureError> errors) {
        checkHasMufflerHatch(errors);
        checkHasInputBus(errors);
        checkHasInputHatch(errors);
        checkHasOutputBus(errors);

        int needed = 0;
        for (MTEHatchEnergy hatch : mEnergyHatches) {
            needed = Math.max(needed, hatch.mTier);
        }
        if (glassTier < needed) {
            errors.add(StructureErrors.glassTierNotEnough(needed));
        }
    }

    @Override
    protected int getMinCasings() {
        return MIN_CASINGS;
    }

    @Override
    protected int getRecipeTier() {
        return impellerTier;
    }

    @Override
    protected int getBaseParallel() {
        // Bigger cell banks process more pulp
        return 1 + (casingCount / 20);
    }

    @Override
    public int getPollutionPerSecond(ItemStack aStack) {
        // Flotation reagents are nasty. Charge for it.
        return 20;
    }

    @Override
    public boolean isRotationChangeAllowed() {
        return false;
    }

    @Override
    public String[] getInfoData() {
        String[] base = super.getInfoData();
        String[] info = new String[base.length + 1];
        System.arraycopy(base, 0, info, 0, base.length);
        info[base.length] = "Impeller tier: " + MoonRecipeMaps.IMPELLER_NAMES[impellerTier];
        return info;
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setInteger("impellerTier", impellerTier);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        impellerTier = aNBT.getInteger("impellerTier");
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType("Flotation Cell")
            .addInfo("Separates minerals by surface chemistry, not density.")
            .addInfo("Collector reagent and pH decide which mineral reports to froth.")
            .addInfo("Rejected material leaves as tailings, not as nothing.")
            .addInfo("The impeller tier sets recovery: Steel, Aluminium, Stainless Steel")
            .addInfo("Stainless impellers and an MV hatch run differential flotation")
            .addPollutionAmount(getPollutionPerSecond(null))
            .beginStructureBlock(5, 6, 5, true)
            .addController("Front centre, third layer from the top")
            .addCasingInfoMin("Wash Plant Casing", MIN_CASINGS, false)
            .addOtherStructurePart("Flotation Impeller (all one tier)", "Centre column")
            .addOtherStructurePart("Borosilicate Glass", "Upper walls; tier must cover the energy hatch")
            .addOtherStructurePart("Steel Frame Box", "Corners of the front and back faces")
            .addInputHatch("Water, collector, frother", 1)
            .addInputBus("Ground ore, depressant", 1)
            .addOutputBus("Concentrate and tailings", 1)
            .addOutputHatch("Recycled process water", 1)
            .addEnergyHatch("Any casing", 1)
            .addMaintenanceHatch("Any casing", 1)
            .addMufflerHatch("Any casing", 1)
            .toolTipFinisher("Moon");
        return tt;
    }

    @Override
    protected IIconContainer[] getFrontOverlays() {
        return MoonStructure.SMELTER;
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return MoonRecipeMaps.flotationRecipes;
    }
}
