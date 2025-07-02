package com.harismehuljic.wyvern.discord.commands;

import com.harismehuljic.wyvern.Wyvern;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import reactor.core.publisher.Mono;

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

        List<ApplicationCommandInteractionOption> options = event.getOptions();

        String titleText = event.getOption("title_text")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .get();

        for (ApplicationCommandInteractionOption option : options) {
            if (option.getName().equals("subtitle_text")) {
                subtitleText = option.getValue().map(ApplicationCommandInteractionOptionValue::asString).get();
            }

            if (option.getName().equals("title_color")) {
                String titleColorProvided = option.getValue().map(ApplicationCommandInteractionOptionValue::asString).get();
                titleColor = minecraftColors.contains(titleColorProvided) ? titleColorProvided : titleColor;
            }

            if (option.getName().equals("subtitle_color")) {
                String subtitleColorProvided = option.getValue().map(ApplicationCommandInteractionOptionValue::asString).get();
                subtitleColor = minecraftColors.contains(subtitleColorProvided) ? subtitleColorProvided : titleColor;
            }
        }

        if (Wyvern.SERVER.getCurrentPlayerCount() == 0) {
            return event.reply()
                    .withEphemeral(false)
                    .withContent("There are no players on the server.");
        }

        Wyvern.SERVER.getPlayerManager().sendToAll(new TitleFadeS2CPacket(secondsToTicks(1), titleStayTicks, secondsToTicks(1)));

        // Send a new title packet to all members
        Wyvern.SERVER.getPlayerManager().sendToAll(new TitleS2CPacket(Text.literal(titleText).formatted(Formatting.byName(titleColor))));

        if (!subtitleText.isEmpty()) {
            Wyvern.SERVER.getPlayerManager().sendToAll(new SubtitleS2CPacket(Text.literal(subtitleText).formatted(Formatting.byName(subtitleColor))));
        }

        return event.reply()
                .withEphemeral(false)
                .withContent(String.format("Title displayed successfully: ```\n%s\n%s\n```", titleText, subtitleText));
    }

    private int secondsToTicks(int seconds) {
        return seconds * ticksPerSecond;
    }
}
