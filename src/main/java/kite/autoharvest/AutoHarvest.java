package kite.autoharvest;

import com.mojang.blaze3d.platform.InputConstants;
import kite.autoharvest.command.ModeCommand;
import kite.autoharvest.config.AutoHarvestConfig;
import kite.autoharvest.config.ModeEnum_a;
import kite.autoharvest.manager.ModeManager;
import kite.autoharvest.mode.*;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AutoHarvest implements ClientModInitializer {
    public static final String MOD_ID = "autoharvest";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final KeyMapping.Category AUTOHARVEST_CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath("kite", "autoharvest"));
    public static final KeyMapping.Category AUTOHARVEST_CATEGORY_MODE = new KeyMapping.Category(Identifier.fromNamespaceAndPath("kite", "autoharvest"));
    public static final KeyMapping TOGGLE_KEY = new KeyMapping("key.autoharvest.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, AUTOHARVEST_CATEGORY);
    public static final KeyMapping CYCLE_MODE_KEY = new KeyMapping("key.autoharvest.cycle", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), AUTOHARVEST_CATEGORY);

    private static final ModeEnum_a[] CYCLE_ORDER = {
            ModeEnum_a.weed, ModeEnum_a.plant, ModeEnum_a.harvest, ModeEnum_a.farmer,
            ModeEnum_a.bonemeal, ModeEnum_a.feed, ModeEnum_a.fishing, ModeEnum_a.hoe
    };

    private static ModeEnum_a getNextMode(ModeEnum_a current) {
        for (int i = 0; i < CYCLE_ORDER.length; i++) {
            if (CYCLE_ORDER[i] == current) {
                return CYCLE_ORDER[(i + 1) % CYCLE_ORDER.length];
            }
        }
        return CYCLE_ORDER[0];
    }

    public static final KeyMapping WEED_KEY = createModeKey("weed");
    public static final KeyMapping PLANT_KEY = createModeKey("plant");
    public static final KeyMapping HARVEST_KEY = createModeKey("harvest");
    public static final KeyMapping FARMER_KEY = createModeKey("farmer");
    public static final KeyMapping BONEMEAL_KEY = createModeKey("bonemeal");
    public static final KeyMapping FEED_KEY = createModeKey("feed");
    public static final KeyMapping FISHING_KEY = createModeKey("fishing");
    public static final KeyMapping HOE_KEY = createModeKey("hoeing");

    private static KeyMapping createModeKey(String name) {
        return new KeyMapping("autoharvest.mode." + name, InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), AUTOHARVEST_CATEGORY_MODE);
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("Hello AutoHarvest!");
        AutoConfig.register(AutoHarvestConfig.class, GsonConfigSerializer::new);
        ModeCommand.register();
        KeyMappingHelper.registerKeyMapping(TOGGLE_KEY);
        KeyMappingHelper.registerKeyMapping(CYCLE_MODE_KEY);
        KeyMappingHelper.registerKeyMapping(WEED_KEY);
        KeyMappingHelper.registerKeyMapping(PLANT_KEY);
        KeyMappingHelper.registerKeyMapping(HARVEST_KEY);
        KeyMappingHelper.registerKeyMapping(FARMER_KEY);
        KeyMappingHelper.registerKeyMapping(BONEMEAL_KEY);
        KeyMappingHelper.registerKeyMapping(FEED_KEY);
        KeyMappingHelper.registerKeyMapping(FISHING_KEY);
        KeyMappingHelper.registerKeyMapping(HOE_KEY);
        ClientPlayConnectionEvents.DISCONNECT.register(((handler, client) -> ModeManager.INSTANCE.clearMode()));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (CYCLE_MODE_KEY.consumeClick()) {
                ModeEnum_a next = getNextMode(AutoHarvestConfig.getInstance().thecurrentMode);
                switchTo(next);
            }
            if (TOGGLE_KEY.consumeClick()) {
                ModeManager.INSTANCE.toggle();
            }
            if (WEED_KEY.consumeClick()) {
                switchTo(ModeEnum_a.weed);
            } else if (PLANT_KEY.consumeClick()) {
                switchTo(ModeEnum_a.plant);
            } else if (HARVEST_KEY.consumeClick()) {
                switchTo(ModeEnum_a.harvest);
            } else if (FARMER_KEY.consumeClick()) {
                switchTo(ModeEnum_a.farmer);
            } else if (BONEMEAL_KEY.consumeClick()) {
                switchTo(ModeEnum_a.bonemeal);
            } else if (FEED_KEY.consumeClick()) {
                switchTo(ModeEnum_a.feed);
            } else if (FISHING_KEY.consumeClick()) {
                switchTo(ModeEnum_a.fishing);
            } else if (HOE_KEY.consumeClick()) {
                switchTo(ModeEnum_a.hoe);
            }
            ModeManager.INSTANCE.tick();
        });
    }

    private static void switchTo(ModeEnum_a configEnum) {
        setModeAndNotify(configEnum.setMode(), configEnum);
    }

    private static void setModeAndNotify(AutoMode mode, ModeEnum_a configEnum) {
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