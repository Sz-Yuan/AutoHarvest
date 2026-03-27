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
    private boolean active = false;

    public void setCurrentMode(AutoMode mode) {
        this.currentMode = mode;
        if (AutoHarvestConfig.getInstance().autoenable) {
            activateMode(mode);
        }
    }

    public void setCachedMode() {
        cachedMode = AutoHarvestConfig.thecurrentMode().setMode();
    }

    private void activateMode(AutoMode mode) {
        if (this.currentMode != null && active) {
            this.currentMode.onDisable();
        }
        this.currentMode = mode;
        this.cachedMode = mode;
        this.active = true;

        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.sendSystemMessage(
                    Component.translatable("autoharvest.message.mode.enabled", mode.getName())
            );
        }
    }

    private void deactivateMode() {
        if (this.currentMode != null && active) {
            this.currentMode.onDisable();
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.sendSystemMessage(
                        Component.translatable("autoharvest.message.disabled")
                );
            }
        }
        this.currentMode = null;
        this.cachedMode = null;
        this.active = false;
    }

    public void toggle() {
        if (active) {
            deactivateMode();
        } else {
            setCachedMode();
            AutoMode modeToActivate = (cachedMode != null) ? cachedMode : AutoHarvestConfig.thecurrentMode().setMode();
            activateMode(modeToActivate);
        }
    }

    public void clearMode() {
        deactivateMode();
    }

    public void tick() {
        if (active && currentMode != null) {
            tickCounter++;
            int interval = AutoHarvestConfig.ticksPerAction();
            if (tickCounter >= interval) {
                currentMode.tick();
                tickCounter = 0;
            }
        }
    }
}