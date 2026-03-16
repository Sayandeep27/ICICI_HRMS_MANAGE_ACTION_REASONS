package com.hrms.actionreason.util;

public class CodeGeneratorUtil {

    public static String generateCode(String name) {

        if (name == null) {
            return null;
        }

        return name
                .trim()
                .toUpperCase()
                .replaceAll(" ", "_");
    }

}