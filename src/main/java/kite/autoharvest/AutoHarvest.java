package kite.autoharvest;

import kite.autoharvest.command.ModeCommand;
import kite.autoharvest.config.AutoHarvestConfig;
import kite.autoharvest.config.modeEnum;
import kite.autoharvest.manager.ModeManager;
import kite.autoharvest.mode.*;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoHarvest implements ClientModInitializer {
    public static final String MOD_ID = "autoharvest";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final KeyBinding.Category AUTOHARVEST_CATEGORY = new KeyBinding.Category(Identifier.of("kite", "autoharvest"));
    public static final KeyBinding.Category AUTOHARVEST_CATEGORY_MODE = new KeyBinding.Category(Identifier.of("kite", "autoharvest"));
    public static final KeyBinding TOGGLE_KEY = new KeyBinding(
            "key.autoharvest.toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            AUTOHARVEST_CATEGORY
    );
    public static final KeyBinding CYCLE_MODE_KEY = new KeyBinding(
            "key.autoharvest.cycle",
            InputUtil.Type.KEYSYM,
            InputUtil.UNKNOWN_KEY.getCode(),
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

    public static final KeyBinding WEED_KEY = createModeKey("weed");
    public static final KeyBinding PLANT_KEY = createModeKey("plant");
    public static final KeyBinding HARVEST_KEY = createModeKey("harvest");
    public static final KeyBinding FARMER_KEY = createModeKey("farmer");
    public static final KeyBinding BONEMEAL_KEY = createModeKey("bonemeal");
    public static final KeyBinding FEED_KEY = createModeKey("feed");
    public static final KeyBinding FISHING_KEY = createModeKey("fishing");
    public static final KeyBinding HOE_KEY = createModeKey("hoeing");

    private static KeyBinding createModeKey(String name) {
        return new KeyBinding(
                "autoharvest.mode." + name,
                InputUtil.Type.KEYSYM,
                InputUtil.UNKNOWN_KEY.getCode(),
                AUTOHARVEST_CATEGORY_MODE
        );
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("Hello AutoHarvest!");
        AutoConfig.register(AutoHarvestConfig.class, GsonConfigSerializer::new);
        ModeCommand.register();
        KeyBindingHelper.registerKeyBinding(TOGGLE_KEY);
        KeyBindingHelper.registerKeyBinding(CYCLE_MODE_KEY);
        KeyBindingHelper.registerKeyBinding(WEED_KEY);
        KeyBindingHelper.registerKeyBinding(PLANT_KEY);
        KeyBindingHelper.registerKeyBinding(HARVEST_KEY);
        KeyBindingHelper.registerKeyBinding(FARMER_KEY);
        KeyBindingHelper.registerKeyBinding(BONEMEAL_KEY);
        KeyBindingHelper.registerKeyBinding(FEED_KEY);
        KeyBindingHelper.registerKeyBinding(FISHING_KEY);
        KeyBindingHelper.registerKeyBinding(HOE_KEY);
        ClientPlayConnectionEvents.DISCONNECT.register(((hander, client) -> ModeManager.INSTANCE.clearMode()));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (CYCLE_MODE_KEY.wasPressed()) {
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
            if (TOGGLE_KEY.wasPressed()) {
                ModeManager.INSTANCE.toggle();
            }
            if (WEED_KEY.wasPressed()) {
                setModeAndNotify(new WeedMode(), modeEnum.weed);
            } else if (PLANT_KEY.wasPressed()) {
                setModeAndNotify(new PlantMode(), modeEnum.plant);
            } else if (HARVEST_KEY.wasPressed()) {
                setModeAndNotify(new HarvestMode(), modeEnum.harvest);
            } else if (FARMER_KEY.wasPressed()) {
                setModeAndNotify(CompositeMode.farmer(), modeEnum.farmer);
            } else if (BONEMEAL_KEY.wasPressed()) {
                setModeAndNotify(new BonemealMode(), modeEnum.bonemeal);
            } else if (FEED_KEY.wasPressed()) {
                setModeAndNotify(new FeedMode(), modeEnum.feed);
            } else if (FISHING_KEY.wasPressed()) {
                setModeAndNotify(new FishingMode(), modeEnum.fishing);
            } else if (HOE_KEY.wasPressed()) {
                setModeAndNotify(new HoeMode(), modeEnum.hoe);
            }
            ModeManager.INSTANCE.tick();
        });
    }

    private void setModeAndNotify(AutoMode mode, modeEnum configEnum) {
        var player = MinecraftClient.getInstance().player;
        ModeManager.INSTANCE.setCurrentMode(mode);
        AutoHarvestConfig.getInstance().thecurrentMode = configEnum;
        AutoHarvestConfig.save();

        if (player != null) {
            String modeName = switch (configEnum) {
                case weed -> Text.translatable("autoharvest.mode.weed").getString();
                case plant -> Text.translatable("autoharvest.mode.plant").getString();
                case harvest -> Text.translatable("autoharvest.mode.harvest").getString();
                case farmer -> CompositeMode.farmmerode_string();
                case bonemeal -> Text.translatable("autoharvest.mode.bonemeal").getString();
                case feed -> Text.translatable("autoharvest.mode.feed").getString();
                case fishing -> Text.translatable("autoharvest.mode.fishing").getString();
                case hoe -> Text.translatable("autoharvest.mode.hoeing").getString();
            };
            player.sendMessage(Text.literal(Text.translatable("autoharvest.mode.switch").getString() + modeName), false);
        }
    }
}