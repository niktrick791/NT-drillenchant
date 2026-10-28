package de.drill;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ConfigManager {

    private final DrillPlugin plugin;

    private String language = "en";
    private boolean enabled;
    private int maxLevel;
    private boolean damageToolPerBlock;
    private boolean stopBeforeBreak;

    private boolean hoeTillingEnabled;
    private boolean hoeBreakReplaceablePlants;

    private boolean lootChestsEnabled;
    private double lootChestChance;

    private boolean villagerTradesEnabled;
    private double villagerTradeChance;

    private boolean enchantingTableEnabled;
    private double enchantingTableChance;

    private boolean fishingEnabled;
    private double fishingChance;

    private boolean piglinBarterEnabled;
    private double piglinBarterChance;

    private String prefix = "§8[§6§lDrill§8] §7";
    private final Map<String, String> messages = new HashMap<String, String>();

    public ConfigManager(DrillPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        this.language = config.getString("language", "en").toLowerCase(Locale.ROOT);
        if (!"de".equals(language) && !"en".equals(language)) {
            this.language = "en";
        }

        this.enabled = config.getBoolean("drill.enabled", true);
        this.maxLevel = config.getInt("drill.max-level", 3);
        this.damageToolPerBlock = config.getBoolean("drill.damage-tool-per-block", true);
        this.stopBeforeBreak = config.getBoolean("drill.stop-before-break", true);

        this.hoeTillingEnabled = config.getBoolean("drill.hoe-tilling.enabled", true);
        this.hoeBreakReplaceablePlants = config.getBoolean("drill.hoe-tilling.break-replaceable-plants", true);

        this.lootChestsEnabled = config.getBoolean("drill.loot-chests.enabled", true);
        this.lootChestChance = config.getDouble("drill.loot-chests.chance", 0.08);

        this.villagerTradesEnabled = config.getBoolean("drill.villager-trades.enabled", true);
        this.villagerTradeChance = config.getDouble("drill.villager-trades.chance", 0.12);

        this.enchantingTableEnabled = config.getBoolean("drill.enchanting-table.enabled", true);
        this.enchantingTableChance = config.getDouble("drill.enchanting-table.chance", 0.15);

        this.fishingEnabled = config.getBoolean("drill.fishing.enabled", true);
        this.fishingChance = config.getDouble("drill.fishing.chance", 0.05);

        this.piglinBarterEnabled = config.getBoolean("drill.piglin-barter.enabled", true);
        this.piglinBarterChance = config.getDouble("drill.piglin-barter.chance", 0.03);

        this.prefix = config.getString("messages.prefix", "§8[§6§lDrill§8] §7");
        this.messages.clear();
        if (config.isConfigurationSection("messages")) {
            for (String key : config.getConfigurationSection("messages").getKeys(false)) {
                messages.put(key, config.getString("messages." + key));
            }
        }
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String lang) {
        String clean = (lang != null && lang.equalsIgnoreCase("de")) ? "de" : "en";
        this.language = clean;
        plugin.getConfig().set("language", clean);
        plugin.saveConfig();
        load();
    }

    public String getMessage(String key, String fallback) {
        if (messages.containsKey(key)) {
            String val = messages.get(key);
            if (val != null && !val.isEmpty()) return val;
        }
        boolean isDe = "de".equalsIgnoreCase(language);
        if ("mode-switched".equals(key)) return isDe ? "§aDrill-Modus geändert auf: §e%mode%" : "§aDrill mode switched to: §e%mode%";
        if ("mode-require-lvl3".equals(key)) return isDe ? "§cNur Werkzeuge mit Drill III können zwischen 2x2 und 3x3 wechseln!" : "§cOnly tools with Drill III can switch between 2x2 and 3x3!";
        if ("hold-tool".equals(key)) return isDe ? "§cBitte halte ein Werkzeug mit Drill in der Haupthand." : "§cPlease hold a tool with Drill in your main hand.";
        if ("no-permission".equals(key)) return isDe ? "§cDazu hast du keine Berechtigung." : "§cYou do not have permission for this.";
        if ("book-received".equals(key)) return isDe ? "§aDu hast ein Verzaubertes Buch (§6Drill %level%§a) erhalten!" : "§aYou received an Enchanted Book (§6Drill %level%§a)!";
        if ("lang-changed".equals(key)) return isDe ? "§aSprache erfolgreich auf §eDeutsch §ageändert!" : "§aLanguage successfully set to §eEnglish§a!";
        if ("drill-enabled".equals(key)) return isDe ? "§aDrill-Funktion §e§lAKTIVIERT§a! (Modus: %mode%)" : "§aDrill function §e§lENABLED§a! (Mode: %mode%)";
        if ("drill-disabled".equals(key)) return isDe ? "§cDrill-Funktion §4§lDEAKTIVIERT§c! (Normales 1x1 Abbauen aktiv)" : "§cDrill function §4§lDISABLED§c! (Normal 1x1 mining active)";
        return fallback;
    }

    public String getPrefix() { return prefix; }
    public boolean isPickaxeAllowed() { return true; }
    public boolean isShovelAllowed() { return true; }
    public boolean isHoeAllowed() { return true; }

    public boolean isEnabled() { return enabled; }
    public int getMaxLevel() { return maxLevel; }
    public boolean isDamageToolPerBlock() { return damageToolPerBlock; }
    public boolean isStopBeforeBreak() { return stopBeforeBreak; }
    public boolean isHoeTillingEnabled() { return hoeTillingEnabled; }
    public boolean isHoeBreakReplaceablePlants() { return hoeBreakReplaceablePlants; }
    public boolean isLootChestsEnabled() { return lootChestsEnabled; }
    public double getLootChestChance() { return lootChestChance; }
    public boolean isVillagerTradesEnabled() { return villagerTradesEnabled; }
    public double getVillagerTradeChance() { return villagerTradeChance; }
    public boolean isEnchantingTableEnabled() { return enchantingTableEnabled; }
    public double getEnchantingTableChance() { return enchantingTableChance; }
    public boolean isFishingEnabled() { return fishingEnabled; }
    public double getFishingChance() { return fishingChance; }
    public boolean isPiglinBarterEnabled() { return piglinBarterEnabled; }
    public double getPiglinBarterChance() { return piglinBarterChance; }
}
