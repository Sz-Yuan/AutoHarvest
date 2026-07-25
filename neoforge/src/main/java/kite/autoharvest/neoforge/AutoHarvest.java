package kite.autoharvest.neoforge;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import kite.autoharvest.AutoHarvestClient;
import kite.autoharvest.command.ModeCommand;
import kite.autoharvest.config.AutoHarvestConfig;
import kite.autoharvest.manager.ModeManager;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = AutoHarvestClient.MOD_ID, dist = Dist.CLIENT)
public class AutoHarvest {
    public AutoHarvest(IEventBus modEventBus, ModContainer modContainer) {
        AutoConfig.register(AutoHarvestConfig.class, GsonConfigSerializer::new);

        modContainer.registerExtensionPoint(
                net.neoforged.neoforge.client.gui.IConfigScreenFactory.class,
                (client, parent) -> me.shedaniel.autoconfig.AutoConfigClient.getConfigScreen(AutoHarvestConfig.class, parent).get());

        modEventBus.addListener(this::registerKeyMappings);
        NeoForge.EVENT_BUS.register(this);
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(AutoHarvestClient.TOGGLE_KEY);
        event.register(AutoHarvestClient.CYCLE_MODE_KEY);
        event.register(AutoHarvestClient.WEED_KEY);
        event.register(AutoHarvestClient.PLANT_KEY);
        event.register(AutoHarvestClient.HARVEST_KEY);
        event.register(AutoHarvestClient.FARMER_KEY);
        event.register(AutoHarvestClient.BONEMEAL_KEY);
        event.register(AutoHarvestClient.FEED_KEY);
        event.register(AutoHarvestClient.FISHING_KEY);
        event.register(AutoHarvestClient.HOE_KEY);
    }

    @SubscribeEvent
    private void registerClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                LiteralArgumentBuilder.<CommandSourceStack>literal("autoharvest")
                        .then(LiteralArgumentBuilder.<CommandSourceStack>literal("toggle")
                                .executes(ModeCommand::executeToggle))
                        .then(LiteralArgumentBuilder.<CommandSourceStack>literal("mode")
                                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("mode",
                                                StringArgumentType.word())
                                        .suggests((context, builder) -> ModeCommand.suggestModes(builder))
                                        .executes(ModeCommand::executeSetMode)))
        );
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        AutoHarvestClient.tick(net.minecraft.client.Minecraft.getInstance());
    }

    @SubscribeEvent
    public void onClientPlayerDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        ModeManager.INSTANCE.clearMode();
    }
}
