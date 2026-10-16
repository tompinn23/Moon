package org.yonside.moon.recipes;

import static gregtech.api.enums.GTValues.RA;
import static gregtech.api.util.GTRecipeBuilder.SECONDS;
import static org.yonside.moon.recipes.MoonRecipeMaps.BUILD_BRONZE;
import static org.yonside.moon.recipes.MoonRecipeMaps.BUILD_PRIMITIVE;
import static org.yonside.moon.recipes.MoonRecipeMaps.BUILD_STEEL;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.yonside.moon.MoonMaterials;
import org.yonside.moon.block.MoonBlocks;

import bartworks.system.material.Werkstoff;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.enums.TierEU;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.util.GTModHandler;
import gregtech.api.util.GTRecipeBuilder;
import gtPlusPlus.core.material.MaterialsOres;
import gtPlusPlus.xmod.gregtech.api.enums.GregtechItemList;

/**
 * Every recipe from the GT Ore Processing Addon design document. All amounts, EU/t and durations are the document's
 * first-pass placeholders.
 */
public class MoonRecipes {

    /** Primitive machines ignore EU; recipes still need a value for the builder. */
    private static final int NO_EU = 0;

    public static void init() {
        mineralSands();
        leadAndSilver();
        copper();
        skyblockGravel();
        sharedGtMachines();
    }

    // ================================================================== Mineral Sands

