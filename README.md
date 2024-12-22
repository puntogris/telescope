# Telescope Plugin for Android Studio

Telescope is an Android Studio plugin designed to make finding drawables across all your modules simple and efficient.

## Features
Telescope has two search modes to help you find what you need:

- **Partial Match Search:** A simple and fast way to locate drawables by name.
- **AI-Powered Search (experimental):** Context-aware search using OpenCLIP models.

### Partial Match Search

This mode scans all modules, including nested ones, to find drawables based on partial matches in their names.

### AI-Powered Search

AI search goes beyond names by understanding context. For instance, if you’re searching for `ic_pencil` but use terms like `edit` or `update`, the AI can still identify relevant matches.

#### How it works

The plugin uses OpenCLIP-compatible models in the GGUF format for efficient and lightweight operation, such as `ViT-B/32` models with `laion2B-s34B-b79K` weights. For inference, it leverages clip.cpp through JNI Java bindings, ensuring smooth integration and performance.

Text and image models are loaded only when needed, keeping the plugin lightweight. Additionally, images are resized internally to 224x224 pixels for optimal results.

## Try it out!

### Web version
I got carried away and made a simplified web version, check it out at [telescope.puntogris.com](https://telescope.puntogri.com
). It includes a terminal where you can see logs in real-time.

![preview](./screenshots/website.jpeg)
*Don't let it fool you, this is the web version*

### Android Studio
The plugin isn’t published yet, but you can install it manually:

 - Clone the repository
 - Run the `buildPlugin` Gradle task to generate a .zip file.
 - In Android Studio, go to Settings > Plugins > Gear Icon > Install Plugin from Disk, and select the .zip file.

### TODO

- Check if it's worth migrating to the way ResourceManager fetches resources
- Fix crash in clip.cpp when encoding text multiple times in a short amount of time
- Publish it or create an upload a release version


