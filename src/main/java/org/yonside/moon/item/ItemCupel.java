package org.yonside.moon.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Bone Ash Cupel for the Lead Hearth's cupellation recipe. Placeholder icons borrow vanilla clay and bowl textures.
 */
public class ItemCupel extends Item {

    public static final int UNFIRED = 0;
    public static final int FIRED = 1;

    @SideOnly(Side.CLIENT)
    private IIcon unfiredIcon, firedIcon;

    public ItemCupel() {
        setHasSubtypes(true);
        setMaxDamage(0);
        setUnlocalizedName("moon.cupel");
        setCreativeTab(CreativeTabs.tabMaterials);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return stack.getItemDamage() == FIRED ? "Bone Ash Cupel" : "Unfired Bone Ash Cupel";
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return stack.getItemDamage() == FIRED ? "item.moon.cupel" : "item.moon.cupel.unfired";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        unfiredIcon = register.registerIcon("moon:cupel");
        firedIcon = register.registerIcon("moon:cupel");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int meta) {
        return meta == FIRED ? firedIcon : unfiredIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack stack, int pass) {
        return stack.getItemDamage() == FIRED ? 0xE8E4DA : 0xFFFFFF;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(item, 1, UNFIRED));
        list.add(new ItemStack(item, 1, FIRED));
    }
}
