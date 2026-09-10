package org.yonside.moon;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.registry.GameData;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.Materials;
import gregtech.api.enums.VoltageIndex;
import gregtech.api.objects.ItemData;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMapBackend;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTUtility;

/**
 * Queues edits against one RecipeMap, then applies them in a single pass:
 * one scan of getAllRecipes(), one removeRecipes() call, then the additions.
 * <p>
 * Mutation always happens on GTRecipe.copy(). Recipes are never edited in
 * place, because the backend's lookup structures are keyed on inputs.
 */
public class RecipeEditor<B extends RecipeMapBackend> {

    private static final Map<Item, String> MOD_ID_CACHE = new ConcurrentHashMap<>();

    public static String modIdOf(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return null;
        Item item = stack.getItem();
        String cached = MOD_ID_CACHE.get(item);
        if (cached != null) return cached;

        String name = GameData.getItemRegistry()
            .getNameForObject(item); // "modid:itemname"
        String modId = name == null ? "" : name.substring(0, Math.max(0, name.indexOf(':')));
        MOD_ID_CACHE.put(item, modId);
        return modId;
    }

    private final RecipeMap<B> recipeMap;
    private final List<EditBuilder> queued = new ArrayList<>();
    private boolean dryRun = false;

    private RecipeEditor(RecipeMap<B> recipeMap) {
        this.recipeMap = recipeMap;
    }

    private static final Map<RecipeMap<?>, RecipeEditor<?>> INSTANCES = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <B extends RecipeMapBackend> RecipeEditor<B> of(RecipeMap<B> recipeMap) {
        return (RecipeEditor<B>) INSTANCES.computeIfAbsent(recipeMap, r -> new RecipeEditor<>((RecipeMap<B>) r));
    }


    public static void run() {
        INSTANCES.forEach((k, v) -> v.apply());
    }

    /** Log what would change, change nothing. */
    public RecipeEditor<B> dryRun(boolean value) {
        this.dryRun = value;
        return this;
    }

    public EditBuilder edit(String label) {
        return new EditBuilder(label);
    }

    public RecipeEditor<B> apply() {
        if (queued.isEmpty()) return this;

        // one scan, bucketed per edit
        IdentityHashMap<GTRecipe, EditBuilder> claimed = new IdentityHashMap<>();
        List<List<GTRecipe>> buckets = new ArrayList<>(queued.size());
        for (int i = 0; i < queued.size(); i++) buckets.add(new ArrayList<>());

        for (GTRecipe recipe : recipeMap.getAllRecipes()) {
            for (int i = 0; i < queued.size(); i++) {
                EditBuilder edit = queued.get(i);
                if (!edit.test(recipe)) continue;

                EditBuilder prior = claimed.put(recipe, edit);
                if (prior != null) {
                    String msg = "overlap in " + name()
                        + ": '"
                        + edit.label
                        + "' and '"
                        + prior.label
                        + "' both match "
                        + describe(recipe);
                    if (edit.strict || prior.strict) throw new IllegalStateException(msg);
                    Moon.LOG.error(msg);
                    continue;
                }
                buckets.get(i)
                    .add(recipe);
            }
        }

        // validate counts before touching anything
        for (int i = 0; i < queued.size(); i++) {
            queued.get(i)
                .validate(buckets.get(i), name());
        }

        Set<GTRecipe> removals = new LinkedHashSet<>();
        List<GTRecipe> additions = new ArrayList<>();

        for (int i = 0; i < queued.size(); i++) {
            EditBuilder edit = queued.get(i);
            List<GTRecipe> matched = buckets.get(i);
            if (matched.isEmpty()) continue;

            removals.addAll(matched);
            if (edit.transforms.isEmpty()) continue; // removal-only

            for (GTRecipe original : matched) {
                GTRecipe copy = original.copy();
                for (Consumer<GTRecipe> t : edit.transforms) t.accept(copy);
                additions.add(copy);
            }
        }

        if (dryRun) {
            StringBuilder sb = new StringBuilder();
            for (GTRecipe r : additions) {
                sb.append("    -> ");
                sb.append(describe(r));
                sb.append("\n");
            }
            Moon.LOG
                .info("DRY {}: would remove {}, add {} {}", name(), removals.size(), additions.size(), sb.toString());
        } else {
            recipeMap.getBackend()
                .removeRecipes(removals); // single rebuild
            for (GTRecipe r : additions) recipeMap.add(r);
            Moon.LOG.info("{}: removed {}, added {}", name(), removals.size(), additions.size());
        }

        queued.clear();
        return this;
    }

