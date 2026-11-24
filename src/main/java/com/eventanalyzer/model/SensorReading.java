package com.eventanalyzer.model;

/** A physical measurement reported by a device, e.g. temperature_celsius = 21.5. */
public record SensorReading(String sensor, double reading) implements EventRecord {
}
