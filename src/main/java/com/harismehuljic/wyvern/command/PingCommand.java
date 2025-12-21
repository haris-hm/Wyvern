package com.harismehuljic.wyvern.command;

import com.harismehuljic.wyvern.Wyvern;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

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
        return 0;
    }

    private static int sendPingWithMessage(CommandContext<ServerCommandSource> context) {
        return 0;
    }
}
