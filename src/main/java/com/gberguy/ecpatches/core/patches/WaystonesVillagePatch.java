package com.gberguy.ecpatches.core.patches;

import com.gberguy.ecpatches.core.MethodPatch;
import com.gberguy.ecpatches.core.PatchId;
import org.objectweb.asm.tree.*;

public final class WaystonesVillagePatch extends MethodPatch {
    public WaystonesVillagePatch() {
        super(PatchId.WAYSTONES_VILLAGE, "net.blay09.mods.waystones.worldgen.VillageWaystoneCreationHandler",
                "getVillagePieceWeight", "(Ljava/util/Random;I)Lnet/minecraft/world/gen/structure/StructureVillagePieces$PieceWeight;",
                "30e46221f20bb26fa91ac3b863d8fcf27127195b99af1ad28f1f899d623362d8",
                "90e324b94770afe183aa051266be67dd2b4df22dbfe5e74d0ef1c68b4c248f69");
    }

    @Override
    protected void edit(MethodNode method, boolean deobfuscated) {
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (instruction.getOpcode() == ICONST_3 && next(instruction) != null
                    && next(instruction).getOpcode() == ICONST_1) {
                InsnList replacement = new InsnList();
                replacement.add(new MethodInsnNode(INVOKESTATIC, "com/gberguy/ecpatches/core/PatchSettings",
                        "current", "()Lcom/gberguy/ecpatches/core/PatchSettings;", false));
                replacement.add(new MethodInsnNode(INVOKEVIRTUAL, "com/gberguy/ecpatches/core/PatchSettings",
                        "villageWaystoneWeight", "()I", false));
                method.instructions.insertBefore(instruction, replacement);
                method.instructions.remove(instruction);
                return;
            }
        }
        throw new IllegalStateException("Missing Waystones village piece weight");
    }
}
