package de.drill;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class DrillPlugin extends JavaPlugin {

    private static DrillPlugin instance;
    private ConfigManager configManager;
    private DrillEnchantment drillEnchantment;

    public static DrillPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        // Configuration
        this.configManager = new ConfigManager(this);

        // Enchantment core
        this.drillEnchantment = new DrillEnchantment(this);

        // Listeners
        getServer().getPluginManager().registerEvents(new DrillMiningListener(this, this.drillEnchantment), this);
        getServer().getPluginManager().registerEvents(new DrillDiscoveryListener(this, this.drillEnchantment), this);
        getServer().getPluginManager().registerEvents(new DrillAnvilListener(this, this.drillEnchantment), this);

        // Commands
        PluginCommand drillCmd = getCommand("drill");
        if (drillCmd != null) {
            DrillCommand handler = new DrillCommand(this, this.drillEnchantment);
            drillCmd.setExecutor(handler);
            drillCmd.setTabCompleter(handler);
        }

        getLogger().info("=================================================");
        getLogger().info("DrillEnchant v" + getPluginMeta().getVersion() + " wurde erfolgreich aktiviert!");
        getLogger().info("2x2 und 3x3 Abbauen & Hoe-Bodenbestellung aktiv.");
        getLogger().info("=================================================");
    }

    @Override
    public void onDisable() {
        getLogger().info("DrillEnchant wurde deaktiviert.");
        instance = null;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DrillEnchantment getDrillEnchantment() {
        return drillEnchantment;
    }
}
