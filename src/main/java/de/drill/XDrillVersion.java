package de.drill;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class XDrillVersion {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    public static final boolean HAS_PDC;
    public static final Material FARMLAND_MAT;

    static {
        boolean pdc = false;
        try {
            Class.forName("org.bukkit.persistence.PersistentDataContainer");
            pdc = true;
        } catch (Throwable ignored) {}
        HAS_PDC = pdc;

        Material f = Material.matchMaterial("FARMLAND");
        if (f == null) {
            f = Material.matchMaterial("SOIL");
        }
        FARMLAND_MAT = (f != null) ? f : Material.DIRT;
    }

    private XDrillVersion() {}

    public static Material getEnchantedBookMaterial() {
        Material m = Material.matchMaterial("ENCHANTED_BOOK");
        return m != null ? m : Material.BOOK;
    }

    public static ItemStack getItemInHand(Player player) {
        if (player == null) return null;
        try {
            return player.getInventory().getItemInMainHand();
        } catch (NoSuchMethodError e) {
            try {
                Method m = player.getClass().getMethod("getItemInHand");
                return (ItemStack) m.invoke(player);
            } catch (Throwable ex) {
                return null;
            }
        }
    }

    public static void setItemInHand(Player player, ItemStack item) {
        if (player == null) return;
        try {
            player.getInventory().setItemInMainHand(item);
        } catch (NoSuchMethodError e) {
            try {
                Method m = player.getClass().getMethod("setItemInHand", ItemStack.class);
                m.invoke(player, item);
            } catch (Throwable ignored) {}
        }
    }

    public static String colorize(String text) {
        if (text == null || text.isEmpty()) return "";
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("&x");
            for (char c : hex.toCharArray()) {
                replacement.append('&').append(c);
            }
            matcher.appendReplacement(sb, replacement.toString());
        }
        matcher.appendTail(sb);
        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }

    public static void playSound(Player player, String primarySound, String fallbackSound, float volume, float pitch) {
        if (player == null) return;
        try {
            Sound s = Sound.valueOf(primarySound);
            player.playSound(player.getLocation(), s, volume, pitch);
            return;
        } catch (Throwable ignored) {}

        if (fallbackSound != null) {
            try {
                Sound s = Sound.valueOf(fallbackSound);
                player.playSound(player.getLocation(), s, volume, pitch);
            } catch (Throwable ignored) {}
        }
    }

    public static void setGlint(ItemMeta meta) {
        if (meta == null) return;
        try {
            Method m = meta.getClass().getMethod("setEnchantmentGlintOverride", boolean.class);
            m.invoke(meta, true);
            return;
        } catch (Throwable ignored) {}

        try {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } catch (Throwable ignored) {}
    }

    public static void setPdcLevel(ItemMeta meta, NamespacedKey key, int level) {
        if (meta == null || key == null || !HAS_PDC) return;
        try {
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(key, PersistentDataType.INTEGER, level);
        } catch (Throwable ignored) {}
    }

    public static int getPdcLevel(ItemMeta meta, NamespacedKey key) {
        if (meta == null || key == null || !HAS_PDC) return 0;
        try {
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            Integer val = pdc.get(key, PersistentDataType.INTEGER);
            return val != null ? val : 0;
        } catch (Throwable ignored) {
            return 0;
        }
    }

    public static void removePdcLevel(ItemMeta meta, NamespacedKey key) {
        if (meta == null || key == null || !HAS_PDC) return;
        try {
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.remove(key);
        } catch (Throwable ignored) {}
    }

    public static int getItemDamage(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable) {
            return ((Damageable) meta).getDamage();
        }
        return item.getDurability();
    }

    public static void setItemDamage(ItemStack item, int damage) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable) {
            ((Damageable) meta).setDamage(damage);
            item.setItemMeta(meta);
        } else {
            item.setDurability((short) damage);
        }
    }
}
