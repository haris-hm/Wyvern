package com.harismehuljic.wyvern.discord.commands;

import com.harismehuljic.wyvern.Wyvern;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

public class TpsCommand implements ApplicationCommand {
    @Override
    public String getName() {
        return "tps";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        return event.reply()
                .withEphemeral(false)
                .withContent("Current server TPS: " + Wyvern.SERVER.getTickManager().getTickRate());
    }
}
