package org.yonside.moon.metatileentity;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.*;
import static gregtech.api.enums.HatchElement.*;
import static gregtech.api.util.GTStructureUtility.*;

import bartworks.API.BorosilicateGlass;
import gregtech.api.casing.Casings;
import gregtech.api.enums.Materials;
import gregtech.api.metatileentity.implementations.MTEHatchEnergy;
import gregtech.api.structure.error.StructureError;
import gregtech.api.structure.error.StructureErrors;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import org.yonside.moon.recipes.MoonRecipes;

import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;

import gregtech.api.GregTechAPI;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.metatileentity.implementations.MTEEnhancedMultiBlockBase;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.MultiblockTooltipBuilder;

import java.util.List;

public class MTEFlotationCell extends MTEEnhancedMultiBlockBase<MTEFlotationCell> implements ISurvivalConstructable {

    private static final String STRUCTURE_MAIN = "main";

    private int casingCount;
    private int glassTier;

    public MTEFlotationCell(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public MTEFlotationCell(String aName) {
        super(aName);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTEFlotationCell(this.mName);
    }

    private static final String STRUCTURE_PIECE_MAIN = "main";

    private static final String[][] structure = new String[][] {
        // slice 0 -- front face, controller
        { "BCCCB", "BGGGB", "BGGGB", "BC~CB", "BCCCB", "BCCCB" },
        // slice 1
        { "CCCCC", "GDDDG", "GDDDG", "CDDDC", "CDDDC", "CCCCC" },
        // slice 2 -- centre, impeller column
        { "CCCCC", "GDADG", "GDADG", "CDADC", "CDADC", "CCCCC" },
        // slice 3
        { "CCCCC", "GDDDG", "GDDDG", "CDDDC", "CDDDC", "CCCCC" },
        // slice 4 -- back face
        { "BCCCB", "BGGGB", "BGGGB", "BCCCB", "BCCCB", "BCCCB" },
    };

    private static final IStructureDefinition<MTEFlotationCell> STRUCTURE_DEFINITION = StructureDefinition
        .<MTEFlotationCell>builder()
        .addShape(STRUCTURE_PIECE_MAIN, structure)
        .addElement('A', Casings.SteelGearBoxCasing.asElement())
        .addElement('B', ofFrame(Materials.Steel))
        .addElement(
            'C',
            buildHatchAdder(MTEFlotationCell.class)
                .atLeast(InputBus, InputHatch, OutputBus, OutputHatch, Maintenance, Energy, Muffler)
                .casingIndex(Casings.WashPlantCasing.textureId)
                .hint(1)
                .buildAndChain(onElementPass(x -> ++x.casingCount, Casings.WashPlantCasing.asElement())))
        // Glass tier gates how hard a separation the cell can run. Verify this
        // signature against your GT5U version -- BorosilicateGlass has several
        // overloads and they've changed.
        .addElement(
            'G',
            BorosilicateGlass.ofBoroGlass(
                (byte) 0,
                (te, tier) -> te.glassTier = tier,
                te -> (byte) te.glassTier))
        // Pulp volume: filled with water, or left empty before the player floods it.
        .addElement('D', ofChain(ofAnyWater(false), isAir()))
        .build();

    @Override
    public IStructureDefinition<MTEFlotationCell> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }



    @Override
    public void construct(ItemStack trigger, boolean hintsOnly) {
        buildPiece(STRUCTURE_PIECE_MAIN, trigger, hintsOnly, 2, 3, 0);
    }

    @Override
    public int survivalConstruct(ItemStack trigger, int elementBudget, ISurvivalBuildEnvironment env) {
        if (mMachine) return -1;
        return survivalBuildPiece(STRUCTURE_PIECE_MAIN, trigger, 2, 3, 0, elementBudget, env, false, true);
    }

    private static final int OFFSET_X = 2;
    private static final int OFFSET_Y = 3;
    private static final int OFFSET_Z = 0;

    private static final int MIN_CASINGS = 50;

