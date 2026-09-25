package com.gberguy.ecpatches.core.patches;

import com.gberguy.ecpatches.core.MethodPatch;
import com.gberguy.ecpatches.core.PatchId;
import org.objectweb.asm.tree.*;

public final class RootsPatch extends MethodPatch {
    public RootsPatch() {
        super(PatchId.ROOTS, "epicsquid.roots.util.StateUtil$StateMatcher", "calculateState",
                "()Lnet/minecraft/block/state/IBlockState;", "635fb80bc07ab504f9acc3b7f25223a3a5169f5ba88bc495435eb26ce48ec52c", "2e9af7c12b910887b7a3b469660648866f51e0ca4d9b57b84f39cfc152445e97");
    }

    @Override
    protected void edit(MethodNode method, boolean deobfuscated) {
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (instruction instanceof JumpInsnNode && instruction.getOpcode() == IFNULL) {
                InsnList check = new InsnList();
                check.add(new VarInsnNode(ALOAD, 2));
                check.add(new FieldInsnNode(GETSTATIC, "net/minecraft/init/Blocks", name("field_150350_a", deobfuscated), "Lnet/minecraft/block/Block;"));
                check.add(new JumpInsnNode(IF_ACMPEQ, ((JumpInsnNode) instruction).label));
                method.instructions.insert(instruction, check);
                return;
            }
        }
        throw new IllegalStateException("Missing Roots block lookup guard");
    }
}
