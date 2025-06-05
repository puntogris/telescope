## Telescope Plugin for Android Studio (Apple Silicon Support Only)

Telescope is an Android Studio plugin designed to make finding drawables across all your modules simple and efficient.

**Note:** This plugin currently supports Apple Silicon devices only and is in an experimental stage.

## Features

Telescope has two search modes to help you find what you need:

- **Fuzzy Search:** A simple and fast way to locate drawables by name.
- **AI-Powered Search (experimental):** Context-aware search using OpenCLIP models.

### Fuzzy Search

This mode scans all modules, including nested ones, to find drawables based on fuzzy matching in their names. It
provides more flexible results by accounting for minor typos or approximate matches.

### AI-Powered Search (experimental)

The AI search is context-aware, so it can find relevant matches even with related terms, like finding `ic_pencil` when
searching for `edit` or `update`.

The plugin uses OpenCLIP-compatible models in the GGUF format for efficient and lightweight operation, specifically the
`ViT-B/32` model with `laion2B-s34B-b79K` weights. For inference, it leverages clip.cpp through JNI Java bindings.

## Try it out!

### Web version

I got carried away and made a simplified web version, check it out
at [telescope.puntogris.com](https://telescope.puntogris.com
). It includes a terminal where you can see logs in real-time.

![preview](./screenshots/website.jpeg)
*Don't let it fool you, this is the web version*

### Android Studio

- Available at the [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/26174-telescope) or search "Telescope" in Android Studio plugin section.
- Download the latest build from the [releases page](https://github.com/puntogris/telescope/releases).

## TODO

- Check if it's worth migrating to the way ResourceManager fetches resources
- Fix crash in clip.cpp when encoding text multiple times in a short amount of time
- Improve project structure 
- Add linux support
- Improve preview scaling in small drawables
- Improve embeddings for light drawables, white background makes us lose details