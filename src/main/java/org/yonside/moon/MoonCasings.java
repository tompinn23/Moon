package org.yonside.moon;

import bartworks.system.material.WerkstoffLoader;
import com.gtnewhorizon.gtnhlib.util.data.BlockSupplier;
import gregtech.api.casing.ICasing;
import net.minecraft.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public enum MoonCasings implements ICasing {
    BoltedStainlessSteelCasing(() -> WerkstoffLoader.BWBlockCasings, 32185, 32185),
    ReboltedStainlessSteelCasing(() -> WerkstoffLoader.BWBlockCasingsAdvanced, 32185, 32185)

    ;

    public final BlockSupplier blockGetter;
    private volatile Block block;
    public final int meta;
    public final int textureId;

    MoonCasings(BlockSupplier blockGetter, int meta, int textureId) {
        this.blockGetter = blockGetter;
        this.meta = meta;
        this.textureId = textureId;
    }

    @Override
    public int getTextureId() {
        if (textureId == -1) {
            throw new UnsupportedOperationException(
                "Casing " + name() + " does not have a casing texture; The result of getTextureId() is undefined.");
        }

        return textureId;
    }

    @Override
    public boolean isTiered() {
        return false;
    }

    @Override
    public @NotNull Block getBlock() {
        if (block == null) {
            block = Objects.requireNonNull(blockGetter.get(), "Block for casing " + name() + " was null");
        }

        return block;
    }

    @Override
    public int getBlockMeta() {
        return meta;
    }
}
