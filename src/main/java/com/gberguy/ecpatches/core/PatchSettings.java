package com.gberguy.ecpatches.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
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
            if (file.exists()) {
                try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
                    properties.load(reader);
                }
            }
            StringBuilder additions = new StringBuilder();
            if (!file.exists()) {
                additions.append("# Eternal Confluence Patchmod\n# Set each fix to true or false. Changes require a full game/server restart.\n");
            }
            for (PatchId id : PatchId.values()) {
                String value = properties.getProperty(id.key);
                if (value == null) {
                    additions.append("\n# Target: ").append(id.modName).append('\n');
                    additions.append("# ").append(id.description).append('\n');
                    additions.append(id.key).append("=true\n");
                    values.put(id, true);
                } else if ("true".equalsIgnoreCase(value.trim()) || "false".equalsIgnoreCase(value.trim())) {
                    values.put(id, Boolean.parseBoolean(value.trim()));
                } else {
                    values.put(id, false);
                    LOG.warn("Invalid value for {} in {}; disabling this patch. Use true or false.", id.key, file);
                }
            }
            if (additions.length() > 0) {
                Files.createDirectories(file.toPath().toAbsolutePath().getParent());
                try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                    writer.write(additions.toString());
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

    public boolean enabled(PatchId id) {
        return Boolean.TRUE.equals(enabled.get(id));
    }
}
