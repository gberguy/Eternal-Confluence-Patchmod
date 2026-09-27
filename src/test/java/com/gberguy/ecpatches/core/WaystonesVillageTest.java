package com.gberguy.ecpatches.core;

import com.gberguy.ecpatches.core.patches.WaystonesVillagePatch;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.jar.JarFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.CheckClassAdapter;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class WaystonesVillageTest implements Opcodes {
    private static final String HANDLER = "net.blay09.mods.waystones.worldgen.VillageWaystoneCreationHandler";
    private static final String COMPONENT = "net/blay09/mods/waystones/worldgen/ComponentVillageWaystone";
    private static final String PIECE = "net/minecraft/world/gen/structure/StructureVillagePieces$PieceWeight";
    private static final String CONFIG = "net/blay09/mods/waystones/WaystoneConfig";
    private static final String WORLD_GEN = CONFIG + "$WorldGen";
    private static final String SETTINGS = "com.gberguy.ecpatches.core.PatchSettings";
    @TempDir Path directory;
    private byte[] original;

    @BeforeEach
    void loadOriginal() throws Exception {
        String path = System.getProperty("ecpatches.waystonesJar");
        assumeTrue(path != null, "Waystones fixture not supplied");
        try (JarFile jar = new JarFile(path);
             InputStream input = jar.getInputStream(jar.getJarEntry(HANDLER.replace('.', '/') + ".class"))) {
            original = read(input);
        }
    }

    private static byte[] read(InputStream input) throws Exception {
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int length;
        while ((length = input.read(buffer)) != -1) output.write(buffer, 0, length);
        return output.toByteArray();
    }

    private PatchSettings settings(String contents) throws Exception {
        Path file = directory.resolve("config/eternalconfluencetweaks.cfg");
        Files.createDirectories(file.getParent());
        Files.write(file, contents.getBytes(StandardCharsets.UTF_8));
        return PatchSettings.read(file.toFile());
    }

    private static ClassNode node(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        return node;
    }

    private static byte[] bytes(ClassNode node) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }

    private static MethodNode target(ClassNode node) {
        for (Object value : node.methods) {
            MethodNode method = (MethodNode) value;
            if (method.name.equals("getVillagePieceWeight")) return method;
        }
        throw new AssertionError("Missing village piece method");
    }

    private static Map<String, String> otherMethods(ClassNode node) {
        Map<String, String> result = new LinkedHashMap<>();
        for (Object value : node.methods) {
            MethodNode method = (MethodNode) value;
            if (!method.name.equals("getVillagePieceWeight")) {
                result.put(method.name + method.desc, MethodFingerprint.of(method));
            }
        }
        return result;
    }

    @Test
    void appliesOnceAndPreservesOtherMethods() throws Exception {
        PatchTransformer transformer = new PatchTransformer(settings(""));
        byte[] patched = transformer.transform(HANDLER, HANDLER, original);
        assertNotSame(original, patched);
        new ClassReader(patched).accept(new CheckClassAdapter(new ClassWriter(0), true), 0);
        assertEquals(otherMethods(node(original)), otherMethods(node(patched)));
        assertSame(patched, transformer.transform(HANDLER, HANDLER, patched));
        assertEquals(MethodPatch.Result.ALREADY_PRESENT, new WaystonesVillagePatch().apply(node(patched)));
        assertArrayEquals(new int[]{100, 1}, execute(patched, 1.0f, 0.75f));
        assertArrayEquals(new int[]{0, 0}, execute(patched, 0.0f, 0.75f));
        assertArrayEquals(new int[]{100, 1}, execute(patched, 0.5f, 0.25f));
        assertArrayEquals(new int[]{0, 0}, execute(patched, 0.5f, 0.75f));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 100, 250, 1000000})
    void releasedSettingsSupplyConfiguredWeight(int weight) throws Exception {
        PatchTransformer transformer = new PatchTransformer(settings("villageWaystoneWeight=" + weight + "\n"));
        byte[] patched = transformer.transform(HANDLER, HANDLER, original);
        assertNotSame(original, patched);
        assertArrayEquals(new int[]{weight, 1}, execute(patched, 1.0f, 0.75f));
        assertArrayEquals(new int[]{0, 0}, execute(patched, 0.0f, 0.75f));
    }

    @ParameterizedTest
    @ValueSource(strings = {"waystonesVillageWeight=false\nvillageWaystoneWeight=250\n", "villageWaystoneWeight=invalid\n", "villageWaystoneWeight=0\n"})
    void disabledAndInvalidSettingsKeepOriginalBehavior(String options) throws Exception {
        PatchTransformer transformer = new PatchTransformer(settings(options));
        byte[] output = transformer.transform(HANDLER, HANDLER, original);
        assertSame(original, output);
        assertArrayEquals(new int[]{3, 1}, execute(output, 1.0f, 0.75f));
    }

    @Test
    void rejectsChangedAndMissingTargets() throws Exception {
        PatchTransformer transformer = new PatchTransformer(settings(""));
        ClassNode changed = node(original);
        target(changed).instructions.insert(new InsnNode(NOP));
        byte[] input = bytes(changed);
        assertSame(input, transformer.transform(HANDLER, HANDLER, input));
        changed.methods.remove(target(changed));
        input = bytes(changed);
        assertSame(input, transformer.transform(HANDLER, HANDLER, input));
    }

    private static ClassWriter emptyClass(String name) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        writer.visit(V1_8, ACC_PUBLIC, name, null, "java/lang/Object", null);
        MethodVisitor constructor = writer.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        constructor.visitCode();
        constructor.visitVarInsn(ALOAD, 0);
        constructor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        constructor.visitInsn(RETURN);
        constructor.visitMaxs(0, 0);
        constructor.visitEnd();
        return writer;
    }

    private static byte[] pieceClass() {
        ClassWriter writer = emptyClass(PIECE);
        writer.visitField(ACC_PUBLIC, "weight", "I", null, null).visitEnd();
        writer.visitField(ACC_PUBLIC, "limit", "I", null, null).visitEnd();
        MethodVisitor constructor = writer.visitMethod(ACC_PUBLIC, "<init>", "(Ljava/lang/Class;II)V", null, null);
        constructor.visitCode();
        constructor.visitVarInsn(ALOAD, 0);
        constructor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        constructor.visitVarInsn(ALOAD, 0);
        constructor.visitVarInsn(ILOAD, 2);
        constructor.visitFieldInsn(PUTFIELD, PIECE, "weight", "I");
        constructor.visitVarInsn(ALOAD, 0);
        constructor.visitVarInsn(ILOAD, 3);
        constructor.visitFieldInsn(PUTFIELD, PIECE, "limit", "I");
        constructor.visitInsn(RETURN);
        constructor.visitMaxs(0, 0);
        constructor.visitEnd();
        writer.visitEnd();
        return writer.toByteArray();
    }

    private int[] execute(byte[] handler, float chance, final float roll) throws Exception {
        Map<String, byte[]> definitions = new HashMap<>();
        ClassNode target = node(handler);
        target.interfaces.clear();
        target.methods.removeIf(value -> ((MethodNode) value).name.equals("buildComponent"));
        definitions.put(HANDLER, bytes(target));
        definitions.put(PIECE.replace('/', '.'), pieceClass());
        ClassWriter component = emptyClass(COMPONENT);
        component.visitEnd();
        definitions.put(COMPONENT.replace('/', '.'), component.toByteArray());
        ClassWriter worldGen = emptyClass(WORLD_GEN);
        worldGen.visitField(ACC_PUBLIC, "villageChance", "F", null, null).visitEnd();
        worldGen.visitEnd();
        definitions.put(WORLD_GEN.replace('/', '.'), worldGen.toByteArray());
        ClassWriter config = emptyClass(CONFIG);
        config.visitField(ACC_PUBLIC | ACC_STATIC, "worldGen", "L" + WORLD_GEN + ";", null, null).visitEnd();
        config.visitEnd();
        definitions.put(CONFIG.replace('/', '.'), config.toByteArray());
        try (JarFile release = new JarFile(System.getProperty("ecpatches.releaseJar"));
             InputStream input = release.getInputStream(release.getJarEntry(SETTINGS.replace('.', '/') + ".class"))) {
            definitions.put(SETTINGS, read(input));
        }
        ClassLoader loader = new ClassLoader(getClass().getClassLoader()) {
            @Override
            protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                byte[] definition = definitions.get(name);
                if (definition == null) return super.loadClass(name, resolve);
                Class<?> type = findLoadedClass(name);
                if (type == null) type = defineClass(name, definition, 0, definition.length);
                if (resolve) resolveClass(type);
                return type;
            }
        };
        loader.loadClass(SETTINGS).getMethod("initialize", java.io.File.class).invoke(null, directory.toFile());
        Class<?> optionsType = loader.loadClass(WORLD_GEN.replace('/', '.'));
        Object options = optionsType.getConstructor().newInstance();
        optionsType.getField("villageChance").setFloat(options, chance);
        loader.loadClass(CONFIG.replace('/', '.')).getField("worldGen").set(null, options);
        Class<?> handlerType = loader.loadClass(HANDLER);
        Object result = handlerType.getMethod("getVillagePieceWeight", Random.class, int.class)
                .invoke(handlerType.getConstructor().newInstance(), new Random() {
                    @Override
                    public float nextFloat() {
                        return roll;
                    }
                }, 0);
        return new int[]{result.getClass().getField("weight").getInt(result), result.getClass().getField("limit").getInt(result)};
    }
}
