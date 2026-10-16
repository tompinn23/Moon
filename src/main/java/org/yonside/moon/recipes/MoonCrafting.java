package org.yonside.moon.recipes;

import static org.yonside.moon.CommonProxy.*;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import org.yonside.moon.block.BlockMachineParts.Part;

import gregtech.api.casing.Casings;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.util.GTModHandler;

/**
 * Crafting for the machine parts and controllers. The design document does not specify these beyond the Spiral
 * Concentrator being steam-craftable; everything up to steam uses bronze, brick and iron so it can be made before LV.
 */
public class MoonCrafting {

    private static final long BITS = GTModHandler.RecipeBits.BITSD;

    public static void init() {
        parts();
        primitiveControllers();
        bronzeControllers();
        steelControllers();
    }

    private static void parts() {
        shaped(Part.SpiralConcentrator.get(1), "PPP", "PIP", "PPP", 'P', "plateBronze", 'I', "pipeMediumBronze");
        shaped(Part.SpiralConcentratorSteel.get(1), "PPP", "PIP", "PPP", 'P', "plateSteel", 'I', "pipeMediumSteel");
        shaped(Part.SpiralConcentratorStainless.get(1), "PPP", "PIP", "PPP", 'P', "plateStainlessSteel", 'I', "pipeMediumStainlessSteel");
        shaped(Part.SpiralConcentratorTitanium.get(1), "PPP", "PIP", "PPP", 'P', "plateTitanium", 'I', "pipeMediumTitanium");

        shaped(Part.DrumMagnetIron.get(1), "RPR", "PFP", "RPR", 'R', "stickIronMagnetic", 'P', "plateIron", 'F', "frameGtSteel");
        shaped(Part.DrumMagnetSteel.get(1), "RPR", "PFP", "RPR", 'R', "stickSteelMagnetic", 'P', "plateSteel", 'F', "frameGtSteel");
        shaped(Part.DrumMagnetNeodymium.get(1), "RPR", "PFP", "RPR", 'R', "stickNeodymiumMagnetic", 'P', "plateSteel", 'F', "frameGtSteel");
        shaped(Part.DrumMagnetSamarium.get(1), "RPR", "PFP", "RPR", 'R', "stickSamariumMagnetic", 'P', "plateTungstenSteel", 'F', "frameGtTungstenSteel");

        shaped(Part.HighTensionRoll.get(1), "PCP", "CSC", "PCP", 'P', "plateBronze", 'C', "plateCopper", 'S', "stickLongIron");
        shaped(Part.AirTableDeck.get(2), "PPP", "PGP", "PPP", 'P', "plateBronze", 'G', "gearGtSmallBronze");
        shaped(Part.PattinsonPot.get(1), "I I", "I I", "III", 'I', "plateIron");
        shaped(Part.RefiningKettle.get(1), "I I", "ISI", "III", 'I', "plateIron", 'S', "stickIron");
        shaped(Part.ClayRetort.get(1), "B B", "B B", "BBB", 'B', new ItemStack(Items.brick));
        shaped(Part.ClayTuyere.get(1), " B ", "B B", " B ", 'B', new ItemStack(Items.brick));
        shaped(Part.TrommelScreen.get(4), "PSP", "S S", "PSP", 'P', "plateBronze", 'S', "stickBronze");
        shaped(Part.JigCell.get(1), "PSP", "PIP", "PPP", 'P', "plateBronze", 'S', "stickBronze", 'I', "pipeMediumBronze");
        shaped(Part.JigCellSteel.get(1), "PSP", "PIP", "PPP", 'P', "plateSteel", 'S', "stickSteel", 'I', "pipeMediumSteel");
        shaped(Part.JigCellStainless.get(1), "PSP", "PIP", "PPP", 'P', "plateStainlessSteel", 'S', "stickStainlessSteel", 'I', "pipeMediumStainlessSteel");
        shaped(Part.JigCellTitanium.get(1), "PSP", "PIP", "PPP", 'P', "plateTitanium", 'S', "stickTitanium", 'I', "pipeMediumTitanium");

        shaped(Part.ImpellerSteel.get(1), "PRP", "RSR", "PRP", 'P', "plateSteel", 'R', "rotorSteel", 'S', "stickLongSteel");
        shaped(Part.ImpellerAluminium.get(1), "PRP", "RSR", "PRP", 'P', "plateAluminium", 'R', "rotorAluminium", 'S', "stickLongAluminium");
        shaped(Part.ImpellerStainless.get(1), "PRP", "RSR", "PRP", 'P', "plateStainlessSteel", 'R', "rotorStainlessSteel", 'S', "stickLongStainlessSteel");
        shaped(Part.FlotationCell.get(1), "PRP", "PMP", "PPP", 'P', "plateSteel", 'R', "rotorSteel", 'M', ItemList.Electric_Motor_LV.get(1));
    }

