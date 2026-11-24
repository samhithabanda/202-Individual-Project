package com.eventanalyzer.parser;

import java.util.Map;
import java.util.Optional;

/**
 * Strategy for turning one raw input line into a map of field names to values.
 * <p>
 * The default implementation is {@link KeyValueLineParser}. Supporting a new input
 * format (for example JSON Lines) only requires another implementation of this interface.
 */
public interface LineParser {

    /**
     * Parses a single line.
     *
     * @param line raw line from the input file
     * @return the parsed fields, or empty if the line is blank or malformed
     */
    Optional<Map<String, String>> parse(String line);
}
