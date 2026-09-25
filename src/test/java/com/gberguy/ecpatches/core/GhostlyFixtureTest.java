package com.gberguy.ecpatches.core;

import com.gberguy.ecpatches.core.patches.GhostlyPatches;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.CheckClassAdapter;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class GhostlyFixtureTest {
    @TempDir Path directory;
    static byte[] fixture(MethodPatch patch, boolean mcp) throws Exception {
        Path jar;
        if (patch.className.startsWith("net.minecraft.")) {
            String path = System.getProperty(mcp ? "ecpatches.minecraftMcp" : "ecpatches.minecraftSrg");
            assumeTrue(path != null, "Minecraft fixture not supplied");
            jar = Paths.get(path);
        } else {
            String directory = System.getProperty(mcp ? "ecpatches.ghostlyMcp" : "ecpatches.ghostlyMods");
            assumeTrue(directory != null, "Ghostly mod fixtures not supplied");
            jar = Paths.get(directory, patch.className.startsWith("electroblob.") ? "ElectroblobsWizardry-4.3.19.jar" : "ToroQuest-Revamped-3-25-2022-fix.jar");
        }
        try (JarFile zip = new JarFile(jar.toFile());
             InputStream input = zip.getInputStream(zip.getJarEntry(patch.className.replace('.', '/') + ".class"))) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
            return output.toByteArray();
        }
    }

    static ClassNode node(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        return node;
    }

    static MethodNode method(ClassNode node, MethodPatch patch) {
        for (Object entry : node.methods) {
            MethodNode method = (MethodNode) entry;
            if (MinecraftNames.canonical(method.name).equals(patch.methodName) && method.desc.equals(patch.descriptor)) return method;
        }
        throw new AssertionError("Missing target method");
    }

    @TestFactory
    Stream<DynamicTest> guardsBothNameEnvironmentsAndPreservesOriginalLogic() {
        return Arrays.stream(GhostlyPatches.create()).flatMap(patch -> Stream.of(false, true).map(mcp ->
                DynamicTest.dynamicTest(patch.className + "#" + patch.methodName + (mcp ? " MCP" : " SRG"), () -> {
                    ClassNode original = node(fixture(patch, mcp));
                    String before = MethodFingerprint.of(method(original, patch));
                    assertEquals(MethodPatch.Result.APPLIED, patch.apply(original));
                    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                    original.accept(writer);
                    new ClassReader(writer.toByteArray()).accept(new CheckClassAdapter(new ClassWriter(0), true), 0);
                    assertEquals(MethodPatch.Result.ALREADY_PRESENT, patch.apply(node(writer.toByteArray())));
                    MethodNode modified = method(original, patch);
                    LabelNode end = null;
                    for (AbstractInsnNode instruction : modified.instructions.toArray()) {
                        if (instruction instanceof JumpInsnNode) { end = ((JumpInsnNode) instruction).label; break; }
                    }
                    assertNotNull(end);
                    while (modified.instructions.getFirst() != end) modified.instructions.remove(modified.instructions.getFirst());
                    modified.instructions.remove(end);
                    modified.instructions.remove(modified.instructions.getFirst());
                    assertEquals(before, MethodFingerprint.of(modified));
                    modified.instructions.insert(new InsnNode(Opcodes.NOP));
                    assertEquals(MethodPatch.Result.UNSUPPORTED, patch.apply(original));
                })));
    }

    @Test
    void sharedWizardryClassCanReceiveBothGuards() throws Exception {
        MethodPatch[] patches = GhostlyPatches.create();
        MethodPatch first = patches[2], second = patches[3];
        ClassNode original = node(fixture(first, false));
        assertEquals(MethodPatch.Result.APPLIED, first.apply(original));
        assertEquals(MethodPatch.Result.APPLIED, second.apply(original));
        assertEquals(MethodPatch.Result.ALREADY_PRESENT, first.apply(original));
        assertEquals(MethodPatch.Result.ALREADY_PRESENT, second.apply(original));
    }

    @Test
    void ancientSwitchActivatesSharedGuardsWhenWizardrySwitchIsOff() throws Exception {
        MethodPatch patch = GhostlyPatches.create()[2];
        byte[] original = fixture(patch, false);
        for (PatchId enabled : new PatchId[]{PatchId.ANCIENT_GHOST, PatchId.WIZARDRY_GHOST, PatchId.TORO_GHOST}) {
            StringBuilder config = new StringBuilder();
            for (PatchId id : PatchId.values()) config.append(id.key).append('=').append(id == enabled).append('\n');
            Path file = directory.resolve(enabled.key + ".cfg");
            Files.write(file, config.toString().getBytes(StandardCharsets.UTF_8));
            PatchTransformer transformer = new PatchTransformer(PatchSettings.read(file.toFile()));
            byte[] result = transformer.transform(patch.className, patch.className, original);
            if (enabled == PatchId.TORO_GHOST) assertSame(original, result);
            else {
                assertNotSame(original, result);
                assertEquals(MethodPatch.Result.ALREADY_PRESENT, GhostlyPatches.create()[2].apply(node(result)));
                assertEquals(MethodPatch.Result.ALREADY_PRESENT, GhostlyPatches.create()[3].apply(node(result)));
            }
        }
    }
}