    @Override
    public void checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack,
                             List<StructureError> errors) {
        casingCount = 0;
        glassTier = 0;

        if (!checkPiece(STRUCTURE_PIECE_MAIN, OFFSET_X, OFFSET_Y, OFFSET_Z, errors)) return;

        checkCasingMin(errors, casingCount, MIN_CASINGS);
        checkHasEnergyHatch(errors);
        checkHasMaintenanceHatch(errors);
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

    // -------------------------------------------------------------- recipes

    @Override
    public RecipeMap<?> getRecipeMap() {
        return MoonRecipes.floatationCellRecipes;
    }

    @Override
    protected ProcessingLogic createProcessingLogic() {
        return new ProcessingLogic().setMaxParallelSupplier(this::getMaxParallelRecipes);
    }

    public int getMaxParallelRecipes() {
        // Bigger cell banks process more pulp. Tie this to whatever your
        // structure scales on -- casing count, glass tier, a length parameter.
        return 1 + (casingCount / 20);
    }

    @Override
    public boolean supportsVoidProtection() {
        return true;
    }

    @Override
    public boolean supportsBatchMode() {
        return true;
    }

    // ------------------------------------------------------- machine params

    @Override
    public int getMaxEfficiency(ItemStack aStack) {
        return 10000;
    }

    @Override
    public int getDamageToComponent(ItemStack aStack) {
        return 0;
    }

    @Override
    public boolean isCorrectMachinePart(ItemStack aStack) {
        return true;
    }

    @Override
    public int getPollutionPerSecond(ItemStack aStack) {
        // Flotation reagents are nasty. Charge for it.
        return 20;
    }

    // -------------------------------------------------------------- display

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType("Flotation Cell")
            .addInfo("Separates minerals by surface chemistry, not density.")
            .addInfo("Collector reagent and pH decide which mineral reports to froth.")
            .addInfo("Rejected material leaves as tailings, not as nothing.")
            .addPollutionAmount(getPollutionPerSecond(null))
            .beginStructureBlock(3, 5, 3, true)
            .addController("Front centre, second layer")
            .addCasingInfoMin("Flotation Casing", 20, false)
            .addInputHatch("Water, collector, frother", 1)
            .addInputBus("Ground ore, depressant", 1)
            .addOutputBus("Concentrate and tailings", 1)
            .addOutputHatch("Recycled process water", 1)
            .addEnergyHatch("Any casing", 1)
            .addMaintenanceHatch("Any casing", 1)
            .addMufflerHatch("Any casing", 1)
            .toolTipFinisher("YourMod");
        return tt;
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity baseMetaTileEntity, ForgeDirection side, ForgeDirection facing,
        int colorIndex, boolean active, boolean redstoneLevel) {

        ITexture casing = Textures.BlockIcons.getCasingTextureForId(Casings.WashPlantCasing.getTextureId());

        if (side != facing) {
            return new ITexture[] { casing };
        }

        if (active) {
            return new ITexture[] { casing, TextureFactory.builder()
                .addIcon(Textures.BlockIcons.OVERLAY_FRONT_MULTI_SMELTER_ACTIVE)
                .extFacing()
                .build(),
                TextureFactory.builder()
                    .addIcon(Textures.BlockIcons.OVERLAY_FRONT_MULTI_SMELTER_ACTIVE_GLOW)
                    .extFacing()
                    .glow()
                    .build(), };
        }

        return new ITexture[] { casing, TextureFactory.builder()
            .addIcon(Textures.BlockIcons.OVERLAY_FRONT_MULTI_SMELTER)
            .extFacing()
            .build(),
            TextureFactory.builder()
                .addIcon(Textures.BlockIcons.OVERLAY_FRONT_MULTI_SMELTER_GLOW)
                .extFacing()
                .glow()
                .build(), };
    }

    @Override
    public boolean isRotationChangeAllowed() {
        return false;
    }

    // ----------------------------------------------------------------- nbt

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setInteger("casingCount", casingCount);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        casingCount = aNBT.getInteger("casingCount");
    }
}
