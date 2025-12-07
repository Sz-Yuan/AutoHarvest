package kite.autoharvest.util;

import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public final class WaterProximityChecker {

    private WaterProximityChecker() {
    }

    private static final Direction[] HORIZONTAL_DIRECTIONS = {
            Direction.NORTH,
            Direction.SOUTH,
            Direction.WEST,
            Direction.EAST
    };

    public static boolean isAdjacentToSourceWaterHorizontally(World world, BlockPos pos) {
        for (Direction dir : HORIZONTAL_DIRECTIONS) {
            BlockPos offsetPos = pos.offset(dir);
            var state = world.getBlockState(offsetPos);
            if (state.getBlock() == Blocks.WATER) {
                FluidState fluid = state.getFluidState();
                if (fluid.isIn(FluidTags.WATER) && fluid.getLevel() == 8) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isWithinHydrationRange(World world, BlockPos pos) {
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos checkPos = pos.add(dx, dy, dz);
                    var state = world.getBlockState(checkPos);
                    var fluidState = world.getFluidState(checkPos);
                    if (fluidState.isIn(FluidTags.WATER) && fluidState.getLevel() == 8) {
                        return false;
                    }
                    if (state.contains(Properties.WATERLOGGED) && state.get(Properties.WATERLOGGED)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}