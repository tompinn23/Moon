package org.yonside.moon.metatileentity.base;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlocksTiered;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofChain;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.onElementPass;
import static gregtech.api.enums.HatchElement.Energy;
import static gregtech.api.enums.HatchElement.InputBus;
import static gregtech.api.enums.HatchElement.InputHatch;
import static gregtech.api.enums.HatchElement.Maintenance;
import static gregtech.api.enums.HatchElement.OutputBus;
import static gregtech.api.enums.HatchElement.OutputHatch;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static gregtech.api.util.GTStructureUtility.ofFrame;
import static gregtech.api.util.GTUtility.validMTEList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.gtnewhorizon.structurelib.alignment.IAlignmentLimits;
import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureElement;

import gregtech.api.casing.Casings;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.Materials;
import gregtech.api.enums.TieredVariant;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.ICasingTextureProvider;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.metatileentity.implementations.MTEExtendedPowerMultiBlockBase;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.structure.error.StructureError;
import gregtech.api.structure.error.StructureErrorRegistry;
import gregtech.api.structure.error.StructureErrors;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTUtility;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.OverclockCalculator;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.MTEHatchSteamBusInput;
import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.MTEHatchSteamBusOutput;
import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.base.MTEHatchCustomFluidBase;

/**
 * Base for the ore processing multiblocks that come in two builds, picked by the casing the player uses:
 * <ul>
 * <li><b>Bronze</b> (Bronze Plated Bricks): runs on a steam hatch at 2 L of steam per EU, never overclocks, capped at
 * {@value #BRONZE_MAX_PARALLEL} parallels, no maintenance.</li>
 * <li><b>Steel</b> (Solid Steel Machine Casing): LV or better energy hatch, maintenance hatch, normal GT
 * overclocking.</li>
 * </ul>
 * Tiered machines list one recipe per tier in their map, with the tier as the special value; the machine runs the
 * best recipe whose special value lies in [{@link #getMinRecipeTier()}, {@link #getRecipeTier()}].
 */
