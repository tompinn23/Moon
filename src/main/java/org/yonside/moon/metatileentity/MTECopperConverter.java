package org.yonside.moon.metatileentity;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.isAir;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;
import static gregtech.api.util.GTStructureUtility.ofFrame;

import java.util.List;

import org.yonside.moon.metatileentity.base.MTEFixedOreMultiBase;
import org.yonside.moon.metatileentity.base.MoonStructure;
import org.yonside.moon.recipes.MoonRecipeMaps;

import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;

import gregtech.api.casing.Casings;
import gregtech.api.enums.Materials;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.structure.error.StructureError;
import gregtech.api.util.MultiblockTooltipBuilder;

/** Peirce-Smith converter: blows copper matte to blister copper, capturing all the sulfur. */
public class MTECopperConverter extends MTEFixedOreMultiBase<MTECopperConverter> {

    // Layers top to bottom, rows front to back. A round barrel lying on its side, hung on two trunnions on frame
    // stands, with the mouth on top under an off-gas hood
    private static final String[][] STRUCTURE = new String[][] {
        { "       ", "       ", "   P   ", "       ", "       " },
        { "  CCC  ", "  CCC  ", "  C-C  ", "  CCC  ", "  CCC  " },
        { " CCCCC ", " C---C ", " C---C ", " C---C ", " CCCCC " },
        { " CC~CC ", " C---C ", "GC---CG", " C---C ", " CCCCC " },
        { " CCCCC ", " C---C ", "BC---CB", " C---C ", " CCCCC " },
        { "  CCC  ", "  CCC  ", "B CCC B", "  CCC  ", "  CCC  " },
        { "       ", "       ", "B     B", "       ", "       " }, };

    private static final IStructureDefinition<MTECopperConverter> STRUCTURE_DEFINITION = StructureDefinition
        .<MTECopperConverter>builder()
        .addShape(STRUCTURE_PIECE_MAIN, transpose(STRUCTURE))
        .addElement('C', buildCasing(MTECopperConverter.class, 1))
        .addElement('P', Casings.SteelPipeCasing.asElement())
        .addElement('G', Casings.SteelGearBoxCasing.asElement())
        .addElement('B', ofFrame(Materials.Steel))
        .addElement('-', isAir())
        .build();

    public MTECopperConverter(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional, 3, 3, 0);
    }

    public MTECopperConverter(String aName) {
        super(aName, 3, 3, 0);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTECopperConverter(mName);
    }

    @Override
    public IStructureDefinition<MTECopperConverter> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    @Override
    protected boolean allowsBronze() {
        return false;
    }

    @Override
    protected void validateStructure(List<StructureError> errors) {
        checkHasInputBus(errors);
        checkHasOutputBus(errors);
        checkHasOutputHatch(errors);
    }

    @Override
    protected int getMinCasings() {
        return 50;
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType("Copper Converter");
        addBuildInfo(tt).addInfo("Blows copper matte to blister copper with a silica flux")
            .addInfo("Three times faster than Welsh roasting; captures all the sulfur dioxide")
            .beginStructureBlock(7, 7, 5, true)
            .addController("Center of the front end of the barrel")
            .addCasingInfoMin("Solid Steel Machine Casing", 50, false)
            .addOtherStructurePart("Steel Gear Box Casing", "2 trunnions, either side of the barrel")
            .addOtherStructurePart("Steel Frame Box", "Trunnion stands")
            .addOtherStructurePart("Steel Pipe Casing", "Off-gas hood above the mouth")
            .addStructureInfo("The barrel and its mouth must be empty")
            .addInputBus("Any casing", 1)
            .addOutputBus("Any casing", 1)
            .addOutputHatch("Sulfur dioxide, any casing", 1)
            .addEnergyHatch("Any casing", 1)
            .addMaintenanceHatch("Any casing", 1)
            .toolTipFinisher("Moon");
        return tt;
    }

    @Override
    protected IIconContainer[] getFrontOverlays() {
        return MoonStructure.SMELTER;
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return MoonRecipeMaps.copperConverterRecipes;
    }
}
