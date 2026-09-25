package com.gberguy.ecpatches.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class MinecraftNames {
    private static final Properties MCP = new Properties();
    private static final Map<String, String> SRG = new HashMap<>();

    static {
        try (InputStream input = MinecraftNames.class.getResourceAsStream("/ecpatches/minecraft-names.properties")) {
            if (input == null) {
                throw new IllegalStateException("Missing Minecraft name mappings");
            }
            MCP.load(input);
            for (String name : MCP.stringPropertyNames()) {
                SRG.put(MCP.getProperty(name), name);
            }
        } catch (IOException error) {
            throw new IllegalStateException(error);
        }
    }

    private MinecraftNames() {
    }

    public static String canonical(String name) {
        String mapped = SRG.get(name);
        return mapped == null ? name : mapped;
    }

    public static String runtime(String srg, boolean deobfuscated) {
        return deobfuscated ? MCP.getProperty(srg, srg) : srg;
    }

    public static boolean isDeobfuscated(MethodNode method) {
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            String name = instruction instanceof MethodInsnNode ? ((MethodInsnNode) instruction).name
                    : instruction instanceof FieldInsnNode ? ((FieldInsnNode) instruction).name : null;
            if (name != null && SRG.containsKey(name)) {
                return true;
            }
        }
        return false;
    }
}
