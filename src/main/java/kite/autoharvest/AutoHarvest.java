package kite.autoharvest;

import com.mojang.blaze3d.platform.InputConstants;
import kite.autoharvest.command.ModeCommand;
import kite.autoharvest.config.AutoHarvestConfig;
import kite.autoharvest.config.modeEnum;
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
    public static final KeyMapping TOGGLE_KEY = new KeyMapping(
            "key.autoharvest.toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            AUTOHARVEST_CATEGORY
    );
    public static final KeyMapping CYCLE_MODE_KEY = new KeyMapping(
            "key.autoharvest.cycle",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            AUTOHARVEST_CATEGORY
    );

    private static modeEnum getNextMode(modeEnum current) {
        return switch (current) {
            case weed -> modeEnum.plant;
            case plant -> modeEnum.harvest;
            case harvest -> modeEnum.farmer;
            case farmer -> modeEnum.bonemeal;
            case bonemeal -> modeEnum.feed;
            case feed -> modeEnum.fishing;
            case fishing -> modeEnum.hoe;
            case hoe -> modeEnum.weed;
        };
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
        return new KeyMapping(
                "autoharvest.mode." + name,
                InputConstants.Type.KEYSYM,
                InputConstants.UNKNOWN.getValue(),
                AUTOHARVEST_CATEGORY_MODE
        );
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
        ClientPlayConnectionEvents.DISCONNECT.register(((_, _) -> ModeManager.INSTANCE.clearMode()));
        ClientTickEvents.END_CLIENT_TICK.register(_ -> {
            if (CYCLE_MODE_KEY.consumeClick()) {
                modeEnum current = AutoHarvestConfig.getInstance().thecurrentMode;
                modeEnum next = getNextMode(current);
                switch (next) {
                    case weed -> setModeAndNotify(new WeedMode(), modeEnum.weed);
                    case plant -> setModeAndNotify(new PlantMode(), modeEnum.plant);
                    case harvest -> setModeAndNotify(new HarvestMode(), modeEnum.harvest);
                    case farmer -> setModeAndNotify(CompositeMode.farmer(), modeEnum.farmer);
                    case bonemeal -> setModeAndNotify(new BonemealMode(), modeEnum.bonemeal);
                    case feed -> setModeAndNotify(new FeedMode(), modeEnum.feed);
                    case fishing -> setModeAndNotify(new FishingMode(), modeEnum.fishing);
                    case hoe -> setModeAndNotify(new HoeMode(), modeEnum.hoe);
                }
            }
            if (TOGGLE_KEY.consumeClick()) {
                ModeManager.INSTANCE.toggle();
            }
            if (WEED_KEY.consumeClick()) {
                setModeAndNotify(new WeedMode(), modeEnum.weed);
            } else if (PLANT_KEY.consumeClick()) {
                setModeAndNotify(new PlantMode(), modeEnum.plant);
            } else if (HARVEST_KEY.consumeClick()) {
                setModeAndNotify(new HarvestMode(), modeEnum.harvest);
            } else if (FARMER_KEY.consumeClick()) {
                setModeAndNotify(CompositeMode.farmer(), modeEnum.farmer);
            } else if (BONEMEAL_KEY.consumeClick()) {
                setModeAndNotify(new BonemealMode(), modeEnum.bonemeal);
            } else if (FEED_KEY.consumeClick()) {
                setModeAndNotify(new FeedMode(), modeEnum.feed);
            } else if (FISHING_KEY.consumeClick()) {
                setModeAndNotify(new FishingMode(), modeEnum.fishing);
            } else if (HOE_KEY.consumeClick()) {
                setModeAndNotify(new HoeMode(), modeEnum.hoe);
            }
            ModeManager.INSTANCE.tick();
        });
    }

    private static void setModeAndNotify(AutoMode mode, modeEnum configEnum) {
        var player = Minecraft.getInstance().player;
        ModeManager.INSTANCE.setCurrentMode(mode);
        AutoHarvestConfig.getInstance().thecurrentMode = configEnum;
        AutoHarvestConfig.save();

        if (player != null) {
            String modeName = switch (configEnum) {
                case weed -> Component.translatable("autoharvest.mode.weed").getString();
                case plant -> Component.translatable("autoharvest.mode.plant").getString();
                case harvest -> Component.translatable("autoharvest.mode.harvest").getString();
                case farmer -> CompositeMode.farmmerode_string();
                case bonemeal -> Component.translatable("autoharvest.mode.bonemeal").getString();
                case feed -> Component.translatable("autoharvest.mode.feed").getString();
                case fishing -> Component.translatable("autoharvest.mode.fishing").getString();
                case hoe -> Component.translatable("autoharvest.mode.hoeing").getString();
            };
            player.sendSystemMessage(Component.literal(Component.translatable("autoharvest.mode.switch").getString() + modeName));
        }
    }
}