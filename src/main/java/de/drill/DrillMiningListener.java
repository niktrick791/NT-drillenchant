package de.drill;

import org.bukkit.*;
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
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DrillMiningListener implements Listener {

    private final DrillPlugin plugin;
    private final DrillEnchantment drillEnchantment;
    private final ThreadLocal<Boolean> isProcessingDrill = ThreadLocal.withInitial(() -> false);

    private final Map<UUID, BlockFace> lastClickedFace = new ConcurrentHashMap<>();
    private final Map<UUID, Vector> lastHitPosition = new ConcurrentHashMap<>();

    public DrillMiningListener(DrillPlugin plugin, DrillEnchantment drillEnchantment) {
        this.plugin = plugin;
        this.drillEnchantment = drillEnchantment;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();

        if (action == Action.LEFT_CLICK_BLOCK) {
            if (event.getBlockFace() != null) {
                lastClickedFace.put(player.getUniqueId(), event.getBlockFace());
            }
            if (event.getInteractionPoint() != null) {
                lastHitPosition.put(player.getUniqueId(), event.getInteractionPoint().toVector());
            }
            return;
        }

        // Hoe Tilling Mechanic (Right click with Hoe)
        if (action == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            if (!plugin.getConfigManager().isHoeTillingEnabled()) {
                return;
            }

            ItemStack item = event.getItem();
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

            Vector hit = event.getInteractionPoint() != null ? event.getInteractionPoint().toVector() : null;
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
            if (!above.getType().isAir()) {
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

            Material currentType = target.getType();
            if (currentType == Material.COARSE_DIRT) {
                target.setType(Material.DIRT);
            } else if (currentType == Material.ROOTED_DIRT) {
                target.setType(Material.DIRT);
                world.dropItemNaturally(target.getLocation().add(0.5, 1.0, 0.5), new ItemStack(Material.HANGING_ROOTS));
            } else {
                target.setType(Material.FARMLAND);
            }

            world.spawnParticle(Particle.BLOCK, target.getLocation().add(0.5, 1.0, 0.5), 6, 0.2, 0.1, 0.2, target.getBlockData());
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
            world.playSound(origin.getLocation(), Sound.ITEM_HOE_TILL, 1.0f, 1.0f);
            player.swingMainHand();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (isProcessingDrill.get()) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();

        if (tool.getType().isAir() || !drillEnchantment.hasDrill(tool) || !drillEnchantment.isEnabled(tool)) {
            return;
        }

        Block origin = event.getBlock();
        if (origin.getType().isAir() || origin.getType().getHardness() < 0) {
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
                if (target.getType().isAir() || target.getType().getHardness() < 0) continue;

                if (player.getGameMode() != GameMode.CREATIVE && plugin.getConfigManager().isStopBeforeBreak() && isToolBrokenOrNearBreak(tool)) {
                    break;
                }

                if (!isToolEffectiveOn(tool, target.getType())) {
                    continue;
                }

                player.breakBlock(target);
            }
        } finally {
            isProcessingDrill.set(false);
        }
    }

    private BlockFace getTargetBlockFace(Player player, Block origin) {
        BlockFace cached = lastClickedFace.get(player.getUniqueId());
        if (cached != null) {
            return cached;
        }

        RayTraceResult ray = player.rayTraceBlocks(5.0);
        if (ray != null && ray.getHitBlockFace() != null) {
            return ray.getHitBlockFace();
        }

        float pitch = player.getLocation().getPitch();
        if (pitch > 45) return BlockFace.UP;
        if (pitch < -45) return BlockFace.DOWN;

        return player.getFacing().getOppositeFace();
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
        return mat == Material.GRASS_BLOCK || mat == Material.DIRT_PATH ||
               mat == Material.DIRT || mat == Material.COARSE_DIRT || mat == Material.ROOTED_DIRT;
    }

    private boolean isReplaceablePlant(Material mat) {
        return mat == Material.SHORT_GRASS || mat == Material.TALL_GRASS ||
               mat == Material.FERN || mat == Material.LARGE_FERN ||
               mat == Material.DANDELION || mat == Material.POPPY ||
               mat == Material.BLUE_ORCHID || mat == Material.ALLIUM ||
               mat == Material.AZURE_BLUET || mat == Material.RED_TULIP ||
               mat == Material.ORANGE_TULIP || mat == Material.WHITE_TULIP ||
               mat == Material.PINK_TULIP || mat == Material.OXEYE_DAISY ||
               mat == Material.CORNFLOWER || mat == Material.LILY_OF_THE_VALLEY ||
               mat == Material.TORCHFLOWER || mat == Material.DEAD_BUSH;
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
            return matName.contains("DIRT") || matName.contains("GRASS_BLOCK") ||
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
        if (tool == null || tool.getType().isAir()) return true;
        ItemMeta meta = tool.getItemMeta();
        if (meta instanceof Damageable dmg) {
            int max = tool.getType().getMaxDurability();
            if (max > 0) {
                return dmg.getDamage() >= (max - 2);
            }
        }
        return false;
    }

    private boolean shouldDamageTool(ItemStack tool) {
        int unbreaking = tool.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.UNBREAKING);
        if (unbreaking <= 0) return true;
        return Math.random() < (1.0 / (unbreaking + 1));
    }

    private void damageTool(Player player, ItemStack tool) {
        ItemMeta meta = tool.getItemMeta();
        if (meta instanceof Damageable dmg) {
            dmg.setDamage(dmg.getDamage() + 1);
            tool.setItemMeta(meta);

            if (dmg.getDamage() >= tool.getType().getMaxDurability()) {
                player.getInventory().setItemInMainHand(null);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            }
        }
    }
}
