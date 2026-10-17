## Repository for Simplywands

A mod that adds a bunch of useful wands.

### Wands

| Wand | What it does |
|---|---|
| Sponge Wand | Removes water in a radius |
| Lava Sponge Wand | Removes lava in a radius |
| Growth Wand | Grows crops in a radius |
| Magnetic Wand | Attracts dropped items (toggleable) |
| Teleportation Wand | Teleports the user forward |
| Illumination Wand | Places a light source block, brightness adjustable with the scroll wheel |
| Acceleration Wand | Accelerates the tick of a block instantly |
| Love Wand | Breeds any willing creature, and lures them closer |
| Ore-Locator Wand | Highlights ores of the block you are looking at |
| Vein-Miner Wand | Mines entire veins of ores |
| Food Wand | Automatically eats for you (toggleable) |
| Resurrection Wand | Brings you back in case of death |
| Wand Pedestal | Holds a wand and runs it automatically |

### Enchanting

Wands with durability can be enchanted at an enchanting table or with an
enchanted book at an anvil. The **Magnetic**, **Food** and **Resurrection**
Wands carry no durability, so they refuse every enchantment - Unbreaking and
Mending would do nothing on them.

**Every damageable wand** accepts:

- **Unbreaking** - each level reduces the chance a use costs durability.
- **Mending** - repairs the wand using your experience.

**Efficiency** only works on wands that actually scale with it, so it is
refused by the **Illumination**, **Teleport** and **Ore-Locator** Wands.

**Fortune** and **Silk Touch** are exclusive to the **Vein-Miner Wand**. They
work because it is the only wand that hands itself to the loot table as the
tool when breaking blocks, so Fortune multiplies ore drops and Silk Touch
returns the raw ore block. Both work in a pedestal as well as in hand.

### What Efficiency does

Efficiency levels scale each wand's main stat. In hand and in a pedestal the
values are configured separately, so they can be tuned independently.

| Wand | In hand | In a pedestal |
|---|---|---|
| Sponge | wider range | acts sooner - it has no radius to widen, so it works through a fluid tank |
| Lava Sponge | wider range | wider range |
| Growth | wider range | wider range |
| Love | wider lure range | wider range |
| Vein-Miner | breaks more blocks per vein | breaks more blocks per operation |
| Acceleration | applies more extra ticks | applies more extra ticks |

The Illumination, Teleport and Ore-Locator Wands are unaffected by Efficiency.

### Configuration

Everything above is configurable in the server config. The Efficiency bonuses
are all separate settings, and setting any of them to `0` turns that effect
off and leaves the wand exactly as configured.

| Setting | Default | Effect |
|---|---|---|
| `spongeWandEfficiencyRange` | 1 | extra range per Efficiency level |
| `lavaSpongeWandEfficiencyRange` | 1 | extra range per Efficiency level |
| `growthWandEfficiencyRange` | 1 | extra range per Efficiency level |
| `loveWandEfficiencyRange` | 1 | extra range per Efficiency level |
| `veinMinerEfficiencyBlocks` | 16 | extra blocks per vein per Efficiency level |
| `accelerationWandEfficiencyTicks` | 64 | extra ticks per Efficiency level |
| `pedestalSpongeEfficiencyInterval` | 4 | ticks taken off the operation interval per Efficiency level |
| `pedestalLavaSpongeEfficiencyRange` | 1 | extra range per Efficiency level |
| `pedestalGrowthEfficiencyRange` | 1 | extra range per Efficiency level |
| `pedestalLoveEfficiencyRange` | 1 | extra range per Efficiency level |
| `pedestalVeinEfficiencyBlocks` | 16 | extra blocks per operation per Efficiency level |
| `pedestalAccelerationEfficiencyTicks` | 16 | extra ticks per Efficiency level |

### Data packs

Which enchantments a wand accepts is data driven, so a pack can change it
without any code change:

- `simplywands:allowed_on_wands` - enchantments every damageable wand takes.
- `simplywands:vein_miner_only_enchantments` - enchantments only the Vein-Miner
  Wand takes.
- `simplywands:efficiency_capable_wands` - the wands that scale with Efficiency.

### Support

For support, please visit the [CurseForge page](https://www.curseforge.com/minecraft/mc-mods/simplywands) 
