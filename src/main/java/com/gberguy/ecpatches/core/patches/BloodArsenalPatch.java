package com.gberguy.ecpatches.core.patches;

import com.gberguy.ecpatches.core.MethodPatch;
import com.gberguy.ecpatches.core.PatchId;
import org.objectweb.asm.tree.*;

public final class BloodArsenalPatch extends MethodPatch {
    public BloodArsenalPatch() {
        super(PatchId.BLOOD_ARSENAL, "arcaratus.bloodarsenal.block.IBABlock", "getItemBlock",
                "(Lnet/minecraft/block/Block;)Lnet/minecraft/item/ItemBlock;", "e2691f38800fe108432a2d04cc88904eeed3bef7699cacbf1de7f495e3c099ed", "c327e0413d7502937465439c4c7f91bad8bc870860ba12a2d149254d6c4ffed2");
    }

    @Override
    protected void edit(MethodNode method, boolean deobfuscated) {
        AbstractInsnNode start = method.instructions.getFirst();
        while (start.getOpcode() < 0) {
            start = start.getNext();
        }
        AbstractInsnNode store = start;
        while (!(store instanceof VarInsnNode && store.getOpcode() == ASTORE && ((VarInsnNode) store).var == 2)) {
            store = store.getNext();
        }
        LabelNode ordinary = new LabelNode();
        LabelNode ready = new LabelNode();
        InsnList prefix = new InsnList();
        prefix.add(new VarInsnNode(ALOAD, 1));
        prefix.add(new TypeInsnNode(INSTANCEOF, "arcaratus/bloodarsenal/block/BlockSlate"));
        prefix.add(new JumpInsnNode(IFEQ, ordinary));
        prefix.add(new VarInsnNode(ALOAD, 1));
        prefix.add(new TypeInsnNode(CHECKCAST, "arcaratus/bloodarsenal/block/BlockSlate"));
        prefix.add(new MethodInsnNode(INVOKEVIRTUAL, "WayofTime/bloodmagic/block/base/BlockEnum", "getItem", "()Lnet/minecraft/item/ItemBlock;", false));
        prefix.add(new VarInsnNode(ASTORE, 2));
        prefix.add(new JumpInsnNode(GOTO, ready));
        prefix.add(ordinary);
        prefix.add(new FrameNode(F_SAME, 0, null, 0, null));
        method.instructions.insertBefore(start, prefix);
        InsnList merge = new InsnList();
        merge.add(ready);
        merge.add(new FrameNode(F_APPEND, 1, new Object[]{"net/minecraft/item/ItemBlock"}, 0, null));
        method.instructions.insert(store, merge);
    }
}
