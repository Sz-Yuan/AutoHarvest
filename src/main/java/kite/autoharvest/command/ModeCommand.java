package kite.autoharvest.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import kite.autoharvest.config.AutoHarvestConfig;
import kite.autoharvest.config.modeEnum;
import kite.autoharvest.manager.ModeManager;
import kite.autoharvest.mode.*;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static kite.autoharvest.config.modeEnum.*;

public class ModeCommand {

    private static final List<String> MODE_NAMES = Arrays.asList(
            "weed", "plant", "harvest", "farmer", "bonemeal", "feed", "fishing", "hoe"
    );

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> {
            var autoharvest = ClientCommands.literal("autoharvest");

            autoharvest
                    .then(ClientCommands.literal("toggle").executes(ModeCommand::executeToggle));

            autoharvest
                    .then(ClientCommands.literal("mode")
                            .then(ClientCommands.argument("mode", StringArgumentType.word())
                                    .suggests((context, builder) -> suggestModes(builder))
                                    .executes(ModeCommand::executeSetMode)
                            )
                    );

            dispatcher.register(autoharvest);
        });
    }

    private static CompletableFuture<Suggestions> suggestModes(SuggestionsBuilder builder) {
        for (String mode : MODE_NAMES) {
            builder.suggest(mode);
        }
        return builder.buildFuture();
    }

    private static int executeToggle(CommandContext<FabricClientCommandSource> context) {
        ModeManager.INSTANCE.toggle();
        return 1;
    }

    private static int executeSetMode(CommandContext<FabricClientCommandSource> context) {
        LocalPlayer player = Minecraft.getInstance().player;
        String modeName = StringArgumentType.getString(context, "mode").toLowerCase();
        switch (modeName) {
            case "weed" -> {
                ModeManager.INSTANCE.setCurrentMode(new WeedMode());
                AutoHarvestConfig.getInstance().thecurrentMode = modeEnum.weed;
                AutoHarvestConfig.save();
                if (player != null) {
                    player.sendSystemMessage(Component.literal(Component.translatable("autoharvest.mode.switch").getString() + Component.translatable("autoharvest.mode.weed").getString()));
                }
                return 1;
            }
            case "plant" -> {
                ModeManager.INSTANCE.setCurrentMode(new PlantMode());
                AutoHarvestConfig.getInstance().thecurrentMode = modeEnum.plant;
                AutoHarvestConfig.save();
                if (player != null) {
                    player.sendSystemMessage(Component.literal(Component.translatable("autoharvest.mode.switch").getString() + Component.translatable("autoharvest.mode.plant").getString()));
                }
                return 1;
            }
            case "hoe" -> {
                ModeManager.INSTANCE.setCurrentMode(new HoeMode());
                AutoHarvestConfig.getInstance().thecurrentMode = modeEnum.hoe;
                AutoHarvestConfig.save();
                if (player != null) {
                    player.sendSystemMessage(Component.literal(Component.translatable("autoharvest.mode.switch").getString() + Component.translatable("autoharvest.mode.hoeing").getString()));
                }
                return 1;
            }
            case "bonemeal" -> {
                ModeManager.INSTANCE.setCurrentMode(new BonemealMode());
                AutoHarvestConfig.getInstance().thecurrentMode = modeEnum.bonemeal;
                AutoHarvestConfig.save();
                if (player != null) {
                    player.sendSystemMessage(Component.literal(Component.translatable("autoharvest.mode.switch").getString() + Component.translatable("autoharvest.mode.bonemeal").getString()));
                }
                return 1;
            }
            case "harvest" -> {
                ModeManager.INSTANCE.setCurrentMode(new HarvestMode());
                AutoHarvestConfig.getInstance().thecurrentMode = modeEnum.harvest;
                AutoHarvestConfig.save();
                if (player != null) {
                    player.sendSystemMessage(Component.literal(Component.translatable("autoharvest.mode.switch").getString() + Component.translatable("autoharvest.mode.harvest").getString()));
                }
                return 1;
            }
            case "farmer" -> {
                ModeManager.INSTANCE.setCurrentMode(CompositeMode.farmer());
                AutoHarvestConfig.getInstance().thecurrentMode = farmer;
                AutoHarvestConfig.save();
                if (player != null) {
                    player.sendSystemMessage(Component.literal(Component.translatable("autoharvest.mode.switch").getString() + CompositeMode.farmmerode_string()));
                }
                return 1;
            }
            case "feed" -> {
                ModeManager.INSTANCE.setCurrentMode(new FeedMode());
                AutoHarvestConfig.getInstance().thecurrentMode = feed;
                AutoHarvestConfig.save();
                if (player != null) {
                    player.sendSystemMessage(Component.literal(Component.translatable("autoharvest.mode.switch").getString() + Component.translatable("autoharvest.mode.feed").getString()));
                }
                return 1;
            }
            case "fishing" -> {
                ModeManager.INSTANCE.setCurrentMode(new FishingMode());
                AutoHarvestConfig.getInstance().thecurrentMode = fishing;
                AutoHarvestConfig.save();
                if (player != null) {
                    player.sendSystemMessage(Component.literal(Component.translatable("autoharvest.mode.switch").getString() + Component.translatable("autoharvest.mode.fishing").getString()));
                }
                return 1;
            }
        }

        if (player != null) {
            player.sendSystemMessage(Component.translatable("autoharvest.mode.error")
            );
        }
        return 0;
    }
}