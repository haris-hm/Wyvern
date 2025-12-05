package com.harismehuljic.wyvern.event;

import com.harismehuljic.wyvern.Wyvern;
import com.harismehuljic.wyvern.discord.DiscordBot;
import discord4j.core.object.Embed;
import discord4j.core.spec.EmbedCreateSpec;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public class ModServerLifecycleEvents {
    public static class ServerStartingEvent implements ServerLifecycleEvents.ServerStarting {
        @Override
        public void onServerStarting(MinecraftServer server) {
            //DiscordBotUtils.sendMessageInGuild(DiscordBotUtils.generateEmbed("Server starting..."));
        }
    }

    public static class ServerStartedEvent implements ServerLifecycleEvents.ServerStarted {
        @Override
        public void onServerStarted(MinecraftServer server) {
            Wyvern.SERVER = server;
            //DiscordBotUtils.sendMessageInGuild(DiscordBotUtils.generateEmbed("Server started!"));
        }
    }

    public static class ServerStoppingEvent implements ServerLifecycleEvents.ServerStopping {
        @Override
        public void onServerStopping(MinecraftServer server) {
            if (Wyvern.CONFIG_DATA.allowLifecycleMessages()) {
                DiscordBot discordBot = Wyvern.DISCORD_BOT;
                EmbedCreateSpec embed = discordBot.generateEmbed("Server stopping...");

                if (Wyvern.CONFIG_DATA.sendLifecycleMessagesToAdminChannel()) {
                    discordBot.sendMessageInGuild(embed, Wyvern.CONFIG_DATA.getAdminChannelId());
                } else {
                    discordBot.sendMessageInGuild(embed);
                }
            }

            Wyvern.DISCORD_BOT.shutdown();
        }
    }

    public static class ServerStoppedEvent implements ServerLifecycleEvents.ServerStopped {
        @Override
        public void onServerStopped(MinecraftServer server) {
            if (Wyvern.CONFIG_DATA.allowLifecycleMessages()) {
                DiscordBot discordBot = Wyvern.DISCORD_BOT;
                EmbedCreateSpec embed = discordBot.generateEmbed("Server stopped!");

                if (Wyvern.CONFIG_DATA.sendLifecycleMessagesToAdminChannel()) {
                    discordBot.sendMessageInGuild(embed, Wyvern.CONFIG_DATA.getAdminChannelId());
                } else {
                    discordBot.sendMessageInGuild(embed);
                }
            }
        }
    }
}
