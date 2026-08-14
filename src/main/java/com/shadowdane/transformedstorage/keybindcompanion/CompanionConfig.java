package com.shadowdane.transformedstorage.keybindcompanion;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

final class CompanionConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("transformed-storage-keybind-companion.json");

    boolean forceApplyDefaultKeyBindings = false;

    static CompanionConfig load() throws IOException {
        if (!Files.exists(CONFIG_PATH)) {
            CompanionConfig config = new CompanionConfig();
            config.save();
            return config;
        }

        try {
            String json = Files.readString(CONFIG_PATH, StandardCharsets.UTF_8);
            CompanionConfig config = GSON.fromJson(json, CompanionConfig.class);
            if (config == null) {
                throw new IOException("Configuration file was empty");
            }
            return config;
        } catch (JsonParseException e) {
            throw new IOException("Invalid JSON in " + CONFIG_PATH, e);
        }
    }

    void save() throws IOException {
        Files.createDirectories(CONFIG_PATH.getParent());

        Path tempPath = CONFIG_PATH.resolveSibling(CONFIG_PATH.getFileName() + ".tmp");
        Files.writeString(tempPath, GSON.toJson(this) + System.lineSeparator(), StandardCharsets.UTF_8);

        try {
            Files.move(tempPath, CONFIG_PATH,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
            Files.move(tempPath, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