    private static void mineralSands() {
        // Sluice Box: first tin and iron, deliberately poor recovery
        Out sluice = new Out().add(Materials.Magnetite.getDust(1))
            .chance(Materials.Cassiterite.getDust(1), 7500)
            .add(Materials.QuartzSand.getDust(48))
            .add(dust(MoonMaterials.MineralSandTailings, 14));
        RA.stdBuilder()
            .itemInputs(dust(MoonMaterials.HeavyMineralSand, 64))
            .itemOutputs(sluice.stacks(), sluice.chances())
            .duration(20 * SECONDS)
            .eut(NO_EU)
            .addTo(MoonRecipeMaps.sluiceBoxRecipes);

        // Wet Concentrator Plant: the spiral tier (special value) splits the 16 units of heavy mineral
        int[][] wcp = { { 1, 8, 8 }, { 2, 11, 5 }, { 3, 13, 3 }, { 4, 14, 2 } };
        for (int[] tier : wcp) {
            RA.stdBuilder()
                .itemInputs(dust(MoonMaterials.HeavyMineralSand, 64))
                .fluidInputs(Materials.Water.getFluid(16_000))
                .itemOutputs(
                    dust(MoonMaterials.WetHeavyMineralConcentrate, tier[1]),
                    Materials.QuartzSand.getDust(48),
                    dust(MoonMaterials.MineralSandTailings, tier[2]))
                .fluidOutputs(Materials.Water.getFluid(14_000))
                .specialValue(tier[0])
                .duration(20 * SECONDS)
                .eut(16)
                .addTo(MoonRecipeMaps.wetConcentratorRecipes);
        }

        // WCP fines recipe: EV tailings reprocessing, Titanium spirals
        RA.stdBuilder()
            .itemInputs(dust(MoonMaterials.MineralSandTailings, 16))
            .fluidInputs(GTModHandler.getDistilledWater(4_000))
            .itemOutputs(dust(MoonMaterials.WetLowGradeHeavyMineralConcentrate, 16))
            .fluidOutputs(Materials.Water.getFluid(3_000))
            .specialValue(4)
            .duration(40 * SECONDS)
            .eut(TierEU.RECIPE_EV)
            .addTo(MoonRecipeMaps.wetConcentratorRecipes);

        // Rotary Dryer
        dry(MoonMaterials.WetHeavyMineralConcentrate, MoonMaterials.DryHeavyMineralConcentrate);
        dry(MoonMaterials.WetLowGradeHeavyMineralConcentrate, MoonMaterials.DryLowGradeHeavyMineralConcentrate);

        // Magnetic Separator, low intensity: every magnet tier
        RA.stdBuilder()
            .itemInputs(dust(MoonMaterials.DryHeavyMineralConcentrate, 16))
            .itemOutputs(Materials.Magnetite.getDust(2), dust(MoonMaterials.NonMagneticHeavyMineralConcentrate, 14))
            .specialValue(1)
            .duration(10 * SECONDS)
            .eut(16)
            .addTo(MoonRecipeMaps.magneticSeparatorRecipes);

        // Magnetic Separator, titanium concentrate: ilmenite is magnetic, rutile is not
        double[][] titanium = { { 1, 2.4, 0.8, 4.8 }, { 2, 3.6, 1.2, 3.2 }, { 3, 5.4, 1.8, 0.8 }, { 4, 6, 2, 0 } };
        for (double[] t : titanium) {
            Out out = new Out().frac(Materials.Ilmenite::getDust, t[1])
                .frac(Materials.Rutile::getDust, t[2])
                .frac(n -> dust(MoonMaterials.TitaniumConcentrate, n), t[3]);
            RA.stdBuilder()
                .itemInputs(dust(MoonMaterials.TitaniumConcentrate, 8))
                .itemOutputs(out.stacks(), out.chances())
                .specialValue((int) t[0])
                .duration(10 * SECONDS)
                .eut(16)
                .addTo(MoonRecipeMaps.magneticSeparatorRecipes);
        }

        // Magnetic Separator, zircon-monazite: locked until Neodymium magnets
        double[][] zircon = { { 3, 1.5, 0.75, 0.75 }, { 4, 2, 1, 0 } };
        for (double[] t : zircon) {
            Out out = new Out().frac(MaterialsOres.ZIRCON::getDust, t[1])
                .frac(Materials.Monazite::getDust, t[2])
                .frac(n -> dust(MoonMaterials.ZirconMonaziteConcentrate, n), t[3]);
            RA.stdBuilder()
                .itemInputs(dust(MoonMaterials.ZirconMonaziteConcentrate, 3))
                .itemOutputs(out.stacks(), out.chances())
                .specialValue((int) t[0])
                .duration(10 * SECONDS)
                .eut(16)
                .addTo(MoonRecipeMaps.magneticSeparatorRecipes);
        }

        // Electrostatic Separator: the power tier (special value) sets the cut
        double[][] electrostatic = { { 0, 3.6, 2, 8.4 }, { 1, 4.5, 2.5, 7 }, { 2, 5.4, 3, 5.6 }, { 3, 6.3, 3.5, 4.2 },
            { 4, 8.1, 4.5, 1.4 }, { 5, 9, 5, 0 } };
        for (double[] t : electrostatic) {
            Out out = new Out().frac(n -> dust(MoonMaterials.ConductorConcentrate, n), t[1])
                .frac(n -> dust(MoonMaterials.NonConductorConcentrate, n), t[2])
                .frac(n -> dust(MoonMaterials.NonMagneticHeavyMineralConcentrate, n), t[3]);
            RA.stdBuilder()
                .itemInputs(dust(MoonMaterials.NonMagneticHeavyMineralConcentrate, 14))
                .itemOutputs(out.stacks(), out.chances())
                .specialValue((int) t[0])
                .duration(10 * SECONDS)
                .eut(32)
                .addTo(MoonRecipeMaps.electrostaticSeparatorRecipes);
        }

        // Air Table: density splits
        airTable(
            dust(MoonMaterials.ConductorConcentrate, 9),
            10,
            Materials.Cassiterite.getDust(1),
            dust(MoonMaterials.TitaniumConcentrate, 8));
        airTable(
            dust(MoonMaterials.NonConductorConcentrate, 5),
            10,
            Materials.Kyanite.getDust(1),
            Materials.GarnetSand.getDust(1),
            dust(MoonMaterials.ZirconMonaziteConcentrate, 3));
        airTable(
            dust(MoonMaterials.DryLowGradeHeavyMineralConcentrate, 16),
            10,
            dust(MoonMaterials.DryHeavyMineralConcentrate, 8),
            Materials.QuartzSand.getDust(8));
    }

    // ================================================================== Lead and silver

