package com.eventanalyzer.model;

/** A state change or alert raised by a device. */
public record DeviceEvent(String device, Severity severity) implements EventRecord {
}
