package com.harismehuljic.wyvern.discord;

import com.harismehuljic.wyvern.Wyvern;
import com.harismehuljic.wyvern.config.ConfigData;
import com.harismehuljic.wyvern.discord.listeners.ApplicationCommandListener;
import com.harismehuljic.wyvern.discord.util.EventRegistrar;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DiscordBot {
    private final Logger discordLogger = LoggerFactory.getLogger(Wyvern.MOD_ID + "_discord");
    private final ExecutorService discordBotThread = Executors.newSingleThreadExecutor();
    private final List<String> applicationCommands;

    private GatewayDiscordClient discordClient;

    public DiscordBot(List<String> applicationCommands) {
        this.applicationCommands = applicationCommands;
    }

    public void initialize() {
        ConfigData configData = Wyvern.CONFIG_DATA;
        if (configData.getDiscordToken() == null || configData.getDiscordToken().isBlank()) {
            this.discordLogger.error("Discord token has not been set up. Please set it in the config file.");
            return;
        }

        if (configData.getDiscordChannelId() == 0) {
            this.discordLogger.error("Discord channel ID has not been set up. Please set it in the config file.");
            return;
        }

        if (configData.getDiscordGuildId() == 0) {
            this.discordLogger.error("Discord guild ID has not been set up. Please set it in the config file.");
            return;
        }

        // Start the Discord bot in a separate thread
        this.discordBotThread.submit(() -> {
            try {
                this.discordClient = DiscordClientBuilder.create(configData.getDiscordToken())
                        .build()
                        .login()
                        .block();

                if (this.discordClient != null) {
                    this.discordLogger.info("Discord bot logged in successfully!");

                    new EventRegistrar(discordClient).registerEvents();

                    if (configData.allowLifecycleMessages()) {
                        this.sendMessageInGuild(this.generateEmbed("Server started!"));
                    }
                }

                try {
                    assert this.discordClient != null;
                    new GuildCommandRegistrar(this.discordClient.getRestClient()).registerCommands(this.applicationCommands);
                }
                catch (Exception e) {
                    this.discordLogger.error("Error during Discord bot command registration:", e);
                }

                //Register our slash command listener
                discordClient.on(ChatInputInteractionEvent.class, ApplicationCommandListener::handle)
                        .then(discordClient.onDisconnect())
                        .block(); // We use .block() as there is not another non-daemon thread and the jvm would close otherwise.
            }
            catch (Exception e) {
                discordLogger.error("Error during Discord bot initialization:", e);
            }

        });
    }

    public void shutdown() {
        this.discordClient.logout().block();
        this.discordBotThread.shutdown();
    }

    public void sendMessageInGuild(String message) {
        this.getBotChannel().createMessage(message).block();
    }

    public void sendMessageInGuild(EmbedCreateSpec embed) {
        this.getBotChannel().createMessage(embed).block();
    }

    public EmbedCreateSpec generateEmbed(String message) {
        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.CYAN)
                .description(message)
                .timestamp(Instant.now())
                .build();

        return embed;
    }

    private TextChannel getBotChannel() {
        return this.discordClient
                .getChannelById(Snowflake.of(Wyvern.CONFIG_DATA.getDiscordChannelId()))
                .cast(TextChannel.class)
                .block();
    }

    // Getters and setters
    public Logger getDiscordLogger() {
        return discordLogger;
    }

    public ExecutorService getDiscordBotThread() {
        return discordBotThread;
    }

    public GatewayDiscordClient getDiscordClient() {
        return discordClient;
    }
}