    private static void leadAndSilver() {
        // Stone age: Sluice Box keeps half the galena, loses all the sphalerite
        RA.stdBuilder()
            .itemInputs(crushed(MoonMaterials.ArgentiferousLeadOre, 64))
            .itemOutputs(dust(MoonMaterials.GalenaConcentrate, 4), dust(MoonMaterials.LeadMillTailings, 60))
            .duration(20 * SECONDS)
            .eut(NO_EU)
            .addTo(MoonRecipeMaps.sluiceBoxRecipes);

        // Lead Hearth: primitive build (vents everything) and steel build (captures SO2 and fume)
        hearth(
            new ItemStack[] { dust(MoonMaterials.GalenaConcentrate, 8), charcoal(2) },
            new ItemStack[] { ingot(MoonMaterials.LeadBullion, 6) },
            new ItemStack[] { ingot(MoonMaterials.LeadBullion, 6), dust(MoonMaterials.LeadFume, 2) },
            Materials.SulfurDioxide.getGas(2_000),
            40);
        hearth(
            new ItemStack[] { dust(MoonMaterials.SphaleriteConcentrate, 6) },
            new ItemStack[] { dust(MoonMaterials.ZincCalcine, 6) },
            new ItemStack[] { dust(MoonMaterials.ZincCalcine, 6) },
            Materials.SulfurDioxide.getGas(1_500),
            30);
        hearthBoth(
            new ItemStack[] { ingot(MoonMaterials.RichLead, 4), MoonBlocks.boneAshCupel(1) },
            new ItemStack[] { Materials.Silver.getIngots(1), dust(MoonMaterials.Litharge, 3) },
            30);
        hearthBoth(
            new ItemStack[] { dust(MoonMaterials.Litharge, 4), charcoal(1) },
            new ItemStack[] { Materials.Lead.getIngots(4) },
            20);
        hearthBoth(
            new ItemStack[] { dust(MoonMaterials.LeadFume, 4), charcoal(1) },
            new ItemStack[] { Materials.Lead.getIngots(4) },
            20);
        hearthBoth(
            new ItemStack[] { dust(MoonMaterials.AntimonyDross, 2), charcoal(1) },
            new ItemStack[] { Materials.Antimony.getIngots(1), Materials.Lead.getIngots(1) },
            20);
        hearthBoth(
            new ItemStack[] { dust(MoonMaterials.AnodeSlime, 1), Materials.Lead.getIngots(3) },
            new ItemStack[] { ingot(MoonMaterials.RichLead, 4) },
            20);

        // Pattinson Pot Row: more pots, better odds on each Rich Lead
        int[][] pots = { { 3, 5000 }, { 5, 7500 }, { 7, 9000 }, { 9, 9700 } };
        for (int[] p : pots) {
            Out out = new Out().add(Materials.Lead.getIngots(12));
            for (int i = 0; i < 4; i++) out.chance(ingot(MoonMaterials.RichLead, 1), p[1]);
            RA.stdBuilder()
                .itemInputs(ingot(MoonMaterials.LeadBullion, 16), charcoal(1))
                .itemOutputs(out.stacks(), out.chances())
                .specialValue(p[0])
                .duration(p[0] * 10 * SECONDS)
                .eut(NO_EU)
                .addTo(MoonRecipeMaps.pattinsonPotRecipes);
        }

        // Steam: Air Table gravity concentration
        airTable(
            crushed(MoonMaterials.ArgentiferousLeadOre, 64),
            20,
            dust(MoonMaterials.BulkSulfideConcentrate, 10),
            dust(MoonMaterials.LeadMillTailings, 54));
        airTable(
            dust(MoonMaterials.BulkSulfideConcentrate, 14),
            10,
            dust(MoonMaterials.GalenaConcentrate, 8),
            dust(MoonMaterials.SphaleriteConcentrate, 6));

        // Refining Kettle: softening, then Parkes desilvering
        RA.stdBuilder()
            .itemInputs(ingot(MoonMaterials.LeadBullion, 16))
            .itemOutputs(ingot(MoonMaterials.SoftenedBullion, 16), dust(MoonMaterials.AntimonyDross, 1))
            .duration(20 * SECONDS)
            .eut(16)
            .addTo(MoonRecipeMaps.refiningKettleRecipes);
        RA.stdBuilder()
            .itemInputs(ingot(MoonMaterials.SoftenedBullion, 16), Materials.Zinc.getIngots(1))
            .itemOutputs(Materials.Lead.getIngots(12), dust(MoonMaterials.ZincCrust, 4))
            .duration(20 * SECONDS)
            .eut(16)
            .addTo(MoonRecipeMaps.refiningKettleRecipes);

        // Retort Furnace: zinc as a vapour, a third lost
        RA.stdBuilder()
            .itemInputs(dust(MoonMaterials.ZincCalcine, 6), charcoal(3))
            .itemOutputs(Materials.Zinc.getIngots(4))
            .duration(40 * SECONDS)
            .eut(NO_EU)
            .addTo(MoonRecipeMaps.retortFurnaceRecipes);
        RA.stdBuilder()
            .itemInputs(dust(MoonMaterials.ZincCrust, 4), charcoal(1))
            .itemOutputs(
                new ItemStack[] { ingot(MoonMaterials.RichLead, 4), Materials.Zinc.getIngots(1) },
                new int[] { 10000, 9000 })
            .duration(30 * SECONDS)
            .eut(NO_EU)
            .addTo(MoonRecipeMaps.retortFurnaceRecipes);

        // LV: Flotation Cell, bulk sulfide float with creosote frother. Impeller tier (special value) sets recovery.
        int[][] bulk = { { 1, 10, 54 }, { 2, 12, 52 }, { 3, 13, 51 } };
        for (int[] b : bulk) {
            RA.stdBuilder()
                .itemInputs(dust(MoonMaterials.ArgentiferousLeadOre, 64))
                .fluidInputs(Materials.Water.getFluid(16_000), Materials.Creosote.getFluid(100))
                .itemOutputs(
                    dust(MoonMaterials.WetBulkSulfideConcentrate, b[1]),
                    dust(MoonMaterials.LeadMillTailings, b[2]))
                .fluidOutputs(Materials.Water.getFluid(14_000))
                .specialValue(b[0])
                .duration(30 * SECONDS)
                .eut(TierEU.RECIPE_LV)
                .addTo(MoonRecipeMaps.flotationRecipes);
        }
        dry(MoonMaterials.WetBulkSulfideConcentrate, MoonMaterials.BulkSulfideConcentrate);

        // MV: differential flotation, galena with xanthate, then sphalerite activated with copper sulfate
        Out diff = new Out().frac(n -> dust(MoonMaterials.GalenaConcentrate, n), 7.5)
            .frac(n -> dust(MoonMaterials.SphaleriteConcentrate, n), 5.5)
            .add(dust(MoonMaterials.LeadMillTailings, 51));
        RA.stdBuilder()
            .itemInputs(dust(MoonMaterials.ArgentiferousLeadOre, 64), GregtechItemList.SodiumEthylXanthate.get(1))
            .fluidInputs(Materials.Water.getFluid(16_000), blueVitriol(100))
            .itemOutputs(diff.stacks(), diff.chances())
            .fluidOutputs(Materials.Water.getFluid(14_000))
            .specialValue(3)
            .duration(50 * SECONDS)
            .eut(TierEU.RECIPE_MV)
            .addTo(MoonRecipeMaps.flotationRecipes);
    }

