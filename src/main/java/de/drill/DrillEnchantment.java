package de.drill;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DrillEnchantment {

    public static final String KEY_LEVEL_NAME = "drill_level";
    public static final String KEY_MODE_NAME = "drill_mode";
    public static final String KEY_ENABLED_NAME = "drill_enabled";

    public static final String MODE_2X2 = "2x2";
    public static final String MODE_3X3 = "3x3";

    private NamespacedKey keyLevel;
    private NamespacedKey keyMode;
    private NamespacedKey keyEnabled;
    private final DrillPlugin plugin;

    public DrillEnchantment(DrillPlugin plugin) {
        this.plugin = plugin;
        try {
            this.keyLevel = new NamespacedKey(plugin, KEY_LEVEL_NAME);
            this.keyMode = new NamespacedKey(plugin, KEY_MODE_NAME);
            this.keyEnabled = new NamespacedKey(plugin, KEY_ENABLED_NAME);
        } catch (Throwable ignored) {
            this.keyLevel = null;
            this.keyMode = null;
            this.keyEnabled = null;
        }
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
        if (item == null || item.getType().name().endsWith("AIR") || !item.hasItemMeta()) {
            return 0;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;

        int pdcLvl = XDrillVersion.getPdcLevel(meta, keyLevel);
        if (pdcLvl > 0) return pdcLvl;

        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            if (lore != null) {
                for (String line : lore) {
                    String clean = ChatColor.stripColor(line).trim();
                    if (clean.equalsIgnoreCase("Drill I")) return 1;
                    if (clean.equalsIgnoreCase("Drill II")) return 2;
                    if (clean.toLowerCase().startsWith("drill ")) {
                        String roman = clean.substring(6).trim();
                        int parsed = fromRoman(roman);
                        if (parsed > 0) return parsed;
                    }
                }
            }
        }
        return 0;
    }

    public boolean isEnabled(ItemStack item) {
        if (!hasDrill(item)) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return true;

        if (XDrillVersion.HAS_PDC && keyEnabled != null) {
            try {
                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                Integer enabled = pdc.get(keyEnabled, PersistentDataType.INTEGER);
                return enabled == null || enabled == 1;
            } catch (Throwable ignored) {}
        }
        return true;
    }

    public void setEnabled(ItemStack item, boolean enabled) {
        if (!hasDrill(item)) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        if (XDrillVersion.HAS_PDC && keyEnabled != null) {
            try {
                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                pdc.set(keyEnabled, PersistentDataType.INTEGER, enabled ? 1 : 0);
                item.setItemMeta(meta);
            } catch (Throwable ignored) {}
        }
    }

    public String getMode(ItemStack item) {
        if (!hasDrill(item)) return MODE_3X3;
        int level = getLevel(item);
        if (level == 1) return MODE_2X2;
        if (level >= 2) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && XDrillVersion.HAS_PDC && keyMode != null) {
                try {
                    String mode = meta.getPersistentDataContainer().get(keyMode, PersistentDataType.STRING);
                    if (mode != null) return mode;
                } catch (Throwable ignored) {}
            }
            return MODE_3X3;
        }
        return MODE_3X3;
    }

    public void setMode(ItemStack item, String mode) {
        if (getLevel(item) < 2) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        if (XDrillVersion.HAS_PDC && keyMode != null) {
            try {
                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                pdc.set(keyMode, PersistentDataType.STRING, mode);
                item.setItemMeta(meta);
            } catch (Throwable ignored) {}
        }
    }

    public boolean toggleEnabled(ItemStack item) {
        boolean current = isEnabled(item);
        boolean newState = !current;
        setEnabled(item, newState);
        return newState;
    }

    public String toggleMode(ItemStack item) {
        String current = getMode(item);
        String next = current.equals(MODE_2X2) ? MODE_3X3 : MODE_2X2;
        setMode(item, next);
        return next;
    }

    public ItemStack applyDrill(ItemStack item, int level) {
        if (item == null || item.getType().name().endsWith("AIR")) {
            return item;
        }
        if (level <= 0) {
            return removeDrill(item);
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        XDrillVersion.setPdcLevel(meta, keyLevel, level);
        XDrillVersion.setGlint(meta);

        if (XDrillVersion.HAS_PDC) {
            try {
                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                if (keyEnabled != null) pdc.set(keyEnabled, PersistentDataType.INTEGER, 1);
                if (keyMode != null && !pdc.has(keyMode, PersistentDataType.STRING)) {
                    pdc.set(keyMode, PersistentDataType.STRING, level == 1 ? MODE_2X2 : MODE_3X3);
                }
            } catch (Throwable ignored) {}
        }

        // Update Lore
        String drillLine = "§7Drill " + toRoman(level);

        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        Iterator<String> it = lore.iterator();
        while (it.hasNext()) {
            String clean = ChatColor.stripColor(it.next()).trim();
            if (clean.toLowerCase().startsWith("drill ")) {
                it.remove();
            }
        }
        lore.add(0, drillLine);
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack removeDrill(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return item;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        XDrillVersion.removePdcLevel(meta, keyLevel);
        if (XDrillVersion.HAS_PDC) {
            try {
                if (keyMode != null) meta.getPersistentDataContainer().remove(keyMode);
                if (keyEnabled != null) meta.getPersistentDataContainer().remove(keyEnabled);
            } catch (Throwable ignored) {}
        }

        if (meta.hasLore()) {
            List<String> lore = new ArrayList<>(meta.getLore());
            Iterator<String> it = lore.iterator();
            while (it.hasNext()) {
                String clean = ChatColor.stripColor(it.next()).trim();
                if (clean.toLowerCase().startsWith("drill ")) {
                    it.remove();
                }
            }
            meta.setLore(lore);
        }

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createBook(int level) {
        ItemStack book = new ItemStack(XDrillVersion.getEnchantedBookMaterial(), 1);
        ItemMeta meta = book.getItemMeta();
        if (meta != null) {
            boolean isDe = plugin.getConfigManager() != null && "de".equalsIgnoreCase(plugin.getConfigManager().getLanguage());
            meta.setDisplayName(XDrillVersion.colorize(isDe ? "&bVerzaubertes Buch" : "&bEnchanted Book"));
            book.setItemMeta(meta);
        }
        return applyDrill(book, level);
    }

    public boolean isApplicable(ItemStack item) {
        if (item == null || item.getType().name().endsWith("AIR")) {
            return false;
        }
        String name = item.getType().name();
        ConfigManager config = plugin.getConfigManager();

        if (config.isPickaxeAllowed() && name.endsWith("_PICKAXE")) return true;
        if (config.isShovelAllowed() && name.endsWith("_SHOVEL")) return true;
        if (config.isHoeAllowed() && name.endsWith("_HOE")) return true;
        if (name.equals("BOOK") || name.equals("ENCHANTED_BOOK")) return true;

        return false;
    }

    public static String toRoman(int n) {
        switch (n) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            default: return String.valueOf(n);
        }
    }

    public static int fromRoman(String roman) {
        if (roman == null) return 0;
        switch (roman.toUpperCase().trim()) {
            case "I": return 1;
            case "II": return 2;
            case "III": return 3;
            default:
                try {
                    return Integer.parseInt(roman);
                } catch (NumberFormatException e) {
                    return 0;
                }
        }
    }
}
