package kite.autoharvest.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "autoharvest")
public class AutoHarvestConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 20)
    public int ticksPerAction = 1; // 默认每 1 tick 执行一次（即每秒 20 次）

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 10, max = 65)
    public int radiusCenti = 45; // 4.5 blocks

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler
    public modeEnum thecurrentMode = modeEnum.plant;

    @ConfigEntry.Gui.Tooltip
    public boolean enableRefill = true;

    @ConfigEntry.Gui.Tooltip
    public boolean autoSwitchFortuneTool = false;

    @ConfigEntry.BoundedDiscrete(min = 1, max = 60)
    @ConfigEntry.Gui.Tooltip
    public long coolDown = 5L;

    @ConfigEntry.BoundedDiscrete(min = 0, max = 3)
    @ConfigEntry.Gui.Tooltip
    public int bambooRadius = 0;

    @ConfigEntry.Gui.Tooltip
    public boolean autoSwitchHotbar = true;

    @ConfigEntry.BoundedDiscrete(min = 10, max = 100)
    @ConfigEntry.Gui.Tooltip
    public int fishingReCastDelay = 10;

    @ConfigEntry.Gui.Tooltip
    public boolean autoSwitchRod = true;

    public double getRadius() {
        return radiusCenti / 10.0;
    }

    public static AutoHarvestConfig getInstance() {
        return AutoConfig.getConfigHolder(AutoHarvestConfig.class).getConfig();
    }


    public static int ticksPerAction() {
        return getInstance().ticksPerAction;
    }

    public static double radius() {
        return getInstance().getRadius();
    }

    public static modeEnum thecurrentMode() {
        return getInstance().thecurrentMode;
    }

    public static void save() {
        AutoConfig.getConfigHolder(AutoHarvestConfig.class).save();
    }

    public static boolean enableRefill() {
        return getInstance().enableRefill;
    }

    public static boolean autoSwitchFortuneTool() {
        return getInstance().autoSwitchFortuneTool;
    }

    public static long coolDown() {
        return getInstance().coolDown * 1000L;
    }

    public static int bambooRadius() {
        return getInstance().bambooRadius;
    }

    public static boolean autoSwitchHotbar() {
        return getInstance().autoSwitchHotbar;
    }

    public static int fishingReCastDelay() {
        return getInstance().fishingReCastDelay;
    }
    public static boolean autoSwitchRod(){
        return getInstance().autoSwitchRod;
    }
}