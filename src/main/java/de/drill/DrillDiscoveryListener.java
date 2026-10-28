package de.drill;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.List;

public class DrillDiscoveryListener implements Listener {

    private final DrillPlugin plugin;
    private final DrillEnchantment drillEnchantment;

    public DrillDiscoveryListener(DrillPlugin plugin, DrillEnchantment drillEnchantment) {
        this.plugin = plugin;
        this.drillEnchantment = drillEnchantment;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLootGenerate(LootGenerateEvent event) {
        if (!plugin.getConfigManager().isLootChestsEnabled() || event.getLootTable() == null) {
            return;
        }

        String key = event.getLootTable().key().value().toLowerCase();
        if (key.contains("chest") || key.contains("dungeon") || key.contains("mineshaft") ||
            key.contains("ancient_city") || key.contains("stronghold") || key.contains("pyramid") ||
            key.contains("temple") || key.contains("bastion") || key.contains("end_city") ||
            key.contains("underwater_ruin") || key.contains("shipwreck") || key.contains("outpost")) {

            if (Math.random() < plugin.getConfigManager().getLootChestChance()) {
                double roll = Math.random();
                int level = roll < 0.65 ? 1 : (roll < 0.90 ? 2 : 3);
                ItemStack book = drillEnchantment.createBook(level);
                event.getLoot().add(book);
            }
        }
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

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;

                ItemStack current = event.getInventory().getItem(0);
                if (current != null && !current.getType().isAir()) {
                    drillEnchantment.applyDrill(current, chosenLevel);
                    player.sendMessage(Component.text("✨ Du hast zusätzlich ")
                            .color(NamedTextColor.GOLD)
                            .append(Component.text("Drill " + DrillEnchantment.toRoman(chosenLevel)).color(NamedTextColor.YELLOW))
                            .append(Component.text(" erhalten!").color(NamedTextColor.GOLD)));
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onVillagerAcquireTrade(VillagerAcquireTradeEvent event) {
        if (!plugin.getConfigManager().isVillagerTradesEnabled()) {
            return;
        }

        MerchantRecipe recipe = event.getRecipe();
        ItemStack result = recipe.getResult();

        if (result.getType() == Material.ENCHANTED_BOOK) {
            if (Math.random() < plugin.getConfigManager().getVillagerTradeChance()) {
                double roll = Math.random();
                int level = roll < 0.65 ? 1 : (roll < 0.90 ? 2 : 3);
                ItemStack drillBook = drillEnchantment.createBook(level);

                MerchantRecipe newRecipe = new MerchantRecipe(
                        drillBook,
                        recipe.getUses(),
                        recipe.getMaxUses(),
                        recipe.hasExperienceReward(),
                        recipe.getVillagerExperience(),
                        recipe.getPriceMultiplier()
                );
                newRecipe.setIngredients(new ArrayList<>(recipe.getIngredients()));
                event.setRecipe(newRecipe);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        if (!plugin.getConfigManager().isFishingEnabled()) {
            return;
        }

        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH && event.getCaught() instanceof Item item) {
            ItemStack caughtItem = item.getItemStack();
            if (caughtItem.getType() == Material.ENCHANTED_BOOK) {
                if (Math.random() < plugin.getConfigManager().getFishingChance()) {
                    int level = Math.random() < 0.8 ? 1 : 2;
                    item.setItemStack(drillEnchantment.createBook(level));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPiglinBarter(PiglinBarterEvent event) {
        if (!plugin.getConfigManager().isPiglinBarterEnabled()) {
            return;
        }

        List<ItemStack> outcome = event.getOutcome();
        for (int i = 0; i < outcome.size(); i++) {
            ItemStack stack = outcome.get(i);
            if (stack.getType() == Material.ENCHANTED_BOOK) {
                if (Math.random() < plugin.getConfigManager().getPiglinBarterChance()) {
                    outcome.set(i, drillEnchantment.createBook(1));
                }
            }
        }
    }
}
