package com.gberguy.ecpatches.core;

import com.gberguy.ecpatches.core.patches.WitcheryVillagesToroWallsPatch;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.CheckClassAdapter;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class WitcheryVillagesToroWallsTest {
    private static final String TARGET = "com.witcherywalls.block.TileEntityVillageWallGen";
    @TempDir Path directory;

    @Test
    void patchesExactWitcheryReleaseOnce() throws Exception {
        byte[] original = original();
        byte[] patched = new PatchTransformer(settings("")).transform(TARGET, TARGET, original);
        assertNotSame(original, patched);
        new ClassReader(patched).accept(new CheckClassAdapter(new ClassWriter(0), true), 0);
        assertSame(patched, new PatchTransformer(settings("")).transform(TARGET, TARGET, patched));
        ClassNode node = new ClassNode();
        new ClassReader(patched).accept(node, 0);
        assertEquals(MethodPatch.Result.ALREADY_PRESENT, new WitcheryVillagesToroWallsPatch().apply(node));
    }

    @Test
    void disabledSettingLeavesWitcheryReleaseUntouched() throws Exception {
        byte[] original = original();
        assertSame(original, new PatchTransformer(settings("witcheryVillagesToroWalls=false\n")).transform(TARGET, TARGET, original));
    }

    private byte[] original() throws Exception {
        String path = System.getProperty("ecpatches.witcheryWallsJar");
        assumeTrue(path != null, "Witchery Villages fixture not supplied");
        try (JarFile jar = new JarFile(path);
             InputStream input = jar.getInputStream(jar.getJarEntry(TARGET.replace('.', '/') + ".class"))) {
            java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
            return output.toByteArray();
        }
    }

    private PatchSettings settings(String contents) throws Exception {
        Path file = directory.resolve("patches.cfg");
        Files.write(file, contents.getBytes(StandardCharsets.UTF_8));
        return PatchSettings.read(file.toFile());
    }
}
