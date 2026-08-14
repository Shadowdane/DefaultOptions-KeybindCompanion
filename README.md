# Transformed Storage - DefaultOptions Keybind Companion

A tiny **Minecraft 26.2 Fabric client-side mod** intended to work alongside **Default Options**.

Its only job is to provide a one-shot, forced reapplication of the keybindings stored in:

`config/defaultoptions/keybindings.txt`

It does **not** read or modify Default Options' `options.txt` and does not touch graphics, audio, mouse, accessibility, resource packs, servers, or any other Minecraft settings.

## Configuration

On first launch the mod creates:

`config/transformed-storage-keybind-companion.json`

```json
{
  "forceApplyDefaultKeyBindings": false
}
```

### `forceApplyDefaultKeyBindings = false`

The mod does nothing.

### `forceApplyDefaultKeyBindings = true`

After Fabric client initialization completes, the mod:

1. Reads `config/defaultoptions/keybindings.txt`.
2. Matches its entries against the key mappings registered in the running client.
3. Forcibly applies those bindings, including mappings belonging to other installed mods.
4. Calls `KeyMapping.resetMapping()` so Minecraft rebuilds its key lookup tables.
5. Saves Minecraft's options/keybindings.
6. Changes `forceApplyDefaultKeyBindings` back to `false` and saves this mod's config.

If the operation fails, the flag intentionally remains `true` so it can be retried after the problem is corrected. The error is logged rather than silently marking the operation complete.

Entries for key mappings that are no longer registered (for example, a removed mod) are logged as warnings and skipped. A run where zero entries can be applied is treated as a failure.

## Default Options file format

The parser targets the Default Options 26.2 format:

```text
key_<mapping-name>:<input-name>:<optional-modifiers>
```

Typical Fabric entries have an empty modifier field. Non-`NONE` modifiers are treated as an error because vanilla/Fabric `KeyMapping` does not expose the NeoForge-style modifier semantics that would be required to reproduce them accurately.

## Target versions

- Minecraft: `26.2`
- Fabric Loader: `0.19.3`
- Fabric API: `0.156.0+26.2`
- Java: `25`

## Building

This repository is structured for Fabric Loom. With Java 25 and Gradle 9.5.1 available:

```bash
gradle build
```

The remapped mod JAR will be produced under `build/libs/`.

A GitHub Actions workflow is also included so pushing the project to GitHub can build the JAR automatically.
