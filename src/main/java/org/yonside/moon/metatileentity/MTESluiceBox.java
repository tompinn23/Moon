package org.yonside.moon.metatileentity;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;
import static gregtech.api.util.GTStructureUtility.ofAnyWater;
import static gregtech.api.util.GTStructureUtility.ofFrame;

import net.minecraft.item.ItemStack;

import org.yonside.moon.metatileentity.base.MTEPrimitiveMultiBase;
import org.yonside.moon.metatileentity.base.MoonStructure;
import org.yonside.moon.recipes.MoonRecipeMaps;

import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;

import gregtech.api.casing.Casings;
import gregtech.api.enums.Materials;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.util.MultiblockTooltipBuilder;

/** Stone age gravity separation: a riffled wooden trough with water running down it. */
public class MTESluiceBox extends MTEPrimitiveMultiBase<MTESluiceBox> {

    public static final int INPUT_SLOTS = 1, OUTPUT_SLOTS = 5;

    private static final String STRUCTURE_PIECE_MAIN = "main";
    private static final int OFFSET_X = 1;
    private static final int OFFSET_Y = 1;
    private static final int OFFSET_Z = 0;

    private static final IStructureDefinition<MTESluiceBox> STRUCTURE_DEFINITION = IStructureDefinition
        .<MTESluiceBox>builder()
        .addShape(
            STRUCTURE_PIECE_MAIN,
            transpose(
                new String[][] { { "ABA", "ADA", "ADA", "ADA", "ADA", "ACA", "AAA" },
                    { "A~A", "AAA", "AAA", "AAA", "AAA", "AAA", "AAA" }, }))
        .addElement('A', Casings.PrimitiveWoodenCasing.asElement())
        .addElement('B', ofFrame(Materials.Wood))
        // Source block at the back of the trough
        .addElement('C', ofAnyWater())
        // Water running down the trough; settled flowing water turns into still water with meta > 0
        .addElement('D', ofAnyWater(true))
        .build();

    public MTESluiceBox(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional, INPUT_SLOTS, OUTPUT_SLOTS);
    }

    public MTESluiceBox(String aName) {
        super(aName, INPUT_SLOTS, OUTPUT_SLOTS);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MTESluiceBox(this.mName);
    }

    @Override
    public IStructureDefinition<MTESluiceBox> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    @Override
    protected boolean checkStructure() {
        return checkPiece(STRUCTURE_PIECE_MAIN, OFFSET_X, OFFSET_Y, OFFSET_Z);
    }

    @Override
    protected void constructStructure(ItemStack trigger, boolean hintsOnly) {
        buildPiece(STRUCTURE_PIECE_MAIN, trigger, hintsOnly, OFFSET_X, OFFSET_Y, OFFSET_Z);
    }

    @Override
    protected int survivalConstructStructure(ItemStack trigger, int elementBudget, ISurvivalBuildEnvironment env) {
        return survivalBuildPiece(STRUCTURE_PIECE_MAIN, trigger, OFFSET_X, OFFSET_Y, OFFSET_Z, elementBudget, env);
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType("Sluice Box")
            .addInfo("Washes heavy minerals out of sand, crushed ore and gravel")
            .addInfo("Recovery is poor: most of the metal is lost to tailings")
            .addInfo("All input/output is done manually through the controller")
            .beginStructureBlock(3, 2, 7, false)
            .addController("Front center, bottom layer")
            .addCasingInfoExactly("Primitive Wooden Casing", 35, false)
            .addOtherStructurePart("Wood Frame Box", "Above the controller")
            .addOtherStructurePart("Water Source", "Back of the trough")
            .addOtherStructurePart("Flowing Water", "Running down the trough to the front")
            .toolTipFinisher("Moon");
        return tt;
    }

    @Override
    protected Casings getCasing() {
        return Casings.PrimitiveWoodenCasing;
    }

    @Override
    protected IIconContainer[] getFrontOverlays() {
        return MoonStructure.WASHER;
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return MoonRecipeMaps.sluiceBoxRecipes;
    }
}
