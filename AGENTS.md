# AGENTS.md

## Project

AutoHarvest — client-side Minecraft mod (26.2) that automates farming. Java 25, no server-side code, no mixins.

Multi-loader architecture using Gradle build-logic convention plugins (`multiloader-common`, `multiloader-loader`), no Architectury.

## Build & Run

```bash
./gradlew :fabric:build       # Build Fabric JAR
./gradlew :neoforge:build     # Build NeoForge JAR
./gradlew :fabric:runClient   # Launch MC client with Fabric
./gradlew :neoforge:runClient # Launch MC client with NeoForge
```

No tests, no linter, no CI, no typecheck step. Build success = correctness.

**Do not run `./gradlew build` or any build command unless explicitly asked.**

## Source Layout

| Path                              | Purpose                                |
|-----------------------------------|----------------------------------------|
| `build-logic/`                    | Gradle convention plugins              |
| `common/src/main/java/`           | Shared code (modes, util, config)      |
| `fabric/src/main/java/`           | Fabric entry point + ModMenu           |
| `neoforge/src/main/java/`         | NeoForge entry point                   |

## Architecture

- **AutoMode interface** (`common`): `tick()`, `getName()`, `onDisable()` — 8 implementations: weed, plant, harvest, farmer, bonemeal, feed, fishing, hoe
- **Config** (`common`): Cloth Config `@Config` interface — fields: ticksPerAction (1–20), radiusCenti (10–65 → blocks/10), thecurrentMode, enableRefill, autoSwitchFortuneTool, coolDown (1–60s), bambooRadius (0–3), autoSwitchHotbar, fishingReCastDelay (10–100), autoSwitchRod, isFlower, autoenable, skipwater, farmerOffhandPlant
- **AutoHarvestClient** (`common`): Shared key mappings (H=toggle, per-mode keys unbound, cycle unbound) + tick dispatching to ModeManager
- **ModeManager** (`common`): Singleton enum; `tick()` calls `currentMode.tick()` every `ticksPerAction` game ticks
- **Platform entry points**: thin wrappers registering Cloth Config, key mappings, client events, commands (`/autoharvest toggle` and `/autoharvest mode <mode>`)
- **Utils** (`common`): BoxUtil (block iteration), InteractionHelper (interact/break blocks/entities), ItemSlotHelper (nearest slot search), ItemRefillHelper (container-click based refill), WaterProximityChecker

## Key Gotchas

- `common` uses `net.neoforged.moddev` only for NeoForm (Minecraft decompilation)
- Fabric/NeoForge pull common source via `multiloader-loader` convention plugin (`compileOnly(project(':common'))` + `commonJava`/`commonResources` configurations)
- Custom Gradle attribute `io.github.mcgradleconventions.loader` set per subproject (`common`, `fabric`, `neoforge`) prevents cross-platform classpath pollution
- No Architectury, no mixins, no access transformers
- Minecraft 26.2 = Spring to Life update; mappings are Mojang (official) via NeoForm
- ModMenu is Fabric-only; NeoForge registers config screen via `IConfigScreenFactory` extension point
- Animal breed whitelist in `mode/animal/Animals.java` — uses registered items for bee flowers
- ItemRefillHelper uses `client.gui.screen()` check — only works when inventory is open or no screen is open
