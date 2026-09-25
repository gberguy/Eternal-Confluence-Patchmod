package com.gberguy.ecpatches.core;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.IdentityHashMap;
import java.util.Map;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public final class MethodFingerprint {
    private MethodFingerprint() {
    }

    public static String of(MethodNode method) {
        StringBuilder text = new StringBuilder();
        Map<LabelNode, Integer> labels = new IdentityHashMap<>();
        int index = 0;
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (instruction instanceof LabelNode) {
                labels.put((LabelNode) instruction, index);
            }
            if (instruction.getOpcode() >= 0) {
                index++;
            }
        }
        append(text, method.access, MinecraftNames.canonical(method.name), method.desc);
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (instruction.getOpcode() < 0) {
                continue;
            }
            append(text, instruction.getOpcode());
            if (instruction instanceof MethodInsnNode) {
                MethodInsnNode value = (MethodInsnNode) instruction;
                append(text, value.owner, MinecraftNames.canonical(value.name), value.desc, value.itf);
            } else if (instruction instanceof FieldInsnNode) {
                FieldInsnNode value = (FieldInsnNode) instruction;
                append(text, value.owner, MinecraftNames.canonical(value.name), value.desc);
            } else if (instruction instanceof VarInsnNode) {
                append(text, ((VarInsnNode) instruction).var);
            } else if (instruction instanceof TypeInsnNode) {
                append(text, ((TypeInsnNode) instruction).desc);
            } else if (instruction instanceof IntInsnNode) {
                append(text, ((IntInsnNode) instruction).operand);
            } else if (instruction instanceof JumpInsnNode) {
                append(text, labels.get(((JumpInsnNode) instruction).label));
            } else if (instruction instanceof LdcInsnNode) {
                constant(text, ((LdcInsnNode) instruction).cst);
            } else if (instruction instanceof IincInsnNode) {
                IincInsnNode value = (IincInsnNode) instruction;
                append(text, value.var, value.incr);
            } else if (instruction instanceof TableSwitchInsnNode) {
                TableSwitchInsnNode value = (TableSwitchInsnNode) instruction;
                append(text, value.min, value.max, labels.get(value.dflt));
                for (Object label : value.labels) {
                    append(text, labels.get(label));
                }
            } else if (instruction instanceof LookupSwitchInsnNode) {
                LookupSwitchInsnNode value = (LookupSwitchInsnNode) instruction;
                append(text, labels.get(value.dflt));
                for (int i = 0; i < value.keys.size(); i++) {
                    append(text, value.keys.get(i), labels.get(value.labels.get(i)));
                }
            } else if (instruction instanceof MultiANewArrayInsnNode) {
                MultiANewArrayInsnNode value = (MultiANewArrayInsnNode) instruction;
                append(text, value.desc, value.dims);
            } else if (instruction instanceof InvokeDynamicInsnNode) {
                InvokeDynamicInsnNode value = (InvokeDynamicInsnNode) instruction;
                append(text, value.name, value.desc);
                constant(text, value.bsm);
                for (Object arg : value.bsmArgs) {
                    constant(text, arg);
                }
            }
        }
        for (Object item : method.tryCatchBlocks) {
            TryCatchBlockNode block = (TryCatchBlockNode) item;
            append(text, labels.get(block.start), labels.get(block.end), labels.get(block.handler), block.type);
        }
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(text.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte value : hash) {
                result.append(String.format("%02x", value & 255));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException(error);
        }
    }

    private static void constant(StringBuilder text, Object value) {
        if (value instanceof Handle) {
            Handle handle = (Handle) value;
            append(text, "handle", handle.getTag(), handle.getOwner(), MinecraftNames.canonical(handle.getName()), handle.getDesc(), handle.isInterface());
        } else if (value instanceof Type) {
            append(text, "type", ((Type) value).getDescriptor());
        } else {
            append(text, value.getClass().getName(), value);
        }
    }

    private static void append(StringBuilder text, Object... values) {
        for (Object value : values) {
            String item = String.valueOf(value);
            text.append(item.length()).append(':').append(item).append(';');
        }
        text.append('\n');
    }
}
