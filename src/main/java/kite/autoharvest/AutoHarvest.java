package kite.autoharvest;

import kite.autoharvest.command.ModeCommand;
import kite.autoharvest.config.AutoHarvestConfig;
import kite.autoharvest.manager.ModeManager;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoHarvest implements ClientModInitializer {
    public static final String MOD_ID = "autoharvest";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final KeyBinding TOGGLE_KEY = new KeyBinding(
            "key.autoharvest.toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "category.autoharvest"
    );

    @Override
    public void onInitializeClient() {
        LOGGER.info("Hello AutoHarvest!");
        AutoConfig.register(AutoHarvestConfig.class, GsonConfigSerializer::new);
        ModeCommand.register();
        KeyBindingHelper.registerKeyBinding(TOGGLE_KEY);
        ClientPlayConnectionEvents.DISCONNECT.register(((hander, client) -> {
            ModeManager.INSTANCE.clearMode();
        }));

        // Tick 逻辑
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (TOGGLE_KEY.wasPressed()) {
                ModeManager.INSTANCE.toggle();
            }
            ModeManager.INSTANCE.tick();
        });
    }
}