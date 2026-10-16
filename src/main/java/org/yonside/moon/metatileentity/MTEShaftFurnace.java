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

import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.structure.error.StructureError;
import gregtech.api.util.MultiblockTooltipBuilder;

/**
 * Blast-blown shaft furnace. The bronze build adds matte smelting and Welsh roasting; the steel build also captures
 * the sulfur dioxide.
 */
public class MTEShaftFurnace extends MTEFixedOreMultiBase<MTEShaftFurnace> {

    private static final IStructureDefinition<MTEShaftFurnace> STRUCTURE_DEFINITION = StructureDefinition
        .<MTEShaftFurnace>builder()
        .addShape(STRUCTURE_PIECE_MAIN, transpose(MTEPrimitiveShaftFurnace.SHAPE))
        .addElement('C', buildCasing(MTEShaftFurnace.class, 1))
        .addElement('T', MoonStructure.part(Part.ClayTuyere))
        .addElement('-', isAir())
        .build();

    public MTEShaftFurnace(int aID, String aName, String aNameRegional) {
        super(
            aID,
            aName,
            aNameRegional,
            MTEPrimitiveShaftFurnace.OFFSET[0],
            MTEPrimitiveShaftFurnace.OFFSET[1],
            MTEPrimitiveShaftFurnace.OFFSET[2]);
    }

    public MTEShaftFurnace(String aName) {
        super(aName, MTEPrimitiveShaftFurnace.OFFSET[0], MTEPrimitiveShaftFurnace.OFFSET[1], MTEPrimitiveShaftFurnace.OFFSET[2]);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTEShaftFurnace(mName);
    }

    @Override
    public IStructureDefinition<MTEShaftFurnace> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    @Override
    protected int getMinRecipeTier() {
        return MoonRecipeMaps.BUILD_BRONZE;
    }

    @Override
    protected void validateStructure(List<StructureError> errors) {
        checkHasInputBus(errors);
        checkHasOutputBus(errors);
        if (build == Build.STEEL) checkHasOutputHatch(errors);
    }

    @Override
    protected int getMinCasings() {
        return 50;
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType("Shaft Furnace");
        addBuildInfo(tt).addInfo("Smelts oxide copper; matte-smelts chalcopyrite with quartz sand flux")
            .addInfo("Welsh-roasts copper matte to blister copper")
            .addInfo("Co-smelts copper-galena concentrate; melts metallic flakes")
            .addInfo("The steel build captures sulfur dioxide in an output hatch")
            .beginStructureBlock(7, 6, 7, false)
            .addController("Front of the hearth, bottom layer")
            .addCasingInfoMin("Bronze Plated Bricks or Solid Steel Machine Casing", 50, false)
            .addOtherStructurePart("Clay Tuyere", "4, around the belly")
            .addStructureInfo("Round shaft: narrow throat, wide belly, narrow hearth; the inside must be empty")
            .addInputBus("Any casing", 1)
            .addOutputBus("Any casing", 1)
            .addOutputHatch("Steel build: sulfur dioxide, any casing", 1)
            .addEnergyHatch("Steel build, any casing", 1)
            .addMaintenanceHatch("Steel build, any casing", 1)
            .addOtherStructurePart("Steam Hatch", "Bronze build (the blast), any casing", 1)
            .toolTipFinisher("Moon");
        return tt;
    }

    @Override
    protected IIconContainer[] getFrontOverlays() {
        return MoonStructure.BLAST;
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return MoonRecipeMaps.shaftFurnaceRecipes;
    }
}
