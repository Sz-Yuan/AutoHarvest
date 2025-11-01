package kite.autoharvest.mode;

import kite.autoharvest.config.AutoHarvestConfig;
import kite.autoharvest.util.InteractionHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

public class FishingMode implements AutoMode {

    private long fishBitesAt = 0;
    private long bobberSpawnTime = 0;
    private double baselineY = Double.NaN;
    private double lastBobberX = Double.NaN;
    private double lastBobberY = Double.NaN;
    private double lastBobberZ = Double.NaN;
    private int stationaryTicks = 0;
    private long firstStuckTime = -1;

    private static final int STATIONARY_THRESHOLD_TICKS = 60;
    private static final long REPEAT_MESSAGE_INTERVAL = 100;
    private static final double HORIZONTAL_MOVEMENT_THRESHOLD = 0.01;
    private static final double DEPTH_THRESHOLD = 0.06;
    private static final int STABLE_DELAY_TICKS = 30;

    @Override
    public void tick() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            resetAllState();
            return;
        }
        Hand rodHand = getFishingRodHand(player);
        if (rodHand == null) {
            int rodSlot = findFishingRodSlot(player);
            if (rodSlot != -1) {
                if (AutoHarvestConfig.autoSwitchRod()) {
                    player.getInventory().setSelectedSlot(rodSlot);
                }
                rodHand = getFishingRodHand(player);
            }
            if (rodHand == null) {
                resetAllState();
                return;
            }
        }

        FishingBobberEntity bobber = player.fishHook;
        long currentTime = getCurrentWorldTime();

        if (fishBitesAt != 0) {
            int delay = AutoHarvestConfig.fishingReCastDelay();
            if (currentTime >= fishBitesAt + delay) {
                InteractionHelper.interactItem(player, rodHand);
                resetAllState();
            }
            return;
        }

        if (bobber == null || !bobber.isAlive()) {
            resetAllState();
            return;
        }

        if (bobberSpawnTime == 0) {
            bobberSpawnTime = currentTime;
        }

        if (currentTime < bobberSpawnTime + STABLE_DELAY_TICKS) {
            if (Double.isNaN(baselineY) || bobber.getY() < baselineY) {
                baselineY = bobber.getY();
            }
            updateStationaryState(bobber, false, currentTime);
            return;
        }

        if (Double.isNaN(baselineY)) {
            baselineY = bobber.getY();
        }

        updateStationaryState(bobber, true, currentTime);
        checkAndNotifyStuck(player, currentTime);

        if (isFishBites(bobber, baselineY)) {
            fishBitesAt = currentTime;
            InteractionHelper.interactItem(player, rodHand);
        }
    }

    private int findFishingRodSlot(ClientPlayerEntity player) {
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getStack(i).isOf(Items.FISHING_ROD)) {
                return i;
            }
        }
        return -1;
    }

    private void updateStationaryState(FishingBobberEntity bobber, boolean allowStuckDetection, long currentTime) {
        double x = bobber.getX();
        double y = bobber.getY();
        double z = bobber.getZ();

        boolean hasMoved = Double.isNaN(lastBobberX) ||
                x != lastBobberX ||
                y != lastBobberY ||
                z != lastBobberZ;

        if (hasMoved) {
            lastBobberX = x;
            lastBobberY = y;
            lastBobberZ = z;
            stationaryTicks = 0;
            firstStuckTime = -1;
        } else {
            stationaryTicks++;
            if (allowStuckDetection && firstStuckTime == -1 && stationaryTicks >= STATIONARY_THRESHOLD_TICKS) {
                firstStuckTime = currentTime;
            }
        }
    }

    private void checkAndNotifyStuck(ClientPlayerEntity player, long currentTime) {
        if (firstStuckTime == -1) {
            return;
        }
        if ((currentTime - firstStuckTime) % REPEAT_MESSAGE_INTERVAL == 0) {
            if (currentTime > firstStuckTime) {
                player.sendMessage(Text.translatable("autoharvest.mode.fishing.error"), true);
            }
        }
    }

    private boolean isFishBites(FishingBobberEntity bobber, double baselineY) {
        double dx = bobber.getX() - bobber.lastX;
        double dz = bobber.getZ() - bobber.lastZ;
        double currentY = bobber.getY();

        boolean horizontalStill = Math.abs(dx) < HORIZONTAL_MOVEMENT_THRESHOLD
                && Math.abs(dz) < HORIZONTAL_MOVEMENT_THRESHOLD;
        boolean hasSunk = (baselineY - currentY) > DEPTH_THRESHOLD;

        return horizontalStill && hasSunk;
    }

    private void resetAllState() {
        fishBitesAt = 0;
        bobberSpawnTime = 0;
        baselineY = Double.NaN;
        lastBobberX = lastBobberY = lastBobberZ = Double.NaN;
        stationaryTicks = 0;
        firstStuckTime = -1;
    }

    private long getCurrentWorldTime() {
        var world = MinecraftClient.getInstance().world;
        return world != null ? world.getTime() : 0L;
    }

    private Hand getFishingRodHand(ClientPlayerEntity player) {
        if (player.getMainHandStack().isOf(Items.FISHING_ROD)) {
            return Hand.MAIN_HAND;
        }
        if (player.getOffHandStack().isOf(Items.FISHING_ROD)) {
            return Hand.OFF_HAND;
        }
        return null;
    }

    @Override
    public String getName() {
        return Text.translatable("autoharvest.mode.fishing").getString();
    }

    @Override
    public void onDisable() {
        resetAllState();
    }
}