package kite.autoharvest.manager;

import kite.autoharvest.config.AutoHarvestConfig;
import kite.autoharvest.mode.AutoMode;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public enum ModeManager {
    INSTANCE;

    private AutoMode currentMode = null;
    private AutoMode cachedMode = null;
    private int tickCounter = 0;

    public void setCurrentMode(AutoMode mode) {
        currentMode = mode;
    }

    public void setCachedMode() {
        cachedMode = AutoHarvestConfig.thecurrentMode().setMode();
    }

    public void setMode(AutoMode mode) {
        if (this.currentMode != null) {
            this.currentMode.onDisable();
        }
        this.currentMode = mode;
        this.cachedMode = mode;

        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.displayClientMessage(
                    Component.translatable("autoharvest.message.mode.enabled", mode.getName()),
                    false
            );
        }
    }

    public void clearMode() {
        if (this.currentMode != null) {
            this.currentMode.onDisable();
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.displayClientMessage(
                        Component.translatable("autoharvest.message.disabled"),
                        false
                );
            }
        }
        this.currentMode = null;
    }

    public void toggle() {
        setCachedMode();
        if (this.currentMode != null) {
            clearMode();
        } else {
            if (this.cachedMode != null) {
                setMode(this.cachedMode);
            } else {
                AutoMode mode = AutoHarvestConfig.thecurrentMode().setMode();
                setMode(mode);
            }
        }
    }

    public void tick() {
        if (currentMode != null) {
            tickCounter++;
            int interval = AutoHarvestConfig.ticksPerAction();
            if (tickCounter >= interval) {
                currentMode.tick();
                tickCounter = 0;
            }
        }
    }
}