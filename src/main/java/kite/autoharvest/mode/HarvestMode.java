package kite.autoharvest.mode;

import kite.autoharvest.config.AutoHarvestConfig;
import kite.autoharvest.util.BoxUtil;
import kite.autoharvest.util.InteractionHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class HarvestMode implements AutoMode {

    private static final Set<Block> HARVEST_CROPS = Set.of(
            Blocks.WHEAT,
            Blocks.CARROTS,
            Blocks.POTATOES,
            Blocks.BEETROOTS,
            Blocks.NETHER_WART,
            Blocks.SUGAR_CANE,
            Blocks.SWEET_BERRY_BUSH
    );

    @Override
    public void tick() {
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
            if (BoxUtil.isOutsideSphere(pos, playerPos, radius)) continue;

            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            if (!HARVEST_CROPS.contains(block)) continue;

            if (block == Blocks.SUGAR_CANE) {
                if (world.getBlockState(pos.below()).is(Blocks.SUGAR_CANE)) {
                    continue;
                }
                BlockPos secondpos = pos.above();
                if (world.getBlockState(secondpos).is(Blocks.SUGAR_CANE)) {
                    if (tryHarvest(player, pos.above())) {
                        return;
                    }
                }
            } else if (state.getBlock() == Blocks.SWEET_BERRY_BUSH && !isNotFullyGrown(state, block)) {
                if (AutoHarvestConfig.autoSwitchFortuneTool()) {
                    int fortuneSlot = findFortuneToolSlot(player);
                    if (fortuneSlot != -1) {
                        player.getInventory().setSelectedSlot(fortuneSlot);
                    }
                }
                InteractionHelper.interactBlock(player, pos, InteractionHand.MAIN_HAND, Direction.UP);
                return;
            } else {
                if (isNotFullyGrown(state, block)) continue;
                if (tryHarvest(player, pos)) {
                    return;
                }
            }
        }
    }

    private boolean isNotFullyGrown(BlockState state, Block block) {
        if (block == Blocks.NETHER_WART) {
            return state.getValue(BlockStateProperties.AGE_3) < 3;
        } else if (block == Blocks.SWEET_BERRY_BUSH) {
            return state.getValue(BlockStateProperties.AGE_3) < 2;
        } else if (block instanceof CropBlock) {
            if (block == Blocks.BEETROOTS) {
                return state.getValue(BlockStateProperties.AGE_3) < 3;
            } else {
                return state.getValue(BlockStateProperties.AGE_7) < 7;
            }
        }
        return false;
    }


    private boolean tryHarvest(LocalPlayer player, BlockPos pos) {
        if (AutoHarvestConfig.autoSwitchFortuneTool()) {
            int fortuneSlot = findFortuneToolSlot(player);
            if (fortuneSlot != -1) {
                player.getInventory().setSelectedSlot(fortuneSlot);
            }
        }
        InteractionHelper.breakBlock(pos, Direction.UP);
        return true;
    }

    private int findFortuneToolSlot(LocalPlayer player) {
        Holder<Enchantment> fortuneHolder = player.level()
                .registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.FORTUNE);

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            Item item = stack.getItem();
            if (isHarvestTool(item)) {
                ItemEnchantments enchantments =
                        stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

                int fortuneLevel = enchantments.getLevel(fortuneHolder);
                if (fortuneLevel >= 1) {
                    return i;
                }
            }
        }
        return -1;
    }


    private boolean isHarvestTool(Item item) {
        if (item == null) return false;
        ItemStack stack = new ItemStack(item);
        return stack.is(ItemTags.AXES) ||
                stack.is(ItemTags.SHOVELS) ||
                stack.is(ItemTags.HOES) ||
                stack.is(ItemTags.PICKAXES);
    }

    @Override
    public String getName() {
        return Component.translatable("autoharvest.mode.harvest").getString();
    }

    @Override
    public void onDisable() {
        // 留空
    }
}