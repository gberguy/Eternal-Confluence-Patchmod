package com.gberguy.ecpatches.core.patches;

import com.gberguy.ecpatches.core.MethodPatch;
import com.gberguy.ecpatches.core.PatchId;
import org.objectweb.asm.tree.*;

public final class LycanitesGhostPatch extends MethodPatch {
    public LycanitesGhostPatch() {
        super(PatchId.LYCANITES_GHOST, "com.lycanitesmobs.core.entity.goals.targeting.TargetingGoal", "isEntityTargetable",
                "(Lnet/minecraft/entity/EntityLivingBase;Z)Z", "93932642abcffe6019608fb71f1ddbc41615ccddeed5e984dc04cdb1b2f92cf0", "362d6e205e654258acd167271c5f8aa5be98ca18c153638971b3674f38a5f3ee");
    }

    @Override
    protected void edit(MethodNode method, boolean deobfuscated) {
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (!(instruction instanceof JumpInsnNode) || instruction.getOpcode() != IF_ACMPNE) {
                continue;
            }
            AbstractInsnNode anchor = next(((JumpInsnNode) instruction).label);
            LabelNode proceed = new LabelNode();
            InsnList check = new InsnList();
            check.add(new VarInsnNode(ALOAD, 1));
            check.add(new TypeInsnNode(INSTANCEOF, "net/minecraft/entity/player/EntityPlayer"));
            check.add(new JumpInsnNode(IFEQ, proceed));
            check.add(new TypeInsnNode(NEW, "net/minecraft/util/ResourceLocation"));
            check.add(new InsnNode(DUP));
            check.add(new LdcInsnNode("tombstone"));
            check.add(new LdcInsnNode("ghostly_shape"));
            check.add(new MethodInsnNode(INVOKESPECIAL, "net/minecraft/util/ResourceLocation", "<init>", "(Ljava/lang/String;Ljava/lang/String;)V", false));
            check.add(new FieldInsnNode(GETSTATIC, "net/minecraftforge/fml/common/registry/ForgeRegistries", "POTIONS", "Lnet/minecraftforge/registries/IForgeRegistry;"));
            check.add(new InsnNode(SWAP));
            check.add(new MethodInsnNode(INVOKEINTERFACE, "net/minecraftforge/registries/IForgeRegistry", "getValue", "(Lnet/minecraft/util/ResourceLocation;)Lnet/minecraftforge/registries/IForgeRegistryEntry;", true));
            check.add(new TypeInsnNode(CHECKCAST, "net/minecraft/potion/Potion"));
            check.add(new VarInsnNode(ASTORE, 3));
            check.add(new VarInsnNode(ALOAD, 3));
            check.add(new JumpInsnNode(IFNULL, proceed));
            check.add(new VarInsnNode(ALOAD, 1));
            check.add(new VarInsnNode(ALOAD, 3));
            check.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/entity/EntityLivingBase", name("func_70644_a", deobfuscated), "(Lnet/minecraft/potion/Potion;)Z", false));
            check.add(new JumpInsnNode(IFEQ, proceed));
            check.add(new InsnNode(ICONST_0));
            check.add(new InsnNode(IRETURN));
            check.add(proceed);
            check.add(new FrameNode(F_SAME, 0, null, 0, null));
            method.instructions.insertBefore(anchor, check);
            method.maxLocals = Math.max(method.maxLocals, 4);
            return;
        }
        throw new IllegalStateException("Missing target identity check");
    }
}
