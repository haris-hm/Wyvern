package com.harismehuljic.wyvern.discord.commands;

import com.harismehuljic.wyvern.Wyvern;
import com.harismehuljic.wyvern.discord.util.Formatting;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;

public class TitleCommand implements ApplicationCommand {
    private final int ticksPerSecond = 20;
    private final Collection<String> minecraftColors = Formatting.getIds();

    @Override
    public String getName() {
        return "title";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        String subtitleText = "";

        String titleColor = "white";
        String subtitleColor = "white";

        int titleStayTicks = secondsToTicks(3);

        boolean titleBolded = false;
        boolean titleItalicized = false;
        boolean subtitleBolded = false;
        boolean subtitleItalicized = false;

        List<ApplicationCommandInteractionOption> options = event.getOptions();

        String titleText = event.getOption("title_text")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .orElse("");

        for (ApplicationCommandInteractionOption option : options) {
            switch (option.getName()) {
                case "subtitle_text":
                    subtitleText = option.getValue().map(ApplicationCommandInteractionOptionValue::asString).orElse("");
                    break;
                case "title_color":
                    String titleColorProvided = option.getValue().map(ApplicationCommandInteractionOptionValue::asString).orElse("white");
                    titleColor = minecraftColors.contains(titleColorProvided) ? titleColorProvided : titleColor;
                    break;
                case "subtitle_color":
                    String subtitleColorProvided = option.getValue().map(ApplicationCommandInteractionOptionValue::asString).orElse("white");
                    subtitleColor = minecraftColors.contains(subtitleColorProvided) ? subtitleColorProvided : titleColor;
                    break;
                case "title_bolded":
                    titleBolded = option.getValue().map(ApplicationCommandInteractionOptionValue::asBoolean).orElse(false);
                    break;
                case "title_italicized":
                    titleItalicized = option.getValue().map(ApplicationCommandInteractionOptionValue::asBoolean).orElse(false);
                    break;
                case "subtitle_bolded":
                    subtitleBolded = option.getValue().map(ApplicationCommandInteractionOptionValue::asBoolean).orElse(false);
                    break;
                case "subtitle_italicized":
                    subtitleItalicized = option.getValue().map(ApplicationCommandInteractionOptionValue::asBoolean).orElse(false);
                    break;
                case "length":
                    int lengthProvided = Math.round(option.getValue().map(ApplicationCommandInteractionOptionValue::asLong).orElse(3L));
                    if (lengthProvided > 15) lengthProvided = 15;
                    titleStayTicks = secondsToTicks(lengthProvided);
                    break;
            }
        }

        if (titleText.isEmpty()) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Error: No title text provided.");
        }

        if (Wyvern.SERVER.getPlayerCount() == 0) {
            return event.reply()
                    .withEphemeral(false)
                    .withContent("There are no players on the server.");
        }

        MutableComponent titleDisplayText = Component.literal(titleText).withStyle(Formatting.getByName(titleColor).getFormat());

        if (titleBolded) titleDisplayText.withStyle(ChatFormatting.BOLD);
        if (titleItalicized) titleDisplayText.withStyle(ChatFormatting.ITALIC);

        Wyvern.SERVER.getPlayerList().broadcastAll(new ClientboundSetTitlesAnimationPacket(secondsToTicks(1), titleStayTicks, secondsToTicks(1)));

        // Send a new title packet to all members
        Wyvern.SERVER.getPlayerList().broadcastAll(new ClientboundSetTitleTextPacket(titleDisplayText));

        if (!subtitleText.isEmpty()) {
            MutableComponent subtitleDisplayText = Component.literal(subtitleText).withStyle(Formatting.getByName(subtitleColor).getFormat());
            if (subtitleBolded) subtitleDisplayText.withStyle(ChatFormatting.BOLD);
            if (subtitleItalicized) subtitleDisplayText.withStyle(ChatFormatting.ITALIC);

            Wyvern.SERVER.getPlayerList().broadcastAll(new ClientboundSetSubtitleTextPacket(subtitleDisplayText));
        }

        String inGameMessage = String.format("@%s just displayed a title!", event.getUser().getUsername());

        for (ServerPlayer spe : Wyvern.SERVER.getPlayerList().getPlayers()) {
            spe.sendSystemMessage(Component.literal(inGameMessage).withStyle(ChatFormatting.AQUA));
        }

        return event.reply()
                .withEphemeral(false)
                .withContent(String.format("Title displayed successfully for %d seconds: ```\n%s\n%s\n```", ticksToSeconds(titleStayTicks), titleText, subtitleText));
    }

    private int secondsToTicks(int seconds) {
        return seconds * ticksPerSecond;
    }

    private int ticksToSeconds(int ticks) {
        return ticks / ticksPerSecond;
    }

    public static void handleCommandCompletion(ChatInputAutoCompleteEvent event) {
        String typing = event.getFocusedOption().getValue()
                .map(ApplicationCommandInteractionOptionValue::asString)
                .orElse("");

        List<ApplicationCommandOptionChoiceData> suggestions = new ArrayList<>();

        for (String color : Formatting.getIds()) {
            if (color.toLowerCase().startsWith(typing)) {
                suggestions.add(ApplicationCommandOptionChoiceData.builder().name(color).value(color).build());
            }
        }

        event.respondWithSuggestions(suggestions).subscribe();
    }
}
