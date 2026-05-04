package kite.autoharvest.mode;

import kite.autoharvest.config.AutoHarvestConfig;
import kite.autoharvest.util.BoxUtil;
import kite.autoharvest.util.InteractionHelper;
import kite.autoharvest.util.ItemSlotHelper;
import kite.autoharvest.util.WaterProximityChecker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class HoeMode implements AutoMode {

    private static final Set<Block> HOEABLE_BLOCKS = Set.of(
            Blocks.DIRT,
            Blocks.GRASS_BLOCK,
            Blocks.COARSE_DIRT,
            Blocks.ROOTED_DIRT
    );

    private static final Set<Item> HOES = Set.of(
            Items.WOODEN_HOE,
            Items.STONE_HOE,
            Items.IRON_HOE,
            Items.GOLDEN_HOE,
            Items.DIAMOND_HOE,
            Items.NETHERITE_HOE
    );

    @Override
    public void tick() {
        ClientLevel world = BoxUtil.getWorld();
        LocalPlayer player = BoxUtil.getPlayer();
        if (world == null || player == null) return;

        Vec3 playerPos = BoxUtil.getPlayerPos();
        if (playerPos == null) return;

        double radius = AutoHarvestConfig.getInstance().getRadius();
        int radiusInt = (int) Math.ceil(radius);

        boolean hasValidBlock = false;
        for (BlockPos pos : BlockPos.withinManhattan(BlockPos.containing(playerPos), radiusInt, radiusInt, radiusInt)) {
            if (BoxUtil.isInSphere(pos, playerPos, radius)) continue;
            if (!HOEABLE_BLOCKS.contains(world.getBlockState(pos).getBlock())) continue;
            if (!world.getBlockState(pos.above()).isAir()) continue;
            if (AutoHarvestConfig.skipWater() && WaterProximityChecker.isWithinHydrationRange(world, pos)) continue;

            hasValidBlock = true;
            break;
        }
        if (!hasValidBlock) {
            return;
        }

        InteractionHand usedHand = getHoeInHand(player);
        if (usedHand == null) {
            int bestSlot = ItemSlotHelper.findNearestSlot(player, HOES, true);

            if (bestSlot != -1 && AutoHarvestConfig.autoSwitchHotbar()) {
                player.getInventory().setSelectedSlot(bestSlot);
            }


            if (bestSlot != -1 && AutoHarvestConfig.autoSwitchHotbar()) {
                player.getInventory().setSelectedSlot(bestSlot);
            }
            return;
        }
        for (BlockPos pos : BlockPos.withinManhattan(BlockPos.containing(playerPos), radiusInt, radiusInt, radiusInt)) {
            if (BoxUtil.isInSphere(pos, playerPos, radius)) continue;
            if (!HOEABLE_BLOCKS.contains(world.getBlockState(pos).getBlock())) continue;
            if (!world.getBlockState(pos.above()).isAir()) continue;
            if (AutoHarvestConfig.skipWater() && WaterProximityChecker.isWithinHydrationRange(world, pos)) continue;

            InteractionHelper.interactBlock(player, pos, usedHand, Direction.UP);
            return;
        }
    }

    private InteractionHand getHoeInHand(LocalPlayer player) {
        if (HOES.contains(player.getMainHandItem().getItem())) {
            return InteractionHand.MAIN_HAND;
        }
        if (HOES.contains(player.getOffhandItem().getItem())) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }

    @Override
    public String getName() {
        return Component.translatable("autoharvest.mode.hoeing").getString();
    }

    @Override
    public void onDisable() {
        // nothing
    }
}