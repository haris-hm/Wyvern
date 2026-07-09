package com.harismehuljic.wyvern.util;

import com.harismehuljic.wyvern.Wyvern;
import java.util.Objects;
import net.minecraft.server.level.ServerPlayer;

public class MessageFormatter {
    public static String formatDiscordMessage(ServerPlayer spe, String message) {
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

    public static String formatName(ServerPlayer spe) {
        Objects.requireNonNull(spe.getDisplayName());

        String playerName = spe.getDisplayName().getString();
        String realName = spe.getGameProfile().name();
        boolean nicknamed = !realName.equals(playerName);

        return nicknamed ? String.format("%s (%s)", playerName, realName) : playerName;
    }

    public static String getHelmetUrl(ServerPlayer spe) {
        return String.format("https://minotar.net/helm/%s/100.png", spe.getStringUUID());
    }
}