    // ================================================================== Copper

    private static void copper() {
        // Stone age: native copper is all kept, half the malachite washes out
        RA.stdBuilder()
            .itemInputs(crushed(MoonMaterials.OxidisedCopperOre, 64))
            .itemOutputs(
                dust(MoonMaterials.NativeCopper, 2),
                dust(MoonMaterials.MalachiteConcentrate, 3),
                dust(MoonMaterials.CopperTailings, 59))
            .duration(20 * SECONDS)
            .eut(NO_EU)
            .addTo(MoonRecipeMaps.sluiceBoxRecipes);
        airTable(
            crushed(MoonMaterials.OxidisedCopperOre, 64),
            20,
            dust(MoonMaterials.NativeCopper, 2),
            dust(MoonMaterials.MalachiteConcentrate, 5),
            dust(MoonMaterials.CopperTailings, 57));

        // Shaft Furnace
        shaftAllBuilds(
            new ItemStack[] { dust(MoonMaterials.MalachiteConcentrate, 8), charcoal(4) },
            new ItemStack[] { Materials.Copper.getIngots(4) },
            40);
        shaftPowered(
            new ItemStack[] { dust(MoonMaterials.ChalcopyriteConcentrate, 8), Materials.QuartzSand.getDust(2),
                charcoal(2) },
            new ItemStack[] { dust(MoonMaterials.CopperMatte, 4), dust(MoonMaterials.IronSlag, 4) },
            Materials.SulfurDioxide.getGas(2_000),
            40);
        shaftPowered(
            new ItemStack[] { dust(MoonMaterials.CopperMatte, 4), charcoal(1) },
            new ItemStack[] { ingot(MoonMaterials.BlisterCopper, 4) },
            Materials.SulfurDioxide.getGas(1_000),
            60);

        // Copper Converter (LV)
        RA.stdBuilder()
            .itemInputs(dust(MoonMaterials.CopperMatte, 8), Materials.QuartzSand.getDust(1))
            .itemOutputs(ingot(MoonMaterials.BlisterCopper, 8))
            .fluidOutputs(Materials.SulfurDioxide.getGas(4_000))
            .duration(20 * SECONDS)
            .eut(TierEU.RECIPE_LV)
            .addTo(MoonRecipeMaps.copperConverterRecipes);

        // Electrorefining (LV): returns the Blue Vitriol, keeps the silver as anode slime
        electrorefine(ingot(MoonMaterials.BlisterCopper, 16), 16, 60);

        // Copper flotation: steam Air Table, then the Flotation Cell Bank
        airTable(
            crushed(MoonMaterials.ChalcopyriteOre, 64),
            20,
            dust(MoonMaterials.CopperLeadConcentrate, 7),
            dust(MoonMaterials.CopperTailings, 57));
        airTable(
            dust(MoonMaterials.CopperLeadConcentrate, 5),
            10,
            dust(MoonMaterials.ChalcopyriteConcentrate, 4),
            dust(MoonMaterials.GalenaConcentrate, 1));

        double[][] bulk = { { 1, 7, 57 }, { 2, 8.5, 55 }, { 3, 9.3, 54 } };
        for (double[] b : bulk) {
            Out out = new Out().frac(n -> dust(MoonMaterials.WetCopperLeadConcentrate, n), b[1])
                .add(dust(MoonMaterials.CopperTailings, (int) b[2]));
            RA.stdBuilder()
                .itemInputs(dust(MoonMaterials.ChalcopyriteOre, 64))
                .fluidInputs(Materials.Water.getFluid(16_000), Materials.Creosote.getFluid(100))
                .itemOutputs(out.stacks(), out.chances())
                .fluidOutputs(Materials.Water.getFluid(14_000))
                .specialValue((int) b[0])
                .duration(30 * SECONDS)
                .eut(TierEU.RECIPE_LV)
                .addTo(MoonRecipeMaps.flotationRecipes);
        }
        dry(MoonMaterials.WetCopperLeadConcentrate, MoonMaterials.CopperLeadConcentrate);

        Out diff = new Out().frac(n -> dust(MoonMaterials.ChalcopyriteConcentrate, n), 7.5)
            .frac(n -> dust(MoonMaterials.GalenaConcentrate, n), 1.8)
            .add(dust(MoonMaterials.CopperTailings, 54));
        RA.stdBuilder()
            .itemInputs(dust(MoonMaterials.ChalcopyriteOre, 64), GregtechItemList.SodiumEthylXanthate.get(1))
            .fluidInputs(Materials.Water.getFluid(16_000))
            .itemOutputs(diff.stacks(), diff.chances())
            .fluidOutputs(Materials.Water.getFluid(14_000))
            .specialValue(3)
            .duration(50 * SECONDS)
            .eut(TierEU.RECIPE_MV)
            .addTo(MoonRecipeMaps.flotationRecipes);
    }

