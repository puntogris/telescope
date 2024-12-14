# Telescope Plugin for AS

Telescope is an Android Studio plugin designed to make finding drawables across all your modules a breeze.

## Features

Telescope offers two powerful search modes:

- **Fuzzy Search:** A classic approach for quick and straightforward lookups.
- **AI-Powered Search:** Advanced search capabilities using OpenCLIP models converted to the efficient GGUF format.

### Fuzzy Search

No surprises here—fuzzy search works just as you'd expect. It performs global scans across all modules, including nested
ones, to locate your drawables based on partial matches or misspellings.

### AI-Powered Search with OpenCLIP (The Fun Stuff 🎉)

AI search takes your drawable hunting to the next level by understanding context and intent. For instance, imagine
you’re looking for a drawable of a pencil icon named `ic_pencil.xml`. Fuzzy search might not help if you search for
terms
like **edit** or **update**, but AI search will intelligently connect the dots and provide accurate matches.

#### Why OpenCLIP and GGUF?

Telescope leverages OpenCLIP-compatible models in GGUF format to balance performance and memory efficiency. By using
separate models for text and vision encoders, the plugin avoids loading unnecessarily large models into memory. These
encoders are used on demand, ensuring smooth operation without compromising capabilities.

### About the models

The models use the `ViT-B/32` architecture with pretrained `laion2B-s34B-b79K` weights. For optimal results, images
should be resized to a resolution of 224x224 pixels. In the plugin we do this internally.

### Useful links

https://github.com/mlfoundations/open_clip

https://github.com/monatis/clip.cpp

### TODO

- Refactor how we get resources and generate previews, unsure if we should set global states for colors and modules to
  match when converting to svg
- Fix crash in clip.cpp when encoding text multiple times in a short amount of time