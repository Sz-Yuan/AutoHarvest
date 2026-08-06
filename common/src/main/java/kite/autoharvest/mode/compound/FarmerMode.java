package kite.autoharvest.mode.compound;

import kite.autoharvest.config.AutoHarvestConfig;
import kite.autoharvest.mode.AutoMode;
import kite.autoharvest.util.BoxUtil;
import kite.autoharvest.util.InteractionHelper;
import kite.autoharvest.util.ItemRefillHelper;
import kite.autoharvest.util.ItemSlotHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FarmerMode implements AutoMode {
    private static final Set<Block> HARVEST_CROPS = Set.of(
            Blocks.WHEAT,
            Blocks.CARROTS,
            Blocks.POTATOES,
            Blocks.BEETROOTS,
            Blocks.NETHER_WART,
            Blocks.SUGAR_CANE,
            Blocks.SWEET_BERRY_BUSH
    );

    private static final Map<Block, Item> CROP_TO_SEED = Map.of(
            Blocks.WHEAT, Items.WHEAT_SEEDS,
            Blocks.CARROTS, Items.CARROT,
            Blocks.POTATOES, Items.POTATO,
            Blocks.BEETROOTS, Items.BEETROOT_SEEDS,
            Blocks.NETHER_WART, Items.NETHER_WART
    );

    private static final Set<Item> REFILLABLE_SEEDS = Set.copyOf(CROP_TO_SEED.values());

    private boolean pendingBreak = false;
    private BlockPos targetPos = null;
    private Item targetSeed = null;
    private boolean targetInteract = false;
    private final Map<BlockPos, Item> pendingPlant = new HashMap<>();

    @Override
    public void tick() {
        Minecraft client = Minecraft.getInstance();
        ClientLevel world = client.level;
        LocalPlayer player = client.player;
        if (world == null || player == null) return;

        if (AutoHarvestConfig.enableRefill() && !player.isCreative() && !player.isSpectator()) {
            ItemStack mainStack = player.getMainHandItem();
            ItemStack offStack = player.getOffhandItem();
            if (!mainStack.isEmpty() && mainStack.getCount() < 64 && REFILLABLE_SEEDS.contains(mainStack.getItem())) {
                ItemRefillHelper.refillMainHand();
            }
            if (!offStack.isEmpty() && offStack.getCount() < 64 && REFILLABLE_SEEDS.contains(offStack.getItem())) {
                ItemRefillHelper.refillOffHand();
            }
        }

        if (pendingBreak && targetPos != null) {
            executeHarvest();
            pendingBreak = false;
            if (targetSeed != null) {
                pendingPlant.put(targetPos, targetSeed);
            }
            targetPos = null;
            targetSeed = null;
            return;
        }

        processPendingPlant(player);
        scanAndHarvest(world, player);
    }

    private void processPendingPlant(LocalPlayer player) {
        if (pendingPlant.isEmpty()) return;

        double range = AutoHarvestConfig.getInstance().getRadius();
        double rangeSq = range * range;

        for (Map.Entry<BlockPos, Item> entry : pendingPlant.entrySet()) {
            BlockPos pos = entry.getKey();

            if (!player.level().getBlockState(pos).isAir()) continue;
            if (player.distanceToSqr(Vec3.atCenterOf(pos)) > rangeSq) continue;
            if (!isPlantableBelow(player, pos, entry.getValue())) continue;

            Item seed = entry.getValue();
            if (doPlant(player, pos, seed)) return;
        }
    }

    private boolean isPlantableBelow(LocalPlayer player, BlockPos pos, Item seed) {
        Block below = player.level().getBlockState(pos.below()).getBlock();
        if (seed == Items.NETHER_WART) {
            return below == Blocks.SOUL_SAND;
        }
        return below == Blocks.FARMLAND;
    }

    private void scanAndHarvest(ClientLevel world, LocalPlayer player) {
        Vec3 playerPos = BoxUtil.getPlayerPos();
        if (playerPos == null) return;

        double radius = AutoHarvestConfig.getInstance().getRadius();
        int radiusInt = (int) Math.ceil(radius);

        for (BlockPos pos : BlockPos.withinManhattan(BlockPos.containing(playerPos), radiusInt, radiusInt, radiusInt)) {
            if (BoxUtil.isOutsideSphere(pos, playerPos, radius)) continue;

            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            if (!HARVEST_CROPS.contains(block)) continue;

            if (block == Blocks.SUGAR_CANE) {
                if (world.getBlockState(pos.below()).is(Blocks.SUGAR_CANE)) continue;
                if (!world.getBlockState(pos.above()).is(Blocks.SUGAR_CANE)) continue;

                targetPos = pos.above();
                targetSeed = null;
                targetInteract = false;
            } else if (block == Blocks.SWEET_BERRY_BUSH) {
                if (isNotFullyGrown(state, block)) continue;

                targetPos = pos;
                targetSeed = null;
                targetInteract = true;
            } else {
                if (isNotFullyGrown(state, block)) continue;

                targetPos = pos;
                targetSeed = CROP_TO_SEED.get(block);
                targetInteract = false;
            }
            checkFortuneAndHarvest(player);
            return;
        }
    }

    private void checkFortuneAndHarvest(LocalPlayer player) {
        if (needsFortuneSwitch(player)) {
            ensureFortuneTool(player);
            pendingBreak = true;
            return;
        }
        executeHarvest();
        if (targetSeed != null) {
            pendingPlant.put(targetPos, targetSeed);
        }
        targetPos = null;
        targetSeed = null;
    }

    private void executeHarvest() {
        if (targetInteract) {
            InteractionHelper.interactBlock(BoxUtil.getPlayer(), targetPos, InteractionHand.MAIN_HAND, Direction.UP);
        } else {
            InteractionHelper.breakBlock(targetPos, Direction.UP);
        }
    }

    private int findSeedSlot(LocalPlayer player, Item seed) {
        if (player.getMainHandItem().getItem() == seed) {
            return player.getInventory().getSelectedSlot();
        }
        if (player.getOffhandItem().getItem() == seed) {
            return -2;
        }
        int hotbarSlot = ItemSlotHelper.findNearestSlot(player, seed);
        if (hotbarSlot != -1) return hotbarSlot;

        for (int i = 9; i < 36; i++) {
            if (player.getInventory().getItem(i).getItem() == seed) {
                return i;
            }
        }
        return -1;
    }

    private boolean doPlant(LocalPlayer player, BlockPos pos, Item seed) {
        int slot = findSeedSlot(player, seed);
        if (slot == -1) return false;

        boolean offhand = AutoHarvestConfig.farmerOffhandPlant();
        ensureSeedAvailable(player, slot, offhand);
        InteractionHelper.interactBlock(player, pos.below(), offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, Direction.UP);
        return true;
    }

    private void ensureSeedAvailable(LocalPlayer player, int slot, boolean offhand) {
        if (offhand) {
            if (slot == -2) return;
            if (slot < 9) {
                swapContainerSlots(player.containerMenu, slot + 36, 45);
            } else {
                swapContainerSlots(player.containerMenu, slot, 45);
            }
            return;
        }

        if (slot == -2) {
            swapContainerSlots(player.containerMenu, player.getInventory().getSelectedSlot() + 36, 45);
            return;
        }

        if (slot < 9) {
            player.getInventory().setSelectedSlot(slot);
            return;
        }

        int hotbarSlot = findHotbarInsertSlot(player);
        swapContainerSlots(player.containerMenu, slot, hotbarSlot);
        player.getInventory().setSelectedSlot(hotbarSlot);
    }

    private void swapContainerSlots(AbstractContainerMenu handler, int sourceSlot, int targetSlot) {
        clickSlot(handler, sourceSlot);
        clickSlot(handler, targetSlot);
        if (!handler.getCarried().isEmpty()) {
            clickSlot(handler, sourceSlot);
        }
    }

    private void clickSlot(AbstractContainerMenu handler, int slotIndex) {
        var client = Minecraft.getInstance();
        if (client.gameMode == null) return;
        if (client.player != null) {
            client.gameMode.handleContainerInput(
                    handler.containerId,
                    slotIndex,
                    0,
                    ContainerInput.PICKUP,
                    client.player
            );
        }
    }

    private int findHotbarInsertSlot(LocalPlayer player) {
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getItem(i).isEmpty()) return i;
        }
        for (int i = 0; i < 9; i++) {
            if (!isFortuneTool(player.getInventory().getItem(i))) return i;
        }
        return 0;
    }

    private boolean needsFortuneSwitch(LocalPlayer player) {
        if (!AutoHarvestConfig.autoSwitchFortuneTool()) return false;
        int fortuneSlot = findFortuneToolSlot(player);
        return fortuneSlot != -1 && player.getInventory().getSelectedSlot() != fortuneSlot;
    }

    private void ensureFortuneTool(LocalPlayer player) {
        if (!AutoHarvestConfig.autoSwitchFortuneTool()) return;
        int fortuneSlot = findFortuneToolSlot(player);
        if (fortuneSlot != -1) {
            player.getInventory().setSelectedSlot(fortuneSlot);
        }
    }

    private int findFortuneToolSlot(LocalPlayer player) {
        Holder<Enchantment> fortuneHolder = player.level()
                .registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.FORTUNE);

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            if (isHarvestTool(stack.getItem())) {
                ItemEnchantments enchantments =
                        stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
                if (enchantments.getLevel(fortuneHolder) >= 1) {
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

    private boolean isFortuneTool(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (!isHarvestTool(stack.getItem())) return false;

        LocalPlayer player = BoxUtil.getPlayer();
        if (player == null) return false;

        Holder<Enchantment> fortuneHolder = player.level()
                .registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.FORTUNE);

        ItemEnchantments enchantments =
                stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        return enchantments.getLevel(fortuneHolder) >= 1;
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

    @Override
    public String getName() {
        return Component.translatable("autoharvest.mode.farmer").getString();
    }

    @Override
    public void onDisable() {
        pendingBreak = false;
        targetPos = null;
        targetSeed = null;
        pendingPlant.clear();
    }
}
