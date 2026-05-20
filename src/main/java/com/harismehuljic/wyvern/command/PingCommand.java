package com.harismehuljic.wyvern.command;

import com.harismehuljic.wyvern.Wyvern;
import com.harismehuljic.wyvern.discord.DiscordBot;
import com.harismehuljic.wyvern.util.MessageFormatter;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.argument;

public class PingCommand {
    private static final SuggestionProvider<ServerCommandSource> USERNAME_PROVIDER = (source, builder) -> CommandSource.suggestMatching(Wyvern.DISCORD_BOT.getGuildMembers(), builder);

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess,
                                CommandManager.RegistrationEnvironment environment) {
        final LiteralCommandNode<ServerCommandSource> pingNode = dispatcher.register(
                CommandManager.literal("ping")
                        .then(argument("username", StringArgumentType.string())
                                .suggests(USERNAME_PROVIDER)
                                .executes(PingCommand::sendPing)
                                .then(argument("message", StringArgumentType.greedyString())
                                        .executes(PingCommand::sendPingWithMessage)
                                )
                        )
        );
    }

    private static int sendPing(CommandContext<ServerCommandSource> context) {
        String recipient = StringArgumentType.getString(context, "username");
        sendMessage(context, recipient, "");
        return 0;
    }

    private static int sendPingWithMessage(CommandContext<ServerCommandSource> context) {

        String recipient = StringArgumentType.getString(context, "username");
        String message = StringArgumentType.getString(context, "message");
        sendMessage(context, recipient, message);
        return 0;
    }

    private static void sendMessage(CommandContext<ServerCommandSource> context, String recipientUsername, String message) {
        DiscordBot discordBot = Wyvern.DISCORD_BOT;
        MinecraftServer server = context.getSource().getServer();
        ServerPlayerEntity sender = context.getSource().getPlayer();

        long memberId = discordBot.getMemberId(recipientUsername);
        String discordMsg = message.trim().isEmpty() ? String.format("<@%d>", memberId) : String.format("<@%d>, %s", memberId, message);

        assert sender != null;
        discordBot.sendCustomMessage(
                MessageFormatter.formatName(sender),
                MessageFormatter.getHelmetUrl(sender),
                discordMsg
        );

        MutableText gameMessage = (MutableText) sender.getDisplayName();

        assert gameMessage != null;
        gameMessage.append(Text.literal(String.format(" just pinged @%s", recipientUsername)));

        if (!message.trim().isEmpty()) {
            gameMessage.append(Text.literal(String.format(" saying \"%s\"", message)));
        }

        for (ServerPlayerEntity spe : server.getPlayerManager().getPlayerList()) {
            spe.sendMessage(gameMessage.formatted(Formatting.ITALIC, Formatting.AQUA));
        }
    }
}
