package de.drill;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

public class DrillDiscoveryListener implements Listener {

    private final DrillPlugin plugin;
    private final DrillEnchantment drillEnchantment;

    public DrillDiscoveryListener(DrillPlugin plugin, DrillEnchantment drillEnchantment) {
        this.plugin = plugin;
        this.drillEnchantment = drillEnchantment;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEnchantItem(EnchantItemEvent event) {
        if (!plugin.getConfigManager().isEnchantingTableEnabled()) {
            return;
        }

        ItemStack item = event.getItem();
        if (!drillEnchantment.isApplicable(item) && item.getType() != Material.BOOK) {
            return;
        }

        double chance = plugin.getConfigManager().getEnchantingTableChance();
        int cost = event.getExpLevelCost();
        if (cost >= 25) {
            chance *= 1.4;
        }

        if (Math.random() < chance) {
            int level = 1;
            if (cost >= 30 && Math.random() < 0.35) {
                level = 2;
            }

            final int chosenLevel = level;
            final Player player = event.getEnchanter();

            plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {
                    if (player == null || !player.isOnline()) return;

                    ItemStack current = event.getInventory().getItem(0);
                    if (current != null && current.getType() != Material.AIR && !current.getType().name().endsWith("AIR")) {
                        drillEnchantment.applyDrill(current, chosenLevel);
                        player.sendMessage(XDrillVersion.colorize("&6✨ Du hast zusätzlich &eDrill " + DrillEnchantment.toRoman(chosenLevel) + "&6 erhalten!"));
                    }
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        if (!plugin.getConfigManager().isFishingEnabled()) {
            return;
        }

        Entity caught = event.getCaught();
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH && caught instanceof Item) {
            Item itemEntity = (Item) caught;
            ItemStack caughtItem = itemEntity.getItemStack();
            Material bookMat = XDrillVersion.getEnchantedBookMaterial();
            if (caughtItem != null && (caughtItem.getType() == Material.BOOK || caughtItem.getType() == bookMat)) {
                if (Math.random() < plugin.getConfigManager().getFishingChance()) {
                    int level = Math.random() < 0.8 ? 1 : 2;
                    itemEntity.setItemStack(drillEnchantment.createBook(level));
                }
            }
        }
    }
}
