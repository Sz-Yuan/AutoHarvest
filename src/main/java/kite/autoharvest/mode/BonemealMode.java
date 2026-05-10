package kite.autoharvest.mode;

import kite.autoharvest.config.AutoHarvestConfig;
import kite.autoharvest.util.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

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
        ClientLevel world = BoxUtil.getWorld();
        LocalPlayer player = BoxUtil.getPlayer();
        if (world == null || player == null) return;

        Vec3 playerPos = BoxUtil.getPlayerPos();
        if (playerPos == null) return;

        if (!player.isCreative() && !player.isSpectator() && AutoHarvestConfig.enableRefill()) {
            ItemStack main = player.getMainHandItem();
            ItemStack off = player.getOffhandItem();
            if (!main.isEmpty() && main.getItem() == Items.BONE_MEAL && main.getCount() < 64) {
                ItemRefillHelper.refillMainHand();
            }
            if (!off.isEmpty() && off.getItem() == Items.BONE_MEAL && off.getCount() < 64) {
                ItemRefillHelper.refillOffHand();
            }
        }

        double radius = AutoHarvestConfig.getInstance().getRadius();

        BoxUtil.forEachBlockInRange(playerPos, radius, pos -> {
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            if (!BONEMEAL_WHITELIST.contains(block)) return false;

            if (!isNotFullyGrown(state, block)) return false;

            return tryUseBonemeal(player, pos);
        });
    }

    private boolean isNotFullyGrown(BlockState state, Block block) {
        if (block instanceof CropBlock) {
            if (block == Blocks.BEETROOTS) {
                return state.getValue(BlockStateProperties.AGE_3) < 3;
            } else {
                return state.getValue(BlockStateProperties.AGE_7) < 7;
            }
        } else if (block instanceof StemBlock) {
            return state.getValue(StemBlock.AGE) < 7;
        } else if (block == Blocks.SWEET_BERRY_BUSH) {
            return state.getValue(BlockStateProperties.AGE_3) < 3;
        }
        return false;
    }

    private boolean tryUseBonemeal(LocalPlayer player, BlockPos pos) {
        InteractionHand hand = null;
        if (player.getMainHandItem().getItem() == Items.BONE_MEAL) {
            hand = InteractionHand.MAIN_HAND;
        } else if (player.getOffhandItem().getItem() == Items.BONE_MEAL) {
            hand = InteractionHand.OFF_HAND;
        }

        if (hand != null) {
            InteractionHelper.interactBlock(player, pos, hand, Direction.UP);
            return true;
        }

        int bestSlot = ItemSlotHelper.findNearestSlot(player, Items.BONE_MEAL);

        if (bestSlot != -1 && AutoHarvestConfig.autoSwitchHotbar()) {
            player.getInventory().setSelectedSlot(bestSlot);
            InteractionHelper.interactBlock(player, pos, InteractionHand.MAIN_HAND, Direction.UP);
            return true;
        }

        return false;
    }

    @Override
    public String getName() {
        return Component.translatable("autoharvest.mode.bonemeal").getString();
    }

    @Override
    public void onDisable() {
        //nothing
    }
}