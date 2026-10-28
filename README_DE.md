# NT-drillenchant

[![Sprache: Deutsch](https://img.shields.io/badge/Sprache-Deutsch-orange.svg)](#)
[![English](https://img.shields.io/badge/Language-English-blue.svg)](README.md)
[![Plattform: Paper / Purpur / Spigot](https://img.shields.io/badge/Plattform-Paper%20%7C%20Purpur%20%7C%20Spigot-green.svg)](#)
[![Minecraft: 1.8.8 - 1.21.4+](https://img.shields.io/badge/Minecraft-1.8.8--1.21.4%2B-brightgreen.svg)](#)
[![Java: 8+](https://img.shields.io/badge/Java-8%2B-red.svg)](#)
[![Donate with PayPal](https://img.shields.io/badge/Donate-PayPal-00457C.svg?logo=paypal&logoColor=white)](https://paypal.me/niktrick)
[![Support on Ko-fi](https://img.shields.io/badge/Support-Ko--fi-FF5E5B.svg?logo=kofi&logoColor=white)](https://ko-fi.com/niktrick)

[🇬🇧 Switch to English Version](README.md) | [🇩🇪 Deutsche Version](README_DE.md)

---

Universelles, leichtgewichtiges Custom-Enchantment-Plugin für Flächenabbau & Ackerbau in Minecraft **1.8.8 bis 1.21.4+** (Paper, Purpur, Spigot).

Fügt die Verzauberung **Drill** hinzu, mit der Spitzhacken, Schaufeln, Äxte und Hacken Blöcke oder Felder in konfigurierbaren 2x2- und 3x3-Bereichen abbauen bzw. bearbeiten können.

---

## Features

- **Drill I (Stufe 1)**: Baut Blöcke ab oder bestellt Felder in einem **2x2**-Bereich.
- **Drill II (Stufe 2)**: Baut Blöcke ab oder bestellt Felder in einem **3x3**-Bereich.
- **Drill III (Stufe 3)**: Im Spiel jederzeit flexibel umschaltbar zwischen **2x2** und **3x3** (`/drill switch`).
- **Unterstützte Werkzeuge**:
  - Spitzhacke (`*_PICKAXE`)
  - Schaufel (`*_SHOVEL`)
  - Axt (`*_AXE`)
  - Hacke (`*_HOE`)
  - Verzauberte Bücher (`BOOK`, `ENCHANTED_BOOK`)
  - *Schwerter, Rüstungen und Fernkampfwaffen sind strikt ausgeschlossen.*
- **Spezielle Hacken-Funktion (Ackerbau)**:
  - Rechtsklick mit einer Drill-Hacke auf Erde, Gras, grobe Erde oder bewurzelte Erde pflügt sofort den gesamten 2x2- bzw. 3x3-Bereich zu Ackerland um!
  - Bewuchs über den Blöcken (hohes Gras, Blumen) wird automatisch sauber entfernt.
- **Natürlicher Erhalt im Gameplay**:
  - **Loot-Truhen**: In Dungeons, Minenschächten, Antiken Städten, Bastionen, Tempeln, End-Städten usw.
  - **Dorfbewohner-Handel**: Bibliothekare können Drill-Bücher anbieten.
  - **Zaubertisch**: Kann regulär auf Werkzeuge oder Bücher gezaubert werden.
  - **Angeln & Piglin-Tausch**: Konfigurierbare Wahrscheinlichkeiten für seltenen Schatz-Loot.
  - **Amboss & Schleifstein**: Vollständig kombinierbar (Drill I + Drill I = Drill II; Drill II + Drill II = Drill III) oder am Schleifstein entzauberbar.
- **Ein- & Ausschalten (Drill I bis III)**:
  - Jedes Drill-Werkzeug kann per `/drill toggle`, `/drill on` oder `/drill off` ein- und ausgeschaltet werden.
  - Im ausgeschalteten Zustand baut das Werkzeug ganz normal im regulären 1x1-Vanilla-Modus ab.
- **Zweisprachig & Live umschaltbar**:
  - Vollständige Unterstützung für Deutsch und Englisch.
  - Jederzeit live umschaltbar mit `/drill lang <de|en>` oder in der `config.yml`.

---

## Befehle & Permissions

| Befehl | Beschreibung | Permission |
| :--- | :--- | :--- |
| `/drill` oder `/drill switch` | Schaltet an/aus (Stufe 1-2) oder wechselt zwischen 2x2 und 3x3 (Stufe 3) | *Keine* |
| `/drill on` / `/drill off` | Aktiviert oder deaktiviert den Drill-Modus für das gehaltene Werkzeug | *Keine* |
| `/drill toggle` | Schaltet den Drill-Status für das gehaltene Werkzeug um | *Keine* |
| `/drill switch [on\|off\|toggle\|2x2\|3x3]` | Legt den Status oder die Abbau-Dimensionen direkt fest | *Keine* |
| `/drill info` | Zeigt Informationen zum Enchantment und Befehle an | *Keine* |
| `/drill book [1-3]` | Gibt ein verzaubertes Drill-Buch | `drill.admin` |
| `/drill give <Spieler> [1-3]` | Gibt einem Spieler ein verzaubertes Drill-Buch | `drill.admin` |
| `/drill reload` | Lädt die `config.yml` neu | `drill.admin` |
| `/drill lang <de\|en>` | Ändert die Sprache des Plugins live | `drill.admin` |

---

## Konfiguration (`config.yml`)

```yaml
# Spracheinstellung: "en" für Englisch, "de" für Deutsch
language: "en"

drill:
  enabled: true
  max-level: 3

  # Haltbarkeit
  damage-tool-per-block: true
  stop-before-break: true # Verhindert versehentliches Zerbrechen des Werkzeugs

  # Ackerbau mit Hacke
  hoe-tilling:
    enabled: true
    break-replaceable-plants: true

  # Fundchancen
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

## Kompatibilität

- **Minecraft**: 1.8.8, 1.12.2, 1.16.5, 1.17, 1.18, 1.19, 1.20, 1.21.x+
- **Server-Software**: Paper, Purpur, Spigot
- **Java**: 8 bis 25+ (kompiliert auf Java 8 Bytecode für maximale Kompatibilität)
---

## 💖 Unterstützung & Spenden

Wenn dir dieses Plugin gefällt, freuen wir uns über deine freiwillige Unterstützung:
- [PayPal Spende](https://paypal.me/niktrick)
- [Ko-fi Support](https://ko-fi.com/niktrick)
- Alle Spenden sind freiwillig und fließen direkt in die Wartung und Weiterentwicklung!
