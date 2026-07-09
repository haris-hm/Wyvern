package com.harismehuljic.wyvern.discord.util;

import com.harismehuljic.wyvern.Wyvern;
import com.harismehuljic.wyvern.discord.DiscordBot;
import com.harismehuljic.wyvern.discord.commands.TitleCommand;
import com.harismehuljic.wyvern.discord.commands.WhitelistCommand;
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
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

import java.net.URI;
import java.net.URISyntaxException;
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

        Optional<URI> messageURI = getMessageUrl(message);
        MutableComponent discordMsg = Component.literal("[Discord]");
        messageURI.ifPresent(uri -> {
            discordMsg.setStyle(
                    Style.EMPTY
                            .withClickEvent(new ClickEvent.OpenUrl(uri))
                            .withHoverEvent(new HoverEvent.ShowText(Component.nullToEmpty("Click to view in Discord")))
                            .applyFormat(ChatFormatting.BLUE)
            );
        });

        discordMsg.append(Component.literal(" " + author).withStyle(ChatFormatting.AQUA));

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

            discordMsg.append(Component.literal(replyText).withStyle(ChatFormatting.AQUA));
        });

        discordMsg.append(Component.literal(" 》 ").withStyle(ChatFormatting.WHITE));

        for (MarkdownSegment msgSegment : MarkdownParser.parse(content)) {
            String textContent = msgSegment.text();
            ChatFormatting[] segmentFormatting = {ChatFormatting.WHITE, ChatFormatting.WHITE};

            switch (msgSegment.type()) {
                case BOLD -> segmentFormatting[1] = ChatFormatting.BOLD;
                case ITALICIZED -> segmentFormatting[1] = ChatFormatting.ITALIC;
                case UNDERLINED -> segmentFormatting[1] = ChatFormatting.UNDERLINE;
            }

            discordMsg.append(Component.literal(textContent).withStyle(segmentFormatting));
        }

        if (!message.getAttachments().isEmpty()) {
            if (!discordMsg.getString().endsWith(" ")) discordMsg.append(Component.literal(" "));
            discordMsg.append(Component.literal("(This message contains images and/or files attached. Click this message to view in Discord.)").withStyle(ChatFormatting.ITALIC, ChatFormatting.WHITE));
        }

        for (ServerPlayer spe : Wyvern.SERVER.getPlayerList().getPlayers()) {
            spe.sendSystemMessage(discordMsg);
        }
    }

    private void processCommandCompletion(ChatInputAutoCompleteEvent event) {
        switch (event.getCommandName()) {
            case "title" -> TitleCommand.handleCommandCompletion(event);
            case "whitelist" -> WhitelistCommand.handleCommandCompletion(event);
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
