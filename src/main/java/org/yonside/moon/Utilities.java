package org.yonside.moon;

import static gregtech.api.util.GTRecipeBuilder.PANIC_MODE_NULL;

import java.util.*;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import org.jetbrains.annotations.NotNull;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.GameRegistry;
import gregtech.api.GregTechAPI;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;

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

    public static final class Range {

        public final int start;
        public final int end; // inclusive
        public final String owner; // null for free ranges

        Range(int start, int end, String owner) {
            this.start = start;
            this.end = end;
            this.owner = owner;
        }

        public int size() {
            return end - start + 1;
        }

        @Override
        public String toString() {
            String span = (start == end) ? String.format("%5d        ", start) : String.format("%5d - %5d", start, end);
            return String.format("%s  %6d  %s", span, size(), owner == null ? "(free)" : owner);
        }
    }

    public static final class Report {

        public final List<Range> used = new ArrayList<Range>();
        public final List<Range> free = new ArrayList<Range>();
        public final Map<String, Integer> countByOwner = new TreeMap<String, Integer>();
        public int arrayLength;
        public int usedCount;
    }

    public static Report scan() {
        final IMetaTileEntity[] table = GregTechAPI.METATILEENTITIES;
        final Report report = new Report();
        report.arrayLength = table.length;

        final String[] owners = new String[table.length];
        for (int id = 0; id < table.length; id++) {
            IMetaTileEntity mte = table[id];
            if (mte == null) continue;

            String owner = ownerOf(mte);
            owners[id] = owner;
            report.usedCount++;

            report.countByOwner.compute(owner, (k, prev) -> prev == null ? 1 : prev + 1);
        }

        // Collapse into contiguous runs that share an owner (used) or are null (free).
        int runStart = 0;
        for (int id = 1; id <= table.length; id++) {
            boolean boundary = (id == table.length) || !sameBucket(owners[id - 1], owners[id]);
            if (!boundary) continue;

            String owner = owners[runStart];
            Range range = new Range(runStart, id - 1, owner);
            if (owner == null) report.free.add(range);
            else report.used.add(range);
            runStart = id;
        }

        Collections.sort(report.free, (a, b) -> b.size() - a.size());

        return report;
    }

    private static boolean sameBucket(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    private static String ownerOf(IMetaTileEntity mte) {
        Class<?> cls = mte.getClass();
        String pkg = cls.getName();

        int cut = pkg.lastIndexOf('.');
        pkg = (cut > 0) ? pkg.substring(0, cut) : pkg;

        String label = shortPackage(pkg);
        String modId = modIdForPackage(pkg);
        return (modId == null) ? label : modId + " [" + label + "]";
    }

    private static String shortPackage(String pkg) {
        String[] parts = pkg.split("\\.");
        int keep = Math.min(parts.length, 3);
        // Skip generic TLD prefixes so "com.github.foo" reports as "foo".
        int from = 0;
        if (parts.length > 2 && (parts[0].equals("com") || parts[0].equals("net") || parts[0].equals("org"))) {
            from = (parts.length > 3 && (parts[1].equals("github") || parts[1].equals("gitlab"))) ? 2 : 1;
            keep = Math.min(parts.length - from, 2);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < from + keep; i++) {
            if (sb.length() > 0) sb.append('.');
            sb.append(parts[i]);
        }
        return sb.toString();
    }

    private static String modIdForPackage(String pkg) {
        for (ModContainer mc : Loader.instance()
            .getActiveModList()) {
            String owned = mc.getMod() == null ? null
                : mc.getMod()
                    .getClass()
                    .getName();
            if (owned == null) continue;
            int cut = owned.lastIndexOf('.');
            if (cut <= 0) continue;
            String modPkg = owned.substring(0, cut);
            // Trim the mod class's package back to its root and test for a prefix match.
            String root = rootOf(modPkg);
            if (root.length() > 0 && pkg.startsWith(root)) return mc.getModId();
        }
        return null;
    }

    private static String rootOf(String pkg) {
        String[] parts = pkg.split("\\.");
        int keep = Math.min(parts.length, 3);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keep; i++) {
            if (sb.length() > 0) sb.append('.');
            sb.append(parts[i]);
        }
        return sb.toString();
    }

    public static List<String> format(Report report, int minFreeGap) {
        List<String> out = new ArrayList<String>();
        out.add("=== GregTech MetaTileEntity ID map ===");
        out.add(
            String.format(
                "array length %d, claimed %d, free %d",
                report.arrayLength,
                report.usedCount,
                report.arrayLength - report.usedCount));
        out.add("");

        out.add("--- claimed ranges ---");
        out.add("   id range      count  owner");
        for (Range r : report.used) out.add(r.toString());
        out.add("");

        out.add("--- free gaps (>= " + minFreeGap + ", largest first) ---");
        out.add("   id range      count");
        for (Range r : report.free) {
            if (r.size() >= minFreeGap) out.add(r.toString());
        }
        out.add("");

        out.add("--- totals by owner ---");
        for (Map.Entry<String, Integer> e : report.countByOwner.entrySet()) {
            out.add(String.format("%6d  %s", e.getValue(), e.getKey()));
        }
        return out;
    }

    public static void dumpMTEs(int minFreeGap) {
        for (String line : format(scan(), minFreeGap)) System.out.println("[MetaIdDump] " + line);
    }

}
