package kite.autoharvest.mode;

import kite.autoharvest.config.AutoHarvestConfig;
import kite.autoharvest.manager.ModeManager;
import kite.autoharvest.util.BoxUtil;
import kite.autoharvest.util.InteractionHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

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
    }


    @Override
    public void tick() {
        ClientWorld world = BoxUtil.getWorld();
        if (world == null) return;

        Vec3d playerPos = BoxUtil.getPlayerPos();
        if (playerPos == null) return;

        double radius = AutoHarvestConfig.radius();
        Box searchBox = BoxUtil.createSearchBox(playerPos, radius);

        // 遍历区域
        int radiusInt = (int) Math.ceil(radius);
        for (BlockPos blockPos : BlockPos.iterateOutwards(BlockPos.ofFloored(playerPos), radiusInt, radiusInt, radiusInt)) {
            if (!searchBox.contains(blockPos.toCenterPos())) continue;
            if (BoxUtil.isInSphere(blockPos, playerPos, radius)) continue;

            MinecraftClient client = MinecraftClient.getInstance();
            Block block = world.getBlockState(blockPos).getBlock();
            if (WEED_BLOCKS.contains(block)) {
                if (client.interactionManager != null) {
                    InteractionHelper.breakBlock(blockPos, Direction.UP);
                    break;
                }
            }
        }
    }

    @Override
    public String getName() {
        return Text.translatable("autoharvest.mode.weed").getString();
    }

    @Override
    public void onDisable() {
//        Nothing
    }
}