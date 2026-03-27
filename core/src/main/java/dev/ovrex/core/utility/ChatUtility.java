package dev.ovrex.core.utility;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ChatUtility {
    public String convertToJsonChat(String message) {
        final StringBuilder result = new StringBuilder("{\"text\":\"\",\"extra\":[");
        boolean first = true;

        final String[] segments = message.split("(?=§)");

        for (String segment : segments) {
            if (segment.isEmpty()) continue;

            String text;
            String color = null;
            boolean bold = false;
            boolean italic = false;
            boolean underlined = false;
            boolean strikethrough = false;
            boolean obfuscated = false;

            if (segment.startsWith("§") && segment.length() >= 2) {
                char code = segment.charAt(1);
                text = segment.substring(2);
                color = getColorName(code);

                switch (code) {
                    case 'l' -> bold = true;
                    case 'o' -> italic = true;
                    case 'n' -> underlined = true;
                    case 'm' -> strikethrough = true;
                    case 'k' -> obfuscated = true;
                    case 'r' -> color = "white";
                }
            } else {
                text = segment;
            }

            if (text.isEmpty()) continue;

            if (!first) {
                result.append(",");
            }
            first = false;

            result.append("{\"text\":\"").append(escapeJson(text)).append("\"");

            if (color != null && !bold && !italic && !underlined && !strikethrough && !obfuscated) {
                result.append(",\"color\":\"").append(color).append("\"");
            }
            if (bold) result.append(",\"bold\":true");
            if (italic) result.append(",\"italic\":true");
            if (underlined) result.append(",\"underlined\":true");
            if (strikethrough) result.append(",\"strikethrough\":true");
            if (obfuscated) result.append(",\"obfuscated\":true");

            result.append("}");
        }

        if (first) {
            result.append("{\"text\":\"").append(escapeJson(message)).append("\"}");
        }

        result.append("]}");
        return result.toString();
    }

    private String getColorName(char code) {
        return switch (code) {
            case '0' -> "black";
            case '1' -> "dark_blue";
            case '2' -> "dark_green";
            case '3' -> "dark_aqua";
            case '4' -> "dark_red";
            case '5' -> "dark_purple";
            case '6' -> "gold";
            case '7' -> "gray";
            case '8' -> "dark_gray";
            case '9' -> "blue";
            case 'a' -> "green";
            case 'b' -> "aqua";
            case 'c' -> "red";
            case 'd' -> "light_purple";
            case 'e' -> "yellow";
            case 'f' -> "white";
            default -> null;
        };
    }

    private String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
