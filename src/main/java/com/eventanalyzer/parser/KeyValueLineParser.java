package com.eventanalyzer.parser;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Parses lines made of space-separated {@code key=value} pairs.
 * <p>
 * Rules:
 * <ul>
 *   <li>Pairs are separated by one or more whitespace characters.</li>
 *   <li>A value containing spaces is wrapped in double quotes: {@code msg="Heartbeat OK"}.
 *       Escaped quotes inside a quoted value are not supported.</li>
 *   <li>Keys and unquoted values never contain spaces.</li>
 *   <li>If a key appears more than once, the last value wins.</li>
 * </ul>
 * A line is rejected as malformed (empty result) when any token has no {@code =},
 * an empty key, an unterminated quote, or text glued directly after a closing quote.
 */
public class KeyValueLineParser implements LineParser {

    @Override
    public Optional<Map<String, String>> parse(String line) {
        if (line == null || line.isBlank()) {
            return Optional.empty();
        }

        Map<String, String> fields = new LinkedHashMap<>();
        int pos = 0;
        int len = line.length();

        while (pos < len) {
            pos = skipWhitespace(line, pos);
            if (pos >= len) {
                break;
            }

            // --- key: everything up to '=' ---
            int keyStart = pos;
            while (pos < len && line.charAt(pos) != '=' && !Character.isWhitespace(line.charAt(pos))) {
                pos++;
            }
            if (pos >= len || line.charAt(pos) != '=' || pos == keyStart) {
                return Optional.empty(); // token without '=' or with an empty key
            }
            String key = line.substring(keyStart, pos);
            if (key.indexOf('"') >= 0) {
                return Optional.empty();
            }
            pos++; // skip '='

            // --- value: quoted or bare ---
            String value;
            if (pos < len && line.charAt(pos) == '"') {
                int closing = line.indexOf('"', pos + 1);
                if (closing < 0) {
                    return Optional.empty(); // unterminated quote
                }
                value = line.substring(pos + 1, closing);
                pos = closing + 1;
                if (pos < len && !Character.isWhitespace(line.charAt(pos))) {
                    return Optional.empty(); // e.g. msg="abc"def
                }
            } else {
                int valueStart = pos;
                while (pos < len && !Character.isWhitespace(line.charAt(pos))) {
                    if (line.charAt(pos) == '"') {
                        return Optional.empty(); // stray quote inside a bare value
                    }
                    pos++;
                }
                value = line.substring(valueStart, pos);
            }
            fields.put(key, value);
        }

        return fields.isEmpty() ? Optional.empty() : Optional.of(fields);
    }

    private static int skipWhitespace(String s, int pos) {
        while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) {
            pos++;
        }
        return pos;
    }
}