    // ---- builder ------------------------------------------------------

    public class EditBuilder {

        private final String label;
        private final List<Predicate<GTRecipe>> filters = new ArrayList<>();
        private final List<Consumer<GTRecipe>> transforms = new ArrayList<>();
        private int expectMin = 1;
        private int expectMax = Integer.MAX_VALUE;
        private boolean strict = true;

        EditBuilder(String label) {
            this.label = label;
        }

        // -- selectors --

        public EditBuilder outputting(ItemStack stack) {
            return matching(r -> anyStackEquals(r.mOutputs, stack));
        }

        public EditBuilder consuming(ItemStack stack) {
            return matching(r -> anyStackEquals(r.mInputs, stack));
        }

        public EditBuilder usingMaterial(Materials material) {
            return matching(r -> {
                if (r.mInputs == null) return false;
                for (ItemStack in : r.mInputs) if (materialOf(in) == material) return true;
                return false;
            });
        }

        public EditBuilder matching(Predicate<GTRecipe> p) {
            filters.add(p);
            return this;
        }

        public EditBuilder atTier(int tier) {
            return matching(r -> GTUtility.getTier(r.mEUt) == tier);
        }

        // -- assertions --

        public EditBuilder expect(int n) {
            expectMin = n;
            expectMax = n;
            return this;
        }

        public EditBuilder expectAtLeast(int n) {
            expectMin = n;
            return this;
        }

        /** Log instead of throwing on count mismatch or overlap. */
        public EditBuilder lenient() {
            strict = false;
            return this;
        }

        public EditBuilder swapMaterial(Materials from, Materials to) {
            return transform(r -> {
                for (int i = 0; i < r.mInputs.length; i++) {
                    ItemStack in = r.mInputs[i];
                    ItemData data = GTOreDictUnificator.getAssociation(in);
                    if (data == null || data.mPrefix == null) continue;
                    if (data.mMaterial == null || data.mMaterial.mMaterial != from) continue;

                    ItemStack swapped = GTOreDictUnificator.get(data.mPrefix, to, in.stackSize);
                    if (swapped == null) {
                        Moon.LOG.info("{}: no {} for {}, left as-is", label, data.mPrefix, to);
                        continue;
                    }
                    r.mInputs[i] = swapped;
                }
            });
        }

        public EditBuilder swapCircuits() {
            return transform(r -> {});
        }

        public EditBuilder swapInput(ItemStack from, ItemStack to) {
            return transform(r -> {
                for (int i = 0; i < r.mInputs.length; i++) {
                    if (GTUtility.areStacksEqual(r.mInputs[i], from, true)) {
                        r.mInputs[i] = to.copy();
                    }
                }
            });
        }

        public EditBuilder scaleDuration(double factor) {
            return transform(r -> r.mDuration = Math.max(1, (int) (r.mDuration * factor)));
        }

        public EditBuilder setEut(long eut) {
            return transform(r -> r.mEUt = Math.toIntExact(eut));
        }

        private static final long MAX_REASONABLE_DURATION = 20 * 60 * 5; // 5 minutes

        /**
         * Re-tiers a recipe as if it had been authored at the target tier.
         *
         * GT overclocks one tier up as EU/t x4, duration /2, so the inverse is
         * EU/t /4, duration x2. Total energy therefore drops by half per tier
         * dropped, which is correct: overclocking is what wastes energy, so a
         * native lower-tier recipe should cost less than the same work overclocked.
         *
         * The round trip is exact. Drop HV to MV, then run it in an HV machine, and
         * GT overclocks it back to the original EU/t and duration.
         *
         * Only recipes currently at fromTier are touched, so this composes with a
         * broad selector without flattening recipes of other tiers.
         */
        public EditBuilder swapVoltage(int fromTier, int toTier) {
            return swapVoltage(fromTier, toTier, 2.0);
        }

