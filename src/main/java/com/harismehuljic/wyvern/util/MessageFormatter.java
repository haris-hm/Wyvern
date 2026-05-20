package com.harismehuljic.wyvern.util;

import com.harismehuljic.wyvern.Wyvern;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Objects;

public class MessageFormatter {
    public static String formatDiscordMessage(ServerPlayerEntity spe, String message) {
        Objects.requireNonNull(spe.getDisplayName());

        String playerName = spe.getDisplayName().getString();
        String realName = spe.getGameProfile().name();
        boolean nicknamed = !realName.equals(playerName);

        String playerNameBolded = nicknamed ? String.format("**%s** (*%s*)", playerName, realName) : String.format("**%s**", playerName);

        return MessageFormatter.formatMessage(Wyvern.CONFIG_DATA.getMessageFormat(), playerNameBolded, message);
    }

    private static String formatMessage(String format, String playerName, String messageContents) {
        return format.replace("{username}", playerName).replace("{message}", messageContents);
    }

    public static String formatName(ServerPlayerEntity spe) {
        Objects.requireNonNull(spe.getDisplayName());

        String playerName = spe.getDisplayName().getString();
        String realName = spe.getGameProfile().name();
        boolean nicknamed = !realName.equals(playerName);

        return nicknamed ? String.format("%s (%s)", playerName, realName) : playerName;
    }

    public static String getHelmetUrl(ServerPlayerEntity spe) {
        return String.format("https://minotar.net/helm/%s/100.png", spe.getUuidAsString());
    }
}
