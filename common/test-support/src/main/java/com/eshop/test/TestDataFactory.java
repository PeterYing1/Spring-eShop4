package com.eshop.test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Static factory methods for generating common test data values.
 *
 * <p>All methods are deterministically random per invocation — they produce
 * plausible values without requiring external state, making tests self-contained.
 */
public final class TestDataFactory {

    private TestDataFactory() {
        // utility class — no instantiation
    }

    /**
     * Returns a random alphanumeric string of the given length.
     */
    public static String randomString(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(length);
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * Returns a random alphanumeric string of length 8 — convenient for names,
     * keys and identifiers in tests.
     */
    public static String randomString() {
        return randomString(8);
    }

    /**
     * Returns a random positive integer in the range [{@code min}, {@code max}).
     */
    public static int randomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max);
    }

    /**
     * Returns a random positive integer in the range [1, 1000).
     */
    public static int randomInt() {
        return randomInt(1, 1000);
    }

    /**
     * Returns a random {@link UUID} as a string — useful for user/buyer IDs.
     */
    public static String randomId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Returns an {@link Instant} one year in the future.
     * Useful for card expiry dates and time-bounded tokens.
     */
    public static Instant futureInstant() {
        return Instant.now().plus(365, ChronoUnit.DAYS);
    }

    /**
     * Returns an {@link Instant} one year in the past.
     * Useful for asserting that a recorded timestamp predates now.
     */
    public static Instant pastInstant() {
        return Instant.now().minus(365, ChronoUnit.DAYS);
    }
}
