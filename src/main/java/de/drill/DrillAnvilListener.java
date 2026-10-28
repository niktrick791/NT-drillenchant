package de.drill;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

public class DrillAnvilListener implements Listener {

    private final DrillPlugin plugin;
    private final DrillEnchantment drillEnchantment;

    public DrillAnvilListener(DrillPlugin plugin, DrillEnchantment drillEnchantment) {
        this.plugin = plugin;
        this.drillEnchantment = drillEnchantment;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inv = event.getInventory();
        ItemStack first = inv.getItem(0);
        ItemStack second = inv.getItem(1);

        if (first == null || second == null || first.getType().name().endsWith("AIR") || second.getType().name().endsWith("AIR")) {
            return;
        }

        int firstLevel = drillEnchantment.getLevel(first);
        int secondLevel = drillEnchantment.getLevel(second);

        if (firstLevel == 0 && secondLevel == 0) {
            return;
        }

        if (secondLevel > 0) {
            if (!drillEnchantment.isApplicable(first)) {
                return;
            }

            int newLevel;
            if (firstLevel == 0) {
                newLevel = secondLevel;
            } else if (firstLevel == secondLevel) {
                newLevel = Math.min(2, firstLevel + 1);
            } else {
                newLevel = Math.max(firstLevel, secondLevel);
            }

            ItemStack result = event.getResult();
            if (result == null || result.getType().name().endsWith("AIR")) {
                result = first.clone();
            } else {
                result = result.clone();
            }

            result = drillEnchantment.applyDrill(result, newLevel);
            event.setResult(result);

            try {
                inv.getClass().getMethod("setRepairCost", int.class).invoke(inv, Math.max(2, newLevel * 3));
            } catch (Throwable ignored) {}
        } else if (firstLevel > 0) {
            ItemStack result = event.getResult();
            if (result != null && !result.getType().name().endsWith("AIR")) {
                result = drillEnchantment.applyDrill(result.clone(), firstLevel);
                event.setResult(result);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onGrindstoneClick(InventoryClickEvent event) {
        if (event.getInventory() == null) return;
        if (event.getInventory().getType().name().equals("GRINDSTONE")) {
            if (event.getRawSlot() == 2) {
                ItemStack current = event.getCurrentItem();
                if (current != null && drillEnchantment.hasDrill(current)) {
                    ItemStack cleaned = drillEnchantment.removeDrill(current.clone());
                    event.setCurrentItem(cleaned);
                }
            }
        }
    }
}
