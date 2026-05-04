package kite.autoharvest.mode;

import kite.autoharvest.config.AutoHarvestConfig;
import kite.autoharvest.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

public class PlantMode implements AutoMode {

    private static final Set<Item> CROP_SEEDS = Set.of(
            Items.WHEAT_SEEDS,
            Items.CARROT,
            Items.POTATO,
            Items.BEETROOT_SEEDS,
            Items.PUMPKIN_SEEDS,
            Items.MELON_SEEDS
    );
    private static final Item NETHER_WART_ITEM = Items.NETHER_WART;
    private static final Item SUGAR_CANE_ITEM = Items.SUGAR_CANE;
    private static final Item BAMBOO_ITEM = Items.BAMBOO;
    private static final Item COCOA_BEANS_ITEM = Items.COCOA_BEANS;
    private static final Item SWEET_BERRIES_ITEM = Items.SWEET_BERRIES;

    private static final Set<Block> SUGARCANE_BASE_BLOCKS = Set.of(
            Blocks.GRASS_BLOCK,
            Blocks.DIRT,
            Blocks.COARSE_DIRT,
            Blocks.ROOTED_DIRT,
            Blocks.SAND,
            Blocks.RED_SAND,
            Blocks.PODZOL,
            Blocks.SUSPICIOUS_SAND,
            Blocks.MYCELIUM,
            Blocks.MUD,
            Blocks.MUDDY_MANGROVE_ROOTS,
            Blocks.MOSS_BLOCK,
            Blocks.PALE_MOSS_BLOCK
    );
    private static final Set<Item> REFILLABLE_PLANT_ITEMS;

    static {
        Set<Item> set = new HashSet<>(CROP_SEEDS);
        set.add(NETHER_WART_ITEM);
        set.add(SUGAR_CANE_ITEM);
        set.add(BAMBOO_ITEM);
        set.add(COCOA_BEANS_ITEM);
        REFILLABLE_PLANT_ITEMS = Set.copyOf(set);
    }

    private static final Set<Block> JUNGLE_LOG_BLOCKS = Set.of(
            Blocks.JUNGLE_LOG,
            Blocks.STRIPPED_JUNGLE_LOG,
            Blocks.JUNGLE_WOOD,
            Blocks.STRIPPED_JUNGLE_WOOD
    );
    private static final Set<Block> SWEET_BERRY_PLANT = Set.of(
            Blocks.GRASS_BLOCK,
            Blocks.DIRT,
            Blocks.PODZOL,
            Blocks.COARSE_DIRT,
            Blocks.MYCELIUM,
            Blocks.MOSS_BLOCK,
            Blocks.PALE_MOSS_BLOCK,
            Blocks.ROOTED_DIRT,
            Blocks.MUD,
            Blocks.MUDDY_MANGROVE_ROOTS,
            Blocks.FARMLAND
    );

    private static boolean hasSeed(LocalPlayer player, Set<Item> seedItem) {
        return ItemSlotHelper.hasItem(player, seedItem);
    }


