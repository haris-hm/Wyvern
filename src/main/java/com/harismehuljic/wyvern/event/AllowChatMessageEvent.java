package com.harismehuljic.wyvern.event;

import com.harismehuljic.wyvern.Wyvern;
import com.harismehuljic.wyvern.util.MessageFormatter;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.text.object.AtlasTextObjectContents;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AllowChatMessageEvent implements ServerMessageEvents.AllowChatMessage {
    /**
     * Called when the server broadcasts a chat message sent by a player, typically
     * from a client GUI or a player-executed command. Returning {@code false}
     * prevents the message from being broadcast and the {@link #CHAT_MESSAGE} event
     * from triggering.
     *
     * <p>If the message is from a player-executed command, this will be called
     * only if {@link #ALLOW_COMMAND_MESSAGE} event did not block the message,
     * and after triggering {@link #COMMAND_MESSAGE} event.
     *
     * @param message the broadcast message with message decorators applied; use {@code message.getContent()} to get the text
     * @param sender  the player that sent the message
     * @param params  the {@link MessageType.Parameters}
     * @return {@code true} if the message should be broadcast, otherwise {@code false}
     */
    @Override
    public boolean allowChatMessage(SignedMessage message, ServerPlayerEntity sender, MessageType.@NotNull Parameters params) {
        String messageContents = message.getSignedContent();

        // Send the newly formatted message to each player
        for (ServerPlayerEntity spe : Objects.requireNonNull(sender.getEntityWorld().getServer()).getPlayerManager().getPlayerList()) {
            assert sender.getDisplayName() != null;

            MutableText senderDisplayName = sender.getDisplayName().copy();
            MutableText separator = Text.literal(" 》 ").formatted(Formatting.WHITE);
            MutableText msg = Text.literal(messageContents).formatted(Formatting.WHITE);
            MutableText emojiTest = MutableText.of(new ObjectTextContent(new AtlasTextObjectContents(Identifier.of("minecraft", "gui"), Identifier.of("emojis/miku_true"))));

            separator.setStyle(separator.getStyle().withBold(false).withItalic(false));
            msg.setStyle(separator.getStyle().withBold(false).withItalic(false));

            spe.sendMessage(senderDisplayName.append(separator).append(msg).append(emojiTest));
        }

        String discordMsg = MessageFormatter.formatDiscordMessage(sender, message.getSignedContent());

        if (Wyvern.CONFIG_DATA.allowChatMessages()) {
            Wyvern.DISCORD_BOT.sendMessageInGuild(discordMsg);
        }

        // Block the sending of the original Minecraft formatted message
        return false;
    }
}
