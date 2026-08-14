package com.shadowdane.transformedstorage.keybindcompanion;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class DefaultOptionsKeybindingFile {
    // Matches Default Options 26.2's keybindings.txt format:
    // key_<mapping-name>:<input-name>:<optional-comma-separated-modifiers>
    private static final Pattern KEY_PATTERN = Pattern.compile("key_([^:]+):([^:]+)(?::(.+)?)?");

    private static final Path KEYBINDINGS_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("defaultoptions")
            .resolve("keybindings.txt");

    record Entry(String mappingName, InputConstants.Key key) {}

    static Map<String, Entry> read() throws IOException {
        if (!Files.isRegularFile(KEYBINDINGS_PATH)) {
            throw new IOException("Default Options keybinding file not found: " + KEYBINDINGS_PATH);
        }

        Map<String, Entry> result = new LinkedHashMap<>();
        int lineNumber = 0;

        try (BufferedReader reader = Files.newBufferedReader(KEYBINDINGS_PATH, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }

                Matcher matcher = KEY_PATTERN.matcher(line);
                if (!matcher.matches()) {
                    throw new IOException("Invalid Default Options keybinding at line " + lineNumber + ": " + line);
                }

                String mappingName = matcher.group(1);
                String inputName = matcher.group(2);
                String modifiers = matcher.group(3);

                if (modifiers != null && !modifiers.isBlank() && !"NONE".equals(modifiers)) {
                    throw new IOException(
                            "Unsupported key modifier(s) at line " + lineNumber + " for Fabric key mapping '"
                                    + mappingName + "': " + modifiers);
                }

                try {
                    InputConstants.Key key = InputConstants.getKey(inputName);
                    result.put(mappingName, new Entry(mappingName, key));
                } catch (RuntimeException e) {
                    throw new IOException(
                            "Invalid input name at line " + lineNumber + " for key mapping '"
                                    + mappingName + "': " + inputName,
                            e);
                }
            }
        }

        if (result.isEmpty()) {
            throw new IOException("Default Options keybinding file contained no usable keybindings: " + KEYBINDINGS_PATH);
        }

        return result;
    }

    static Path path() {
        return KEYBINDINGS_PATH;
    }
}
