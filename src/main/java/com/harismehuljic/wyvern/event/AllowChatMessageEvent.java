package com.harismehuljic.wyvern.event;

import com.harismehuljic.wyvern.Wyvern;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;

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
    public boolean allowChatMessage(SignedMessage message, ServerPlayerEntity sender, MessageType.Parameters params) {
        String messageContents = message.getSignedContent();
        String discordMsg;

        String playerName = String.format("**%s**", Objects.requireNonNull(sender.getDisplayName()).getString());
        String realName = Objects.requireNonNull(sender.getGameProfile().getName());

        if (!realName.isEmpty() && realName.equals(playerName)) {
            discordMsg = String.format("**%s**  》%s", playerName, messageContents);
        }
        else {
            discordMsg = String.format("**%s** (*%s*)  》%s", playerName, realName, messageContents);
        }

        if (Wyvern.CONFIG_DATA.allowChatMessages()) {
            Wyvern.DISCORD_BOT.sendMessageInGuild(discordMsg);
        }

        return true;
    }
}
