package com.harismehuljic.wyvern.discord.commands;

import com.harismehuljic.wyvern.Wyvern;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TitleCommand implements ApplicationCommand {
    private final int ticksPerSecond = 20;
    private final Collection<String> minecraftColors = Formatting.getNames(true, false);

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

        if (Wyvern.SERVER.getCurrentPlayerCount() == 0) {
            return event.reply()
                    .withEphemeral(false)
                    .withContent("There are no players on the server.");
        }

        MutableText titleDisplayText = Text.literal(titleText).formatted(Formatting.byName(titleColor));

        if (titleBolded) titleDisplayText.formatted(Formatting.BOLD);
        if (titleItalicized) titleDisplayText.formatted(Formatting.ITALIC);

        Wyvern.SERVER.getPlayerManager().sendToAll(new TitleFadeS2CPacket(secondsToTicks(1), titleStayTicks, secondsToTicks(1)));

        // Send a new title packet to all members
        Wyvern.SERVER.getPlayerManager().sendToAll(new TitleS2CPacket(titleDisplayText));

        if (!subtitleText.isEmpty()) {
            MutableText subtitleDisplayText = Text.literal(subtitleText).formatted(Formatting.byName(subtitleColor));
            if (subtitleBolded) subtitleDisplayText.formatted(Formatting.BOLD);
            if (subtitleItalicized) subtitleDisplayText.formatted(Formatting.ITALIC);

            Wyvern.SERVER.getPlayerManager().sendToAll(new SubtitleS2CPacket(subtitleDisplayText));
        }

        String inGameMessage = String.format("@%s just displayed a title!", event.getUser().getUsername());

        for (ServerPlayerEntity spe : Wyvern.SERVER.getPlayerManager().getPlayerList()) {
            spe.sendMessage(Text.literal(inGameMessage).formatted(Formatting.AQUA));
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

        for (String color : Formatting.getNames(true, false)) {
            if (color.toLowerCase().startsWith(typing)) {
                suggestions.add(ApplicationCommandOptionChoiceData.builder().name(color).value(color).build());
            }
        }

        event.respondWithSuggestions(suggestions).subscribe();
    }
}
