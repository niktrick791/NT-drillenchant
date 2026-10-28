package de.drill;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.AnvilView;

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
        ItemStack first = inv.getFirstItem();
        ItemStack second = inv.getSecondItem();

        if (first == null || second == null || first.getType().isAir() || second.getType().isAir()) {
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
                newLevel = Math.min(plugin.getConfigManager().getMaxLevel(), firstLevel + 1);
            } else {
                newLevel = Math.max(firstLevel, secondLevel);
            }

            ItemStack result = event.getResult();
            if (result == null || result.getType().isAir()) {
                result = first.clone();
            } else {
                result = result.clone();
            }

            drillEnchantment.applyDrill(result, newLevel);
            event.setResult(result);

            int currentCost = event.getView().getRepairCost();
            event.getView().setRepairCost(Math.max(1, currentCost + (newLevel * 2)));
        } else if (firstLevel > 0) {
            ItemStack result = event.getResult();
            if (result != null && !result.getType().isAir()) {
                result = result.clone();
                drillEnchantment.applyDrill(result, firstLevel);
                event.setResult(result);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAnvilClick(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.ANVIL) {
            return;
        }

        if (event.getRawSlot() != 2) {
            return;
        }

        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType().isAir() || !drillEnchantment.hasDrill(current)) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        AnvilInventory anvilInv = (AnvilInventory) event.getInventory();
        int cost = event.getView() instanceof AnvilView anvilView ? anvilView.getRepairCost() : 1;

        if (player.getGameMode() != GameMode.CREATIVE && player.getLevel() < cost) {
            player.sendMessage(Component.text("Nicht genügend Erfahrung! (Benötigt: " + cost + " Level)")
                    .color(NamedTextColor.RED));
            event.setCancelled(true);
            return;
        }

        ItemStack cursor = event.getCursor();
        if (!cursor.getType().isAir()) {
            return;
        }

        event.setCancelled(true);

        if (player.getGameMode() != GameMode.CREATIVE) {
            player.setLevel(Math.max(0, player.getLevel() - cost));
        }

        event.getView().setCursor(current.clone());

        ItemStack first = anvilInv.getFirstItem();
        ItemStack second = anvilInv.getSecondItem();

        if (first != null) {
            int amt = first.getAmount() - 1;
            first.setAmount(amt);
            anvilInv.setFirstItem(amt <= 0 ? null : first);
        }

        if (second != null) {
            int amt = second.getAmount() - 1;
            second.setAmount(amt);
            anvilInv.setSecondItem(amt <= 0 ? null : second);
        }

        anvilInv.setResult(null);
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
    }

    @EventHandler
    public void onGrindstoneClick(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.GRINDSTONE) {
            return;
        }

        if (event.getRawSlot() == 2) {
            ItemStack current = event.getCurrentItem();
            if (current != null && drillEnchantment.hasDrill(current)) {
                ItemStack stripped = current.clone();
                drillEnchantment.removeDrill(stripped);
                event.setCurrentItem(stripped);
            }
        }
    }
}
