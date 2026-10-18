package com.gymmate.shared.domain;

/** Framework-free string checks for domain code. */
public final class Strings {

    private Strings() {
    }

    /** Same contract as Spring's {@code StringUtils.hasText}: not null and contains a non-whitespace character. */
    public static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
