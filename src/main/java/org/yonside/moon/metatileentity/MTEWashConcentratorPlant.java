package org.yonside.moon.metatileentity;

import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import gregtech.api.casing.Casings;
import gregtech.api.enums.Materials;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEEnhancedMultiBlockBase;
import gregtech.api.metatileentity.implementations.MTEHatchEnergy;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.render.TextureFactory;
import gregtech.api.structure.error.StructureError;
import gregtech.api.util.MultiblockTooltipBuilder;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import org.jetbrains.annotations.NotNull;
import org.yonside.moon.recipes.MoonRecipes;

import java.util.Collection;
import java.util.List;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.*;
import static gregtech.api.enums.HatchElement.*;
import static gregtech.api.util.GTStructureUtility.*;

public class MTEWashConcentratorPlant extends MTEEnhancedMultiBlockBase<MTEWashConcentratorPlant> implements ISurvivalConstructable {

    private static final String STRUCTURE_PIECE_MAIN = "main";
    private int casingCount;

    private static final String[][] STRUCTURE = new String[][]{
        {"EEEEE","EEEEE","EEFEE","EEEEE","EEEEE"},
        {"B   B"," AAA "," ADA "," AAA ","B   B"},
        {"B   B","     "," ADA ","     ","B   B"},
        {"B   B","   A ","  D  "," A   ","B   B"},
        {"B   B","  A  ","  D  ","  A  ","B   B"},
        {"B   B"," A   ","  D  ","   A ","B   B"},
        {"B   B","     "," ADA ","     ","B   B"},
        {"B   B","   A ","  D  "," A   ","B   B"},
        {"B   B","  A  ","  D  ","  A  ","B   B"},
        {"B   B"," A   ","  D  ","   A ","B   B"},
        {"B   B","     "," ADA ","     ","B   B"},
        {"B   B"," AAA "," ADA "," AAA ","B   B"},
        {"CC~CC","CCCCC","CCCCC","CCCCC","CCCCC"},

    };

    private static final IStructureDefinition<MTEWashConcentratorPlant> STRUCTURE_DEFINITION = StructureDefinition
        .<MTEWashConcentratorPlant>builder()
        .addShape(STRUCTURE_PIECE_MAIN, transpose(STRUCTURE))
        .addElement('A', Casings.SteelGearBoxCasing.asElement())
        .addElement('B', ofFrame(Materials.Steel))
        .addElement('C', buildHatchAdder(MTEWashConcentratorPlant.class)
            .atLeast(OutputBus, OutputHatch, Maintenance, Energy)
            .casingIndex(Casings.WashPlantCasing.textureId)
            .hint(1)
            .buildAndChain(onElementPass(x -> ++x.casingCount, Casings.WashPlantCasing.asElement())))
        .addElement('E', buildHatchAdder(MTEWashConcentratorPlant.class)
            .atLeast(InputBus, InputHatch)
            .casingIndex(Casings.WashPlantCasing.textureId)
            .hint(2)
            .buildAndChain(onElementPass(x -> ++x.casingCount, Casings.WashPlantCasing.asElement())))
        .addElement('D', ofChain(ofAnyWater(true), isAir()))
        .addElement('F', buildHatchAdder(MTEWashConcentratorPlant.class)
            .atLeast(Muffler)
            .casingIndex(Casings.WashPlantCasing.textureId)
            .hint(3)
            .build())
        .build();

    public MTEWashConcentratorPlant(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public MTEWashConcentratorPlant(String aName) {
        super(aName);
    }



    private static final int OFFSET_X = 2;
    private static final int OFFSET_Y = 12;
    private static final int OFFSET_Z = 0;

    @Override
    public void construct(ItemStack trigger, boolean hintsOnly) {
        buildPiece(STRUCTURE_PIECE_MAIN, trigger, hintsOnly, OFFSET_X, OFFSET_Y, OFFSET_Z);
    }

    @Override
    public int survivalConstruct(ItemStack trigger, int elementBudget, ISurvivalBuildEnvironment env) {
        if (mMachine) return -1;
        return survivalBuildPiece(STRUCTURE_PIECE_MAIN, trigger, OFFSET_X, OFFSET_Y, OFFSET_Z, elementBudget, env, false, true);
    }

    @Override
    public IStructureDefinition<MTEWashConcentratorPlant> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    private static final int MIN_CASINGS = 36;

    @Override
    public void checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack,
                             List<StructureError> errors) {
        casingCount = 0;

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
    }

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
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MTEWashConcentratorPlant(this.mName);
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity iGregTechTileEntity, ForgeDirection side, ForgeDirection facing, int colorIndex, boolean active, boolean redstoneLevel) {
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
                .build(),
        };
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return MoonRecipes.washConcentratorRecipes;
    }
}