public abstract class MTEOreMultiBase<T extends MTEOreMultiBase<T>> extends MTEExtendedPowerMultiBlockBase<T>
    implements ISurvivalConstructable, ICasingTextureProvider {

    public enum Build {
        NONE,
        BRONZE,
        STEEL
    }

    public static final int STEAM_PER_EU = 2;
    public static final int BRONZE_MAX_PARALLEL = 4;

    protected Build build = Build.NONE;
    /** Index + 1 of the casing in {@link #getCasingTiers()}; 1 is the steam build. */
    protected int casingTier = -1;
    protected int casingCount;
    protected int frameTier = -1;
    private boolean frameMismatch;
    public final ArrayList<MTEHatchCustomFluidBase> steamHatches = new ArrayList<>();

    protected MTEOreMultiBase(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    protected MTEOreMultiBase(String aName) {
        super(aName);
    }

    // ---------------------------------------------------------------- subclass hooks

    /** Check every structure piece. Casings and hatches are counted by the shared elements. */
    protected abstract boolean checkStructure(List<StructureError> errors);

    /** Extra per-machine checks once the build is known (required hatches, part tiers). */
    protected abstract void validateStructure(List<StructureError> errors);

    protected abstract int getMinCasings();

    protected abstract IIconContainer[] getFrontOverlays();

    protected boolean allowsBronze() {
        return true;
    }

    /**
     * For a machine that does not use the shared build casing: the build it always is. When null, the build comes from
     * Bronze Plated Bricks or Solid Steel Machine Casing.
     */
    protected Build getFixedBuild() {
        return null;
    }

    protected boolean allowsSteel() {
        return true;
    }

    /** Highest recipe special value this structure can run. Defaults to the build. */
    protected int getRecipeTier() {
        return build.ordinal();
    }

    /** Lowest recipe special value this machine accepts. */
    protected int getMinRecipeTier() {
        return 0;
    }

    /** Parallels before the bronze cap is applied. */
    protected int getBaseParallel() {
        return 1;
    }

    public final int getMaxParallel() {
        int parallel = Math.max(1, getBaseParallel());
        return build == Build.BRONZE ? Math.min(parallel, BRONZE_MAX_PARALLEL) : parallel;
    }

    public Build getBuild() {
        return build;
    }

    // ---------------------------------------------------------------- structure elements

    /** The usual two builds: Bronze Plated Bricks run on steam, Solid Steel Machine Casing on EU. */
    protected static final Casings[] TWO_BUILDS = { Casings.BronzePlatedBricks, Casings.SolidSteelMachineCasing };
    protected static final Materials[] TWO_BUILD_FRAMES = { Materials.Bronze, Materials.Steel };

    /** Fully tiered machines: Bronze is the steam tier; Steel, Stainless Steel and Titanium run on EU. */
    protected static final Casings[] FOUR_TIERS = { Casings.BronzePlatedBricks, Casings.SolidSteelMachineCasing,
        Casings.CleanStainlessSteelMachineCasing, Casings.StableTitaniumMachineCasing };
    protected static final Materials[] FOUR_TIER_FRAMES = { Materials.Bronze, Materials.Steel, Materials.StainlessSteel,
        Materials.Titanium };
    public static final String[] FOUR_TIER_NAMES = { "None", "Bronze", "Steel", "Stainless Steel", "Titanium" };

    /** Tooltip lines for a fully tiered machine. */
    protected static MultiblockTooltipBuilder addTierInfo(MultiblockTooltipBuilder tt, String parts) {
        return tt
            .addInfo(
                "Every part tiers together: casing, frame boxes and " + parts
                    + " must all be one tier")
            .addInfo(
                EnumChatFormatting.GOLD + "Bronze"
                    + EnumChatFormatting.GRAY
                    + " (Bronze Plated Bricks): steam tier, one Steam Hatch, 2 L steam per EU, no overclocks")
            .addInfo(
                EnumChatFormatting.WHITE + "Steel"
                    + EnumChatFormatting.GRAY
                    + " (Solid Steel), "
                    + EnumChatFormatting.WHITE
                    + "Stainless Steel"
                    + EnumChatFormatting.GRAY
                    + " (Clean Stainless), "
                    + EnumChatFormatting.WHITE
                    + "Titanium"
                    + EnumChatFormatting.GRAY
                    + " (Stable Titanium): EU tiers with Energy and Maintenance Hatches");
    }

    /**
     * The casing tiers this machine accepts, lowest first. Tier 1 is the steam build; tier 2 and up run on EU. Must be
     * the same list the structure definition's casing elements were built with.
     */
    protected Casings[] getCasingTiers() {
        return TWO_BUILDS;
    }

    /** The casing hatches and the controller are textured with. */
    protected Casings getBuildCasing() {
        Casings[] tiers = getCasingTiers();
        return casingTier >= 1 && casingTier <= tiers.length ? tiers[casingTier - 1] : tiers[0];
    }

    /** Casing that may hold any hatch, in the usual two builds. */
    protected static <T extends MTEOreMultiBase<T>> IStructureElement<T> buildCasing(Class<T> type, int hint) {
        return buildCasing(type, hint, TWO_BUILDS);
    }

    /**
     * Casing that may hold any hatch (steam hatch and steam buses included). All casing in the structure must be one
     * tier. The casing, not a hatch, is what previews and auto-build place.
     */
    protected static <T extends MTEOreMultiBase<T>> IStructureElement<T> buildCasing(Class<T> type, int hint,
        Casings... tiers) {
        return buildHatchAdder(type)
            .atLeast(
                InputBus,
                InputHatch,
                OutputBus,
                OutputHatch,
                Maintenance,
                Energy,
                MoonHatchElement.SteamHatch,
                MoonHatchElement.SteamInputBus,
                MoonHatchElement.SteamOutputBus)
            .casingIndex(tiers[0].textureId)
            .hint(hint)
            .buildAndChain(plainCasing(tiers));
    }

    /** Casing with no hatches allowed, in the usual two builds. */
    protected static <T extends MTEOreMultiBase<T>> IStructureElement<T> plainCasing() {
        return plainCasing(TWO_BUILDS);
    }

    /** Casing with no hatches allowed. */
    protected static <T extends MTEOreMultiBase<T>> IStructureElement<T> plainCasing(Casings... tiers) {
        List<Pair<Block, Integer>> known = new ArrayList<>();
        for (Casings c : tiers) known.add(Pair.of(c.getBlock(), c.getBlockMeta()));
        return onElementPass(t -> t.casingCount++, ofBlocksTiered((block, meta) -> {
            for (int i = 0; i < tiers.length; i++) {
                if (block == tiers[i].getBlock() && meta == tiers[i].getBlockMeta()) return i + 1;
            }
            return null;
        }, known, -1, (t, tier) -> t.casingTier = tier, t -> t.casingTier));
    }

    /** Frame boxes in the usual two builds: bronze or steel, matching the casing. */
    protected static <T extends MTEOreMultiBase<T>> IStructureElement<T> buildFrame() {
        return tieredFrame(TWO_BUILD_FRAMES);
    }

    /** Frame boxes whose material tier, lowest first, must match the casing tier. */
    @SuppressWarnings("unchecked")
    protected static <T extends MTEOreMultiBase<T>> IStructureElement<T> tieredFrame(Materials... tiers) {
        IStructureElement<T>[] options = new IStructureElement[tiers.length];
        for (int i = 0; i < tiers.length; i++) {
            final int tier = i + 1;
            options[i] = onElementPass(t -> t.markFrame(tier), ofFrame(tiers[i]));
        }
        return ofChain(options);
    }

    protected final void markFrame(int tier) {
        if (frameTier == -1) frameTier = tier;
        else if (frameTier != tier) frameMismatch = true;
    }

    /** A block that comes in a bronze and a steel flavour (pipe casings, gearboxes); must match the build. */
    protected static <T extends MTEOreMultiBase<T>> IStructureElement<T> buildPart(Casings bronze, Casings steel,
        java.util.function.BiConsumer<T, Integer> setter, java.util.function.Function<T, Integer> getter) {
        return ofBlocksTiered((block, meta) -> {
            if (block == bronze.getBlock() && meta == bronze.getBlockMeta()) return Build.BRONZE.ordinal();
            if (block == steel.getBlock() && meta == steel.getBlockMeta()) return Build.STEEL.ordinal();
            return null;
        },
            ImmutableList.of(
                Pair.of(bronze.getBlock(), bronze.getBlockMeta()),
                Pair.of(steel.getBlock(), steel.getBlockMeta())),
            -1,
            setter,
            getter);
    }

    public boolean addSteamHatch(IGregTechTileEntity tileEntity, int casingIndex) {
        if (tileEntity == null) return false;
        IMetaTileEntity mte = tileEntity.getMetaTileEntity();
        if (mte instanceof MTEHatchCustomFluidBase hatch && hatch.mLockedFluid != null
            && hatch.mLockedFluid.equals(Materials.Steam.mGas)
            && steamHatches.isEmpty()) {
            hatch.updateTexture(casingIndex);
            hatch.updateCraftingIcon(getMachineCraftingIcon());
            return steamHatches.add(hatch);
        }
        return false;
    }

    public boolean addSteamBus(IGregTechTileEntity tileEntity, int casingIndex) {
        if (tileEntity == null) return false;
        IMetaTileEntity mte = tileEntity.getMetaTileEntity();
        if (mte instanceof MTEHatchSteamBusInput bus) {
            bus.updateTexture(casingIndex);
            bus.updateCraftingIcon(getMachineCraftingIcon());
            bus.mRecipeMap = getRecipeMap();
            addIfSmartInput(bus);
            return mInputBusses.add(bus);
        }
        if (mte instanceof MTEHatchSteamBusOutput bus) {
            bus.updateTexture(casingIndex);
            bus.updateCraftingIcon(getMachineCraftingIcon());
            return mOutputBusses.add(bus);
        }
        return false;
    }

    @Override
    public void clearHatches() {
        super.clearHatches();
        steamHatches.clear();
    }

    // ---------------------------------------------------------------- structure check

    @Override
    public void checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack, List<StructureError> errors) {
        casingTier = -1;
        casingCount = 0;
        frameTier = -1;
        frameMismatch = false;
        build = Build.NONE;

        if (!checkStructure(errors)) return;

        if (getFixedBuild() != null) build = getFixedBuild();
        else if (casingTier == 1) build = Build.BRONZE;
        else if (casingTier >= 2) build = Build.STEEL;
        else {
            errors.add(StructureErrorRegistry.UNKNOWN_TIER);
            return;
        }

        if (build == Build.BRONZE && !allowsBronze()) {
            errors.add(StructureErrors.of("This machine needs Solid Steel Machine Casing"));
        }
        if (build == Build.STEEL && !allowsSteel()) {
            errors.add(StructureErrors.of("This machine needs Bronze Plated Bricks"));
        }
        if (getFixedBuild() == null && frameTier != -1 && (frameMismatch || frameTier != casingTier)) {
            errors.add(StructureErrors.of("Frame boxes must be the same material tier as the casing"));
        }

        checkCasingMin(errors, casingCount, getMinCasings());

        if (build == Build.BRONZE) {
            if (steamHatches.isEmpty()) {
                errors.add(StructureErrors.of("GT5U.gui.text.structure_error.missing_steam_input"));
            }
            if (!mEnergyHatches.isEmpty() || !mExoticEnergyHatches.isEmpty()) {
                errors.add(StructureErrors.of("Bronze builds run on steam: remove the energy hatch"));
            }
        } else {
            checkHasEnergyHatch(errors);
            checkHasMaintenanceHatch(errors);
            if (!steamHatches.isEmpty()) {
                errors.add(StructureErrors.of("EU builds cannot use a steam hatch"));
            }
        }

        validateStructure(errors);

        hasMaintenanceChecks = build == Build.STEEL;
        updateHatchTextures();
    }

    private void updateHatchTextures() {
        int id = getBuildCasing().textureId;
        for (List<? extends MTEHatch> list : Arrays.<List<? extends MTEHatch>>asList(
            steamHatches,
            mInputBusses,
            mOutputBusses,
            mInputHatches,
            mOutputHatches,
            mEnergyHatches,
            mMaintenanceHatches)) {
            for (MTEHatch hatch : list) hatch.updateTexture(id);
        }
    }

    // ---------------------------------------------------------------- processing

    @Override
    protected ProcessingLogic createProcessingLogic() {
        return new ProcessingLogic() {

            @Override
            @Nonnull
            protected Stream<GTRecipe> findRecipeMatches(@Nullable RecipeMap<?> map) {
                int min = getMinRecipeTier();
                int max = getRecipeTier();
                return super.findRecipeMatches(map).filter(r -> r.mSpecialValue >= min && r.mSpecialValue <= max)
                    .sorted(
                        Comparator.comparingInt((GTRecipe r) -> r.mSpecialValue)
                            .reversed());
            }

            @Override
            @Nonnull
            protected OverclockCalculator createOverclockCalculator(@Nonnull GTRecipe recipe) {
                if (build == Build.BRONZE) return OverclockCalculator.ofNoOverclock(recipe);
                return super.createOverclockCalculator(recipe);
            }
        }.setMaxParallelSupplier(this::getMaxParallel);
    }

    @Override
    protected void setProcessingLogicPower(ProcessingLogic logic) {
        if (build == Build.BRONZE) {
            // Steam stands in for LV power; amperage covers every parallel so only voltage gates recipes
            logic.setAvailableVoltage(GTValues.V[1]);
            logic.setAvailableAmperage(getMaxParallel());
            logic.setAmperageOC(false);
            logic.setMaxTierSkips(0);
        } else {
            super.setProcessingLogicPower(logic);
        }
    }

    @Override
    public boolean onRunningTick(ItemStack aStack) {
        if (build != Build.BRONZE) return super.onRunningTick(aStack);
        if (lEUt < 0 && !drainSteam(getActualEnergyUsage() * STEAM_PER_EU)) {
            stopMachine(ShutDownReasonRegistry.POWER_LOSS);
            return false;
        }
        return true;
    }

    private boolean drainSteam(long amount) {
        if (amount > Integer.MAX_VALUE) return false;
        for (MTEHatchCustomFluidBase hatch : validMTEList(steamHatches)) {
            FluidStack simulated = hatch.drain((int) amount, false);
            if (simulated != null && simulated.amount >= amount) {
                hatch.drain((int) amount, true);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean supportsVoidProtection() {
        return true;
    }

    @Override
    public boolean supportsBatchMode() {
        return true;
    }

    @Override
    public boolean supportsInputSeparation() {
        return true;
    }

    @Override
    public boolean getDefaultBatchMode() {
        return false;
    }

    // ---------------------------------------------------------------- display

    /** Shared tooltip lines describing the two builds. */
    protected MultiblockTooltipBuilder addBuildInfo(MultiblockTooltipBuilder tt) {
        if (allowsBronze() && allowsSteel()) {
            tt.addInfo(
                EnumChatFormatting.GOLD + "Bronze build"
                    + EnumChatFormatting.GRAY
                    + ": Bronze Plated Bricks, one Steam Hatch, 2 L steam per EU, no overclocks, max "
                    + BRONZE_MAX_PARALLEL
                    + " parallels")
                .addInfo(
                    EnumChatFormatting.WHITE + "Steel build"
                        + EnumChatFormatting.GRAY
                        + ": Solid Steel Machine Casing, Energy and Maintenance Hatch, normal overclocks");
        } else if (allowsSteel()) {
            tt.addInfo("Solid Steel Machine Casing only; runs on EU with normal overclocks");
        }
        return tt;
    }

    @Override
    public TieredVariant getTieredVariant() {
        return build == Build.STEEL ? TieredVariant.STEEL : TieredVariant.BRONZE;
    }

    @Override
    public ITexture getCasingTexture() {
        return getBuildCasing().getCasingTexture();
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, ForgeDirection side, ForgeDirection aFacing,
        int colorIndex, boolean aActive, boolean redstoneLevel) {
        IIconContainer[] overlays = getFrontOverlays();
        return gregtech.api.enums.Textures.BlockIcons
            .createTextureWithCasing(this, side, aFacing, aActive, overlays[0], overlays[1], overlays[2], overlays[3]);
    }

    @Override
    public byte getUpdateData() {
        return (byte) casingTier;
    }

    @Override
    public void onValueUpdate(byte aValue) {
        casingTier = aValue;
        if (getFixedBuild() != null) build = getFixedBuild();
        else build = aValue == 1 ? Build.BRONZE : aValue >= 2 ? Build.STEEL : Build.NONE;
    }

    @Override
    protected IAlignmentLimits getInitialAlignmentLimits() {
        // wet machines spill if tipped over
        return (d, r, f) -> d.offsetY == 0 && r.isNotRotated() && f.isNotFlipped();
    }

    @Override
    public String[] getInfoData() {
        ArrayList<String> info = new ArrayList<>(Arrays.asList(super.getInfoData()));
        info.add("Build: " + EnumChatFormatting.YELLOW + build.name() + EnumChatFormatting.RESET);
        info.add("Parallels: " + EnumChatFormatting.YELLOW + getMaxParallel() + EnumChatFormatting.RESET);
        return info.toArray(new String[0]);
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setInteger("moonBuild", build.ordinal());
        aNBT.setInteger("moonCasingTier", casingTier);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        int b = aNBT.getInteger("moonBuild");
        build = b >= 0 && b < Build.values().length ? Build.values()[b] : Build.NONE;
        casingTier = aNBT.hasKey("moonCasingTier") ? aNBT.getInteger("moonCasingTier") : -1;
    }

    /** Power tier the Electrostatic Separator style recipes key on: 0 for steam, else the energy hatch tier. */
    protected int getPowerTier() {
        if (build != Build.STEEL) return 0;
        return Math.max(1, GTUtility.getTier(getMaxInputVoltage()));
    }
}
