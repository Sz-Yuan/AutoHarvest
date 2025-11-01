package kite.autoharvest.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class BoxUtil {

    public static Vec3d getPlayerPos() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return null;
        return player.getPos();
    }

    public static ClientWorld getWorld() {
        return MinecraftClient.getInstance().world;
    }

    public static ClientPlayerEntity getPlayer() {
        return MinecraftClient.getInstance().player;
    }

    public static Box createSearchBox(Vec3d center, double radius) {
        return new Box(
                center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius
        );
    }

    public static boolean isInSphere(BlockPos pos, Vec3d center, double radius) {
        return !center.isInRange(pos.toCenterPos(), radius);
    }
}