package com.gberguy.ecpatches.core.patches;

import com.gberguy.ecpatches.core.MethodPatch;
import com.gberguy.ecpatches.core.PatchId;
import org.objectweb.asm.tree.*;

public final class LycanitesMeleePatch extends MethodPatch {
    public LycanitesMeleePatch() {
        super(PatchId.LYCANITES_MELEE, "com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal", "func_75246_d",
                "()V", "0fcbdfe2c5685df1303cf5aeedd35bdd46cc954039fa02c07bc3a4f63b9dfebd", "184bdd5cd9802ae1c372d2d55377349d2c18f6955e522e723328aa91a7722f80");
    }

    @Override
    protected void edit(MethodNode method, boolean deobfuscated) {
        String owner = "com/lycanitesmobs/core/entity/goals/actions/AttackMeleeGoal";
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode) || !"getMeleeAttackRange".equals(((MethodInsnNode) instruction).name)) {
                continue;
            }
            AbstractInsnNode comparison = next(instruction);
            if (comparison == null || comparison.getOpcode() != DCMPG) {
                continue;
            }
            AbstractInsnNode branch = next(comparison);
            if (!(branch instanceof JumpInsnNode) || branch.getOpcode() != IFGT) {
                throw new IllegalStateException("Unexpected melee range branch");
            }
            InsnList check = new InsnList();
            check.add(new VarInsnNode(ALOAD, 0));
            check.add(new FieldInsnNode(GETFIELD, owner, "host", "Lcom/lycanitesmobs/core/entity/BaseCreatureEntity;"));
            check.add(new MethodInsnNode(INVOKEVIRTUAL, "com/lycanitesmobs/core/entity/BaseCreatureEntity", name("func_70635_at", deobfuscated), "()Lnet/minecraft/entity/ai/EntitySenses;", false));
            check.add(new VarInsnNode(ALOAD, 0));
            check.add(new FieldInsnNode(GETFIELD, owner, "attackTarget", "Lnet/minecraft/entity/EntityLivingBase;"));
            check.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/entity/ai/EntitySenses", name("func_75522_a", deobfuscated), "(Lnet/minecraft/entity/Entity;)Z", false));
            check.add(new JumpInsnNode(IFEQ, ((JumpInsnNode) branch).label));
            method.instructions.insert(branch, check);
            return;
        }
        throw new IllegalStateException("Missing melee range calculation");
    }
}
