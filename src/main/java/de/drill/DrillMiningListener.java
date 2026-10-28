package de.drill;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DrillMiningListener implements Listener {

    private final DrillPlugin plugin;
    private final DrillEnchantment drillEnchantment;
    private final ThreadLocal<Boolean> isProcessingDrill = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    private final Map<UUID, BlockFace> lastClickedFace = new ConcurrentHashMap<>();
    private final Map<UUID, Vector> lastHitPosition = new ConcurrentHashMap<>();

    public DrillMiningListener(DrillPlugin plugin, DrillEnchantment drillEnchantment) {
        this.plugin = plugin;
        this.drillEnchantment = drillEnchantment;
    }

    private Vector getSafeInteractionPoint(PlayerInteractEvent event) {
        try {
            Method m = event.getClass().getMethod("getInteractionPoint");
            Location loc = (Location) m.invoke(event);
            if (loc != null) return loc.toVector();
        } catch (Throwable ignored) {}
        return null;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();

        if (action == Action.LEFT_CLICK_BLOCK) {
            if (event.getBlockFace() != null) {
                lastClickedFace.put(player.getUniqueId(), event.getBlockFace());
            }
            Vector point = getSafeInteractionPoint(event);
            if (point != null) {
                lastHitPosition.put(player.getUniqueId(), point);
            }
            return;
        }

        // Hoe Tilling Mechanic (Right click with Hoe)
        if (action == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            if (!plugin.getConfigManager().isHoeTillingEnabled()) {
                return;
            }

            ItemStack item = XDrillVersion.getItemInHand(player);
            if (item == null || !drillEnchantment.hasDrill(item) || !drillEnchantment.isEnabled(item)) {
                return;
            }

            String matName = item.getType().name();
            if (!matName.endsWith("_HOE")) {
                return;
            }

            Block origin = event.getClickedBlock();
            if (!isTillable(origin.getType())) {
                return;
            }

            if (event.getBlockFace() != BlockFace.UP) {
                return;
            }

            String mode = drillEnchantment.getMode(item);
            if (mode == null) {
                mode = DrillEnchantment.MODE_3X3;
            }

            Vector hit = getSafeInteractionPoint(event);
            tillArea(player, item, origin, mode, hit);
        }
    }

    private void tillArea(Player player, ItemStack hoe, Block origin, String mode, Vector hit) {
        int[][] offsets = calculateHorizontalOffsets(origin, mode, hit, player);
        World world = origin.getWorld();
        int tilledCount = 0;

        for (int[] offset : offsets) {
            Block target = origin.getRelative(offset[0], 0, offset[1]);
            if (!isTillable(target.getType())) {
                continue;
            }

            Block above = target.getRelative(BlockFace.UP);
            if (!above.getType().name().endsWith("AIR")) {
                if (isReplaceablePlant(above.getType())) {
                    if (plugin.getConfigManager().isHoeBreakReplaceablePlants()) {
                        above.breakNaturally();
                    } else {
                        continue;
                    }
                } else {
                    continue;
                }
            }

            target.setType(XDrillVersion.FARMLAND_MAT);
            tilledCount++;

            if (player.getGameMode() != GameMode.CREATIVE && plugin.getConfigManager().isDamageToolPerBlock()) {
                if (shouldDamageTool(hoe)) {
                    damageTool(player, hoe);
                    if (plugin.getConfigManager().isStopBeforeBreak() && isToolBrokenOrNearBreak(hoe)) {
                        break;
                    }
                }
            }
        }

        if (tilledCount > 0) {
            XDrillVersion.playSound(player, "ITEM_HOE_TILL", "DIG_GRASS", 1.0f, 1.0f);
            try {
                player.getClass().getMethod("swingMainHand").invoke(player);
            } catch (Throwable ignored) {}
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (isProcessingDrill.get()) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack tool = XDrillVersion.getItemInHand(player);
        if (tool == null) return;

        if (tool.getType().name().endsWith("AIR") || !drillEnchantment.hasDrill(tool) || !drillEnchantment.isEnabled(tool)) {
            return;
        }

        Block origin = event.getBlock();
        if (origin.getType().name().endsWith("AIR")) {
            return;
        }

        if (!drillEnchantment.isApplicable(tool)) {
            return;
        }

        if (!isToolEffectiveOn(tool, origin.getType())) {
            return;
        }

        String mode = drillEnchantment.getMode(tool);
        if (mode == null) {
            mode = DrillEnchantment.MODE_3X3;
        }

        BlockFace face = getTargetBlockFace(player, origin);
        Vector hit = lastHitPosition.get(player.getUniqueId());

        List<Block> targets = calculateBlocksToBreak(player, origin, face, mode, hit, tool);

        isProcessingDrill.set(true);
        try {
            for (Block target : targets) {
                if (target.equals(origin)) continue;
                if (target.getType().name().endsWith("AIR")) continue;

                if (player.getGameMode() != GameMode.CREATIVE && plugin.getConfigManager().isStopBeforeBreak() && isToolBrokenOrNearBreak(tool)) {
                    break;
                }

                if (!isToolEffectiveOn(tool, target.getType())) {
                    continue;
                }

                breakBlock(player, target, tool);
            }
        } finally {
            isProcessingDrill.set(false);
        }
    }

    private void breakBlock(Player player, Block target, ItemStack tool) {
        try {
            Method m = player.getClass().getMethod("breakBlock", Block.class);
            m.invoke(player, target);
            return;
        } catch (Throwable ignored) {}

        BlockBreakEvent breakEvent = new BlockBreakEvent(target, player);
        Bukkit.getPluginManager().callEvent(breakEvent);
        if (!breakEvent.isCancelled()) {
            target.breakNaturally(tool);
        }
    }

    private BlockFace getTargetBlockFace(Player player, Block origin) {
        BlockFace cached = lastClickedFace.get(player.getUniqueId());
        if (cached != null) {
            return cached;
        }

        try {
            Method rt = player.getClass().getMethod("rayTraceBlocks", double.class);
            Object ray = rt.invoke(player, 5.0);
            if (ray != null) {
                Method getFace = ray.getClass().getMethod("getHitBlockFace");
                BlockFace bf = (BlockFace) getFace.invoke(ray);
                if (bf != null) return bf;
            }
        } catch (Throwable ignored) {}

        float pitch = player.getLocation().getPitch();
        if (pitch > 45) return BlockFace.UP;
        if (pitch < -45) return BlockFace.DOWN;

        try {
            Method getFacing = player.getClass().getMethod("getFacing");
            BlockFace facing = (BlockFace) getFacing.invoke(player);
            if (facing != null) return facing.getOppositeFace();
        } catch (Throwable ignored) {}

        return BlockFace.UP;
    }

    private List<Block> calculateBlocksToBreak(Player player, Block origin, BlockFace face, String mode, Vector hit, ItemStack tool) {
        List<Block> list = new ArrayList<>();
        boolean is3x3 = DrillEnchantment.MODE_3X3.equals(mode);

        if (face == BlockFace.UP || face == BlockFace.DOWN) {
            if (is3x3) {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        list.add(origin.getRelative(dx, 0, dz));
                    }
                }
            } else {
                int[] xOff = get2x2Offset(hit != null ? hit.getX() : null, origin.getX(), player.getLocation().getYaw(), true);
                int[] zOff = get2x2Offset(hit != null ? hit.getZ() : null, origin.getZ(), player.getLocation().getYaw(), false);
                for (int dx : xOff) {
                    for (int dz : zOff) {
                        list.add(origin.getRelative(dx, 0, dz));
                    }
                }
            }
        } else if (face == BlockFace.NORTH || face == BlockFace.SOUTH) {
            if (is3x3) {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        list.add(origin.getRelative(dx, dy, 0));
                    }
                }
            } else {
                int[] xOff = get2x2Offset(hit != null ? hit.getX() : null, origin.getX(), player.getLocation().getYaw(), true);
                int[] yOff = get2x2VerticalOffset(hit != null ? hit.getY() : null, origin.getY(), player.getEyeLocation().getY());
                for (int dx : xOff) {
                    for (int dy : yOff) {
                        list.add(origin.getRelative(dx, dy, 0));
                    }
                }
            }
        } else {
            if (is3x3) {
                for (int dz = -1; dz <= 1; dz++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        list.add(origin.getRelative(0, dy, dz));
                    }
                }
            } else {
                int[] zOff = get2x2Offset(hit != null ? hit.getZ() : null, origin.getZ(), player.getLocation().getYaw(), false);
                int[] yOff = get2x2VerticalOffset(hit != null ? hit.getY() : null, origin.getY(), player.getEyeLocation().getY());
                for (int dz : zOff) {
                    for (int dy : yOff) {
                        list.add(origin.getRelative(0, dy, dz));
                    }
                }
            }
        }

        return list;
    }

    private int[][] calculateHorizontalOffsets(Block origin, String mode, Vector hit, Player player) {
        if (DrillEnchantment.MODE_3X3.equals(mode)) {
            int[][] offsets = new int[9][2];
            int idx = 0;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    offsets[idx++] = new int[]{dx, dz};
                }
            }
            return offsets;
        } else {
            int[] xOff = get2x2Offset(hit != null ? hit.getX() : null, origin.getX(), player.getLocation().getYaw(), true);
            int[] zOff = get2x2Offset(hit != null ? hit.getZ() : null, origin.getZ(), player.getLocation().getYaw(), false);
            return new int[][]{
                    {xOff[0], zOff[0]},
                    {xOff[0], zOff[1]},
                    {xOff[1], zOff[0]},
                    {xOff[1], zOff[1]}
            };
        }
    }

    private int[] get2x2Offset(Double hitCoord, int blockCoord, float playerYaw, boolean isXAxis) {
        if (hitCoord != null) {
            double frac = hitCoord - blockCoord;
            return frac >= 0.5 ? new int[]{0, 1} : new int[]{-1, 0};
        }

        float normYaw = (playerYaw % 360 + 360) % 360;
        if (isXAxis) {
            return (normYaw >= 180 && normYaw <= 360) ? new int[]{0, 1} : new int[]{-1, 0};
        } else {
            return (normYaw <= 90 || normYaw >= 270) ? new int[]{0, 1} : new int[]{-1, 0};
        }
    }

    private int[] get2x2VerticalOffset(Double hitY, int blockY, double playerEyeY) {
        if (hitY != null) {
            double frac = hitY - blockY;
            return frac >= 0.5 ? new int[]{0, 1} : new int[]{-1, 0};
        }
        return playerEyeY > blockY + 0.5 ? new int[]{-1, 0} : new int[]{0, 1};
    }

    private boolean isTillable(Material mat) {
        if (mat == null) return false;
        String name = mat.name();
        return name.contains("GRASS") || name.contains("DIRT") || name.contains("PATH");
    }

    private boolean isReplaceablePlant(Material mat) {
        if (mat == null) return false;
        String name = mat.name();
        return name.contains("GRASS") || name.contains("FERN") || name.contains("FLOWER") ||
               name.contains("TULIP") || name.contains("DANDELION") || name.contains("POPPY") ||
               name.contains("BUSH") || name.contains("ROSE");
    }

    private boolean isToolEffectiveOn(ItemStack tool, Material blockMat) {
        String toolName = tool.getType().name();
        String matName = blockMat.name();

        if (toolName.endsWith("_PICKAXE")) {
            return matName.contains("STONE") || matName.contains("DEEPSLATE") ||
                   matName.contains("ORE") || matName.contains("GRANITE") ||
                   matName.contains("DIORITE") || matName.contains("ANDESITE") ||
                   matName.contains("TUFF") || matName.contains("BASALT") ||
                   matName.contains("BLACKSTONE") || matName.contains("NETHERRACK") ||
                   matName.contains("END_STONE") || matName.contains("OBSIDIAN") ||
                   matName.contains("BRICK") || matName.contains("TERRACOTTA") ||
                   matName.contains("CONCRETE") || matName.contains("SANDSTONE") ||
                   matName.contains("PRISMARINE") || matName.contains("QUARTZ") ||
                   matName.contains("COPPER") || matName.contains("IRON") ||
                   matName.contains("GOLD") || matName.contains("ANVIL");
        }

        if (toolName.endsWith("_SHOVEL")) {
            return matName.contains("DIRT") || matName.contains("GRASS") ||
                   matName.contains("SAND") || matName.contains("GRAVEL") ||
                   matName.contains("CLAY") || matName.contains("SNOW") ||
                   matName.contains("SOUL") || matName.contains("MUD") ||
                   matName.contains("CONCRETE_POWDER");
        }

        if (toolName.endsWith("_AXE")) {
            return matName.contains("LOG") || matName.contains("WOOD") ||
                   matName.contains("PLANKS") || matName.contains("LEAVES") ||
                   matName.contains("BAMBOO") || matName.contains("BOOKSHELF") ||
                   matName.contains("CHEST") || matName.contains("BARREL") ||
                   matName.contains("FENCE") || matName.contains("GATE") ||
                   matName.contains("DOOR") || matName.contains("TRAPDOOR") ||
                   matName.contains("SLAB") || matName.contains("STAIRS") ||
                   matName.contains("SIGN");
        }

        if (toolName.endsWith("_HOE")) {
            return matName.contains("HAY") || matName.contains("LEAVES") ||
                   matName.contains("WART") || matName.contains("SCULK") ||
                   matName.contains("MOSS") || matName.contains("SPONGE") ||
                   matName.contains("TARGET") || matName.contains("SHROOMLIGHT");
        }

        return false;
    }

    private boolean isToolBrokenOrNearBreak(ItemStack tool) {
        if (tool == null || tool.getType().name().endsWith("AIR")) return true;
        int max = tool.getType().getMaxDurability();
        if (max > 0) {
            int damage = XDrillVersion.getItemDamage(tool);
            return damage >= (max - 2);
        }
        return false;
    }

    private static final org.bukkit.enchantments.Enchantment UNBREAKING_ENCH;
    static {
        org.bukkit.enchantments.Enchantment e = org.bukkit.enchantments.Enchantment.getByName("DURABILITY");
        if (e == null) e = org.bukkit.enchantments.Enchantment.getByName("UNBREAKING");
        UNBREAKING_ENCH = e;
    }

    private boolean shouldDamageTool(ItemStack tool) {
        int unbreaking = UNBREAKING_ENCH != null ? tool.getEnchantmentLevel(UNBREAKING_ENCH) : 0;
        if (unbreaking <= 0) return true;
        return Math.random() < (1.0 / (unbreaking + 1));
    }

    private void damageTool(Player player, ItemStack tool) {
        int max = tool.getType().getMaxDurability();
        int newDmg = XDrillVersion.getItemDamage(tool) + 1;
        XDrillVersion.setItemDamage(tool, newDmg);

        if (max > 0 && newDmg >= max) {
            XDrillVersion.setItemInHand(player, null);
            XDrillVersion.playSound(player, "ENTITY_ITEM_BREAK", "ITEM_BREAK", 1.0f, 1.0f);
        }
    }
}
