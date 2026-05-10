package kite.autoharvest.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;

public final class WaterProximityChecker {

    private WaterProximityChecker() {
    }

    private static final Direction[] HORIZONTAL_DIRECTIONS = {
            Direction.NORTH,
            Direction.SOUTH,
            Direction.WEST,
            Direction.EAST
    };

    public static boolean isAdjacentToSourceWaterHorizontally(Level world, BlockPos pos) {
        for (Direction dir : HORIZONTAL_DIRECTIONS) {
            BlockPos offsetPos = pos.relative(dir);
            var state = world.getBlockState(offsetPos);
            if (state.getBlock() == Blocks.WATER) {
                FluidState fluid = state.getFluidState();
                if (fluid.is(FluidTags.WATER) && fluid.isSource()) {
                    return true;
                }
            }
            if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasNoWaterNearby(Level world, BlockPos pos) {
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                {
                    BlockPos checkPos = pos.offset(dx, 0, dz);
                    var state = world.getBlockState(checkPos);
                    var fluidState = world.getFluidState(checkPos);
                    if (fluidState.is(FluidTags.WATER) && fluidState.isSource()) {
                        return false;
                    }
                    if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}