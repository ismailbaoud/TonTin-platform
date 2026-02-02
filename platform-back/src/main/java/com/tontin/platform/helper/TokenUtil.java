package com.tontin.platform.helper;

import org.apache.commons.text.RandomStringGenerator;

public class TokenUtil {

    private static final RandomStringGenerator GENERATOR =
        new RandomStringGenerator.Builder()
            .withinRange('0', 'z')
            .filteredBy(Character::isLetterOrDigit)
            .build();

    public static String generate(int length) {
        return GENERATOR.generate(length);
    }
}