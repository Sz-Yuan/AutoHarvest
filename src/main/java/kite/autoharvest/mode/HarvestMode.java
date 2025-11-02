package kite.autoharvest.mode;

import kite.autoharvest.config.AutoHarvestConfig;
import kite.autoharvest.util.BoxUtil;
import kite.autoharvest.util.InteractionHelper;
import net.minecraft.block.*;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.*;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

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
        ClientWorld world = BoxUtil.getWorld();
        ClientPlayerEntity player = BoxUtil.getPlayer();
        if (world == null || player == null) return;

        Vec3d playerPos = BoxUtil.getPlayerPos();
        if (playerPos == null) return;

        double radius = AutoHarvestConfig.getInstance().getRadius();
        Box searchBox = BoxUtil.createSearchBox(playerPos, radius);
        int radiusInt = (int) Math.ceil(radius);

        for (BlockPos pos : BlockPos.iterateOutwards(BlockPos.ofFloored(playerPos), radiusInt, radiusInt, radiusInt)) {
            if (!searchBox.contains(pos.toCenterPos())) continue;
            if (BoxUtil.isInSphere(pos, playerPos, radius)) continue;

            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            if (!HARVEST_CROPS.contains(block)) continue;

            if (block == Blocks.SUGAR_CANE) {
                if (world.getBlockState(pos.down()).isOf(Blocks.SUGAR_CANE)) {
                    continue;
                }
                BlockPos secondpos = pos.up();
                if (world.getBlockState(secondpos).isOf(Blocks.SUGAR_CANE)) {
                    if (tryHarvest(player, pos.up())) {
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
                InteractionHelper.interactBlock(player, pos, Hand.MAIN_HAND, Direction.UP);
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
            return state.get(Properties.AGE_3) < 3;
        } else if (block == Blocks.SWEET_BERRY_BUSH) {
            return state.get(Properties.AGE_3) < 2;
        } else if (block instanceof CropBlock) {
            if (block == Blocks.BEETROOTS) {
                return state.get(Properties.AGE_3) < 3;
            } else {
                return state.get(Properties.AGE_7) < 7;
            }
        }
        return false;
    }


    private boolean tryHarvest(ClientPlayerEntity player, BlockPos pos) {
        if (AutoHarvestConfig.autoSwitchFortuneTool()) {
            int fortuneSlot = findFortuneToolSlot(player);
            if (fortuneSlot != -1) {
                player.getInventory().setSelectedSlot(fortuneSlot);
            }
        }
        InteractionHelper.breakBlock(pos, Direction.UP);
        return true;
    }

    private int findFortuneToolSlot(ClientPlayerEntity player) {
        DynamicRegistryManager registryManager = player.getEntityWorld().getRegistryManager();
        RegistryWrapper.Impl<Enchantment> enchantmentRegistry = registryManager.getOrThrow(RegistryKeys.ENCHANTMENT);

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;

            Item item = stack.getItem();
            if (isHarvestTool(item)) {
                ItemEnchantmentsComponent enchantments =
                        stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);

                RegistryEntry<Enchantment> fortuneEntry = enchantmentRegistry.getOrThrow(Enchantments.FORTUNE);
                int fortuneLevel = enchantments.getLevel(fortuneEntry);
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
        return stack.isIn(ItemTags.AXES) ||
                stack.isIn(ItemTags.SHOVELS) ||
                stack.isIn(ItemTags.HOES) ||
                stack.isIn(ItemTags.PICKAXES);
    }

    @Override
    public String getName() {
        return Text.translatable("autoharvest.mode.harvest").getString();
    }

    @Override
    public void onDisable() {
        // 留空
    }
}