package com.shadowdane.transformedstorage.keybindcompanion;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public final class TransformedStorageKeybindCompanion implements ClientModInitializer {
    public static final String MOD_ID = "transformed_storage_keybind_companion";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(this::onClientStarted);
    }

    private void onClientStarted(Minecraft minecraft) {
        final CompanionConfig config;
        try {
            config = CompanionConfig.load();
        } catch (Exception e) {
            LOGGER.error("Failed to load companion configuration. No keybindings were changed.", e);
            return;
        }

        if (!config.forceApplyDefaultKeyBindings) {
            LOGGER.debug("forceApplyDefaultKeyBindings is false; no keybindings will be changed.");
            return;
        }

        LOGGER.info("forceApplyDefaultKeyBindings is true; forcibly applying Default Options keybindings from {}",
                DefaultOptionsKeybindingFile.path());

        try {
            ApplyResult result = applyDefaultKeybindings(minecraft);

            // Save Minecraft's resulting keybindings before marking the one-shot operation complete.
            minecraft.options.save();

            config.forceApplyDefaultKeyBindings = false;
            config.save();

            LOGGER.info(
                    "Successfully applied {} Default Options keybindings ({} stale/unregistered entries skipped). "
                            + "Saved Minecraft options and reset forceApplyDefaultKeyBindings to false.",
                    result.applied(), result.unregistered());
        } catch (Exception e) {
            // Intentionally leave the config flag true so the user can fix the problem and retry on next launch.
            config.forceApplyDefaultKeyBindings = true;
            LOGGER.error(
                    "Failed to forcibly apply Default Options keybindings. "
                            + "forceApplyDefaultKeyBindings will remain true for the next launch.",
                    e);
        }
    }

    private ApplyResult applyDefaultKeybindings(Minecraft minecraft) throws Exception {
        Map<String, DefaultOptionsKeybindingFile.Entry> defaults = DefaultOptionsKeybindingFile.read();

        Map<String, KeyMapping> registeredMappings = new HashMap<>();
        for (KeyMapping keyMapping : minecraft.options.keyMappings) {
            registeredMappings.put(keyMapping.getName(), keyMapping);
        }

        int applied = 0;
        int unregistered = 0;

        for (DefaultOptionsKeybindingFile.Entry entry : defaults.values()) {
            KeyMapping keyMapping = registeredMappings.get(entry.mappingName());
            if (keyMapping == null) {
                unregistered++;
                LOGGER.warn("Default Options contains key mapping '{}' but it is not registered in this client; skipping it.",
                        entry.mappingName());
                continue;
            }

            keyMapping.setKey(entry.key());
            applied++;
            LOGGER.debug("Applied key mapping '{}' -> {}", entry.mappingName(), entry.key().getName());
        }

        if (applied == 0) {
            throw new IllegalStateException(
                    "Default Options keybindings were parsed, but none matched registered Minecraft key mappings");
        }

        // Rebuild Minecraft's static key lookup tables after changing KeyMapping instances directly.
        KeyMapping.resetMapping();
        return new ApplyResult(applied, unregistered);
    }

    private record ApplyResult(int applied, int unregistered) {}
}