    // ================================================================== Skyblock gravel route

    private static void skyblockGravel() {
        // Stone age: Sluice Box on raw gravel
        Out sluice = new Out().add(dust(MoonMaterials.NativeCopper, 1))
            .chance(dust(MoonMaterials.GalenaConcentrate, 1), 5000)
            .chance(Materials.Silver.getNuggets(1), 1000)
            .add(new ItemStack(Blocks.cobblestone, 32))
            .add(new ItemStack(Blocks.sand, 30));
        RA.stdBuilder()
            .itemInputs(new ItemStack(Blocks.gravel, 64))
            .itemOutputs(sluice.stacks(), sluice.chances())
            .duration(20 * SECONDS)
            .eut(NO_EU)
            .addTo(MoonRecipeMaps.sluiceBoxRecipes);

        // Trommel
        RA.stdBuilder()
            .itemInputs(new ItemStack(Blocks.gravel, 64))
            .fluidInputs(Materials.Water.getFluid(4_000))
            .itemOutputs(
                dust(MoonMaterials.PayGravel, 32),
                new ItemStack(Blocks.sand, 16),
                new ItemStack(Blocks.cobblestone, 16))
            .duration(10 * SECONDS)
            .eut(16)
            .addTo(MoonRecipeMaps.trommelRecipes);

        // Jig Bank: the jig cell tier (special value) splits the 9.5 units of heavy grains
        double[][] jig = { { 1, 5, 59 }, { 2, 7, 57 }, { 3, 8, 56 }, { 4, 8.5, 55 } };
        for (double[] j : jig) {
            Out out = new Out().frac(n -> dust(MoonMaterials.JigConcentrate, n), j[1])
                .add(dust(MoonMaterials.JigTailings, (int) j[2]));
            RA.stdBuilder()
                .itemInputs(dust(MoonMaterials.PayGravel, 64))
                .fluidInputs(Materials.Water.getFluid(16_000))
                .itemOutputs(out.stacks(), out.chances())
                .fluidOutputs(Materials.Water.getFluid(14_000))
                .specialValue((int) j[0])
                .duration(20 * SECONDS)
                .eut(16)
                .addTo(MoonRecipeMaps.jigBankRecipes);
        }

        // Separation: magnet at any tier, then Air Table (steam) or mill and screen (LV)
        RA.stdBuilder()
            .itemInputs(dust(MoonMaterials.JigConcentrate, 10))
            .itemOutputs(Materials.Magnetite.getDust(2), dust(MoonMaterials.NonMagneticJigConcentrate, 8))
            .specialValue(1)
            .duration(10 * SECONDS)
            .eut(16)
            .addTo(MoonRecipeMaps.magneticSeparatorRecipes);
        airTable(
            dust(MoonMaterials.NonMagneticJigConcentrate, 10),
            10,
            dust(MoonMaterials.CopperGalenaConcentrate, 8),
            Materials.GarnetSand.getDust(2));
        RA.stdBuilder()
            .itemInputs(dust(MoonMaterials.NonMagneticJigConcentrate, 1))
            .itemOutputs(dust(MoonMaterials.MilledJigConcentrate, 1))
            .duration(5 * SECONDS)
            .eut(TierEU.RECIPE_LV)
            .addTo(RecipeMaps.maceratorRecipes);
        RA.stdBuilder()
            .itemInputs(dust(MoonMaterials.MilledJigConcentrate, 10))
            .itemOutputs(dust(MoonMaterials.MetallicFlakes, 4), dust(MoonMaterials.MineralFines, 6))
            .duration(10 * SECONDS)
            .eut(TierEU.RECIPE_LV)
            .addTo(RecipeMaps.sifterRecipes);
        airTable(
            dust(MoonMaterials.MineralFines, 6),
            10,
            dust(MoonMaterials.GalenaConcentrate, 4),
            Materials.GarnetSand.getDust(2));

        // Smelting and silver
        shaftPowered(
            new ItemStack[] { dust(MoonMaterials.CopperGalenaConcentrate, 8), charcoal(2) },
            new ItemStack[] { ingot(MoonMaterials.ArgentiferousCopper, 4), ingot(MoonMaterials.LeadBullion, 3) },
            null,
            40);
        shaftAllBuilds(
            new ItemStack[] { dust(MoonMaterials.MetallicFlakes, 4), charcoal(1) },
            new ItemStack[] { ingot(MoonMaterials.ArgentiferousCopper, 4) },
            20);
        // Liquation (the Saiger process): lead drains out of the copper carrying the silver
        hearthBoth(
            new ItemStack[] { ingot(MoonMaterials.ArgentiferousCopper, 12), Materials.Lead.getIngots(4) },
            new ItemStack[] { Materials.Copper.getIngots(12), ingot(MoonMaterials.RichLead, 4) },
            40);
        hearthBoth(
            new ItemStack[] { ingot(MoonMaterials.ArgentiferousCopper, 12), ingot(MoonMaterials.LeadBullion, 4) },
            new ItemStack[] { Materials.Copper.getIngots(12), ingot(MoonMaterials.RichLead, 4) },
            40);
        electrorefine(ingot(MoonMaterials.ArgentiferousCopper, 12), 12, 45);
    }

