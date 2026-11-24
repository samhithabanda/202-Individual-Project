package com.eventanalyzer.model;

/** A badge / credential attempt at a door and how long the system took to decide. */
public record AccessRecord(String user, String door, AccessResult result, long durationMs)
        implements EventRecord {
}
