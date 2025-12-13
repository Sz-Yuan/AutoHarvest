package kite.autoharvest.mode;

import kite.autoharvest.config.AutoHarvestConfig;
import kite.autoharvest.util.BoxUtil;
import kite.autoharvest.util.InteractionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

public class WeedMode implements AutoMode {
    private static final Set<Block> WEED_BLOCKS = new HashSet<>();

    static {
        WEED_BLOCKS.add(Blocks.SHORT_GRASS);
        WEED_BLOCKS.add(Blocks.TALL_GRASS);
        WEED_BLOCKS.add(Blocks.FERN);
        WEED_BLOCKS.add(Blocks.DEAD_BUSH);
        WEED_BLOCKS.add(Blocks.DANDELION);
        WEED_BLOCKS.add(Blocks.POPPY);
        WEED_BLOCKS.add(Blocks.BLUE_ORCHID);
        WEED_BLOCKS.add(Blocks.ALLIUM);
        WEED_BLOCKS.add(Blocks.AZURE_BLUET);
        WEED_BLOCKS.add(Blocks.RED_TULIP);
        WEED_BLOCKS.add(Blocks.ORANGE_TULIP);
        WEED_BLOCKS.add(Blocks.WHITE_TULIP);
        WEED_BLOCKS.add(Blocks.PINK_TULIP);
        WEED_BLOCKS.add(Blocks.OXEYE_DAISY);
        WEED_BLOCKS.add(Blocks.CORNFLOWER);
        WEED_BLOCKS.add(Blocks.LILY_OF_THE_VALLEY);
        WEED_BLOCKS.add(Blocks.WITHER_ROSE);
        WEED_BLOCKS.add(Blocks.SUNFLOWER);
        WEED_BLOCKS.add(Blocks.LILAC);
        WEED_BLOCKS.add(Blocks.ROSE_BUSH);
        WEED_BLOCKS.add(Blocks.PEONY);
        WEED_BLOCKS.add(Blocks.BUSH);
        WEED_BLOCKS.add(Blocks.SHORT_DRY_GRASS);
        WEED_BLOCKS.add(Blocks.TALL_DRY_GRASS);
        WEED_BLOCKS.add(Blocks.PITCHER_PLANT);
        WEED_BLOCKS.add(Blocks.TORCHFLOWER);
        WEED_BLOCKS.add(Blocks.LARGE_FERN);
        WEED_BLOCKS.add(Blocks.WILDFLOWERS);
        WEED_BLOCKS.add(Blocks.PINK_PETALS);
        WEED_BLOCKS.add(Blocks.LEAF_LITTER);
    }

    @Override
    public void tick() {
        ClientLevel world = BoxUtil.getWorld();
        if (world == null) return;

        Vec3 playerPos = BoxUtil.getPlayerPos();
        if (playerPos == null) return;

        double radius = AutoHarvestConfig.radius();
        AABB searchBox = BoxUtil.createSearchBox(playerPos, radius);

        // 遍历区域
        int radiusInt = (int) Math.ceil(radius);
        for (BlockPos blockPos : BlockPos.withinManhattan(BlockPos.containing(playerPos), radiusInt, radiusInt, radiusInt)) {
            if (!searchBox.contains(blockPos.getCenter())) continue;
            if (BoxUtil.isInSphere(blockPos, playerPos, radius)) continue;

            Minecraft client = Minecraft.getInstance();
            Block block = world.getBlockState(blockPos).getBlock();

            if ((block instanceof FlowerBlock || block instanceof TallFlowerBlock) && AutoHarvestConfig.isFlower()) {
                continue;
            }
            if ((block instanceof FlowerBedBlock || block == Blocks.LEAF_LITTER) && AutoHarvestConfig.isFlower()){
                continue;
            }

            if (WEED_BLOCKS.contains(block)) {
                if (client.gameMode != null) {
                    InteractionHelper.breakBlock(blockPos, Direction.UP);
                    break;
                }
            }
        }
    }

    @Override
    public String getName() {
        return Component.translatable("autoharvest.mode.weed").getString();
    }

    @Override
    public void onDisable() {
//        Nothing
    }
}