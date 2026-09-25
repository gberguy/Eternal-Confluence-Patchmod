package com.gberguy.ecpatches.core;

import com.gberguy.ecpatches.core.patches.MorechidsPatch;
import com.towboat.morechids.block.subtile.CustomOrechidSubtile;
import com.towboat.morechids.tweaker.MorechidRegistry;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import org.objectweb.asm.Opcodes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class MorechidsRuntimeTest {
    @TempDir Path directory;

    private static final class GeneratorLoader extends ClassLoader {
        Class<?> define(String name, byte[] bytes) {
            return defineClass(name, bytes, 0, bytes.length);
        }
    }

    @Test
    void repairedGeneratorCreatesWorkingCustomFlowers() throws Exception {
        assumeTrue(System.getProperty("ecpatches.originalMods") != null, "Original mod fixtures not supplied");
        MethodPatch patch = new MorechidsPatch();
        byte[] original = PatchFixtureTest.fixture(patch, "ecpatches.originalMods");
        Class<?> broken = new GeneratorLoader().define(patch.className, original);
        boolean modernAsm;
        try {
            Opcodes.class.getField("ASM9");
            modernAsm = true;
        } catch (NoSuchFieldException absent) {
            modernAsm = false;
        }
        if (modernAsm) {
            InvocationTargetException failure = assertThrows(InvocationTargetException.class,
                    () -> broken.getMethod("generateMorechid", String.class).invoke(null, "broken_test"));
            assertTrue(failure.getCause() instanceof IllegalArgumentException);
            assertTrue(failure.getCause().getMessage().contains("Invalid descriptor"));
        }
        PatchTransformer transformer = new PatchTransformer(PatchSettings.read(directory.resolve("patches.cfg").toFile()));
        Class<?> repaired = new GeneratorLoader().define(patch.className, transformer.transform(patch.className, patch.className, original));
        for (String name : new String[]{"moon", "mars", "mercury", "jupiter", "saturn", "uranus", "neptune", "pluto", "eris"}) {
            Class<?> flowerClass = (Class<?>) repaired.getMethod("generateMorechid", String.class).invoke(null, name);
            CustomOrechidSubtile flower = (CustomOrechidSubtile) flowerClass.getConstructor().newInstance();
            assertEquals("Morechid_" + name, flowerClass.getSimpleName());
            assertEquals(name, flower.name);
            assertSame(MorechidRegistry.DEFINITION, flower.definition);
        }
    }
}
