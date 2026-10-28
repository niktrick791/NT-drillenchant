package de.drill;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final DrillPlugin plugin;

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

    public ConfigManager(DrillPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

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
    }

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
