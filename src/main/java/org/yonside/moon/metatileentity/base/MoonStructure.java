package org.yonside.moon.metatileentity.base;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlocksTiered;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import net.minecraft.block.Block;

import org.apache.commons.lang3.tuple.Pair;
import org.yonside.moon.block.BlockMachineParts;
import org.yonside.moon.block.MoonBlocks;

import com.gtnewhorizon.structurelib.structure.IStructureElement;

import gregtech.api.enums.Textures.BlockIcons;
import gregtech.api.interfaces.IIconContainer;

/** Shared structure elements and controller overlays for the ore processing multiblocks. */
public final class MoonStructure {

    private MoonStructure() {}

    public static <T> IStructureElement<T> part(BlockMachineParts.Part part) {
        return ofBlock(MoonBlocks.machineParts, part.meta());
    }

    /**
     * One of several tiers of a part, all the same tier within a structure. The tier passed to the setter is the index
     * in {@code tiers} plus one; 0 means unset.
     */
    public static <T> IStructureElement<T> tieredPart(BlockMachineParts.Part[] tiers, BiConsumer<T, Integer> setter,
        Function<T, Integer> getter) {
        List<Pair<Block, Integer>> known = new ArrayList<>();
        for (BlockMachineParts.Part tier : tiers) known.add(Pair.of(MoonBlocks.machineParts, tier.meta()));
        return ofBlocksTiered((block, meta) -> {
            if (block != MoonBlocks.machineParts) return null;
            for (int i = 0; i < tiers.length; i++) if (tiers[i].meta() == meta) return i + 1;
            return null;
        }, known, 0, setter, getter);
    }


    /** Inactive, inactive glow, active, active glow. */
    public static final IIconContainer[] WASHER = { BlockIcons.OVERLAY_FRONT_STEAM_WASHER,
        BlockIcons.OVERLAY_FRONT_STEAM_WASHER_GLOW, BlockIcons.OVERLAY_FRONT_STEAM_WASHER_ACTIVE,
        BlockIcons.OVERLAY_FRONT_STEAM_WASHER_ACTIVE_GLOW };

    public static final IIconContainer[] FURNACE = { BlockIcons.OVERLAY_FRONT_STEAM_FURNACE,
        BlockIcons.OVERLAY_FRONT_STEAM_FURNACE_GLOW, BlockIcons.OVERLAY_FRONT_STEAM_FURNACE_ACTIVE,
        BlockIcons.OVERLAY_FRONT_STEAM_FURNACE_ACTIVE_GLOW };

    public static final IIconContainer[] BLAST = { BlockIcons.OVERLAY_FRONT_ELECTRIC_BLAST_FURNACE,
        BlockIcons.OVERLAY_FRONT_ELECTRIC_BLAST_FURNACE_GLOW, BlockIcons.OVERLAY_FRONT_ELECTRIC_BLAST_FURNACE_ACTIVE,
        BlockIcons.OVERLAY_FRONT_ELECTRIC_BLAST_FURNACE_ACTIVE_GLOW };

    public static final IIconContainer[] SMELTER = { BlockIcons.OVERLAY_FRONT_MULTI_SMELTER,
        BlockIcons.OVERLAY_FRONT_MULTI_SMELTER_GLOW, BlockIcons.OVERLAY_FRONT_MULTI_SMELTER_ACTIVE,
        BlockIcons.OVERLAY_FRONT_MULTI_SMELTER_ACTIVE_GLOW };

    public static final IIconContainer[] CENTRIFUGE = { BlockIcons.OVERLAY_FRONT_STEAM_CENTRIFUGE,
        BlockIcons.OVERLAY_FRONT_STEAM_CENTRIFUGE_GLOW, BlockIcons.OVERLAY_FRONT_STEAM_CENTRIFUGE_ACTIVE,
        BlockIcons.OVERLAY_FRONT_STEAM_CENTRIFUGE_ACTIVE_GLOW };

    public static final IIconContainer[] MACERATOR = { BlockIcons.OVERLAY_FRONT_STEAM_MACERATOR,
        BlockIcons.OVERLAY_FRONT_STEAM_MACERATOR_GLOW, BlockIcons.OVERLAY_FRONT_STEAM_MACERATOR_ACTIVE,
        BlockIcons.OVERLAY_FRONT_STEAM_MACERATOR_ACTIVE_GLOW };

    public static final IIconContainer[] DISTILLERY = { BlockIcons.OVERLAY_FRONT_DISTILLATION_TOWER,
        BlockIcons.OVERLAY_FRONT_DISTILLATION_TOWER_GLOW, BlockIcons.OVERLAY_FRONT_DISTILLATION_TOWER_ACTIVE,
        BlockIcons.OVERLAY_FRONT_DISTILLATION_TOWER_ACTIVE_GLOW };

    public static final IIconContainer[] ALLOY_SMELTER = { BlockIcons.OVERLAY_FRONT_STEAM_ALLOY_SMELTER,
        BlockIcons.OVERLAY_FRONT_STEAM_ALLOY_SMELTER_GLOW, BlockIcons.OVERLAY_FRONT_STEAM_ALLOY_SMELTER_ACTIVE,
        BlockIcons.OVERLAY_FRONT_STEAM_ALLOY_SMELTER_ACTIVE_GLOW };
}
