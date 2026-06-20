package kite.autoharvest.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

public class BoxUtil {

    public static Vec3 getPlayerPos() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return null;
        return player.position();
    }

    public static ClientLevel getWorld() {
        return Minecraft.getInstance().level;
    }

    public static LocalPlayer getPlayer() {
        return Minecraft.getInstance().player;
    }

    public static AABB createSearchBox(Vec3 center, double radius) {
        return new AABB(
                center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius
        );
    }

    public static boolean isOutsideSphere(BlockPos pos, Vec3 center, double radius) {
        return !center.closerThan(Vec3.atCenterOf(pos), radius);
    }

    public static void forEachBlockInRange(Vec3 playerPos, double radius, Predicate<BlockPos> action) {
        AABB searchBox = createSearchBox(playerPos, radius);
        int radiusInt = (int) Math.ceil(radius);
        for (BlockPos pos : BlockPos.withinManhattan(BlockPos.containing(playerPos), radiusInt, radiusInt, radiusInt)) {
            if (!searchBox.contains(Vec3.atCenterOf(pos))) continue;
            if (isOutsideSphere(pos, playerPos, radius)) continue;
            if (action.test(pos)) return;
        }
    }
}
