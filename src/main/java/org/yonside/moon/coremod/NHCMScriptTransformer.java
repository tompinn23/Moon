package org.yonside.moon.coremod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.yonside.moon.config.CoreConfig;

/**
 * Rewrites String constants (LDC) and static field references (GETSTATIC)
 * inside regions of a target class, per the rules in PatchRules.
 *
 * Only constants change, never stack shape, so frames stay valid and the
 * writer can reuse the original constant pool.
 */
public class NHCMScriptTransformer implements IClassTransformer {

    private static volatile Map<String, List<CoreConfig.Rule>> byClass = Collections.emptyMap();

    static void setRules(Map<String, List<CoreConfig.Rule>> rules) {
        byClass = rules;
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return null;
        List<CoreConfig.Rule> applicable = byClass.get(transformedName);
        if (applicable == null) return basicClass;

        ClassReader cr = new ClassReader(basicClass);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        boolean changed = false;
        for (CoreConfig.Rule rule : applicable) {
            changed |= apply(rule, cn);
        }
        if (!changed) return basicClass;

        ClassWriter cw = new ClassWriter(cr, 0); // constants only: no frame recomputation
        cn.accept(cw);
        return cw.toByteArray();
    }

    // ---- rule application ---------------------------------------------

    private static boolean apply(CoreConfig.Rule rule, ClassNode cn) {
        int swaps = 0;
        int regionsTouched = 0;

        for (MethodNode mn : cn.methods) {
            if (!rule.methods.isEmpty() && !rule.methods.contains(mn.name)) continue;

            AbstractInsnNode[] insns = mn.instructions.toArray();
            for (int[] region : regionsOf(rule, insns)) {
                List<AbstractInsnNode> pending = new ArrayList<>();
                boolean marked = rule.requireAny.isEmpty();

                for (int i = region[0]; i < region[1]; i++) {
                    AbstractInsnNode insn = insns[i];

                    if (insn instanceof LdcInsnNode) {
                        Object cst = ((LdcInsnNode) insn).cst;
                        if (!(cst instanceof String)) continue;
                        if (!marked && rule.requireAny.contains(cst)) marked = true;
                        if (rule.stringSwaps.containsKey(cst)) pending.add(insn);

                    } else if (insn.getOpcode() == Opcodes.GETSTATIC) {
                        FieldInsnNode f = (FieldInsnNode) insn;
                        if (rule.fieldSwaps.containsKey(new CoreConfig.FieldRef(f.owner, f.name))) pending.add(insn);
                    }
                }

                if (!marked || pending.isEmpty()) continue;

                for (AbstractInsnNode insn : pending) {
                    rewrite(rule, cn, mn, insn);
                    swaps++;
                }
                regionsTouched++;
            }
        }

        String complaint = rule.checkCount(swaps);
        if (complaint != null) {
            String message = "[patch] " + rule + ": " + complaint + " - the target has moved under the patch";
            if (CoreConfig.failFast()) throw new IllegalStateException(message);
            MoonCore.LOG.error(message);
            // note: rewrites already applied to this ClassNode are kept
        }

        MoonCore.LOG.info("{}: {} swap(s) across {} region(s)", rule, swaps, regionsTouched);
        return swaps > 0;
    }

    private static void rewrite(CoreConfig.Rule rule, ClassNode cn, MethodNode mn, AbstractInsnNode insn) {
        if (insn instanceof LdcInsnNode) {
            LdcInsnNode ldc = (LdcInsnNode) insn;
            String from = (String) ldc.cst;
            ldc.cst = rule.stringSwaps.get(from);
            if (CoreConfig.verbose()) {
                MoonCore.LOG.info("  {}.{}: \"{}\" -> \"{}\"", cn.name, mn.name, from, ldc.cst);
            }
        } else {
            FieldInsnNode f = (FieldInsnNode) insn;
            CoreConfig.FieldRef to = rule.fieldSwaps.get(new CoreConfig.FieldRef(f.owner, f.name));
            if (CoreConfig.verbose()) {
                MoonCore.LOG.info("  {}.{}: {}.{} -> {}", cn.name, mn.name, f.owner, f.name, to);
            }
            f.owner = to.owner;
            f.name = to.name;
        }
    }

    // ---- regions ------------------------------------------------------

    /**
     * Half-open [start, end) index pairs. With no sites configured the whole
     * method is one region; otherwise each region ends at a delimiting call,
     * which for a varargs registration call covers exactly that call's
     * arguments, since evaluation is left to right.
     */
    private static List<int[]> regionsOf(CoreConfig.Rule rule, AbstractInsnNode[] insns) {
        List<int[]> regions = new ArrayList<>();
        if (rule.sites.isEmpty()) {
            regions.add(new int[] { 0, insns.length });
            return regions;
        }
        int start = 0;
        for (int i = 0; i < insns.length; i++) {
            if (!isSite(rule, insns[i])) continue;
            regions.add(new int[] { start, i });
            start = i + 1;
        }
        return regions;
    }

    private static boolean isSite(CoreConfig.Rule rule, AbstractInsnNode insn) {
        int op = insn.getOpcode();
        if (op != Opcodes.INVOKEVIRTUAL && op != Opcodes.INVOKEINTERFACE && op != Opcodes.INVOKESTATIC) {
            return false;
        }
        return rule.sites.contains(((MethodInsnNode) insn).name);
    }
}
