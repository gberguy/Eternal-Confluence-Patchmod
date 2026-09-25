package com.gberguy.ecpatches.core;

import java.util.Arrays;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public abstract class MethodPatch implements Opcodes {
    public enum Result { APPLIED, ALREADY_PRESENT, UNSUPPORTED }

    public final PatchId id;
    public final String className;
    public final String methodName;
    public final String descriptor;
    private final String originalFingerprint;
    private final String[] fixedFingerprints;

    protected MethodPatch(PatchId id, String className, String methodName, String descriptor,
            String originalFingerprint, String... fixedFingerprints) {
        this.id = id;
        this.className = className;
        this.methodName = methodName;
        this.descriptor = descriptor;
        this.originalFingerprint = originalFingerprint;
        this.fixedFingerprints = fixedFingerprints;
    }

    public final Result apply(ClassNode owner) {
        if (!className.replace('.', '/').equals(owner.name)) {
            return Result.UNSUPPORTED;
        }
        MethodNode method = null;
        for (Object value : owner.methods) {
            MethodNode candidate = (MethodNode) value;
            if (methodName.equals(MinecraftNames.canonical(candidate.name)) && descriptor.equals(candidate.desc)) {
                if (method != null) {
                    return Result.UNSUPPORTED;
                }
                method = candidate;
            }
        }
        if (method == null) {
            return Result.UNSUPPORTED;
        }
        String fingerprint = MethodFingerprint.of(method);
        if (Arrays.asList(fixedFingerprints).contains(fingerprint)) {
            return Result.ALREADY_PRESENT;
        }
        if (!originalFingerprint.equals(fingerprint)) {
            return Result.UNSUPPORTED;
        }
        edit(method, MinecraftNames.isDeobfuscated(method));
        if (!Arrays.asList(fixedFingerprints).contains(MethodFingerprint.of(method))) {
            throw new IllegalStateException("Unexpected patch output for " + id.key);
        }
        return Result.APPLIED;
    }

    protected abstract void edit(MethodNode method, boolean deobfuscated);

    protected static AbstractInsnNode next(AbstractInsnNode instruction) {
        AbstractInsnNode result = instruction.getNext();
        while (result != null && result.getOpcode() < 0) {
            result = result.getNext();
        }
        return result;
    }

    protected static String name(String srg, boolean deobfuscated) {
        return MinecraftNames.runtime(srg, deobfuscated);
    }
}
