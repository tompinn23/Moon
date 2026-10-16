package org.yonside.moon.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * The process blocks that sit inside the ore processing multiblocks. None of them carry hatches, so they are plain
 * blocks with one meta per part.
 */
public class BlockMachineParts extends Block {

    public enum Part {

        SpiralConcentrator("Bronze Spiral Concentrator", "gregtech:iconsets/MACHINE_CASING_PIPE_BRONZE"),
        DrumMagnetIron("Drum Magnet (Iron)", "gregtech:iconsets/BLOCK_IRONMAGNETIC"),
        DrumMagnetSteel("Drum Magnet (Steel)", "gregtech:iconsets/BLOCK_STEELMAGNETIC"),
        DrumMagnetNeodymium("Drum Magnet (Neodymium)", "gregtech:iconsets/BLOCK_NEODYMIUMMAGNETIC"),
        DrumMagnetSamarium("Drum Magnet (Samarium)", "gregtech:iconsets/BLOCK_SAMARIUM"),
        HighTensionRoll("High-Tension Roll", "gregtech:iconsets/MACHINE_COIL_CUPRONICKEL"),
        AirTableDeck("Air Table Deck", "gregtech:iconsets/MACHINE_CASING_GRATE"),
        PattinsonPot("Pattinson Pot", "gregtech:iconsets/MACHINE_CASING_FIREBOX_STEEL"),
        RefiningKettle("Refining Kettle", "gregtech:iconsets/MACHINE_CASING_FIREBOX_BRONZE"),
        ClayRetort("Clay Retort", "minecraft:hardened_clay"),
        FlotationCell("Flotation Cell", "gregtech:iconsets/MACHINE_CASING_TANK_1"),
        ClayTuyere("Clay Tuyere", "minecraft:hardened_clay_stained_orange"),
        TrommelScreen("Trommel Screen", "gregtech:iconsets/LARGE_SIEVE_GRATE"),
        JigCell("Bronze Jig Cell", "gregtech:iconsets/INDUSTRIAL_SIEVE_CASING"),
        // Higher spiral tiers; appended so earlier metas stay put
        SpiralConcentratorSteel("Steel Spiral Concentrator", "gregtech:iconsets/MACHINE_CASING_PIPE_STEEL"),
        SpiralConcentratorStainless("Stainless Steel Spiral Concentrator",
            "gregtech:iconsets/MACHINE_CASING_CLEAN_STAINLESSSTEEL"),
        SpiralConcentratorTitanium("Titanium Spiral Concentrator", "gregtech:iconsets/MACHINE_CASING_PIPE_TITANIUM"),
        JigCellSteel("Steel Jig Cell", "gregtech:iconsets/MACHINE_CASING_GRATE"),
        JigCellStainless("Stainless Steel Jig Cell", "gregtech:iconsets/MACHINE_CASING_CLEAN_STAINLESSSTEEL"),
        JigCellTitanium("Titanium Jig Cell", "gregtech:iconsets/MACHINE_CASING_PIPE_TITANIUM"),
        ImpellerSteel("Steel Flotation Impeller", "gregtech:iconsets/MACHINE_CASING_GEARBOX_STEEL"),
        ImpellerAluminium("Aluminium Flotation Impeller", "gregtech:iconsets/MACHINE_CASING_GEARBOX_TITANIUM"),
        ImpellerStainless("Stainless Steel Flotation Impeller", "gregtech:iconsets/MACHINE_CASING_GEARBOX_TUNGSTENSTEEL");

        /** Tiered parts in order; the index + 1 is the tier. */
        public static final Part[] SPIRALS = { SpiralConcentrator, SpiralConcentratorSteel,
            SpiralConcentratorStainless, SpiralConcentratorTitanium };
        public static final Part[] JIG_CELLS = { JigCell, JigCellSteel, JigCellStainless, JigCellTitanium };
        public static final Part[] IMPELLERS = { ImpellerSteel, ImpellerAluminium, ImpellerStainless };

        public final String displayName;
        final String texture;

        Part(String displayName, String texture) {
            this.displayName = displayName;
            this.texture = texture;
        }

        public int meta() {
            return ordinal();
        }

        public ItemStack get(int amount) {
            return new ItemStack(MoonBlocks.machineParts, amount, ordinal());
        }

        public static Part byMeta(int meta) {
            Part[] values = values();
            return values[meta >= 0 && meta < values.length ? meta : 0];
        }
    }

    @SideOnly(Side.CLIENT)
    private IIcon[] icons;

    public BlockMachineParts() {
        super(Material.iron);
        setBlockName("moon.machineParts");
        setHardness(5.0F);
        setResistance(10.0F);
        setStepSound(soundTypeMetal);
        setHarvestLevel("pickaxe", 1);
        setCreativeTab(CreativeTabs.tabMisc);
    }

    @Override
    public int damageDropped(int meta) {
        return meta;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        Part[] parts = Part.values();
        icons = new IIcon[parts.length];
        for (Part part : parts) {
            icons[part.ordinal()] = register.registerIcon(part.texture);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return icons[Part.byMeta(meta)
            .ordinal()];
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        for (Part part : Part.values()) {
            list.add(new ItemStack(item, 1, part.ordinal()));
        }
    }

    public static class ItemBlockMachineParts extends ItemBlock {

        public ItemBlockMachineParts(Block block) {
            super(block);
            setHasSubtypes(true);
            setMaxDamage(0);
        }

        @Override
        public int getMetadata(int meta) {
            return meta;
        }

        @Override
        public String getUnlocalizedName(ItemStack stack) {
            return "tile.moon.machineParts." + Part.byMeta(stack.getItemDamage())
                .name();
        }

        @Override
        public String getItemStackDisplayName(ItemStack stack) {
            return Part.byMeta(stack.getItemDamage()).displayName;
        }
    }
}