    private static void primitiveControllers() {
        ItemStack firebrick = Casings.Firebricks.toStack(1);
        ItemStack furnace = new ItemStack(Blocks.furnace);

        shaped(sluiceBox.getStackForm(1), "PFP", "PCP", "PPP", 'P', "plankWood", 'F', new ItemStack(Items.flint), 'C', new ItemStack(Blocks.cobblestone));
        shaped(primitiveLeadHearth.getStackForm(1), "BBB", "BFB", "BBB", 'B', firebrick, 'F', furnace);
        shaped(pattinsonPotRow.getStackForm(1), "BPB", "BFB", "BBB", 'B', firebrick, 'P', Part.PattinsonPot.get(1), 'F', furnace);
        shaped(retortFurnace.getStackForm(1), "BRB", "BFB", "BBB", 'B', firebrick, 'R', Part.ClayRetort.get(1), 'F', furnace);
        shaped(primitiveShaftFurnace.getStackForm(1), "BTB", "BFB", "BBB", 'B', firebrick, 'T', Part.ClayTuyere.get(1), 'F', furnace);
    }

    private static void bronzeControllers() {
        ItemStack bricks = Casings.BronzePlatedBricks.toStack(1);

        bronze(wetConcentratorPlant.getStackForm(1), Part.SpiralConcentrator.get(1), bricks);
        bronze(rotaryDryer.getStackForm(1), new ItemStack(Blocks.furnace), bricks);
        bronze(magneticSeparator.getStackForm(1), Part.DrumMagnetIron.get(1), bricks);
        bronze(electrostaticSeparator.getStackForm(1), Part.HighTensionRoll.get(1), bricks);
        bronze(airTable.getStackForm(1), Part.AirTableDeck.get(1), bricks);
        bronze(refiningKettle.getStackForm(1), Part.RefiningKettle.get(1), bricks);
        bronze(shaftFurnace.getStackForm(1), primitiveShaftFurnace.getStackForm(1), bricks);
        bronze(trommel.getStackForm(1), Part.TrommelScreen.get(1), bricks);
        bronze(jigBank.getStackForm(1), Part.JigCell.get(1), bricks);
    }

    private static void bronze(ItemStack controller, ItemStack part, ItemStack bricks) {
        shaped(controller, "PXP", "GHG", "PXP", 'P', "plateBronze", 'X', part, 'G', "gearGtSmallBronze", 'H', bricks);
    }

    private static void steelControllers() {
        Object circuit = OrePrefixes.circuit.get(Materials.LV);
        ItemStack hull = ItemList.Hull_LV.get(1);

        shaped(leadHearth.getStackForm(1), "PCP", "FHF", "PCP", 'P', "plateSteel", 'C', circuit, 'F', primitiveLeadHearth.getStackForm(1), 'H', hull);
        shaped(flotationCellBank.getStackForm(1), "PCP", "MHM", "PFP", 'P', "plateSteel", 'C', circuit, 'M', ItemList.Electric_Pump_LV.get(1), 'H', hull, 'F', Part.ImpellerSteel.get(1));
        shaped(copperConverter.getStackForm(1), "PCP", "MHM", "PCP", 'P', "plateSteel", 'C', circuit, 'M', ItemList.Electric_Motor_LV.get(1), 'H', hull);
    }

    private static void shaped(ItemStack result, Object... recipe) {
        GTModHandler.addCraftingRecipe(result, BITS, recipe);
    }
}
