package com.harismehuljic.wyvern.discord.listeners;

import com.harismehuljic.wyvern.discord.commands.*;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class ApplicationCommandListener {
    //An array list of classes that implement the SlashCommand interface
    private final static List<ApplicationCommand> applicationCommands = new ArrayList<>();

    static {
        //We register our commands here when the class is initialized
        applicationCommands.add(new PingCommand());
        applicationCommands.add(new PlayersCommand());
        applicationCommands.add(new TitleCommand());
        applicationCommands.add(new TpsCommand());
    }

    public static Mono<Void> handle(ChatInputInteractionEvent event) {
        // Convert our array list to a flux that we can iterate through
        return Flux.fromIterable(applicationCommands)
                //Filter out all commands that don't match the name of the command this event is for
                .filter(command -> command.getName().equals(event.getCommandName()))
                // Get the first (and only) item in the flux that matches our filter
                .next()
                //have our command class handle all the logic related to its specific command.
                .flatMap(command -> command.handle(event));
    }
}
