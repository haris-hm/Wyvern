package com.harismehuljic.wyvern.command;

import com.harismehuljic.wyvern.Wyvern;
import com.harismehuljic.wyvern.discord.DiscordBot;
import com.harismehuljic.wyvern.util.MessageFormatter;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.argument;

public class PingCommand {
    private static final SuggestionProvider<CommandSourceStack> USERNAME_PROVIDER = (source, builder) -> SharedSuggestionProvider.suggest(Wyvern.DISCORD_BOT.getGuildMembers(), builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess,
                                Commands.CommandSelection environment) {
        final LiteralCommandNode<CommandSourceStack> pingNode = dispatcher.register(
                Commands.literal("ping")
                        .then(argument("username", StringArgumentType.string())
                                .suggests(USERNAME_PROVIDER)
                                .executes(PingCommand::sendPing)
                                .then(argument("message", StringArgumentType.greedyString())
                                        .executes(PingCommand::sendPingWithMessage)
                                )
                        )
        );
    }

    private static int sendPing(CommandContext<CommandSourceStack> context) {
        String recipient = StringArgumentType.getString(context, "username");
        sendMessage(context, recipient, "");
        return 0;
    }

    private static int sendPingWithMessage(CommandContext<CommandSourceStack> context) {

        String recipient = StringArgumentType.getString(context, "username");
        String message = StringArgumentType.getString(context, "message");
        sendMessage(context, recipient, message);
        return 0;
    }

    private static void sendMessage(CommandContext<CommandSourceStack> context, String recipientUsername, String message) {
        DiscordBot discordBot = Wyvern.DISCORD_BOT;
        MinecraftServer server = context.getSource().getServer();
        ServerPlayer sender = context.getSource().getPlayer();

        long memberId = discordBot.getMemberId(recipientUsername);
        String discordMsg = message.trim().isEmpty() ? String.format("<@%d>", memberId) : String.format("<@%d>, %s", memberId, message);

        assert sender != null;
        discordBot.sendCustomMessage(
                MessageFormatter.formatName(sender),
                MessageFormatter.getHelmetUrl(sender),
                discordMsg
        );

        MutableComponent gameMessage = (MutableComponent) sender.getDisplayName();

        assert gameMessage != null;
        gameMessage.append(Component.literal(String.format(" just pinged @%s", recipientUsername)));

        if (!message.trim().isEmpty()) {
            gameMessage.append(Component.literal(String.format(" saying \"%s\"", message)));
        }

        for (ServerPlayer spe : server.getPlayerList().getPlayers()) {
            spe.sendSystemMessage(gameMessage.withStyle(ChatFormatting.ITALIC, ChatFormatting.AQUA));
        }
    }
}
