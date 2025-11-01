package kite.autoharvest.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class InteractionHelper {

    private InteractionHelper() {
    }

    public static void interactBlock(ClientPlayerEntity player, BlockPos blockPos, Hand hand, Direction side) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.interactionManager == null) return;
        Vec3d hitPos = blockPos.toCenterPos();
        BlockHitResult hitResult = new BlockHitResult(hitPos, side, blockPos, false);
        client.interactionManager.interactBlock(player, hand, hitResult);
    }

    public static void breakBlock(BlockPos blockPos, Direction side) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.interactionManager == null) return;
        client.interactionManager.attackBlock(blockPos, side);
    }

    public static void interactEntity(ClientPlayerEntity player, Entity target, Hand hand) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.interactionManager == null) return;
        client.interactionManager.interactEntity(player, target, hand);
    }
    public static void interactItem(ClientPlayerEntity player, Hand hand) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.interactionManager != null) {
            client.interactionManager.interactItem(player, hand);
        }
    }
}