    // ================================================================== GT machines and crafting

    private static void sharedGtMachines() {
        // Grinding to flotation feed: a fixed recipe until the EV Fine Grinding Mill brings liberation
        for (Werkstoff ore : new Werkstoff[] { MoonMaterials.ArgentiferousLeadOre, MoonMaterials.ChalcopyriteOre }) {
            RA.stdBuilder()
                .itemInputs(crushed(ore, 1))
                .itemOutputs(dust(ore, 1))
                .duration(5 * SECONDS)
                .eut(TierEU.RECIPE_LV)
                .addTo(RecipeMaps.maceratorRecipes);
        }

        // Furnace: native copper and fire-refined blister copper (fire refining loses the silver)
        GTModHandler.addSmeltingRecipe(dust(MoonMaterials.NativeCopper, 1), Materials.Copper.getIngots(1));
        GTModHandler.addSmeltingRecipe(ingot(MoonMaterials.BlisterCopper, 1), Materials.Copper.getIngots(1));

        // Bone Ash Cupel: the skeleton farm is the first silver gate
        GTModHandler.addShapelessCraftingRecipe(
            MoonBlocks.unfiredCupel(1),
            GTModHandler.RecipeBits.BITSD,
            new Object[] { boneMeal(), boneMeal(), boneMeal(), boneMeal(), new ItemStack(Items.clay_ball) });
        GTModHandler.addSmeltingRecipe(MoonBlocks.unfiredCupel(1), MoonBlocks.boneAshCupel(1));
    }

