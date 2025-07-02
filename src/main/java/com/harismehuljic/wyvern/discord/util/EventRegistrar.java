package com.harismehuljic.wyvern.discord.util;

import com.harismehuljic.wyvern.Wyvern;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class EventRegistrar {
    private GatewayDiscordClient discordClient;

    public EventRegistrar(GatewayDiscordClient discordClient) {
        this.discordClient = discordClient;
    }

    public void registerEvents() {
        this.discordClient.on(MessageCreateEvent.class).subscribe(event -> {
            Message message = event.getMessage();
            String content = message.getContent();
            MessageChannel channel = message.getChannel().block();
            String author = message.getAuthor().map(User::getUsername).orElse("Unknown User");

            // Early return if the message is from a bot, the server hasn't initialized yet, or the channel isn't the bot's channel
            if (message.getAuthor().get().isBot() || Wyvern.SERVER == null || channel.getId().asLong() != Wyvern.CONFIG_DATA.getDiscordChannelId()) { return; }

            MutableText discordMsg = Text.literal("[Discord] ")
                    .formatted(Formatting.BLUE)
                    .append(Text.literal(author).formatted(Formatting.AQUA))
                    .append(Text.literal(" 》 " + content).formatted(Formatting.WHITE));

            for (ServerPlayerEntity spe : Wyvern.SERVER.getPlayerManager().getPlayerList()) {
                spe.sendMessage(discordMsg);
            }
        });

        this.discordClient.on(ChatInputAutoCompleteEvent.class).subscribe(event -> {
            if (event.getCommandName().equals("title")) {
                // Get the string value of what the user is currently typing
                String typing = event.getFocusedOption().getValue()
                        .map(ApplicationCommandInteractionOptionValue::asString)
                        .orElse(""); // In case the user has not started typing, we return an empty string

                List<ApplicationCommandOptionChoiceData> suggestions = new ArrayList<>();

                for (String color : Formatting.getNames(true, false)) {
                    suggestions.add(ApplicationCommandOptionChoiceData.builder().name(color).value(color).build());
                }

                // Finally, return the list of choices to the user
                event.respondWithSuggestions(suggestions).subscribe();
            }
        });
    }


}
