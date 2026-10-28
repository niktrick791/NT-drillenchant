package de.drill;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class DrillCommand implements CommandExecutor, TabCompleter {

    private final DrillPlugin plugin;
    private final DrillEnchantment drillEnchantment;

    public DrillCommand(DrillPlugin plugin, DrillEnchantment drillEnchantment) {
        this.plugin = plugin;
        this.drillEnchantment = drillEnchantment;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            if (args.length > 0 && (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("give"))) {
                return handleAdminConsole(sender, args);
            }
            sender.sendMessage("§cDieser Befehl kann nur als Spieler ausgeführt werden.");
            return true;
        }

        if (args.length == 0) {
            return handleDefaultSwitch(player);
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        if (sub.equals("on") || sub.equals("enable")) {
            return setDrillEnabled(player, true);
        }

        if (sub.equals("off") || sub.equals("disable")) {
            return setDrillEnabled(player, false);
        }

        if (sub.equals("toggle")) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            int level = drillEnchantment.getLevel(hand);
            if (level == 0) {
                player.sendMessage("§8[§6Drill§8] §cBitte halte ein Werkzeug mit Drill in der Haupthand.");
                return true;
            }
            boolean newState = drillEnchantment.toggleEnabled(hand);
            if (newState) {
                player.sendMessage("§8[§6Drill§8] §aDrill-Funktion §e§lAKTIVIERT§a! (Modus: " + drillEnchantment.getMode(hand) + ")");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            } else {
                player.sendMessage("§8[§6Drill§8] §cDrill-Funktion §4§lDEAKTIVIERT§c! (Normales 1x1 Abbauen aktiv)");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 0.8f);
            }
            return true;
        }

        if (sub.equals("switch") || sub.equals("mode")) {
            if (args.length >= 2) {
                String option = args[1].toLowerCase(Locale.ROOT);
                if (option.equals("on") || option.equals("enable")) {
                    return setDrillEnabled(player, true);
                }
                if (option.equals("off") || option.equals("disable")) {
                    return setDrillEnabled(player, false);
                }
                if (option.equals("toggle")) {
                    ItemStack hand = player.getInventory().getItemInMainHand();
                    int level = drillEnchantment.getLevel(hand);
                    if (level == 0) {
                        player.sendMessage("§8[§6Drill§8] §cBitte halte ein Werkzeug mit Drill in der Haupthand.");
                        return true;
                    }
                    boolean newState = drillEnchantment.toggleEnabled(hand);
                    if (newState) {
                        player.sendMessage("§8[§6Drill§8] §aDrill-Funktion §e§lAKTIVIERT§a! (Modus: " + drillEnchantment.getMode(hand) + ")");
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                    } else {
                        player.sendMessage("§8[§6Drill§8] §cDrill-Funktion §4§lDEAKTIVIERT§c! (Normales 1x1 Abbauen aktiv)");
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 0.8f);
                    }
                    return true;
                }
                if (option.equals("2x2") || option.equals("3x3")) {
                    ItemStack hand = player.getInventory().getItemInMainHand();
                    int level = drillEnchantment.getLevel(hand);
                    if (level == 0) {
                        player.sendMessage("§8[§6Drill§8] §cBitte halte ein Werkzeug mit Drill in der Haupthand.");
                        return true;
                    }
                    if (level < 3) {
                        player.sendMessage("§8[§6Drill§8] §cNur Werkzeuge mit Drill III können zwischen 2x2 und 3x3 wechseln! " +
                                           "(Drill I = 2x2, Drill II = 3x3)");
                        return true;
                    }
                    drillEnchantment.setMode(hand, option);
                    drillEnchantment.setEnabled(hand, true);
                    player.sendMessage("§8[§6Drill§8] §aDrill-Modus gewechselt auf: §e§l" + option);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                    return true;
                }
            }
            return handleDefaultSwitch(player);
        }

        if (sub.equals("info") || sub.equals("help")) {
            player.sendMessage("§6§l================== Drill Enchantment ==================");
            player.sendMessage("§eDrill I:   §7Baut Blöcke / bestellt Felder in §b2x2§7 ab.");
            player.sendMessage("§eDrill II:  §7Baut Blöcke / bestellt Felder in §b3x3§7 ab.");
            player.sendMessage("§eDrill III: §7Baut in 2x2 oder 3x3 ab. Wechseln mit §b/drill switch§7!");
            player.sendMessage("§eAn/Aus:    §7Jeder Drill kann mit §b/drill on§7 / §b/drill off§7 oder §b/drill toggle§7 ein- und ausgeschaltet werden.");
            player.sendMessage("§eTools:     §aSpitzhacke, Schaufel, Axt, Hacke §8(keine Schwerter!)");
            if (player.hasPermission("drill.admin")) {
                player.sendMessage("§6Admin-Befehle:");
                player.sendMessage("§e/drill book [1-3] §7- Gibt dir ein Verzaubertes Buch");
                player.sendMessage("§e/drill give <Spieler> [1-3] §7- Gibt einem Spieler ein Buch");
                player.sendMessage("§e/drill reload §7- Lädt die Konfiguration neu");
            }
            player.sendMessage("§6§l=====================================================");
            return true;
        }

        if (sub.equals("book")) {
            if (!player.hasPermission("drill.admin")) {
                player.sendMessage("§8[§6Drill§8] §cDazu hast du keine Berechtigung.");
                return true;
            }

            int level = 1;
            if (args.length >= 2) {
                try {
                    level = Math.max(1, Math.min(plugin.getConfigManager().getMaxLevel(), Integer.parseInt(args[1])));
                } catch (NumberFormatException ignored) {}
            }

            ItemStack book = drillEnchantment.createBook(level);
            player.getInventory().addItem(book);
            player.sendMessage("§8[§6Drill§8] §aDu hast ein Verzaubertes Buch (§eDrill " + DrillEnchantment.toRoman(level) + "§a) erhalten!");
            return true;
        }

        if (sub.equals("give")) {
            if (!player.hasPermission("drill.admin")) {
                player.sendMessage("§8[§6Drill§8] §cDazu hast du keine Berechtigung.");
                return true;
            }

            if (args.length < 2) {
                player.sendMessage("§8[§6Drill§8] §cVerwendung: /drill give <Spieler> [1-3]");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null || !target.isOnline()) {
                player.sendMessage("§8[§6Drill§8] §cSpieler nicht gefunden.");
                return true;
            }

            int level = 1;
            if (args.length >= 3) {
                try {
                    level = Math.max(1, Math.min(plugin.getConfigManager().getMaxLevel(), Integer.parseInt(args[2])));
                } catch (NumberFormatException ignored) {}
            }

            ItemStack book = drillEnchantment.createBook(level);
            target.getInventory().addItem(book);
            target.sendMessage("§8[§6Drill§8] §aDu hast ein Verzaubertes Buch (§eDrill " + DrillEnchantment.toRoman(level) + "§a) erhalten!");
            player.sendMessage("§8[§6Drill§8] §a" + target.getName() + " hat ein Drill " + DrillEnchantment.toRoman(level) + " Buch erhalten.");
            return true;
        }

        if (sub.equals("reload")) {
            if (!player.hasPermission("drill.admin")) {
                player.sendMessage("§8[§6Drill§8] §cDazu hast du keine Berechtigung.");
                return true;
            }

            plugin.getConfigManager().load();
            player.sendMessage("§8[§6Drill§8] §aKonfiguration erfolgreich neu geladen.");
            return true;
        }

        player.sendMessage("§8[§6Drill§8] §cUnbekannter Befehl. Verwende §e/drill info §coder §e/drill switch [on|off|2x2|3x3]");
        return true;
    }

    private boolean setDrillEnabled(Player player, boolean enabled) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        int level = drillEnchantment.getLevel(hand);

        if (level == 0) {
            player.sendMessage("§8[§6Drill§8] §cBitte halte ein Werkzeug mit Drill in der Haupthand.");
            return true;
        }

        drillEnchantment.setEnabled(hand, enabled);
        if (enabled) {
            player.sendMessage("§8[§6Drill§8] §aDrill-Funktion §e§lAKTIVIERT§a! (Modus: " + drillEnchantment.getMode(hand) + ")");
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
        } else {
            player.sendMessage("§8[§6Drill§8] §cDrill-Funktion §4§lDEAKTIVIERT§c! (Normales 1x1 Abbauen aktiv)");
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 0.8f);
        }
        return true;
    }

    private boolean handleDefaultSwitch(Player player) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        int level = drillEnchantment.getLevel(hand);

        if (level == 0) {
            player.sendMessage("§8[§6Drill§8] §cBitte halte ein Werkzeug mit Drill in der Haupthand.");
            return true;
        }

        if (level < 3) {
            boolean newState = drillEnchantment.toggleEnabled(hand);
            if (newState) {
                player.sendMessage("§8[§6Drill§8] §aDrill-Funktion §e§lAKTIVIERT§a! (Modus: " + drillEnchantment.getMode(hand) + ")");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            } else {
                player.sendMessage("§8[§6Drill§8] §cDrill-Funktion §4§lDEAKTIVIERT§c! (Normales 1x1 Abbauen aktiv)");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 0.8f);
            }
            return true;
        }

        if (!drillEnchantment.isEnabled(hand)) {
            drillEnchantment.setEnabled(hand, true);
            player.sendMessage("§8[§6Drill§8] §aDrill-Funktion §e§lAKTIVIERT§a! (Modus: " + drillEnchantment.getMode(hand) + ")");
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            return true;
        }

        String newMode = drillEnchantment.toggleMode(hand);
        player.sendMessage("§8[§6Drill§8] §aDrill-Modus gewechselt auf: §e§l" + newMode);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
        return true;
    }

    private boolean handleAdminConsole(CommandSender sender, String[] args) {
        if (args[0].equalsIgnoreCase("reload")) {
            plugin.getConfigManager().load();
            sender.sendMessage("§8[§6Drill§8] §aKonfiguration erfolgreich neu geladen.");
            return true;
        }
        if (args[0].equalsIgnoreCase("give") && args.length >= 2) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cSpieler nicht gefunden.");
                return true;
            }
            int level = 1;
            if (args.length >= 3) {
                try { level = Integer.parseInt(args[2]); } catch (Exception ignored) {}
            }
            target.getInventory().addItem(drillEnchantment.createBook(level));
            sender.sendMessage("§aDrill " + level + " an " + target.getName() + " gegeben.");
            return true;
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>(List.of("switch", "toggle", "on", "off", "info"));
            if (sender.hasPermission("drill.admin")) {
                subs.add("book");
                subs.add("give");
                subs.add("reload");
            }
            return subs.stream().filter(s -> s.startsWith(args[0].toLowerCase(Locale.ROOT))).toList();
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            if (sub.equals("switch") || sub.equals("mode")) {
                return List.of("on", "off", "toggle", "2x2", "3x3").stream().filter(s -> s.startsWith(args[1].toLowerCase(Locale.ROOT))).toList();
            }
            if (sub.equals("book") && sender.hasPermission("drill.admin")) {
                return List.of("1", "2", "3").stream().filter(s -> s.startsWith(args[1])).toList();
            }
            if (sub.equals("give") && sender.hasPermission("drill.admin")) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                        .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(args[1].toLowerCase(Locale.ROOT))).toList();
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give") && sender.hasPermission("drill.admin")) {
            return List.of("1", "2", "3").stream().filter(s -> s.startsWith(args[2])).toList();
        }

        return Collections.emptyList();
    }
}
