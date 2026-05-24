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
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

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

        // Early return if the message is from a bot/webhook, the server hasn't initialized yet, or the channel isn't the bot's channel
        if (message.getWebhookId().isPresent() || (message.getAuthor().isPresent() && message.getAuthor().get().isBot())) {
            return;
        }

        if (Wyvern.SERVER == null ||
                Objects.requireNonNull(channel).getId().asLong() != Wyvern.CONFIG_DATA.getDiscordChannelId()) {
            return;
        }

        String author = message.getAuthor().map(User::getUsername).orElse("Unknown User");

        Wyvern.LOGGER.info("Received message {}, bot?: {}", content, message.getAuthor().get().isBot());


        Optional<URI> messageURI = getMessageUrl(message);
        MutableText discordMsg = Text.literal("[Discord]");
        messageURI.ifPresent(uri -> {
            discordMsg.setStyle(
                    Style.EMPTY
                            .withClickEvent(new ClickEvent.OpenUrl(uri))
                            .withHoverEvent(new HoverEvent.ShowText(Text.of("Click to view in Discord")))
                            .withFormatting(Formatting.BLUE)
            );
        });

        discordMsg.append(Text.literal(" " + author).formatted(Formatting.AQUA));

        message.getReferencedMessage().ifPresent(referencedMessage -> {
            AtomicReference<String> referencedContent = new AtomicReference<>(referencedMessage.getContent().trim());
            String referencedAuthor = referencedMessage.getAuthor().map(User::getUsername).orElse("Unknown User");

            if (referencedMessage.getAuthor().isPresent() &&
                    referencedMessage.getAuthor().get().getId().equals(this.discordClient.getSelfId())) {
                if (!referencedMessage.getEmbeds().isEmpty()) {
                    referencedMessage.getEmbeds().getFirst().getDescription().ifPresent(referencedContent::set);
                }
            }

            if (referencedMessage.getWebhookId().isPresent()) {
                referencedAuthor = referencedMessage.getUserData().username();
            }

            if (referencedContent.get().isEmpty() && !referencedMessage.getAttachments().isEmpty()) {
                referencedContent.set("Image/File posted");
            }

            if (referencedContent.get().length() > 40) {
                referencedContent.set(referencedContent.get().substring(0, 40) + "...");
            }

            String replyText = String.format(" in reply to %s saying \"%s\"", referencedAuthor, referencedContent);

            discordMsg.append(Text.literal(replyText).formatted(Formatting.AQUA));
        });

        discordMsg.append(Text.literal(" 》 ").formatted(Formatting.WHITE));

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

        if (!message.getAttachments().isEmpty()) {
            if (!discordMsg.getString().endsWith(" ")) discordMsg.append(Text.literal(" "));
            discordMsg.append(Text.literal("(This message contains images and/or files attached. Click this message to view in Discord.)").formatted(Formatting.ITALIC, Formatting.WHITE));
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
        bot.addToMemberCache(newMember.getUsername(), newMember.getId().asLong());
    }

    private void processMemberLeave(MemberLeaveEvent event, DiscordBot bot) {
        if (event.getMember().isEmpty()) return;
        Member newMember = event.getMember().get();
        if (newMember.isBot()) return;
        bot.removeFromMemberCache(newMember.getUsername());
    }

    private Optional<URI> getMessageUrl(Message message) {
        long guildId = Wyvern.CONFIG_DATA.getDiscordGuildId();
        long channelId = message.getChannelId().asLong();
        long messageId = message.getId().asLong();

        try {
            return Optional.of(new URI(
                    String.format(
                            "https://discord.com/channels/%d/%d/%d",
                            guildId,
                            channelId,
                            messageId
                    )
            ));
        } catch (URISyntaxException e) {
            Wyvern.LOGGER.warn("Invalid Discord message URI generated: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
