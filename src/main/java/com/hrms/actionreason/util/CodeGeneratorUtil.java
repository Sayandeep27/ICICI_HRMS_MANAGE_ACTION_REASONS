package com.hrms.actionreason.util;

public class CodeGeneratorUtil {

    private CodeGeneratorUtil() {
    }

    public static String generateCode(String name) {

        if (name == null) {
            return null;
        }

        return name
                .trim()
                .toUpperCase()
                .replaceAll("\\s+", "_");
    }

    public static String toTitleCase(String value) {

        if (value == null || value.isBlank()) {
            return value;
        }

        String[] parts = value.trim().toLowerCase().split("\\s+");
        StringBuilder builder = new StringBuilder();

        for (String part : parts) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }

        return builder.toString();
    }

}
