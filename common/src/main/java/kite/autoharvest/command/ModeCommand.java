package kite.autoharvest.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import kite.autoharvest.config.AutoHarvestConfig;
import kite.autoharvest.config.ModeEnum;
import kite.autoharvest.manager.ModeManager;
import kite.autoharvest.mode.AutoMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModeCommand {

    private static final List<String> MODE_NAMES = Arrays.asList(
            "weed", "plant", "harvest", "farmer", "bonemeal", "feed", "fishing", "hoe"
    );

    public static CompletableFuture<Suggestions> suggestModes(SuggestionsBuilder builder) {
        for (String mode : MODE_NAMES) {
            builder.suggest(mode);
        }
        return builder.buildFuture();
    }

    public static int executeToggle(CommandContext<?> context) {
        ModeManager.INSTANCE.toggle();
        return 1;
    }

    public static int executeSetMode(CommandContext<?> context) {
        LocalPlayer player = Minecraft.getInstance().player;
        String modeName = StringArgumentType.getString(context, "mode").toLowerCase();

        ModeEnum mode;
        try {
            mode = ModeEnum.valueOf(modeName);
        } catch (IllegalArgumentException e) {
            if (player != null) {
                player.sendSystemMessage(Component.translatable("autoharvest.mode.error"));
            }
            return 0;
        }

        AutoMode autoMode = mode.setMode();
        ModeManager.INSTANCE.setCurrentMode(autoMode);
        AutoHarvestConfig.getInstance().thecurrentMode = mode;
        AutoHarvestConfig.save();
        if (player != null) {
            player.sendSystemMessage(Component.literal(
                    Component.translatable("autoharvest.mode.switch").getString() + autoMode.getName()));
        }
        return 1;
    }
}
