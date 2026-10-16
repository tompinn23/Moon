package org.yonside.moon.metatileentity.base;

import static gregtech.api.objects.XSTR.XSTR_INSTANCE;
import static gregtech.api.util.GTWaila.getMachineProgressString;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.input.Keyboard;
import org.yonside.moon.gui.MTEPrimitiveMultiGui;

import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.gtnewhorizon.structurelib.alignment.IAlignment;
import com.gtnewhorizon.structurelib.alignment.IAlignmentLimits;
import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;

import gregtech.api.casing.Casings;
import gregtech.api.covers.CoverRegistry;
import gregtech.api.enums.HarvestTool;
import gregtech.api.enums.TieredVariant;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.ISecondaryDescribable;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.modularui.IGetTitleColor;
import gregtech.api.interfaces.tileentity.ICasingTextureProvider;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.interfaces.tileentity.RecipeMapWorkable;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.modularui2.GTGuiTheme;
import gregtech.api.modularui2.GTGuiThemes;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTUtility;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.common.pollution.Pollution;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

/**
 * Base for the stone-age multiblocks: no hatches, no power, items go in and out through the controller like the Coke
 * Oven. Subclasses supply the structure, the casing and the recipe map.
 * <p>
 * A recipe's special value is the structure tier it needs (0 for most). The machine runs the best recipe whose tier is
 * at most {@link #getRecipeTier()}.
 */
