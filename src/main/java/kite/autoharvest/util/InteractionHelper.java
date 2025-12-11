package kite.autoharvest.util;


import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class InteractionHelper {

    private InteractionHelper() {
    }

    public static void interactBlock(LocalPlayer player, BlockPos blockPos, InteractionHand hand, Direction side) {
        Minecraft client = Minecraft.getInstance();
        if (client.gameMode == null) return;
        Vec3 hitPos = blockPos.getCenter();
        BlockHitResult hitResult = new BlockHitResult(hitPos, side, blockPos, false);
        client.gameMode.useItemOn(player, hand, hitResult);
    }

    public static void breakBlock(BlockPos blockPos, Direction side) {
        Minecraft client = Minecraft.getInstance();
        if (client.gameMode == null) return;
        client.gameMode.startDestroyBlock(blockPos, side);
    }

    public static void interactEntity(LocalPlayer player, Entity target, InteractionHand hand) {
        Minecraft client = Minecraft.getInstance();
        if (client.gameMode == null) return;
        client.gameMode.interact(player, target, hand);
    }
    public static void interactItem(LocalPlayer player, InteractionHand hand) {
        Minecraft client = Minecraft.getInstance();
        if (client.gameMode != null) {
            client.gameMode.useItem(player, hand);
        }
    }
}