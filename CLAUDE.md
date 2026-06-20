# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AutoHarvest is a client-side Minecraft Fabric mod (26.1.2) that automates farming tasks: planting, harvesting, weeding, feeding animals, fishing, hoeing, bonemealing, and a composite "farmer" mode. The mod uses Mojang mappings and requires Java 25.

## Build Commands

```bash
./gradlew build              # Build the mod JAR
./gradlew runClient          # Launch Minecraft client with the mod
./gradlew genSources         # Generate Minecraft source for IDE navigation
```

Output JAR is in `build/libs/`.

## Architecture

**Entry point**: `kite.autoharvest.AutoHarvest` (implements `ClientModInitializer`). Registers keybindings, commands, and the client tick handler.

**Mode system** — the core pattern:
- `AutoMode` interface (`tick()`, `getName()`, `onDisable()`) — all modes implement this
- `ModeEnum` enum — each constant has a `setMode()` factory that creates the corresponding `AutoMode` instance
- `ModeManager` (singleton enum) — holds the active `AutoMode`, handles toggle/activate/deactivate, calls `currentMode.tick()` on each client tick (throttled by `ticksPerAction` config)
- `CompositeMode` — composes multiple `AutoMode` instances; used for "farmer" mode (PlantMode + HarvestMode)

**Mode implementations** in `kite.autoharvest.mode`:
- `PlantMode` — plants seeds on farmland, sugar cane near water, bamboo, cocoa on jungle logs, nether wart on soul sand
- `HarvestMode` — breaks mature crops; auto-switches to Fortune tool
- `WeedMode` — clears grass/tall grass from farmland
- `FeedMode` — feeds breedable animals; shears sheep; uses cooldown cache per entity UUID
- `FishingMode` — detects fish bites by tracking bobber Y-position sink; stateful with baseline/stationary tracking
- `HoeMode` — converts grass/dirt to farmland near water
- `BonemealMode` — applies bone meal to immature crops

**Config**: `AutoHarvestConfig` uses Cloth Config (`@Config` annotation) with `AutoConfig.register()`. Config is accessed via static helpers like `AutoHarvestConfig.radius()`, `AutoHarvestConfig.ticksPerAction()`.

**Commands**: `/autoharvest mode <name>` and `/autoharvest toggle` registered via Fabric's client command API in `ModeCommand`.

**Keybindings**: `H` toggles the mod. Each mode has a dedicated keybinding (unbound by default). A cycle keybinding steps through modes in order.

**Utilities** in `kite.autoharvest.util`:
- `BoxUtil` — AABB creation, sphere-range block iteration, player/world accessors
- `InteractionHelper` — wraps `gameMode.useItemOn()`, `startDestroyBlock()`, `interact()` for blocks/entities
- `ItemSlotHelper` — hotbar slot searching for specific items
- `ItemRefillHelper` — refills main/offhand from inventory
- `WaterProximityChecker` — checks adjacency to water source blocks
- `ItemWhitelist` — combined set of all plantable/breedable items

**Animal data**: `Animals.BREEDABLE_WHITELIST` maps `Class<? extends Entity>` to `Set<Item>` of valid breeding foods.

## Key Patterns

- Modes operate on the **client thread only** — no server-side logic, no mixins
- Block iteration uses `BlockPos.withinManhattan()` filtered by AABB then sphere check
- Item selection priority: main hand → off hand → nearest hotbar slot (auto-switch if config enabled)
- Config values are accessed through static methods on `AutoHarvestConfig`, not directly from the instance