        /**
         * As above with an explicit duration factor per tier dropped.
         *
         * 2.0 native re-tier; energy halves per tier (the default)
         * 4.0 energy neutral; 4x slower per tier
         * 1.0 same duration, quarter energy: a straight buff
         */
        public EditBuilder swapVoltage(int fromTier, int toTier, double durationFactorPerTier) {
            if (fromTier < 0 || fromTier > VoltageIndex.MAX) {
                throw new IllegalArgumentException("fromTier out of range: " + fromTier);
            }
            if (toTier < 0 || toTier > VoltageIndex.MAX) {
                throw new IllegalArgumentException("toTier out of range: " + toTier);
            }
            if (durationFactorPerTier <= 0) {
                throw new IllegalArgumentException("durationFactorPerTier must be positive");
            }

            final int steps = fromTier - toTier;
            final String from = GTValues.VN[fromTier];
            final String to = GTValues.VN[toTier];

            return transform(r -> {
                if (GTUtility.getTier(r.mEUt) != fromTier) return;

                long scaled = scaleDurationFor(r.mDuration, steps, durationFactorPerTier);

                int oldEut = r.mEUt;
                int oldDuration = r.mDuration;

                r.mEUt = (int) GTValues.VP[toTier];
                r.mDuration = (int) Math.max(1L, Math.min(Integer.MAX_VALUE, scaled));

                if (scaled > MAX_REASONABLE_DURATION) {
                    Moon.LOG.error(
                        "{}: {} -> {} gives {}t ({}s) - was {}t @ {}EU/t",
                        label,
                        from,
                        to,
                        scaled,
                        scaled / 20,
                        oldDuration,
                        oldEut);
                }
            });
        }

        /** Powers of two use shifts; anything else falls back to pow. */
        private static long scaleDurationFor(int duration, int steps, double factorPerTier) {
            if (factorPerTier == 2.0) {
                return steps >= 0 ? (long) duration << Math.min(steps, 62) : duration >> Math.min(-steps, 31);
            }
            if (factorPerTier == 4.0) {
                int shift = 2 * Math.abs(steps);
                return steps >= 0 ? (long) duration << Math.min(shift, 62) : duration >> Math.min(shift, 31);
            }
            if (factorPerTier == 1.0) {
                return duration;
            }
            return Math.round(duration * Math.pow(factorPerTier, steps));
        }

        /** Selector counterpart, so expect() counts only the recipes swapVoltage will touch. */

        public EditBuilder transform(Consumer<GTRecipe> t) {
            transforms.add(t);
            return this;
        }

        public RecipeEditor<B> queue() {
            queued.add(this);
            return RecipeEditor.this;
        }

        public RecipeEditor<B> queueRemoval() {
            transforms.clear();
            queued.add(this);
            return RecipeEditor.this;
        }

        // -- internals --

        boolean test(GTRecipe r) {
            for (Predicate<GTRecipe> f : filters) if (!f.test(r)) return false;
            return true;
        }

        void validate(List<GTRecipe> matched, String mapName) {
            if (matched.size() >= expectMin && matched.size() <= expectMax) return;
            String msg = label + ": matched "
                + matched.size()
                + ", expected "
                + (expectMin == expectMax ? String.valueOf(expectMin) : expectMin + ".." + expectMax)
                + " in "
                + mapName;
            if (strict) throw new IllegalStateException(msg);
            Moon.LOG.info(msg);
            matched.clear();
        }
    }

    // ---- helpers ------------------------------------------------------

    private String name() {
        return recipeMap.toString();
    }

    private static boolean anyStackEquals(ItemStack[] arr, ItemStack target) {
        if (arr == null) return false;
        for (ItemStack s : arr) if (s != null && GTUtility.areStacksEqual(s, target, true)) return true;
        return false;
    }

    private static Materials materialOf(ItemStack stack) {
        if (stack == null) return null;
        ItemData data = GTOreDictUnificator.getAssociation(stack);
        if (data == null || data.mMaterial == null) return null;
        return data.mMaterial.mMaterial;
    }

    private static String describe(GTRecipe r) {
        StringBuilder sb = new StringBuilder();
        for (ItemStack in : r.mInputs) {
            sb.append(in.stackSize)
                .append("x")
                .append(in.getDisplayName())
                .append(", ");
        }
        sb.append("| ")
            .append(r.mDuration)
            .append("t @ ")
            .append(r.mEUt)
            .append("EU/t");
        return sb.toString();
    }
}
