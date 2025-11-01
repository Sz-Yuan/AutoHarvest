package kite.autoharvest.mode;

import kite.autoharvest.config.AutoHarvestConfig;
import kite.autoharvest.util.BoxUtil;
import kite.autoharvest.util.InteractionHelper;
import kite.autoharvest.util.WaterProximityChecker;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

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
        ClientWorld world = BoxUtil.getWorld();
        ClientPlayerEntity player = BoxUtil.getPlayer();
        if (world == null || player == null) return;

        Vec3d playerPos = BoxUtil.getPlayerPos();
        if (playerPos == null) return;

        double radius = AutoHarvestConfig.getInstance().getRadius();
        int radiusInt = (int) Math.ceil(radius);

        // 检查周围是否存在可锄地的方块
        boolean hasValidBlock = false;
        for (BlockPos pos : BlockPos.iterateOutwards(BlockPos.ofFloored(playerPos), radiusInt, radiusInt, radiusInt)) {
            if (BoxUtil.isInSphere(pos, playerPos, radius)) continue;
            if (!HOEABLE_BLOCKS.contains(world.getBlockState(pos).getBlock())) continue;
            if (!world.getBlockState(pos.up()).isAir()) continue;
            if (WaterProximityChecker.isWithinHydrationRange(world, pos)) continue;

            hasValidBlock = true;
            break;
        }
        if (!hasValidBlock) {
            return;
        }

        Hand usedHand = getHoeInHand(player);
        if (usedHand == null) {
            int currentSlot = player.getInventory().getSelectedSlot();
            int bestSlot = -1;
            int minDistance = Integer.MAX_VALUE;

            for (int i = 0; i < 9; i++) {
                var stack = player.getInventory().getStack(i);
                if (!stack.isEmpty() && HOES.contains(stack.getItem())) {
                    int diff = Math.abs(i - currentSlot);
                    int distance = Math.min(diff, 9 - diff);
                    if (distance < minDistance) {
                        minDistance = distance;
                        bestSlot = i;
                    }
                }
            }

            if (bestSlot != -1 && AutoHarvestConfig.autoSwitchHotbar()) {
                player.getInventory().setSelectedSlot(bestSlot);
            }
            return;
        }
        for (BlockPos pos : BlockPos.iterateOutwards(BlockPos.ofFloored(playerPos), radiusInt, radiusInt, radiusInt)) {
            if (BoxUtil.isInSphere(pos, playerPos, radius)) continue;
            if (!HOEABLE_BLOCKS.contains(world.getBlockState(pos).getBlock())) continue;
            if (!world.getBlockState(pos.up()).isAir()) continue;
            if (WaterProximityChecker.isWithinHydrationRange(world, pos)) continue;

            InteractionHelper.interactBlock(player, pos, usedHand, Direction.UP);
            return;
        }
    }

    private Hand getHoeInHand(ClientPlayerEntity player) {
        if (HOES.contains(player.getMainHandStack().getItem())) {
            return Hand.MAIN_HAND;
        }
        if (HOES.contains(player.getOffHandStack().getItem())) {
            return Hand.OFF_HAND;
        }
        return null;
    }

    @Override
    public String getName() {
        return Text.translatable("autoharvest.mode.hoeing").getString();
    }

    @Override
    public void onDisable() {
        // 可选：重置状态（目前无状态，可留空）
    }
}