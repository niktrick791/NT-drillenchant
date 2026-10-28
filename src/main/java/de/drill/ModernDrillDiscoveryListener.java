package de.drill;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.List;

public class ModernDrillDiscoveryListener implements Listener {

    private final DrillPlugin plugin;
    private final DrillEnchantment drillEnchantment;

    public ModernDrillDiscoveryListener(DrillPlugin plugin, DrillEnchantment drillEnchantment) {
        this.plugin = plugin;
        this.drillEnchantment = drillEnchantment;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLootGenerate(LootGenerateEvent event) {
        if (!plugin.getConfigManager().isLootChestsEnabled() || event.getLootTable() == null) {
            return;
        }

        try {
            String key = event.getLootTable().getKey().toString().toLowerCase();
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
        } catch (Throwable ignored) {}
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onVillagerAcquireTrade(VillagerAcquireTradeEvent event) {
        if (!plugin.getConfigManager().isVillagerTradesEnabled()) {
            return;
        }

        try {
            MerchantRecipe recipe = event.getRecipe();
            ItemStack result = recipe.getResult();
            Material bookMat = XDrillVersion.getEnchantedBookMaterial();

            if (result.getType() == bookMat || result.getType() == Material.BOOK) {
                if (Math.random() < plugin.getConfigManager().getVillagerTradeChance()) {
                    double roll = Math.random();
                    int level = roll < 0.65 ? 1 : (roll < 0.90 ? 2 : 3);
                    ItemStack drillBook = drillEnchantment.createBook(level);

                    int price = 16 + (level * 8);
                    ItemStack emeraldCost = new ItemStack(Material.EMERALD, price);
                    ItemStack bookCost = new ItemStack(Material.BOOK, 1);

                    List<ItemStack> ingredients = new ArrayList<ItemStack>();
                    ingredients.add(emeraldCost);
                    ingredients.add(bookCost);

                    MerchantRecipe newRecipe = new MerchantRecipe(drillBook, recipe.getMaxUses());
                    newRecipe.setIngredients(ingredients);
                    newRecipe.setExperienceReward(recipe.hasExperienceReward());
                    newRecipe.setVillagerExperience(recipe.getVillagerExperience());

                    event.setRecipe(newRecipe);
                }
            }
        } catch (Throwable ignored) {}
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPiglinBarter(PiglinBarterEvent event) {
        if (!plugin.getConfigManager().isPiglinBarterEnabled()) {
            return;
        }

        try {
            if (Math.random() < plugin.getConfigManager().getPiglinBarterChance()) {
                ItemStack book = drillEnchantment.createBook(1);
                event.getOutcome().add(book);
            }
        } catch (Throwable ignored) {}
    }
}
