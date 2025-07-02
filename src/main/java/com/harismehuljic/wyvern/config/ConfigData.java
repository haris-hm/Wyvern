package com.harismehuljic.wyvern.config;

public class ConfigData {
    private String discordToken = "";
    private long discordGuildId = 0;
    private long discordChannelId = 0;
    private boolean suppressLifecycleMessages = false;
    private boolean suppressChatMessages = false;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("discordToken = " + discordToken + "\n")
                .append("discordGuildId = " + discordGuildId + "\n")
                .append("discordChannelId = " + discordChannelId + "\n")
                .append("suppressLifecycleMessages = " + suppressLifecycleMessages + "\n");

        return builder.toString();
    }

    //Getters and setters
    public String getDiscordToken() {
        return discordToken;
    }

    public long getDiscordGuildId() {
        return discordGuildId;
    }

    public long getDiscordChannelId() {
        return discordChannelId;
    }

    public boolean allowLifecycleMessages() {
        return !suppressLifecycleMessages;
    }

    public boolean allowChatMessages() { return !suppressChatMessages; }
}
