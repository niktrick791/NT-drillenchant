# DrillEnchant

Eigenstaendiges Minecraft-Plugin fuer Paper & Purpur (1.21.x / 1.26.x, Java 25), das das Custom-Enchantment **Drill** hinzufuegt.

## Features

- **Drill I (Level 1)**: Baut Blöcke oder bestellt Felder in einem **2x2** Bereich ab.
- **Drill II (Level 2)**: Baut Blöcke oder bestellt Felder in einem **3x3** Bereich ab.
- **Drill III (Level 3)**: Umschaltbar zwischen **2x2** und **3x3** Modus per Befehl `/drill switch` (oder `/drill switch [2x2|3x3]`).
- **Anwendbare Werkzeuge**:
  - Spitzhacke (`*_PICKAXE`)
  - Schaufel (`*_SHOVEL`)
  - Axt (`*_AXE`)
  - Hacke (`*_HOE`)
  - Verzauberte Buecher (`BOOK`, `ENCHANTED_BOOK`)
  - **Ausgeschlossen**: Schwerter (`*_SWORD`), Rüstung, Bögen, etc.
- **Hoe-Spezialfunktion**:
  - Rechtsklick mit einer Hacke auf Gras / Erde / grobe Erde / bewurzelte Erde verwandelt direkt einen **2x2** bzw. **3x3** Bereich in Farmland (bzw. Erde)!
  - Pflanzen über dem Block (z. B. hohes Gras, Blumen) werden automatisch entfernt.
- **Erhalt der Verzauberung**:
  - **Loot-Kisten**: In Dungeons, Minenschächten, Antiken Städten, Bastionen, Tempeln, End-Städten usw.
  - **Dorfbewohner-Handel**: Bibliothekare bieten Drill-Bücher zum Tausch an.
  - **Verzauberungstisch**: Chance beim Verzaubern von Büchern oder geeigneten Werkzeugen (ab Level 30 auch Drill II).
  - **Amboss**: Kombinieren von Büchern mit Werkzeugen, Drill I + Drill I = Drill II, Drill II + Drill II = Drill III.
  - **Schleifstein**: Entfernt Drill wie gewohnt.

- **Ein- & Ausschalten (Drill I bis III)**:
  - Jedes Drill-Werkzeug (Stufe 1 bis 3) kann per `/drill on`, `/drill off` oder `/drill toggle` (bzw. `/drill switch on/off/toggle`) an- und ausgeschaltet werden.
  - Im ausgeschalteten Zustand (`[DEAKTIVIERT]`) baut das Werkzeug ganz normal im regulären 1x1 Vanilla-Modus ab.
  - Nur **Drill III** kann zusätzlich zwischen der Größe **2x2** und **3x3** wechseln.

## Befehle & Permissions

- `/drill on` / `/drill off` – Aktiviert oder deaktiviert den Drill-Modus für das gehaltene Werkzeug
- `/drill toggle` – Schaltet den Drill-Modus an oder aus
- `/drill switch` – Schaltet bei Drill I & II zwischen An/Aus um; bei Drill III zwischen 2x2 und 3x3
- `/drill switch [on|off|toggle|2x2|3x3]` – Direktes Festlegen des Status bzw. der Größe (2x2/3x3 nur bei Drill III)
- `/drill info` – Zeigt Informationen zum Enchantment an
- `/drill book [1-3]` – Gibt ein verzaubertes Buch (`drill.admin`)
- `/drill give <Spieler> [1-3]` – Gibt einem Spieler ein verzaubertes Buch (`drill.admin`)
- `/drill reload` – Lädt die `config.yml` neu (`drill.admin`)
