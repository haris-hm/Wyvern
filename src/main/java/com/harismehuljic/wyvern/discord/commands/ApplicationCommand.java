package com.harismehuljic.wyvern.discord.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

public interface ApplicationCommand {
    String getName();

    Mono<Void> handle(ChatInputInteractionEvent event);
}
