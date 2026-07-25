package kite.autoharvest;

import com.mojang.blaze3d.platform.InputConstants;
import kite.autoharvest.config.AutoHarvestConfig;
import kite.autoharvest.config.ModeEnum;
import kite.autoharvest.manager.ModeManager;
import kite.autoharvest.mode.AutoMode;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoHarvestClient {
    public static final String MOD_ID = "autoharvest";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final KeyMapping.Category AUTOHARVEST_CATEGORY =
            new KeyMapping.Category(Identifier.fromNamespaceAndPath("kite", "autoharvest"));
    public static final KeyMapping.Category AUTOHARVEST_CATEGORY_MODE =
            new KeyMapping.Category(Identifier.fromNamespaceAndPath("kite", "autoharvest"));

    public static final KeyMapping TOGGLE_KEY = new KeyMapping(
            "key.autoharvest.toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            AUTOHARVEST_CATEGORY);
    public static final KeyMapping CYCLE_MODE_KEY = new KeyMapping(
            "key.autoharvest.cycle",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            AUTOHARVEST_CATEGORY);

    private static final ModeEnum[] CYCLE_ORDER = {
            ModeEnum.weed, ModeEnum.plant, ModeEnum.harvest, ModeEnum.farmer,
            ModeEnum.bonemeal, ModeEnum.feed, ModeEnum.fishing, ModeEnum.hoe
    };

    public static final KeyMapping WEED_KEY = createModeKey("weed");
    public static final KeyMapping PLANT_KEY = createModeKey("plant");
    public static final KeyMapping HARVEST_KEY = createModeKey("harvest");
    public static final KeyMapping FARMER_KEY = createModeKey("farmer");
    public static final KeyMapping BONEMEAL_KEY = createModeKey("bonemeal");
    public static final KeyMapping FEED_KEY = createModeKey("feed");
    public static final KeyMapping FISHING_KEY = createModeKey("fishing");
    public static final KeyMapping HOE_KEY = createModeKey("hoeing");

    private static KeyMapping createModeKey(String name) {
        return new KeyMapping(
                "autoharvest.mode." + name,
                InputConstants.Type.KEYSYM,
                InputConstants.UNKNOWN.getValue(),
                AUTOHARVEST_CATEGORY_MODE);
    }

    public static void tick(Minecraft client) {
        if (CYCLE_MODE_KEY.consumeClick()) {
            ModeEnum next = getNextMode(AutoHarvestConfig.getInstance().thecurrentMode);
            switchTo(next);
        }
        if (TOGGLE_KEY.consumeClick()) {
            ModeManager.INSTANCE.toggle();
        }
        if (WEED_KEY.consumeClick()) {
            switchTo(ModeEnum.weed);
        } else if (PLANT_KEY.consumeClick()) {
            switchTo(ModeEnum.plant);
        } else if (HARVEST_KEY.consumeClick()) {
            switchTo(ModeEnum.harvest);
        } else if (FARMER_KEY.consumeClick()) {
            switchTo(ModeEnum.farmer);
        } else if (BONEMEAL_KEY.consumeClick()) {
            switchTo(ModeEnum.bonemeal);
        } else if (FEED_KEY.consumeClick()) {
            switchTo(ModeEnum.feed);
        } else if (FISHING_KEY.consumeClick()) {
            switchTo(ModeEnum.fishing);
        } else if (HOE_KEY.consumeClick()) {
            switchTo(ModeEnum.hoe);
        }
        ModeManager.INSTANCE.tick();
    }

    private static ModeEnum getNextMode(ModeEnum current) {
        for (int i = 0; i < CYCLE_ORDER.length; i++) {
            if (CYCLE_ORDER[i] == current) {
                return CYCLE_ORDER[(i + 1) % CYCLE_ORDER.length];
            }
        }
        return CYCLE_ORDER[0];
    }

    private static void switchTo(ModeEnum configEnum) {
        setModeAndNotify(configEnum.setMode(), configEnum);
    }

    public static void setModeAndNotify(AutoMode mode, ModeEnum configEnum) {
        var player = Minecraft.getInstance().player;
        ModeManager.INSTANCE.setCurrentMode(mode);
        AutoHarvestConfig.getInstance().thecurrentMode = configEnum;
        AutoHarvestConfig.save();

        if (player != null) {
            player.sendSystemMessage(Component.literal(
                    Component.translatable("autoharvest.mode.switch").getString() + mode.getName()));
        }
    }
}
