package com.harismehuljic.wyvern.discord.commands;

import com.harismehuljic.wyvern.Wyvern;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.MinecraftClientHttpException;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.rest.util.Color;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.server.players.UserWhiteListEntry;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class WhitelistCommand implements ApplicationCommand {
    @Override
    public String getName() {
        return "whitelist";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        List<ApplicationCommandInteractionOption> options = event.getOptions();
        MinecraftServer server = Wyvern.SERVER;

        if (server == null) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Sorry, the server is not online yet.");
        }

        if (options.isEmpty()) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Sorry, something went wrong.");
        }

        String subcommand = options.getFirst().getName();

        return switch (subcommand) {
            case "list" -> handleList(event, server);
            case "add" -> handleAdd(event, server);
            case "remove" -> handleRemove(event, server);
            default -> event.reply()
                    .withEphemeral(true)
                    .withContent("Sorry, something went wrong.");
        };
    }

    @Nullable NameAndId fetchPlayer(String username, MinecraftServer server) {
        Services apiServices = server.services();
        GameProfile profile = apiServices.profileResolver().fetchByName(username).orElse(null);

        if (profile == null) {
            return null;
        }

        return new NameAndId(profile);
    }

    private Mono<Void> handleList(ChatInputInteractionEvent event, MinecraftServer server) {
        String joinedNames = String.join("\n", server.getPlayerList().getWhiteListNames());

        int playerCount = server.getPlayerList().getWhiteListNames().length;
        String description = String.format("There %s currently **%s** %s whitelisted.",
                playerCount == 1 ? "is" : "are",
                playerCount,
                playerCount == 1 ? "player" : "players"
        );

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.CYAN)
                .title(String.format("%s", Wyvern.CONFIG_DATA.getServerName()))
                .description(description)
                .addField("Whitelisted Players:", joinedNames, false)
                .timestamp(Instant.now())
                .build();

        return event.reply()
                .withEphemeral(true)
                .withEmbeds(embed);
    }

    private Mono<Void> handleAdd(ChatInputInteractionEvent event, MinecraftServer server) {
        String username = event.getOption("add")
                .flatMap(addOption -> addOption.getOption("username"))
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .orElse(null);

        if (username == null || !username.matches("^[a-zA-Z0-9_]{2,16}$")) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Please provide a valid username.");
        }

        UserWhiteList whitelist = server.getPlayerList().getWhiteList();
        NameAndId player = this.fetchPlayer(username, server);

        if (player == null) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent(String.format("It seems the player \"%s\" doesn't exist.", username));
        }

        whitelist.add(new UserWhiteListEntry(player));

        return event.reply()
                .withEphemeral(true)
                .withContent(String.format("Player \"%s\" has been successfully added to the whitelist!", username));
    }

    private Mono<Void> handleRemove(ChatInputInteractionEvent event, MinecraftServer server) {
        String username = event.getOption("remove")
                .flatMap(addOption -> addOption.getOption("username"))
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .orElse(null);

        if (username == null || !username.matches("^[a-zA-Z0-9_]{2,16}$")) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Please provide a valid username.");
        }

        UserWhiteList whitelist = server.getPlayerList().getWhiteList();
        NameAndId player = this.fetchPlayer(username, server);

        if (player == null) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent(String.format("It seems the player \"%s\" doesn't exist.", username));
        } else if (!whitelist.isWhiteListed(player)) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent(String.format("It seems the player \"%s\" isn't whitelisted on this server.", username));
        }

        whitelist.remove(player);
        Objects.requireNonNull(server.getPlayerList().getPlayerByName(player.name()))
                .connection
                .disconnect(Component.nullToEmpty("You are not white-listed on this server!"));

        return event.reply()
                .withEphemeral(true)
                .withContent(String.format("Player \"%s\" has been successfully removed from the whitelist!", username));
    }

    public static void handleCommandCompletion(ChatInputAutoCompleteEvent event) {
        if (event.getOption("remove").isEmpty()) {
            event.respondWithSuggestions(List.of()).subscribe();
            return;
        }

        String typing = event.getFocusedOption().getValue()
                .map(ApplicationCommandInteractionOptionValue::asString)
                .orElse("").toLowerCase();

        List<ApplicationCommandOptionChoiceData> suggestions = new ArrayList<>();
        MinecraftServer server = Wyvern.SERVER;

        if (server != null) {
            for (String name : server.getPlayerList().getWhiteListNames()) {
                if (name.toLowerCase().startsWith(typing)) {
                    suggestions.add(ApplicationCommandOptionChoiceData.builder().name(name).value(name).build());

                    // Discord has a maximum of 25 suggestions
                    if (suggestions.size() == 25) {
                        break;
                    }
                }
            }
        }

        event.respondWithSuggestions(suggestions).subscribe();
    }
}
