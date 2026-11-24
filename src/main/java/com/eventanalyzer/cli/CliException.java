package com.eventanalyzer.cli;

/** Thrown when the command-line arguments are missing or invalid. */
public class CliException extends Exception {
    public CliException(String message) {
        super(message);
    }
}
