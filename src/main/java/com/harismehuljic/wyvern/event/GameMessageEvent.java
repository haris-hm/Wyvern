package com.harismehuljic.wyvern.event;

import com.harismehuljic.wyvern.Wyvern;
import com.harismehuljic.wyvern.discord.DiscordBot;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

public class GameMessageEvent implements ServerMessageEvents.GameMessage {
    /**
     * Called when the server broadcasts a game message to all players. Game messages
     * include death messages, join/leave messages, and advancement messages. Is not called
     * when {@linkplain #ALLOW_GAME_MESSAGE game messages are blocked}.
     *
     * @param server  the server that sent the message
     * @param message the broadcast message
     * @param overlay {@code true} when the message is an overlay
     */
    @Override
    public void onGameMessage(MinecraftServer server, Text message, boolean overlay) {
        String msg = message.getString();
        DiscordBot discordBot = Wyvern.DISCORD_BOT;

        String advancementRegex = "\\[[^\\]]*\\]";
        msg = msg.replaceFirst(advancementRegex, "**$0**");

        String discordMsg = String.format("*%s*", msg);

        if (Wyvern.CONFIG_DATA.allowChatMessages()) {
            discordBot.sendMessageInGuild(discordBot.generateEmbed(discordMsg));
        }
    }
}
