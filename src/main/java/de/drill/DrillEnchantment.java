package de.drill;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class DrillEnchantment {

    public static final String KEY_LEVEL_NAME = "drill_level";
    public static final String KEY_MODE_NAME = "drill_mode";
    public static final String KEY_ENABLED_NAME = "drill_enabled";

    public static final String MODE_2X2 = "2x2";
    public static final String MODE_3X3 = "3x3";

    private final NamespacedKey keyLevel;
    private final NamespacedKey keyMode;
    private final NamespacedKey keyEnabled;
    private final DrillPlugin plugin;

    public DrillEnchantment(DrillPlugin plugin) {
        this.plugin = plugin;
        this.keyLevel = new NamespacedKey(plugin, KEY_LEVEL_NAME);
        this.keyMode = new NamespacedKey(plugin, KEY_MODE_NAME);
        this.keyEnabled = new NamespacedKey(plugin, KEY_ENABLED_NAME);
    }

    public NamespacedKey getKeyLevel() {
        return keyLevel;
    }

    public NamespacedKey getKeyMode() {
        return keyMode;
    }

    public NamespacedKey getKeyEnabled() {
        return keyEnabled;
    }

    public boolean hasDrill(ItemStack item) {
        return getLevel(item) > 0;
    }

    public int getLevel(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return 0;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        Integer level = pdc.get(keyLevel, PersistentDataType.INTEGER);
        return level != null ? level : 0;
    }

    public boolean isEnabled(ItemStack item) {
        if (!hasDrill(item)) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return true;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        Integer enabled = pdc.get(keyEnabled, PersistentDataType.INTEGER);
        return enabled == null || enabled == 1;
    }

    public void setEnabled(ItemStack item, boolean enabled) {
        if (item == null || !item.hasItemMeta()) return;
        int level = getLevel(item);
        if (level <= 0) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(keyEnabled, PersistentDataType.INTEGER, enabled ? 1 : 0);
        item.setItemMeta(meta);

        // Update lore
        applyDrill(item, level, getMode(item), enabled);
    }

    public boolean toggleEnabled(ItemStack item) {
        boolean newState = !isEnabled(item);
        setEnabled(item, newState);
        return newState;
    }

    public String getMode(ItemStack item) {
        int level = getLevel(item);
        if (level <= 0) return null;
        if (level == 1) return MODE_2X2;
        if (level == 2) return MODE_3X3;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return MODE_3X3;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String mode = pdc.get(keyMode, PersistentDataType.STRING);
        return (mode != null && mode.equals(MODE_2X2)) ? MODE_2X2 : MODE_3X3;
    }

    public String toggleMode(ItemStack item) {
        if (item == null || getLevel(item) != 3) {
            return null;
        }

        String current = getMode(item);
        String next = MODE_2X2.equals(current) ? MODE_3X3 : MODE_2X2;
        setMode(item, next);
        return next;
    }

    public void setMode(ItemStack item, String mode) {
        if (item == null || !item.hasItemMeta()) return;
        int level = getLevel(item);
        if (level != 3) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(keyMode, PersistentDataType.STRING, mode);
        item.setItemMeta(meta);

        applyDrill(item, level, mode, isEnabled(item));
    }

    public ItemStack applyDrill(ItemStack item, int level) {
        String defaultMode = level == 1 ? MODE_2X2 : MODE_3X3;
        if (level == 3) {
            String existingMode = getMode(item);
            if (existingMode != null) {
                defaultMode = existingMode;
            }
        }
        return applyDrill(item, level, defaultMode, isEnabled(item));
    }

    public ItemStack applyDrill(ItemStack item, int level, String mode) {
        return applyDrill(item, level, mode, isEnabled(item));
    }

    public ItemStack applyDrill(ItemStack item, int level, String mode, boolean enabled) {
        if (item == null || item.getType().isAir()) {
            return item;
        }
        if (level <= 0) {
            return removeDrill(item);
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(keyLevel, PersistentDataType.INTEGER, level);
        pdc.set(keyEnabled, PersistentDataType.INTEGER, enabled ? 1 : 0);

        if (level == 3) {
            pdc.set(keyMode, PersistentDataType.STRING, mode != null ? mode : MODE_3X3);
        } else {
            pdc.remove(keyMode);
        }

        meta.setEnchantmentGlintOverride(true);

        Component drillLine;
        boolean isBook = item.getType() == Material.BOOK || item.getType() == Material.ENCHANTED_BOOK;

        if (!enabled && !isBook) {
            drillLine = Component.text("Drill " + toRoman(level) + " ")
                    .color(NamedTextColor.DARK_GRAY)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text("[DEAKTIVIERT]").color(NamedTextColor.RED));
        } else if (level == 3 && !isBook) {
            drillLine = Component.text("Drill III ")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text("[" + (mode != null ? mode : MODE_3X3) + "]").color(NamedTextColor.GOLD));
        } else {
            drillLine = Component.text("Drill " + toRoman(level))
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false);
        }

        List<Component> lore = meta.lore();
        if (lore == null) {
            lore = new ArrayList<>();
        } else {
            lore = new ArrayList<>(lore);
            lore.removeIf(line -> {
                String plain = PlainTextComponentSerializer.plainText().serialize(line);
                return plain.toLowerCase().startsWith("drill ");
            });
        }

        lore.add(0, drillLine);
        meta.lore(lore);

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack removeDrill(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return item;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (pdc.has(keyLevel, PersistentDataType.INTEGER)) {
            pdc.remove(keyLevel);
            pdc.remove(keyMode);
            pdc.remove(keyEnabled);

            List<Component> lore = meta.lore();
            if (lore != null) {
                lore = new ArrayList<>(lore);
                lore.removeIf(line -> {
                    String plain = PlainTextComponentSerializer.plainText().serialize(line);
                    return plain.toLowerCase().startsWith("drill ");
                });
                meta.lore(lore.isEmpty() ? null : lore);
            }

            if (meta.getEnchants().isEmpty()) {
                meta.setEnchantmentGlintOverride(null);
            }

            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createBook(int level) {
        return createBook(level, level == 1 ? MODE_2X2 : MODE_3X3);
    }

    public ItemStack createBook(int level, String mode) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Verzaubertes Buch")
                    .color(NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
            book.setItemMeta(meta);
        }
        return applyDrill(book, level, mode, true);
    }

    public boolean isApplicable(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        Material mat = item.getType();
        if (mat == Material.BOOK || mat == Material.ENCHANTED_BOOK) return true;

        String name = mat.name();
        if (name.endsWith("_SWORD")) return false;

        return name.endsWith("_PICKAXE") || name.endsWith("_SHOVEL") ||
               name.endsWith("_AXE") || name.endsWith("_HOE");
    }

    public static String toRoman(int number) {
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            default -> String.valueOf(number);
        };
    }
}
