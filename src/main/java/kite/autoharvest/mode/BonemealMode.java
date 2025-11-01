package kite.autoharvest.mode;

import kite.autoharvest.config.AutoHarvestConfig;
import kite.autoharvest.util.*;
import net.minecraft.block.*;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

public class BonemealMode implements AutoMode {

    private static final Set<Block> BONEMEAL_WHITELIST = Set.of(
            Blocks.WHEAT,
            Blocks.CARROTS,
            Blocks.POTATOES,
            Blocks.BEETROOTS,
            Blocks.PUMPKIN_STEM,
            Blocks.MELON_STEM,
            Blocks.SWEET_BERRY_BUSH
    );

    @Override
    public void tick() {
        ClientWorld world = BoxUtil.getWorld();
        ClientPlayerEntity player = BoxUtil.getPlayer();
        if (world == null || player == null) return;

        Vec3d playerPos = BoxUtil.getPlayerPos();
        if (playerPos == null) return;

        if (!player.isCreative() && !player.isSpectator() && AutoHarvestConfig.enableRefill()) {
            ItemStack main = player.getMainHandStack();
            ItemStack off = player.getOffHandStack();
            if (!main.isEmpty() && main.getItem() == Items.BONE_MEAL && main.getCount() < 64) {
                ItemRefillHelpermain.refillHands();
            }
            if (!off.isEmpty() && off.getItem() == Items.BONE_MEAL && off.getCount() < 64) {
                ItemRefillHelperoff.refillOffHand();
            }
        }

        double radius = AutoHarvestConfig.getInstance().getRadius();
        Box searchBox = BoxUtil.createSearchBox(playerPos, radius);
        int radiusInt = (int) Math.ceil(radius);

        for (BlockPos pos : BlockPos.iterateOutwards(BlockPos.ofFloored(playerPos), radiusInt, radiusInt, radiusInt)) {
            if (!searchBox.contains(pos.toCenterPos())) continue;
            if (BoxUtil.isInSphere(pos, playerPos, radius)) continue;

            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            if (!BONEMEAL_WHITELIST.contains(block)) continue;

            if (!isNotFullyGrown(state, block)) continue;

            if (tryUseBonemeal(player, pos)) {
                return;
            }
        }
    }

    private boolean isNotFullyGrown(BlockState state, Block block) {
        if (block instanceof CropBlock) {
            if (block == Blocks.BEETROOTS) {
                return state.get(Properties.AGE_3) < 3;
            } else {
                return state.get(Properties.AGE_7) < 7;
            }
        } else if (block instanceof StemBlock) {
            return state.get(StemBlock.AGE) < 7;
        } else if (block == Blocks.SWEET_BERRY_BUSH) {
            return state.get(Properties.AGE_3) < 3;
        }
        return false;
    }

    private boolean tryUseBonemeal(ClientPlayerEntity player, BlockPos pos) {
        Hand hand = null;
        if (player.getMainHandStack().getItem() == Items.BONE_MEAL) {
            hand = Hand.MAIN_HAND;
        } else if (player.getOffHandStack().getItem() == Items.BONE_MEAL) {
            hand = Hand.OFF_HAND;
        }

        if (hand != null) {
            InteractionHelper.interactBlock(player, pos, hand, Direction.UP);
            return true;
        }

        int currentSlot = player.getInventory().getSelectedSlot();
        int bestSlot = -1;
        int minDistance = Integer.MAX_VALUE;

        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getStack(i).getItem() == Items.BONE_MEAL) {
                int dist = Math.abs(i - currentSlot);
                if (dist < minDistance) {
                    minDistance = dist;
                    bestSlot = i;
                }
            }
        }

        if (bestSlot != -1) {
            player.getInventory().setSelectedSlot(bestSlot);
            InteractionHelper.interactBlock(player, pos, Hand.MAIN_HAND, Direction.UP);
            return true;
        }

        return false;
    }

    @Override
    public String getName() {
        return Text.translatable("autoharvest.mode.bonemeal").getString();
    }

    @Override
    public void onDisable() {
        // 留空
    }
}