# Pipez - AI Fabric Port

> **This is an AI-generated port of [Pipez](https://github.com/henkelmax/pipez) (originally a NeoForge mod by henkelmax) to the Fabric mod loader.** The port was generated using Claude Opus 4.6. It may contain bugs or incomplete features — use at your own risk.

## About Pipez

Pipez is a mod that introduces simple, highly configurable pipes designed to create as little lag as possible.

### Pipe Types

- **Item Pipes** - Transfer items between inventories
- **Fluid Pipes** - Transfer fluids between tanks
- **Energy Pipes** - Transfer energy between machines
- **Gas Pipes** - Transfer gases (requires Mekanism)
- **Universal Pipes** - Combines all four types into one

### Features

**Filter System:** Highly configurable filtering with blacklist/whitelist options, redstone modes, distribution modes, tag filtering, and custom NBT data with three matching modes.

**Upgrade Tiers:** Five upgrade levels — Basic, Improved, Advanced, Ultimate, and Infinity (uncraftable by default) — each unlocking additional features and increasing transfer rates.

**Performance:** Only extracting pipes have block entities, pipes avoid loading unnecessary chunks, and rendering is optimized for server efficiency.

### Configuring Pipes

Pipes require extraction configuration to function. Sneak-click pipe ends with a wrench to enable extraction, then click the extracting portion to modify modes, apply filters, or add upgrades.

Minimum upgrade levels for features:
- **Basic:** Redstone modes
- **Improved:** Distribution modes
- **Advanced:** Filter modes and filters

## Fabric Port Details

- **Minecraft:** 1.21.10
- **Mod Loader:** Fabric (Loader 0.18.4)
- **Fabric API:** 0.138.4+1.21.10

## Original Mod

- [Pipez on Modrinth](https://modrinth.com/mod/pipez)
- [Pipez on CurseForge](https://www.curseforge.com/minecraft/mc-mods/pipez)
- [Original Source (NeoForge)](https://github.com/henkelmax/pipez)
- [Credits](https://modrepo.de/minecraft/pipez/credits)

## Disclaimer

This port is not affiliated with or endorsed by the original Pipez author (henkelmax). All credit for the original mod design, textures, and concepts goes to them. This is an independently generated Fabric port using AI tooling.
