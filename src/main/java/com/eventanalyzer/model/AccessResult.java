package com.eventanalyzer.model;

import java.util.Optional;

/** Outcome of an access attempt. */
public enum AccessResult {
    GRANTED, DENIED;

    /** Parses an exact (case-sensitive) result name; empty if it is not GRANTED or DENIED. */
    public static Optional<AccessResult> parse(String value) {
        if (value == null) {
            return Optional.empty();
        }
        for (AccessResult r : values()) {
            if (r.name().equals(value)) {
                return Optional.of(r);
            }
        }
        return Optional.empty();
    }
}
