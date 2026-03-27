package kite.autoharvest.mode;

import kite.autoharvest.config.AutoHarvestConfig;
import kite.autoharvest.util.InteractionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Items;

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
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            resetAllState();
            return;
        }
        InteractionHand rodHand = getFishingRodHand(player);
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

        FishingHook bobber = player.fishing;
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

    private int findFishingRodSlot(LocalPlayer player) {
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getItem(i).is(Items.FISHING_ROD)) {
                return i;
            }
        }
        return -1;
    }

    private void updateStationaryState(FishingHook bobber, boolean allowStuckDetection, long currentTime) {
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

    private void checkAndNotifyStuck(LocalPlayer player, long currentTime) {
        if (firstStuckTime == -1) {
            return;
        }
        if ((currentTime - firstStuckTime) % REPEAT_MESSAGE_INTERVAL == 0) {
            if (currentTime > firstStuckTime) {
                player.sendOverlayMessage(Component.translatable("autoharvest.mode.fishing.error"));
            }
        }
    }

    private boolean isFishBites(FishingHook bobber, double baselineY) {
        double dx = bobber.getX() - bobber.xo;
        double dz = bobber.getZ() - bobber.zo;
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
        var world = Minecraft.getInstance().level;
        return world != null ? world.getGameTime() : 0L;
    }

    private InteractionHand getFishingRodHand(LocalPlayer player) {
        if (player.getMainHandItem().is(Items.FISHING_ROD)) {
            return InteractionHand.MAIN_HAND;
        }
        if (player.getOffhandItem().is(Items.FISHING_ROD)) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }

    @Override
    public String getName() {
        return Component.translatable("autoharvest.mode.fishing").getString();
    }

    @Override
    public void onDisable() {
        resetAllState();
    }
}