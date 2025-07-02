package com.harismehuljic.wyvern.discord.commands;

import com.harismehuljic.wyvern.Wyvern;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import net.minecraft.server.network.ServerPlayerEntity;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Objects;

public class PlayersCommand implements ApplicationCommand {

    @Override
    public String getName() {
        return "players";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        if (Wyvern.SERVER == null) {
            return event.reply()
                .withEphemeral(true)
                .withContent("Sorry, the server is not online yet.");
        }

        StringBuilder playerNameList = new StringBuilder();

        int playerCount = Wyvern.SERVER.getCurrentPlayerCount();
        String description = String.format("There %s currently **%s** %s online.",
                playerCount == 1 ? "is" : "are",
                playerCount,
                playerCount == 1 ? "player" : "players"
        );

        for (ServerPlayerEntity spe : Wyvern.SERVER.getPlayerManager().getPlayerList()) {
            String playerName = Objects.requireNonNull(spe.getDisplayName()).getString();
            playerNameList.append(String.format("%s\n", playerName));
        }

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
            .color(Color.CYAN)
            .title("Miku SMP")
            .description(description)
            .addField("Players online:", playerNameList.toString(), false)
            .timestamp(Instant.now())
            .build();

        return event.reply()
            .withEphemeral(false)
            .withEmbeds(embed);
    }
}
