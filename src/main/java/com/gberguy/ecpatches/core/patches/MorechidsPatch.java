package com.gberguy.ecpatches.core.patches;

import com.gberguy.ecpatches.core.MethodPatch;
import com.gberguy.ecpatches.core.PatchId;
import org.objectweb.asm.tree.*;

public final class MorechidsPatch extends MethodPatch {
    public MorechidsPatch() {
        super(PatchId.MORECHIDS, "com.towboat.morechids.asm.MorechidClassBuilder", "generateMorechid",
                "(Ljava/lang/String;)Ljava/lang/Class;", "7cc4f3d9cb729510fd83058b82d2e900f5f84b8c6fd6e937215dfaa4824e8ea7", "e5966c6a42dfd8fd0772adea8801e0a1ac6f4935b81700c4bd2af8b757d157b8");
    }

    @Override
    protected void edit(MethodNode method, boolean deobfuscated) {
        int replaced = 0;
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (instruction instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (call.getOpcode() == INVOKESTATIC && "org/objectweb/asm/Type".equals(call.owner)
                        && "getType".equals(call.name) && "(Ljava/lang/String;)Lorg/objectweb/asm/Type;".equals(call.desc)) {
                    call.name = "getObjectType";
                    replaced++;
                }
            }
        }
        if (replaced != 2) {
            throw new IllegalStateException("Expected two MoreChids type constructions");
        }
    }
}
