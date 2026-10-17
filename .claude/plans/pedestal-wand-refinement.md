# Pedestal Wand Refinement

## Scope
Refine the existing pedestal automation into the requested wand-specific behavior without disturbing handheld wand behavior.

## Implementation
1. **Config and shared pedestal state**
   - Move pedestal settings into each corresponding wand section in `Config.java` (ranges, intervals, capacities, durability-use thresholds, food amount/saturation, light strength, resurrection policy).
   - Add redstone deactivation checks and a per-pedestal owner UUID persisted with the inserted wand.
   - Change magnetic storage to exactly three item slots and keep it exposed only through NeoForge automation capability; adjacent inventories/pipes remain supported.
   - Add a single, safe pedestal durability-use helper that damages the inserted wand only after a completed operation, including food threshold accounting.

2. **Fluid and magnetic behavior**
   - Preserve the 32-bucket cap while collecting water source blocks immediately above/below.
   - For lava, search connected source blocks within configured range, process furthest-first, and stop at capacity.
   - Transfer fluid/items to touching capabilities; drop magnetic contents on wand removal/break and discard fluids on sponge-wand removal.

3. **Teleport station**
   - Render a floating spinning Eye of Ender marker for a pedestal containing a Teleport Wand.
   - Right-clicking while facing a station teleports the player to it only within configured station range; shift-right-click retains normal handheld teleport behavior.
   - Use the pedestal that is targeted/selected, validate destination safety, dimension, cooldown, and durability.

4. **Ore locator and light**
   - Right-click an Ore Locator pedestal with any non-empty block item/stack; use that exact block as the match target and send highlight positions within pedestal range. Empty-hand right-click remains wand removal.
   - Replace the current ineffective sparse light-block implementation with a strong propagated light source strategy that respects redstone deactivation and cleans up only blocks owned by the pedestal. Keep placement bounded/configurable and do not overwrite real blocks.

5. **Resurrection and food**
   - Persist the player UUID who inserted a Resurrection Wand.
   - Register a lethal-damage/death handler that finds that owner’s active station, validates same-dimension or configured cross-dimension policy, teleports/rescues them with totem effects, and consumes/damages the pedestal wand according to use rules.
   - Food Wand replenishes configured food/saturation and only damages after the configured accumulated amount is granted.

6. **Verification**
   - Compile after each logical batch, inspect the diff for accidental changes, and run available test tasks. Report third-party pipe behavior and visual lighting as requiring in-game verification if no automated test is possible.
