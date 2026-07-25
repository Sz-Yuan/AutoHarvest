package kite.autoharvest.fabric;

import com.mojang.brigadier.arguments.StringArgumentType;
import kite.autoharvest.AutoHarvestClient;
import kite.autoharvest.command.ModeCommand;
import kite.autoharvest.config.AutoHarvestConfig;
import kite.autoharvest.manager.ModeManager;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class AutoHarvest implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AutoHarvestClient.LOGGER.info("Hello AutoHarvest (Fabric)!");

        AutoConfig.register(AutoHarvestConfig.class, GsonConfigSerializer::new);

        KeyMappingHelper.registerKeyMapping(AutoHarvestClient.TOGGLE_KEY);
        KeyMappingHelper.registerKeyMapping(AutoHarvestClient.CYCLE_MODE_KEY);
        KeyMappingHelper.registerKeyMapping(AutoHarvestClient.WEED_KEY);
        KeyMappingHelper.registerKeyMapping(AutoHarvestClient.PLANT_KEY);
        KeyMappingHelper.registerKeyMapping(AutoHarvestClient.HARVEST_KEY);
        KeyMappingHelper.registerKeyMapping(AutoHarvestClient.FARMER_KEY);
        KeyMappingHelper.registerKeyMapping(AutoHarvestClient.BONEMEAL_KEY);
        KeyMappingHelper.registerKeyMapping(AutoHarvestClient.FEED_KEY);
        KeyMappingHelper.registerKeyMapping(AutoHarvestClient.FISHING_KEY);
        KeyMappingHelper.registerKeyMapping(AutoHarvestClient.HOE_KEY);

        ClientPlayConnectionEvents.DISCONNECT.register(((handler, client) -> ModeManager.INSTANCE.clearMode()));
        ClientTickEvents.END_CLIENT_TICK.register(AutoHarvestClient::tick);

        registerCommands();
    }

    private void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> {
            dispatcher.register(
                    ClientCommands.literal("autoharvest")
                            .then(ClientCommands.literal("toggle")
                                    .executes(ModeCommand::executeToggle))
                            .then(ClientCommands.literal("mode")
                                    .then(ClientCommands.argument("mode", StringArgumentType.word())
                                            .suggests((context, builder) -> ModeCommand.suggestModes(builder))
                                            .executes(ModeCommand::executeSetMode)))
            );
        });
    }
}
