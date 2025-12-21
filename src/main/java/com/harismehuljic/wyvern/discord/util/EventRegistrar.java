package com.harismehuljic.wyvern.discord.util;

import com.harismehuljic.wyvern.Wyvern;
import com.harismehuljic.wyvern.discord.DiscordBot;
import com.harismehuljic.wyvern.discord.util.markdown.MarkdownParser;
import com.harismehuljic.wyvern.discord.util.markdown.MarkdownSegment;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.Member;
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
import java.util.Objects;

public class EventRegistrar {
    private final GatewayDiscordClient discordClient;
    private final DiscordBot discordBot;

    public EventRegistrar(GatewayDiscordClient discordClient, DiscordBot discordBot) {
        this.discordClient = discordClient;
        this.discordBot = discordBot;
    }

    public void registerEvents() {
        this.discordClient.on(MessageCreateEvent.class).subscribe(this::processMessage);
        this.discordClient.on(ChatInputAutoCompleteEvent.class).subscribe(this::processCommandCompletion);
        this.discordClient.on(MemberJoinEvent.class).subscribe(event -> processMemberJoin(event, this.discordBot));
        this.discordClient.on(MemberLeaveEvent.class).subscribe(event -> processMemberLeave(event, this.discordBot));
    }

    private void processMessage(MessageCreateEvent event) {
        Message message = event.getMessage();
        String content = message.getContent();
        MessageChannel channel = message.getChannel().block();
        String author = message.getAuthor().map(User::getUsername).orElse("Unknown User");

        // Early return if the message is from a bot, the server hasn't initialized yet, or the channel isn't the bot's channel
        if (message.getAuthor().get().isBot() || Wyvern.SERVER == null ||
                Objects.requireNonNull(channel).getId().asLong() != Wyvern.CONFIG_DATA.getDiscordChannelId()) {
            return;
        }

        MutableText discordMsg = Text.literal("[Discord] ")
                .formatted(Formatting.BLUE)
                .append(Text.literal(author).formatted(Formatting.AQUA))
                .append(Text.literal(" 》 ").formatted(Formatting.WHITE));

        for (MarkdownSegment msgSegment : MarkdownParser.parse(content)) {
            String textContent = msgSegment.text();
            Formatting[] segmentFormatting = {Formatting.WHITE, Formatting.WHITE};

            switch (msgSegment.type()) {
                case BOLD -> segmentFormatting[1] = Formatting.BOLD;
                case ITALICIZED -> segmentFormatting[1] = Formatting.ITALIC;
                case UNDERLINED -> segmentFormatting[1] = Formatting.UNDERLINE;
            }

            discordMsg.append(Text.literal(textContent).formatted(segmentFormatting));
        }

        for (ServerPlayerEntity spe : Wyvern.SERVER.getPlayerManager().getPlayerList()) {
            spe.sendMessage(discordMsg);
        }
    }

    private void processCommandCompletion(ChatInputAutoCompleteEvent event) {
        if (event.getCommandName().equals("title")) {
            String typing = event.getFocusedOption().getValue()
                    .map(ApplicationCommandInteractionOptionValue::asString)
                    .orElse("");

            List<ApplicationCommandOptionChoiceData> suggestions = new ArrayList<>();

            for (String color : Formatting.getNames(true, false)) {
                suggestions.add(ApplicationCommandOptionChoiceData.builder().name(color).value(color).build());
            }

            // Finally, return the list of choices to the user
            event.respondWithSuggestions(suggestions).subscribe();
        }
    }

    private void processMemberJoin(MemberJoinEvent event, DiscordBot bot) {
        Member newMember = event.getMember();
        if (newMember.isBot()) return;
        bot.addToMemberCache(newMember.getUsername());
    }

    private void processMemberLeave(MemberLeaveEvent event, DiscordBot bot) {
        if (event.getMember().isEmpty()) return;
        Member newMember = event.getMember().get();
        if (newMember.isBot()) return;
        bot.removeFromMemberCache(newMember.getUsername());
    }
}
