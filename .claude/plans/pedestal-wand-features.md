# Pedestal Wand Features

Implement the requested pedestal-held wand behaviors in verified batches, preserving existing handheld behavior.

## Decisions/defaults
- Use separate pedestal-specific config values for ranges, intervals, capacities, and amounts.
- Run automatic behavior server-side on scheduled intervals.
- Expose NeoForge item/fluid capabilities for pipe and adjacent-inventory compatibility.
- Teleport uses the nearest eligible pedestal by default if multiple are available.
- Illumination uses maintained vanilla light blocks within the configured radius, without replacing non-replaceable blocks.
- Sponge/lava fluid is discarded when that wand is removed; stored magnetic items drop when its wand is removed or the pedestal breaks.
- Resurrection cross-dimension behavior is configurable and OFF by default; active pedestal chunks are force-loaded only while needed.

## Batch 1: foundation and automation storage
1. Extend `PedestalBlockEntity` with wand classification, server-side tick entry, persisted/synchronized fluid and item storage, and capability-backed handlers.
2. Register item/fluid capabilities and implement robust adjacent transfer through sided capabilities.
3. Update pedestal insertion/removal/break paths for stored resource semantics.
4. Add pedestal-specific config values for ranges, intervals, capacities, and amounts.
5. Compile before proceeding.

## Batch 2: passive world effects
Implement scheduled server-side handlers for water/lava source collection above/below (32-bucket cap), crop growth at pedestal Y, magnetic item attraction and inventory transfer, acceleration below, animal breeding, food replenishment, bounded vein mining, and light-block maintenance. Compile and smoke-test grouped behaviors.

## Batch 3: player-facing integrations
Implement nearest-pedestal teleport destinations and effects; right-click pedestal with a block to trigger ore highlighting; resurrection using a safe active pedestal with configurable cross-dimension behavior default OFF and managed chunk force-loading. Compile and verify client/server synchronization.

## Batch 4: verification
Add/update GameTests if practical, run compilation and available tests, inspect diff and remove diagnostics. Clearly report any behavior needing in-game verification, especially third-party pipe interoperability and light placement.