    /** Kyanite electrolysis per GT's per-atom convention for Al2SiO5. Registered after GT's version is removed. */
    public static void addKyaniteElectrolysis() {
        RA.stdBuilder()
            .itemInputs(Materials.Kyanite.getDust(8))
            .itemOutputs(Materials.Aluminium.getDust(2), Materials.Silicon.getDust(1))
            .fluidOutputs(Materials.Oxygen.getGas(5_000))
            .duration(20 * SECONDS)
            .eut(TierEU.RECIPE_LV)
            .addTo(RecipeMaps.electrolyzerRecipes);
    }

    // ================================================================== helpers

    private static void dry(Werkstoff wet, Werkstoff dry) {
        RA.stdBuilder()
            .itemInputs(dust(wet, 16))
            .itemOutputs(dust(dry, 16))
            .duration(10 * SECONDS)
            .eut(16)
            .addTo(MoonRecipeMaps.rotaryDryerRecipes);
    }

    private static void airTable(ItemStack input, int seconds, ItemStack... outputs) {
        RA.stdBuilder()
            .itemInputs(input)
            .itemOutputs(outputs)
            .duration(seconds * SECONDS)
            .eut(8)
            .addTo(MoonRecipeMaps.airTableRecipes);
    }

    /** A Lead Hearth recipe whose steel build also captures sulfur dioxide and fume. */
    private static void hearth(ItemStack[] inputs, ItemStack[] primitiveOutputs, ItemStack[] steelOutputs,
        FluidStack steelGas, int seconds) {
        RA.stdBuilder()
            .itemInputs(inputs)
            .itemOutputs(primitiveOutputs)
            .specialValue(BUILD_PRIMITIVE)
            .duration(seconds * SECONDS)
            .eut(NO_EU)
            .addTo(MoonRecipeMaps.leadHearthRecipes);
        GTRecipeBuilder steel = RA.stdBuilder()
            .itemInputs(inputs)
            .itemOutputs(steelOutputs)
            .specialValue(BUILD_STEEL)
            .duration(seconds * SECONDS)
            .eut(TierEU.RECIPE_LV);
        if (steelGas != null) steel.fluidOutputs(steelGas);
        steel.addTo(MoonRecipeMaps.leadHearthRecipes);
    }

    /** A Lead Hearth recipe that is the same in both builds. */
    private static void hearthBoth(ItemStack[] inputs, ItemStack[] outputs, int seconds) {
        hearth(inputs, outputs, outputs, null, seconds);
    }

