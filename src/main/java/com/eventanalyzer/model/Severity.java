package com.eventanalyzer.model;

import java.util.Optional;

/**
 * Device event severity. Declared from highest to lowest, so a lower ordinal
 * means a more severe level (CRITICAL &gt; ERROR &gt; WARN &gt; INFO).
 */
public enum Severity {
    CRITICAL, ERROR, WARN, INFO;

    /** Returns true if this level is more severe than {@code other}. */
    public boolean isHigherThan(Severity other) {
        return this.ordinal() < other.ordinal();
    }

    /** Parses an exact (case-sensitive) severity name; empty if it is not a valid level. */
    public static Optional<Severity> parse(String value) {
        if (value == null) {
            return Optional.empty();
        }
        for (Severity s : values()) {
            if (s.name().equals(value)) {
                return Optional.of(s);
            }
        }
        return Optional.empty();
    }
}
