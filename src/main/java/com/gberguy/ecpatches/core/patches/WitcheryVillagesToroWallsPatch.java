package com.gberguy.ecpatches.core.patches;

import com.gberguy.ecpatches.core.MethodPatch;
import com.gberguy.ecpatches.core.PatchId;
import org.objectweb.asm.tree.*;

public final class WitcheryVillagesToroWallsPatch extends MethodPatch {
    public WitcheryVillagesToroWallsPatch() {
        super(PatchId.WITCHERY_VILLAGES_TORO_WALLS, "com.witcherywalls.block.TileEntityVillageWallGen",
                "setStructure", "(Ljava/util/List;Lnet/minecraft/world/gen/structure/StructureVillagePieces$Start;)V",
                "ba2a27cbcff5391dc2dc5b9a85987a8fbdf80ffd73c1a2d0aa6458918d55a91f",
                "996b2878f5c9cc06936f3863979900145684e45632e0553b44ec939936aa58b3");
    }

    @Override
    protected void edit(MethodNode method, boolean deobfuscated) {
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (instruction.getOpcode() == RETURN) {
                InsnList addition = new InsnList();
                addition.add(new VarInsnNode(ALOAD, 0));
                addition.add(new FieldInsnNode(GETFIELD, "com/witcherywalls/block/TileEntityVillageWallGen",
                        "pathBounds", "Ljava/util/List;"));
                addition.add(new VarInsnNode(ALOAD, 1));
                addition.add(new MethodInsnNode(INVOKESTATIC, "com/gberguy/ecpatches/runtime/WitcheryVillagesToroWallBounds",
                        "extend", "(Ljava/util/List;Ljava/util/List;)V", false));
                method.instructions.insertBefore(instruction, addition);
                return;
            }
        }
        throw new IllegalStateException("Missing Witchery Villages setStructure return");
    }
}