    /** A Shaft Furnace recipe every build runs (primitive copy without EU, powered copy at 16 EU/t). */
    private static void shaftAllBuilds(ItemStack[] inputs, ItemStack[] outputs, int seconds) {
        RA.stdBuilder()
            .itemInputs(inputs)
            .itemOutputs(outputs)
            .specialValue(BUILD_PRIMITIVE)
            .duration(seconds * SECONDS)
            .eut(NO_EU)
            .addTo(MoonRecipeMaps.shaftFurnaceRecipes);
        RA.stdBuilder()
            .itemInputs(inputs)
            .itemOutputs(outputs)
            .specialValue(BUILD_BRONZE)
            .duration(seconds * SECONDS)
            .eut(16)
            .addTo(MoonRecipeMaps.shaftFurnaceRecipes);
    }

    /** A sulfide Shaft Furnace recipe: bronze and steel only; the steel build captures the gas if there is any. */
    private static void shaftPowered(ItemStack[] inputs, ItemStack[] outputs, FluidStack steelGas, int seconds) {
        RA.stdBuilder()
            .itemInputs(inputs)
            .itemOutputs(outputs)
            .specialValue(BUILD_BRONZE)
            .duration(seconds * SECONDS)
            .eut(16)
            .addTo(MoonRecipeMaps.shaftFurnaceRecipes);
        if (steelGas == null) return;
        RA.stdBuilder()
            .itemInputs(inputs)
            .itemOutputs(outputs)
            .fluidOutputs(steelGas)
            .specialValue(BUILD_STEEL)
            .duration(seconds * SECONDS)
            .eut(TierEU.RECIPE_LV)
            .addTo(MoonRecipeMaps.shaftFurnaceRecipes);
    }

    /** GT Electrolyzer electrorefining: impure copper anodes to cathode copper plus silver-bearing anode slime. */
    private static void electrorefine(ItemStack anodes, int copper, int seconds) {
        RA.stdBuilder()
            .itemInputs(anodes)
            .fluidInputs(blueVitriol(1_000))
            .itemOutputs(Materials.Copper.getIngots(copper), dust(MoonMaterials.AnodeSlime, 1))
            .fluidOutputs(blueVitriol(1_000))
            .duration(seconds * SECONDS)
            .eut(TierEU.RECIPE_LV)
            .addTo(RecipeMaps.electrolyzerRecipes);
    }

    private static ItemStack dust(Werkstoff material, int amount) {
        return material.get(OrePrefixes.dust, amount);
    }

    private static ItemStack crushed(Werkstoff material, int amount) {
        return material.get(OrePrefixes.crushed, amount);
    }

    private static ItemStack ingot(Werkstoff material, int amount) {
        return material.get(OrePrefixes.ingot, amount);
    }

    private static ItemStack charcoal(int amount) {
        return new ItemStack(Items.coal, amount, 1);
    }

    private static ItemStack boneMeal() {
        return new ItemStack(Items.dye, 1, 15);
    }

    private static FluidStack blueVitriol(int amount) {
        if (ItemList.sBlueVitriol == null) {
            throw new IllegalStateException("GT's Blue Vitriol fluid is not registered yet; register recipes later");
        }
        return new FluidStack(ItemList.sBlueVitriol, amount);
    }

    /**
     * Builds a GT output list with chances. Fractional amounts from the design document become a whole stack plus a
     * chanced single item, e.g. 2.4 → 2 guaranteed and a 40% roll for one more.
     */
    private static final class Out {

        private final List<ItemStack> stacks = new ArrayList<>();
        private final List<Integer> chances = new ArrayList<>();

        Out add(ItemStack stack) {
            return chance(stack, 10000);
        }

        Out chance(ItemStack stack, int chance) {
            stacks.add(stack);
            chances.add(chance);
            return this;
        }

        Out frac(IntFunction<ItemStack> stack, double amount) {
            int whole = (int) Math.floor(amount + 1e-6);
            int fraction = (int) Math.round((amount - whole) * 10000);
            if (whole > 0) add(stack.apply(whole));
            if (fraction > 0) chance(stack.apply(1), fraction);
            return this;
        }

        ItemStack[] stacks() {
            return stacks.toArray(new ItemStack[0]);
        }

        int[] chances() {
            return chances.stream()
                .mapToInt(Integer::intValue)
                .toArray();
        }
    }
}
