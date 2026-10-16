package org.yonside.moon.block;

import net.minecraft.item.ItemStack;

import org.yonside.moon.item.ItemCupel;

import cpw.mods.fml.common.registry.GameRegistry;

public class MoonBlocks {

    public static BlockMachineParts machineParts;
    public static ItemCupel cupel;

    public static void init() {
        machineParts = new BlockMachineParts();
        GameRegistry.registerBlock(machineParts, BlockMachineParts.ItemBlockMachineParts.class, "machineParts");

        cupel = new ItemCupel();
        GameRegistry.registerItem(cupel, "cupel");
    }

    public static ItemStack unfiredCupel(int amount) {
        return new ItemStack(cupel, amount, ItemCupel.UNFIRED);
    }

    public static ItemStack boneAshCupel(int amount) {
        return new ItemStack(cupel, amount, ItemCupel.FIRED);
    }

}
