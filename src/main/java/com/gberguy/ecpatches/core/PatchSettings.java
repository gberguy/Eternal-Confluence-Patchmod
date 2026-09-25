package com.gberguy.ecpatches.core;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.EnumMap;
import java.util.Properties;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class PatchSettings {
    private static final Logger LOG = LogManager.getLogger("EternalConfluencePatchmod");
    private static volatile PatchSettings current;
    private final EnumMap<PatchId, Boolean> enabled;

    private PatchSettings(EnumMap<PatchId, Boolean> enabled) {
        this.enabled = enabled;
    }

    public static synchronized void initialize(File gameDirectory) {
        if (current == null) {
            current = read(new File(gameDirectory, "config/eternalconfluencepatchmod.cfg"));
        }
    }

    public static PatchSettings current() {
        if (current == null) {
            File home = Launch.minecraftHome;
            initialize(home == null ? new File(System.getProperty("user.dir")) : home);
        }
        return current;
    }

    public static PatchSettings read(File file) {
        EnumMap<PatchId, Boolean> values = new EnumMap<>(PatchId.class);
        Properties properties = new Properties();
        try {
            String original = file.exists() ? new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8) : "";
            properties.load(new StringReader(original));
            for (PatchId id : PatchId.values()) {
                String value = properties.getProperty(id.key);
                if (value == null) {
                    values.put(id, true);
                } else if ("true".equalsIgnoreCase(value.trim()) || "false".equalsIgnoreCase(value.trim())) {
                    values.put(id, Boolean.parseBoolean(value.trim()));
                } else {
                    values.put(id, false);
                    LOG.warn("Invalid value for {} in {}; disabling this patch. Use true or false.", id.key, file);
                }
            }
            String rendered = render(properties, original);
            if (!rendered.equals(original)) {
                Path parent = file.toPath().toAbsolutePath().getParent();
                Files.createDirectories(parent);
                Path temporary = Files.createTempFile(parent, "ecpatches-", ".tmp");
                try {
                    Files.write(temporary, rendered.getBytes(StandardCharsets.UTF_8));
                    try {
                        Files.move(temporary, file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                    } catch (AtomicMoveNotSupportedException unsupported) {
                        Files.move(temporary, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                } finally {
                    Files.deleteIfExists(temporary);
                }
            }
        } catch (IOException | IllegalArgumentException | SecurityException error) {
            LOG.error("Cannot load patch configuration {}; disabling all patches for this launch.", file, error);
            for (PatchId id : PatchId.values()) {
                values.put(id, false);
            }
        }
        return new PatchSettings(values);
    }

    private static String render(Properties properties, String original) throws IOException {
        String ghostHeader = "# Corail Tombstone - Ghostly Shape Compatibility";
        String ghostDescription = "# Makes mobs from enabled mods ignore players affected by Ghostly Shape, including players they already targeted.";
        Set<String> generated = new HashSet<>(Arrays.asList(
                "# Eternal Confluence Patchmod", "# General Fixes", ghostHeader, ghostDescription,
                "# Set each fix to true or false. Changes require a full game/server restart.",
                "# Target: Lycanites Mobs / Corail Tombstone",
                "# Uses BlockSlate's existing subtype-aware item instead of a generic ItemBlock. Tested with 1.12.2-2.2.2-31.",
                "# Rejects an air block returned by the block registry when resolving a state matcher. Tested with 1.12.2-3.1.9.2.",
                "# Requires the melee target to be visible before a melee attack can proceed. Tested with 1.12.2-2.0.8.10.",
                "# Prevents targeting players with tombstone:ghostly_shape. Does nothing special when that potion is absent. Tested with Lycanites Mobs 1.12.2-2.0.8.10.",
                "# Fixes invalid ASM descriptors when generating custom Orechid classes by using internal-name-aware type construction. Tested with 1.3.0."));
        Set<String> keys = new HashSet<>();
        for (PatchId id : PatchId.values()) {
            keys.add(id.key);
            generated.add("# Target: " + id.modName);
            if (!id.description.isEmpty()) generated.add("# " + id.description);
        }
        StringBuilder retained = new StringBuilder();
        StringBuilder logical = new StringBuilder();
        for (String line : original.split("\r?\n")) {
            logical.append(line).append('\n');
            int slashes = 0;
            for (int i = line.length() - 1; i >= 0 && line.charAt(i) == '\\'; i--) slashes++;
            if ((slashes & 1) != 0 && !line.trim().startsWith("#") && !line.trim().startsWith("!")) continue;
            Properties entry = new Properties();
            entry.load(new StringReader(logical.toString()));
            boolean known = entry.size() == 1 && keys.contains(entry.stringPropertyNames().iterator().next());
            if (!known && !generated.contains(logical.toString().trim())) retained.append(logical);
            logical.setLength(0);
        }
        if (logical.length() > 0) retained.append(logical);
        StringBuilder output = new StringBuilder("# Eternal Confluence Patchmod\n\n# General Fixes\n");
        for (boolean ghostly : new boolean[]{false, true}) {
            if (ghostly) output.append('\n').append(ghostHeader).append('\n').append(ghostDescription).append('\n');
            for (PatchId id : PatchId.values()) {
                if (id.ghostly() != ghostly) continue;
                output.append("\n# Target: ").append(id.modName).append('\n');
                if (!id.description.isEmpty()) output.append("# ").append(id.description).append('\n');
                String value = properties.getProperty(id.key, "true").trim().replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r");
                output.append(id.key).append('=').append(value).append('\n');
            }
        }
        String extra = retained.toString().trim();
        if (!extra.isEmpty()) output.append('\n').append(extra).append('\n');
        return output.toString();
    }

    public boolean enabled(PatchId id) {
        return Boolean.TRUE.equals(enabled.get(id));
    }
}
