package com.harismehuljic.wyvern.discord.util;

import net.minecraft.ChatFormatting;

import java.util.*;
import java.util.stream.Collectors;

public enum Formatting {
    BLACK("black", ChatFormatting.BLACK),
    DARK_BLUE("dark_blue", ChatFormatting.DARK_BLUE),
    DARK_GREEN("dark_green", ChatFormatting.DARK_GREEN),
    DARK_AQUA("dark_aqua", ChatFormatting.DARK_AQUA),
    DARK_RED("dark_red", ChatFormatting.DARK_RED),
    DARK_PURPLE("dark_purple", ChatFormatting.DARK_PURPLE),
    GOLD("gold", ChatFormatting.GOLD),
    GRAY("gray", ChatFormatting.GRAY),
    DARK_GRAY("dark_gray", ChatFormatting.DARK_GRAY),
    BLUE("blue", ChatFormatting.BLUE),
    GREEN("green", ChatFormatting.GREEN),
    AQUA("aqua", ChatFormatting.AQUA),
    RED("red", ChatFormatting.RED),
    LIGHT_PURPLE("light_purple", ChatFormatting.LIGHT_PURPLE),
    YELLOW("yellow", ChatFormatting.YELLOW),
    WHITE("white", ChatFormatting.WHITE);

    private static final Map<String, Formatting> FORMATTING_BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(format -> cleanName(format.id), f -> f));
    private final String id;
    private final ChatFormatting format;

    private static String cleanName(final String name) {
        return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
    }

    Formatting(final String id, final ChatFormatting format) {
        this.id = id;
        this.format = format;
    }

    public String getId() {
        return this.id.toLowerCase(Locale.ROOT);
    }

    public String getName() {
        return this.id.replace('_', ' ');
    }

    public ChatFormatting getFormat() {
        return this.format;
    }

    public static Collection<String> getIds() {
        ArrayList<String> names = new ArrayList<>();

        for (Formatting format : values()) {
            names.add(format.getId());
        }

        return names;
    }

    public static Formatting getByName(final String name) {
        return name == null ? Formatting.WHITE : FORMATTING_BY_NAME.get(cleanName(name));
    }

    public static String getIdFromFormat(final ChatFormatting target) {
        for (Formatting format : values()) {
            if (format.getFormat() == target) {
                return format.getId();
            }
        }

        return Formatting.WHITE.getId();
    }
}
