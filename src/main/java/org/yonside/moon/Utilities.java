package org.yonside.moon;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import org.jetbrains.annotations.NotNull;

import static gregtech.api.util.GTRecipeBuilder.PANIC_MODE_NULL;

public class Utilities {

    public static @NotNull ItemStack getModItem(String modId, String name, int amount, int meta) {
        final var item = GameRegistry.findItem(modId, name);
        if (item == null) return invalidItem(modId, name);

        return new ItemStack(item, amount, meta);
    }

    private static @NotNull ItemStack invalidItem(String modId, String name) {
        if (!PANIC_MODE_NULL) {
            final var fire = new ItemStack(Blocks.fire);
            fire.setStackDisplayName(EnumChatFormatting.RED + "Missing Item: " + modId + ":" + name);
            return fire;
        } else {
            throw new RuntimeException("Could not find ItemStack: " + modId + ":" + name);
        }
    }
}