    @Override
    public void tick() {
        Minecraft client = Minecraft.getInstance();
        if ((client.player != null && !client.player.isCreative() && !client.player.isSpectator() && client.level != null && AutoHarvestConfig.enableRefill())) {
            ItemStack mainHandStack = client.player.getMainHandItem();
            ItemStack offHandStack = client.player.getOffhandItem();
            if (!mainHandStack.isEmpty() && mainHandStack.getCount() < 64 && REFILLABLE_PLANT_ITEMS.contains(mainHandStack.getItem())) {
                ItemRefillHelper.refillMainHand();
            }
            if (!offHandStack.isEmpty() && offHandStack.getCount() < 64 && REFILLABLE_PLANT_ITEMS.contains(offHandStack.getItem())) {
                ItemRefillHelper.refillOffHand();
            }
        }

        ClientLevel world = BoxUtil.getWorld();
        LocalPlayer player = BoxUtil.getPlayer();
        if (world == null || player == null) return;

        Vec3 playerPos = BoxUtil.getPlayerPos();
        if (playerPos == null) return;

        double radius = AutoHarvestConfig.getInstance().getRadius();
        AABB searchBox = BoxUtil.createSearchBox(playerPos, radius);
        int radiusInt = (int) Math.ceil(radius);

        for (BlockPos pos : BlockPos.withinManhattan(BlockPos.containing(playerPos), radiusInt, radiusInt, radiusInt)) {
            if (!searchBox.contains(pos.getCenter())) continue;
            if (BoxUtil.isInSphere(pos, playerPos, radius)) continue;

            if (!world.getBlockState(pos).isAir()) continue;

            BlockPos basePos = pos.below();
            Block baseBlock = world.getBlockState(basePos).getBlock();

            boolean canPlantCrop = (baseBlock == Blocks.FARMLAND) && hasSeed(player, CROP_SEEDS);
            boolean canPlantSugarcane = SUGARCANE_BASE_BLOCKS.contains(baseBlock)
                    && WaterProximityChecker.isAdjacentToSourceWaterHorizontally(world, basePos)
                    && hasSeed(player, Set.of(SUGAR_CANE_ITEM));
            boolean canPlantNetherWart = (baseBlock == Blocks.SOUL_SAND);
            boolean canPlantBamboo = SUGARCANE_BASE_BLOCKS.contains(baseBlock) && hasSeed(player, Set.of(BAMBOO_ITEM));
            Direction cocoaFacing = null;
            BlockPos cocoaLogPos = null;

            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos logPos = pos.offset(dir.getOpposite().getUnitVec3i());
                if (JUNGLE_LOG_BLOCKS.contains(world.getBlockState(logPos).getBlock())) {
                    cocoaFacing = dir;
                    cocoaLogPos = logPos;
                    break;
                }
            }
            boolean canPlantCocoa = (cocoaFacing != null);
            boolean canPlantSweetBerries = SWEET_BERRY_PLANT.contains(baseBlock);

            Item targetSeed = null;

            if (canPlantNetherWart) {
                targetSeed = findBestSeed(player, Set.of(NETHER_WART_ITEM));
            } else if (canPlantCocoa) {
                targetSeed = findBestSeed(player, Set.of(COCOA_BEANS_ITEM));
            } else if (canPlantSugarcane) {
                targetSeed = findBestSeed(player, Set.of(SUGAR_CANE_ITEM));
            } else if (canPlantBamboo) {
                int bambooSpacing = AutoHarvestConfig.bambooRadius();
                if (bambooSpacing == 0) {
                    targetSeed = findBestSeed(player, Set.of(BAMBOO_ITEM));
                }
                boolean hasNearbyBamboo = false;
                if (bambooSpacing > 0) {
                    for (int dx = -bambooSpacing; dx <= bambooSpacing; dx++) {
                        for (int dz = -bambooSpacing; dz <= bambooSpacing; dz++) {
                            BlockPos checkPos = basePos.offset(dx, 1, dz);
                            Block block = world.getBlockState(checkPos).getBlock();
                            if (block == Blocks.BAMBOO || block == Blocks.BAMBOO_SAPLING) {
                                hasNearbyBamboo = true;
                                break;
                            }
                        }
                        if (hasNearbyBamboo) break;
                    }
                }
                if (!hasNearbyBamboo) {
                    targetSeed = findBestSeed(player, Set.of(BAMBOO_ITEM));
                }
            } else if (canPlantCrop) {
                targetSeed = findBestSeed(player, CROP_SEEDS);
            } else if (canPlantSweetBerries) {
                targetSeed = findBestSeed(player, Set.of(SWEET_BERRIES_ITEM));
            }

            if (targetSeed == null) continue;

            InteractionHand usedHand = null;
            if (player.getMainHandItem().getItem() == targetSeed) {
                usedHand = InteractionHand.MAIN_HAND;
            } else if (player.getOffhandItem().getItem() == targetSeed) {
                usedHand = InteractionHand.OFF_HAND;
            }

            if (usedHand != null) {
                if (canPlantCocoa) {
                    InteractionHelper.interactBlock(player, cocoaLogPos, usedHand, cocoaFacing);
                } else {
                    InteractionHelper.interactBlock(player, basePos, usedHand, Direction.UP);
                }
                return;
            }
            int currentSlot = player.getInventory().getSelectedSlot();
            int bestSlot = -1;
            int minDistance = Integer.MAX_VALUE;

            for (int i = 0; i < 9; i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty() && stack.getItem() == targetSeed) {
                    int distance = Math.abs(i - currentSlot);
                    if (distance < minDistance) {
                        minDistance = distance;
                        bestSlot = i;
                    }
                }
            }

            if (bestSlot != -1) {
                if (AutoHarvestConfig.autoSwitchHotbar()) {
                    player.getInventory().setSelectedSlot(bestSlot);
                }
                InteractionHelper.interactBlock(player, basePos, InteractionHand.MAIN_HAND, Direction.UP);
                return;
            }
        }
    }

    private Item findBestSeed(LocalPlayer player, Set<Item> allowedSeeds) {
        if (allowedSeeds.contains(player.getMainHandItem().getItem())) {
            return player.getMainHandItem().getItem();
        }
        if (allowedSeeds.contains(player.getOffhandItem().getItem())) {
            return player.getOffhandItem().getItem();
        }

        return ItemSlotHelper.findNearestItem(player, allowedSeeds);
    }


    @Override
    public String getName() {
        return Component.translatable("autoharvest.mode.plant").getString();
    }

    @Override
    public void onDisable() {
        //nothing
    }
}