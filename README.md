# NT-drillenchant

[![Language: English](https://img.shields.io/badge/Language-English-blue.svg)](#)
[![Deutsch](https://img.shields.io/badge/Sprache-Deutsch-orange.svg)](README_DE.md)
[![Platform: Paper / Purpur / Spigot](https://img.shields.io/badge/Platform-Paper%20%7C%20Purpur%20%7C%20Spigot-green.svg)](#)
[![Minecraft: 1.8.8 - 1.21.4+](https://img.shields.io/badge/Minecraft-1.8.8--1.21.4%2B-brightgreen.svg)](#)
[![Java: 8+](https://img.shields.io/badge/Java-8%2B-red.svg)](#)

[🇩🇪 Zur deutschen Version wechseln](README_DE.md) | [🇬🇧 English Version](README.md)

---

Universal, lightweight area-mining & soil-tilling custom enchantment plugin for Minecraft **1.8.8 to 1.21.4+** (Paper, Purpur, Spigot).

Adds the custom **Drill** enchantment allowing pickaxes, shovels, axes, and hoes to mine or till fields in configurable 2x2 and 3x3 areas.

---

## Features

- **Drill I (Level 1)**: Mines blocks or tills fields in a **2x2** area.
- **Drill II (Level 2)**: Mines blocks or tills fields in a **3x3** area.
- **Drill III (Level 3)**: Freely toggleable between **2x2** and **3x3** modes on the fly (`/drill switch`).
- **Supported Tools**:
  - Pickaxes (`*_PICKAXE`)
  - Shovels (`*_SHOVEL`)
  - Axes (`*_AXE`)
  - Hoes (`*_HOE`)
  - Enchanted Books (`BOOK`, `ENCHANTED_BOOK`)
  - *Swords, armor, and ranged weapons are strictly excluded.*
- **Special Hoe Tilling**:
  - Right-clicking dirt, grass, coarse dirt, or rooted dirt with a drill hoe immediately tills the entire 2x2 or 3x3 area into farmland!
  - Replaceable foliage (tall grass, flowers) above tilled ground is cleanly broken.
- **Natural Acquisition**:
  - **Loot Chests**: Dungeons, Mineshafts, Ancient Cities, Bastions, Desert/Jungle Temples, End Cities, etc.
  - **Villager Trading**: Librarians can offer Drill enchanted books.
  - **Enchanting Table**: Naturally obtainable when enchanting books or tools.
  - **Fishing & Piglin Bartering**: Configurable chance to obtain from treasure loot.
  - **Anvil & Grindstone**: Combine books/tools (Drill I + Drill I = Drill II; Drill II + Drill II = Drill III) or disenchant at a grindstone.
- **On/Off Toggle Support**:
  - Any drill tool can be temporarily enabled or disabled (`/drill toggle`, `/drill on`, `/drill off`) without removing the enchantment.
  - When disabled, the tool functions normally in standard 1x1 vanilla mode.
- **Bilingual & Live Language Switching**:
  - Fully supports English and German.
  - Switch anytime via `/drill lang <de|en>` or in `config.yml`.

---

## Commands & Permissions

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/drill` or `/drill switch` | Toggles on/off (Level 1-2) or switches between 2x2 and 3x3 (Level 3) | *None* |
| `/drill on` / `/drill off` | Enables or disables drill area effect for the held tool | *None* |
| `/drill toggle` | Toggles drill state for the held tool | *None* |
| `/drill switch [on\|off\|toggle\|2x2\|3x3]` | Directly sets drill status or area dimensions | *None* |
| `/drill info` | Shows enchantment information and commands | *None* |
| `/drill book [1-3]` | Spawns an enchanted Drill book | `drill.admin` |
| `/drill give <player> [1-3]` | Gives an enchanted Drill book to a player | `drill.admin` |
| `/drill reload` | Reloads `config.yml` | `drill.admin` |
| `/drill lang <de\|en>` | Changes plugin language live | `drill.admin` |

---

## Configuration (`config.yml`)

```yaml
# Language setting: "en" for English, "de" for German
language: "en"

drill:
  enabled: true
  max-level: 3

  # Durability settings
  damage-tool-per-block: true
  stop-before-break: true # Prevents the tool from breaking accidentally on extra blocks

  # Tilling with Hoe
  hoe-tilling:
    enabled: true
    break-replaceable-plants: true

  # Discovery chances
  loot-chests:
    enabled: true
    chance: 0.08

  villager-trades:
    enabled: true
    chance: 0.12

  enchanting-table:
    enabled: true
    chance: 0.15

  fishing:
    enabled: true
    chance: 0.05

  piglin-barter:
    enabled: true
    chance: 0.03
```

---

## Compatibility

- **Minecraft**: 1.8.8, 1.12.2, 1.16.5, 1.17, 1.18, 1.19, 1.20, 1.21.x+
- **Server Software**: Paper, Purpur, Spigot
- **Java**: 8 to 25+ (compiled to Java 8 bytecode for maximum compatibility)
