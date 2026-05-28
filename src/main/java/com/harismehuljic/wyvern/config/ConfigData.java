package com.harismehuljic.wyvern.config;

import com.harismehuljic.wyvern.Wyvern;

public class ConfigData {
    private String serverName = "SMP";
    private String discordToken = "";
    private long discordGuildId = 0;
    private long discordChannelId = 0;
    private long adminChannelId = 0;
    private boolean suppressLifecycleMessages = false;
    private boolean suppressChatMessages = false;
    private boolean sendLifecycleMessagesToAdminChannel = false;
    private String messageFormat = "<{username}>  {message}";

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("discordToken = " + discordToken + "\n")
                .append("discordGuildId = " + discordGuildId + "\n")
                .append("discordChannelId = " + discordChannelId + "\n")
                .append("adminChannelId = " + adminChannelId + "\n")
                .append("suppressLifecycleMessages = " + suppressLifecycleMessages + "\n")
                .append("suppressChatMessages = " + suppressChatMessages + "\n")
                .append("sendLifecycleMessagesToAdminChannel = " + sendLifecycleMessagesToAdminChannel + "\n")
                .append("messageFormat = " + messageFormat + "\n");

        return builder.toString();
    }

    //Getters and setters
    public String getServerName() {
        return serverName;
    }

    public String getDiscordToken() {
        return discordToken;
    }

    public long getDiscordGuildId() {
        return discordGuildId;
    }

    public long getDiscordChannelId() {
        return discordChannelId;
    }

    public long getAdminChannelId() {
        return adminChannelId;
    }

    public boolean allowLifecycleMessages() {
        return !suppressLifecycleMessages;
    }

    public boolean allowChatMessages() {
        return !suppressChatMessages;
    }

    public boolean sendLifecycleMessagesToAdminChannel() {
        return sendLifecycleMessagesToAdminChannel;
    }

    public String getMessageFormat() {
        if (messageFormat == null || messageFormat.isEmpty() || !messageFormat.contains("{username}") || !messageFormat.contains("{message}")) {
            Wyvern.LOGGER.warn("Invalid message format in config: {}. Format must contain both a \"{username}\" and a \"{message}\" field. Using default Minecraft format.", messageFormat);
            messageFormat = "<{username}>  {message}";
        }

        return messageFormat;
    }
}
