package com.eventanalyzer.model;

/**
 * Marker interface for a classified line of the event stream.
 * Every record type (sensor reading, device event, access record, ...) implements it,
 * so new record types can be added without changing the pipeline.
 */
public interface EventRecord {
}
