package com.gberguy.ecpatches.core.patches;

import com.gberguy.ecpatches.core.MethodPatch;
import com.gberguy.ecpatches.core.PatchId;
import com.gberguy.ecpatches.core.PatchSettings;
import org.objectweb.asm.tree.*;

public final class GhostlyTargetPatch extends MethodPatch {
    private final PatchId[] integrations;
    private final String mobField;
    private final String mobType;

    public GhostlyTargetPatch(String owner, String method, String descriptor, String[] original, String[] fixed,
            String mobField, String mobType, PatchId... integrations) {
        super(integrations[0], owner, method, descriptor, original, fixed);
        this.integrations = integrations;
        this.mobField = mobField;
        this.mobType = mobType;
    }

    @Override
    public boolean enabled(PatchSettings settings) {
        for (PatchId integration : integrations) if (settings.enabled(integration)) return true;
        return false;
    }

    @Override
    protected void edit(MethodNode method, boolean deobfuscated) {
        LabelNode proceed = new LabelNode();
        InsnList check = new InsnList();
        check.add(new VarInsnNode(ALOAD, 0));
        if (mobField != null) {
            check.add(new FieldInsnNode(GETFIELD, className.replace('.', '/'), name(mobField, deobfuscated), "L" + mobType + ";"));
        }
        check.add(new VarInsnNode(ALOAD, 1));
        check.add(new MethodInsnNode(INVOKESTATIC, "com/gberguy/ecpatches/runtime/GhostlyShape", "shouldIgnore", "(Ljava/lang/Object;Ljava/lang/Object;)Z", false));
        check.add(new JumpInsnNode(IFEQ, proceed));
        check.add(new InsnNode(ICONST_0));
        check.add(new InsnNode(IRETURN));
        check.add(proceed);
        check.add(new FrameNode(F_SAME, 0, null, 0, null));
        method.instructions.insert(check);
    }
}
