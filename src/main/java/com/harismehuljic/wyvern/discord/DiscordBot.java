package com.harismehuljic.wyvern.discord;

import com.harismehuljic.wyvern.Wyvern;
import com.harismehuljic.wyvern.config.ConfigData;
import com.harismehuljic.wyvern.discord.listeners.ApplicationCommandListener;
import com.harismehuljic.wyvern.discord.util.EventRegistrar;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import discord4j.rest.util.Color;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DiscordBot {
    private final ExecutorService discordBotThread = Executors.newSingleThreadExecutor();
    private final List<String> applicationCommands;
    private final List<String> cachedMemberNames = new CopyOnWriteArrayList<>();

    private GatewayDiscordClient discordClient;

    public DiscordBot(List<String> applicationCommands) {
        this.applicationCommands = applicationCommands;
    }

    public void initialize() {
        ConfigData configData = Wyvern.CONFIG_DATA;

        if (configData.getDiscordToken() == null || configData.getDiscordToken().isBlank()) {
            Wyvern.LOGGER.error("Discord token has not been set up. Please set it in the config file.");
            return;
        }

        if (configData.getDiscordChannelId() == 0) {
            Wyvern.LOGGER.error("Discord channel ID has not been set up. Please set it in the config file.");
            return;
        }

        if (configData.getDiscordGuildId() == 0) {
            Wyvern.LOGGER.error("Discord guild ID has not been set up. Please set it in the config file.");
            return;
        }

        // Start the Discord bot in a separate thread
        this.discordBotThread.submit(() -> {
            try {
                IntentSet intents = IntentSet.of(
                        Intent.MESSAGE_CONTENT,
                        Intent.GUILD_MEMBERS,
                        Intent.GUILD_MESSAGES,
                        Intent.GUILD_MESSAGE_REACTIONS
                );

                this.discordClient = DiscordClientBuilder.create(configData.getDiscordToken())
                        .build()
                        .gateway()
                        .setEnabledIntents(intents)
                        .login()
                        .block();

                if (this.discordClient != null) {
                    Wyvern.LOGGER.info("Discord bot logged in successfully!");

                    refreshMemberCache();
                    new EventRegistrar(discordClient, this).registerEvents();

                    EmbedCreateSpec embed = this.generateEmbed("Server started!");

                    if (configData.allowLifecycleMessages()) {
                        if (configData.sendLifecycleMessagesToAdminChannel()) {
                            this.sendMessageInGuild(embed, configData.getAdminChannelId());
                        } else {
                            this.sendMessageInGuild(embed);
                        }
                    }
                }

                try {
                    assert this.discordClient != null;
                    new GuildCommandRegistrar(this.discordClient.getRestClient()).registerCommands(this.applicationCommands);
                } catch (Exception e) {
                    Wyvern.LOGGER.error("Error during Discord bot command registration:", e);
                }

                //Register our slash command listener
                discordClient.on(ChatInputInteractionEvent.class, ApplicationCommandListener::handle)
                        .then(discordClient.onDisconnect())
                        .block(); // We use .block() as there is not another non-daemon thread and the jvm would close otherwise.
            } catch (Exception e) {
                Wyvern.LOGGER.error("Error during Discord bot initialization:", e);
            }

        });
    }

    public void shutdown() {
        this.discordClient.logout().block();
        this.discordBotThread.shutdown();
        Wyvern.LOGGER.info("Discord bot has been shut down.");
    }

    private boolean isNotInitialized() {
        boolean discordClientExists = this.discordClient != null;
        if (!discordClientExists) {
            Wyvern.LOGGER.warn("Discord bot is not initialized. Cannot send message.");
        }
        return !discordClientExists;
    }

    public void sendMessageInGuild(String message) {
        if (this.isNotInitialized()) {
            return;
        }

        this.getBotChannel().createMessage(message).block();
    }

    public void sendMessageInGuild(String message, long channelId) {
        if (this.isNotInitialized()) {
            return;
        }

        TextChannel channel = this.discordClient
                .getChannelById(Snowflake.of(channelId))
                .cast(TextChannel.class)
                .block();

        if (channel != null) {
            channel.createMessage(message).block();
        }
    }

    public void sendMessageInGuild(EmbedCreateSpec embed) {
        if (this.isNotInitialized()) {
            return;
        }

        this.getBotChannel().createMessage(embed).block();
    }

    public void sendMessageInGuild(EmbedCreateSpec embed, long channelId) {
        if (this.isNotInitialized()) {
            return;
        }

        TextChannel channel = this.discordClient
                .getChannelById(Snowflake.of(channelId))
                .cast(TextChannel.class)
                .block();

        if (channel != null) {
            channel.createMessage(embed).block();
        }
    }

    public EmbedCreateSpec generateEmbed(String message) {
        return EmbedCreateSpec.builder()
                .color(Color.CYAN)
                .description(message)
                .timestamp(Instant.now())
                .build();
    }

    private void refreshMemberCache() {
        ConfigData configData = Wyvern.CONFIG_DATA;
        Snowflake guildId = Snowflake.of(configData.getDiscordGuildId());

        cachedMemberNames.clear();

        Flux<Member> guildMembers = this.discordClient.getGuildMembers(guildId);

        guildMembers.subscribe(member -> {
            if (member.isBot()) return;
            cachedMemberNames.add(member.getUsername());
        }, error -> Wyvern.LOGGER.error("Error processing Discord guild members: {}", error.getMessage()));
    }

    public void addToMemberCache(String username) {
        this.cachedMemberNames.add(username);
    }

    public void removeFromMemberCache(String username) {
        this.cachedMemberNames.remove(username);
    }

    public List<String> getGuildMembers() {
        return new ArrayList<>(cachedMemberNames);
    }

    private TextChannel getBotChannel() {
        return this.discordClient
                .getChannelById(Snowflake.of(Wyvern.CONFIG_DATA.getDiscordChannelId()))
                .cast(TextChannel.class)
                .block();
    }
}