public abstract class MTEPrimitiveMultiBase<T extends MTEPrimitiveMultiBase<T>> extends MetaTileEntity
    implements IAlignment, ISurvivalConstructable, RecipeMapWorkable, IGetTitleColor, ISecondaryDescribable,
    ICasingTextureProvider {

    public static final int INPUT_STACK_LIMIT = 1024;

    public final int inputSlots;
    public final int outputSlots;

    private MultiblockTooltipBuilder tooltipBuilder;

    public int maxProgressTime = 0;
    public int progressTime = 0;
    public boolean mMachine = false;
    private volatile boolean mUpdated;
    public int mUpdate = 5;

    public ItemStack[] mOutputItems;

    protected MTEPrimitiveMultiBase(int aID, String aName, String aNameRegional, int inputSlots, int outputSlots) {
        super(aID, aName, aNameRegional, inputSlots + outputSlots);
        this.inputSlots = inputSlots;
        this.outputSlots = outputSlots;
    }

    protected MTEPrimitiveMultiBase(String aName, int inputSlots, int outputSlots) {
        super(aName, inputSlots + outputSlots);
        this.inputSlots = inputSlots;
        this.outputSlots = outputSlots;
    }

    // ---------------------------------------------------------------- subclass hooks

    @Override
    public abstract IStructureDefinition<T> getStructureDefinition();

    /** Check the whole structure; use {@link #checkPiece}. */
    protected abstract boolean checkStructure();

    /** Build the hologram or the structure; use {@link #buildPiece}. */
    protected abstract void constructStructure(ItemStack trigger, boolean hintsOnly);

    /** Survival auto-build; use {@link #survivalBuildPiece}. */
    protected abstract int survivalConstructStructure(ItemStack trigger, int elementBudget,
        ISurvivalBuildEnvironment env);

    protected abstract MultiblockTooltipBuilder createTooltip();

    protected abstract Casings getCasing();

    protected abstract IIconContainer[] getFrontOverlays();

    /** Highest recipe special value this structure can run. */
    protected int getRecipeTier() {
        return 0;
    }

    /** Pollution while working. */
    protected int getPollutionPerSecond() {
        return 0;
    }

    /** Called when a recipe starts so subclasses can scale its duration. */
    protected int getRecipeDuration(GTRecipe recipe) {
        return recipe.mDuration;
    }

    // ---------------------------------------------------------------- structure helpers

    @SuppressWarnings("unchecked")
    private T self() {
        return (T) this;
    }

    protected final boolean checkPiece(String piece, int horizontalOffset, int verticalOffset, int depthOffset) {
        IGregTechTileEntity base = getBaseMetaTileEntity();
        return getStructureDefinition().check(
            self(),
            piece,
            base.getWorld(),
            getExtendedFacing(),
            base.getXCoord(),
            base.getYCoord(),
            base.getZCoord(),
            horizontalOffset,
            verticalOffset,
            depthOffset,
            !mMachine);
    }

    protected final void buildPiece(String piece, ItemStack trigger, boolean hintsOnly, int horizontalOffset,
        int verticalOffset, int depthOffset) {
        IGregTechTileEntity base = getBaseMetaTileEntity();
        getStructureDefinition().buildOrHints(
            self(),
            trigger,
            piece,
            base.getWorld(),
            getExtendedFacing(),
            base.getXCoord(),
            base.getYCoord(),
            base.getZCoord(),
            horizontalOffset,
            verticalOffset,
            depthOffset,
            hintsOnly);
    }

    protected final int survivalBuildPiece(String piece, ItemStack trigger, int horizontalOffset, int verticalOffset,
        int depthOffset, int elementBudget, ISurvivalBuildEnvironment env) {
        IGregTechTileEntity base = getBaseMetaTileEntity();
        return getStructureDefinition().survivalBuild(
            self(),
            trigger,
            piece,
            base.getWorld(),
            getExtendedFacing(),
            base.getXCoord(),
            base.getYCoord(),
            base.getZCoord(),
            horizontalOffset,
            verticalOffset,
            depthOffset,
            elementBudget,
            env,
            false);
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        constructStructure(stackSize, hintsOnly);
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        if (mMachine) return -1;
        return survivalConstructStructure(stackSize, elementBudget, env);
    }

    @Override
    public ExtendedFacing getExtendedFacing() {
        return ExtendedFacing.of(getBaseMetaTileEntity().getFrontFacing());
    }

    @Override
    public void setExtendedFacing(ExtendedFacing alignment) {
        getBaseMetaTileEntity().setFrontFacing(alignment.getDirection());
    }

    @Override
    public IAlignmentLimits getAlignmentLimits() {
        return (d, r, f) -> (d.flag & (ForgeDirection.UP.flag | ForgeDirection.DOWN.flag)) == 0 && r.isNotRotated()
            && f.isNotFlipped();
    }

    @Override
    public boolean isFacingValid(ForgeDirection facing) {
        return (facing.flag & (ForgeDirection.UP.flag | ForgeDirection.DOWN.flag)) == 0;
    }

    // ---------------------------------------------------------------- tooltip

    protected final MultiblockTooltipBuilder getTooltip() {
        if (tooltipBuilder == null) tooltipBuilder = createTooltip();
        return tooltipBuilder;
    }

    @Override
    public String[] getStructureDescription(ItemStack stackSize) {
        return getTooltip().getStructureHint();
    }

    @Override
    public String[] getDescription() {
        return getCurrentDescription();
    }

    @Override
    public boolean isDisplaySecondaryDescription() {
        return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
    }

    @Override
    public String[] getPrimaryDescription() {
        return getTooltip().getInformation();
    }

    @Override
    public String[] getSecondaryDescription() {
        return getTooltip().getStructureInformation();
    }

    // ---------------------------------------------------------------- tile entity basics

    @Override
    public byte getTileEntityBaseType() {
        return HarvestTool.WrenchLevel0.toTileEntityBaseType();
    }

    @Override
    public boolean isTeleporterCompatible() {
        return false;
    }

    @Override
    public boolean allowCoverOnSide(ForgeDirection side, ItemStack coverItem) {
        return CoverRegistry.getCoverPlacer(coverItem)
            .allowOnPrimitiveBlock() && super.allowCoverOnSide(side, coverItem);
    }

    @Override
    public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
        openGui(aPlayer);
        return true;
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        aNBT.setInteger("progressTime", progressTime);
        aNBT.setInteger("maxProgressTime", maxProgressTime);
        if (mOutputItems != null) {
            aNBT.setInteger("mOutputCount", mOutputItems.length);
            for (int i = 0; i < mOutputItems.length; i++) {
                if (mOutputItems[i] != null) {
                    NBTTagCompound tNBT = new NBTTagCompound();
                    mOutputItems[i].writeToNBT(tNBT);
                    aNBT.setTag("mOutputItem" + i, tNBT);
                }
            }
        }
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        mUpdate = 5;
        progressTime = aNBT.getInteger("progressTime");
        maxProgressTime = aNBT.getInteger("maxProgressTime");
        // A recipe can have more output entries than slots (they stack), so the pending list has its own length
        int count = aNBT.hasKey("mOutputCount") ? aNBT.getInteger("mOutputCount") : outputSlots;
        mOutputItems = new ItemStack[count];
        for (int i = 0; i < count; i++) {
            mOutputItems[i] = GTUtility.loadItem(aNBT, "mOutputItem" + i);
        }
    }

    @Override
    public void onMachineBlockUpdate() {
        mUpdated = true;
    }

    // ---------------------------------------------------------------- processing

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTimer) {
        if (!aBaseMetaTileEntity.isServerSide()) return;

        if (mUpdated) {
            // same duct tape as the BBF: re-arm the structure check after a burst of block updates
            if (mUpdate < 0) mUpdate = 5;
            mUpdated = false;
        }
        if (mUpdate-- == 0) {
            mMachine = checkStructure();
        }
        if (mMachine) {
            if (maxProgressTime > 0) {
                if (++progressTime >= maxProgressTime) {
                    addOutputProducts();
                    mOutputItems = null;
                    progressTime = 0;
                    maxProgressTime = 0;
                }
                if (aTimer % 20 == 0 && getPollutionPerSecond() > 0) {
                    Pollution.addPollution(aBaseMetaTileEntity, getPollutionPerSecond());
                }
            } else if (aBaseMetaTileEntity.isAllowedToWork()) {
                checkRecipe();
            }
        }
        aBaseMetaTileEntity.setActive(maxProgressTime > 0 && mMachine);
    }

    private void addOutputProducts() {
        if (mOutputItems == null) return;
        for (ItemStack output : mOutputItems) {
            if (output != null) insertOutput(output);
        }
    }

    /** Merge a stack into the output slots; space was reserved by {@link #hasSpaceFor} when the run started. */
    private void insertOutput(ItemStack stack) {
        int remaining = stack.stackSize;
        for (int i = inputSlots; i < inputSlots + outputSlots && remaining > 0; i++) {
            ItemStack slot = mInventory[i];
            if (slot != null && GTUtility.areStacksEqual(slot, stack)) {
                int move = Math.min(remaining, slot.getMaxStackSize() - slot.stackSize);
                slot.stackSize += move;
                remaining -= move;
            }
        }
        for (int i = inputSlots; i < inputSlots + outputSlots && remaining > 0; i++) {
            if (mInventory[i] == null) {
                int move = Math.min(remaining, stack.getMaxStackSize());
                mInventory[i] = GTUtility.copyAmount(move, stack);
                remaining -= move;
            }
        }
    }

    /** Conservative space check: every possible output gets its own empty slot unless it stacks onto one. */
    private boolean hasSpaceFor(GTRecipe recipe) {
        ItemStack[] simulated = new ItemStack[outputSlots];
        for (int i = 0; i < outputSlots; i++) simulated[i] = GTUtility.copyOrNull(mInventory[inputSlots + i]);
        for (ItemStack output : recipe.mOutputs) {
            if (output == null) continue;
            int remaining = output.stackSize;
            for (int i = 0; i < outputSlots && remaining > 0; i++) {
                if (simulated[i] != null && GTUtility.areStacksEqual(simulated[i], output)) {
                    int move = Math.min(remaining, simulated[i].getMaxStackSize() - simulated[i].stackSize);
                    simulated[i].stackSize += move;
                    remaining -= move;
                }
            }
            for (int i = 0; i < outputSlots && remaining > 0; i++) {
                if (simulated[i] == null) {
                    int move = Math.min(remaining, output.getMaxStackSize());
                    simulated[i] = GTUtility.copyAmount(move, output);
                    remaining -= move;
                }
            }
            if (remaining > 0) return false;
        }
        return true;
    }

    private boolean checkRecipe() {
        ItemStack[] inputs = new ItemStack[inputSlots];
        System.arraycopy(mInventory, 0, inputs, 0, inputSlots);

        int tier = getRecipeTier();
        List<GTRecipe> candidates = getRecipeMap().findRecipeQuery()
            .items(inputs)
            .findAll()
            .filter(r -> r.mSpecialValue <= tier)
            .sorted(Comparator.comparingInt((GTRecipe r) -> r.mSpecialValue)
                .reversed())
            .collect(Collectors.toCollection(ArrayList::new));

        for (GTRecipe recipe : candidates) {
            if (!hasSpaceFor(recipe)) continue;
            if (!recipe.isRecipeInputEqual(true, null, inputs)) continue;
            for (int i = 0; i < inputSlots; i++) {
                if (mInventory[i] != null && mInventory[i].stackSize == 0) mInventory[i] = null;
            }

            // Roll chanced outputs up front so the result is fixed for the whole run
            mOutputItems = new ItemStack[recipe.mOutputs.length];
            for (int i = 0; i < recipe.mOutputs.length; i++) {
                ItemStack output = recipe.getOutput(i);
                if (output != null && XSTR_INSTANCE.nextInt(10000) < recipe.getOutputChance(i)) {
                    mOutputItems[i] = output.copy();
                }
            }
            maxProgressTime = getRecipeDuration(recipe);
            return true;
        }
        mOutputItems = null;
        return false;
    }

    @Override
    public int getProgresstime() {
        return progressTime;
    }

    @Override
    public int maxProgresstime() {
        return maxProgressTime;
    }

    @Override
    public int increaseProgress(int aProgress) {
        progressTime += aProgress;
        return maxProgressTime - progressTime;
    }

    @Override
    public int getSlotLimit(int slot) {
        return slot >= 0 && slot < inputSlots ? INPUT_STACK_LIMIT : super.getSlotLimit(slot);
    }

    @Override
    public int getStackSizeLimit(int slot, ItemStack stack) {
        // Inputs ignore the item's own max stack size so a full 64-batch plus the next can pile up
        return slot >= 0 && slot < inputSlots ? INPUT_STACK_LIMIT : super.getStackSizeLimit(slot, stack);
    }

    @Override
    public boolean allowPullStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
        ItemStack aStack) {
        return false;
    }

    @Override
    public boolean allowPutStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
        ItemStack aStack) {
        return false;
    }

    // ---------------------------------------------------------------- rendering, waila, gui

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, ForgeDirection side, ForgeDirection aFacing,
        int colorIndex, boolean aActive, boolean redstoneLevel) {
        IIconContainer[] overlays = getFrontOverlays();
        return gregtech.api.enums.Textures.BlockIcons
            .createTextureWithCasing(this, side, aFacing, aActive, overlays[0], overlays[1], overlays[2], overlays[3]);
    }

    @Override
    public ITexture getCasingTexture() {
        return getCasing().getCasingTexture();
    }

    @Override
    public void getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        if (!getBaseMetaTileEntity().isInvalidTileEntity()) {
            NBTTagCompound nbt = accessor.getNBTData();
            currenttip.add(
                getMachineProgressString(
                    getBaseMetaTileEntity().isActive(),
                    nbt.getInteger("maxProgressTime"),
                    nbt.getInteger("progressTime")));
        }
    }

    @Override
    public void getWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y,
        int z) {
        super.getWailaNBTData(player, tile, tag, world, x, y, z);
        if (!getBaseMetaTileEntity().isInvalidTileEntity()) {
            tag.setInteger("progressTime", getProgresstime());
            tag.setInteger("maxProgressTime", maxProgresstime());
        }
    }

    @Override
    protected boolean useMui2() {
        return true;
    }

    @Override
    public GTGuiTheme getGuiTheme() {
        return GTGuiThemes.TIERED_VARIANTS.get(getTieredVariant());
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings uiSettings) {
        return new MTEPrimitiveMultiGui(this).build(data, syncManager, uiSettings);
    }

    @Override
    public TieredVariant getTieredVariant() {
        return TieredVariant.PRIMITIVE;
    }

    @Override
    public int getTitleColor() {
        return COLOR_TITLE_WHITE.get();
    }
}
