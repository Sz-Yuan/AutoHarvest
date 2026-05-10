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
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class WeedMode implements AutoMode {
    private static final Set<Block> WEED_BLOCKS = Set.of(
            Blocks.SHORT_GRASS,
            Blocks.TALL_GRASS,
            Blocks.FERN,
            Blocks.DEAD_BUSH,
            Blocks.DANDELION,
            Blocks.POPPY,
            Blocks.BLUE_ORCHID,
            Blocks.ALLIUM,
            Blocks.AZURE_BLUET,
            Blocks.RED_TULIP,
            Blocks.ORANGE_TULIP,
            Blocks.WHITE_TULIP,
            Blocks.PINK_TULIP,
            Blocks.OXEYE_DAISY,
            Blocks.CORNFLOWER,
            Blocks.LILY_OF_THE_VALLEY,
            Blocks.WITHER_ROSE,
            Blocks.SUNFLOWER,
            Blocks.LILAC,
            Blocks.ROSE_BUSH,
            Blocks.PEONY,
            Blocks.BUSH,
            Blocks.SHORT_DRY_GRASS,
            Blocks.TALL_DRY_GRASS,
            Blocks.PITCHER_PLANT,
            Blocks.TORCHFLOWER,
            Blocks.LARGE_FERN,
            Blocks.WILDFLOWERS,
            Blocks.PINK_PETALS,
            Blocks.LEAF_LITTER,
            Blocks.SEAGRASS,
            Blocks.TALL_SEAGRASS
    );

    @Override
    public void tick() {
        ClientLevel world = BoxUtil.getWorld();
        if (world == null) return;

        Vec3 playerPos = BoxUtil.getPlayerPos();
        if (playerPos == null) return;

        double radius = AutoHarvestConfig.radius();

        BoxUtil.forEachBlockInRange(playerPos, radius, blockPos -> {
            Minecraft client = Minecraft.getInstance();
            Block block = world.getBlockState(blockPos).getBlock();

            if ((block instanceof FlowerBlock || block instanceof TallFlowerBlock) && AutoHarvestConfig.isFlower()) {
                return false;
            }
            if ((block instanceof FlowerBedBlock || block == Blocks.LEAF_LITTER) && AutoHarvestConfig.isFlower()) {
                return false;
            }

            if (WEED_BLOCKS.contains(block) && client.gameMode != null) {
                InteractionHelper.breakBlock(blockPos, Direction.UP);
                return true;
            }
            return false;
        });
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