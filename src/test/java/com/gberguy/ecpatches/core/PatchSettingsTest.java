package com.gberguy.ecpatches.core;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

public class PatchSettingsTest {
    @TempDir Path directory;

    @Test
    void generatesExplainedDefaultsAndDoesNotRewriteThem() throws Exception {
        Path file = directory.resolve("config/patches.cfg");
        PatchSettings settings = PatchSettings.read(file.toFile());
        String contents = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        for (PatchId id : PatchId.values()) {
            assertTrue(settings.enabled(id));
            assertTrue(contents.contains(id.key + "=true"));
            assertTrue(contents.contains(id.modName));
            assertTrue(contents.contains(id.description));
        }
        PatchSettings.read(file.toFile());
        assertEquals(contents, new String(Files.readAllBytes(file), StandardCharsets.UTF_8));
    }

    @Test
    void preservesDisabledOptionsAndAddsMissingOnes() throws Exception {
        Path file = directory.resolve("patches.cfg");
        String initial = "# custom\n" + PatchId.ROOTS.key + "=false";
        Files.write(file, initial.getBytes(StandardCharsets.UTF_8));
        PatchSettings settings = PatchSettings.read(file.toFile());
        assertFalse(settings.enabled(PatchId.ROOTS));
        assertTrue(settings.enabled(PatchId.MORECHIDS));
        assertTrue(new String(Files.readAllBytes(file), StandardCharsets.UTF_8).startsWith(initial));
        assertFalse(PatchSettings.read(file.toFile()).enabled(PatchId.ROOTS));
    }

    @Test
    void invalidValueDisablesOnlyItsOption() throws Exception {
        Path file = directory.resolve("patches.cfg");
        Files.write(file, (PatchId.ROOTS.key + "=maybe\n").getBytes(StandardCharsets.UTF_8));
        PatchSettings settings = PatchSettings.read(file.toFile());
        assertFalse(settings.enabled(PatchId.ROOTS));
        assertTrue(settings.enabled(PatchId.MORECHIDS));
    }

    @Test
    void unreadableConfigDisablesAll() {
        PatchSettings settings = PatchSettings.read(directory.toFile());
        for (PatchId id : PatchId.values()) {
            assertFalse(settings.enabled(id));
        }
    }

    @Test
    void absentTargetsNeverResolveTheirClasses() {
        PatchTransformer transformer = new PatchTransformer(PatchSettings.read(directory.resolve("patches.cfg").toFile()));
        byte[] bytes = {1, 2, 3};
        assertSame(bytes, transformer.transform("unrelated.Class", "unrelated.Class", bytes));
        for (MethodPatch patch : PatchTransformer.patches().values()) {
            assertNull(transformer.transform(patch.className, patch.className, null));
            assertSame(bytes, transformer.transform(patch.className, patch.className, bytes));
        }
    }
}
