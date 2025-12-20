package com.harismehuljic.wyvern.discord.util.markdown;

import java.util.*;

public class MarkdownParser {

    public static List<MarkdownSegment> parse(String input) {
        List<MarkdownSegment> result = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();

        int i = 0;
        while (i < input.length()) {
            if (i + 1 < input.length() && input.charAt(i) == '*' && input.charAt(i + 1) == '*') {
                flush(result, buffer);
                i += 2;
                int start = i;

                while (i + 1 < input.length() &&
                        !(input.charAt(i) == '*' && input.charAt(i + 1) == '*')) {
                    i++;
                }

                result.add(new MarkdownSegment(MarkdownSegmentType.BOLD, input.substring(start, i)));
                i += 2;
            } else if (i + 1 < input.length() && input.charAt(i) == '_' && input.charAt(i + 1) == '_') {
                flush(result, buffer);
                i += 2;
                int start = i;

                while (i + 1 < input.length() &&
                        !(input.charAt(i) == '_' && input.charAt(i + 1) == '_')) {
                    i++;
                }

                result.add(new MarkdownSegment(MarkdownSegmentType.UNDERLINED, input.substring(start, i)));
                i += 2;
            } else if (input.charAt(i) == '*') {
                flush(result, buffer);
                i++;
                int start = i;

                while (i < input.length() && input.charAt(i) != '*') {
                    i++;
                }

                result.add(new MarkdownSegment(MarkdownSegmentType.ITALICIZED, input.substring(start, i)));
                i++;
            } else {
                buffer.append(input.charAt(i));
                i++;
            }
        }

        flush(result, buffer);
        return result;
    }

    private static void flush(List<MarkdownSegment> result, StringBuilder buffer) {
        if (!buffer.isEmpty()) {
            result.add(new MarkdownSegment(MarkdownSegmentType.PLAIN, buffer.toString()));
            buffer.setLength(0);
        }
    }
}

