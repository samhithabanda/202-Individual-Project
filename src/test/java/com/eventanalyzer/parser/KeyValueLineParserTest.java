package com.eventanalyzer.parser;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeyValueLineParserTest {

    private final KeyValueLineParser parser = new KeyValueLineParser();

    @Test
    void parsesSimpleKeyValuePairs() {
        Map<String, String> f = parser.parse(
                "ts=2024-05-10T08:00:00Z sensor=temperature_celsius device=thermostat-01 reading=21.5").orElseThrow();
        assertEquals(4, f.size());
        assertEquals("2024-05-10T08:00:00Z", f.get("ts"));
        assertEquals("temperature_celsius", f.get("sensor"));
        assertEquals("thermostat-01", f.get("device"));
        assertEquals("21.5", f.get("reading"));
    }

    @Test
    void parsesQuotedValueContainingSpaces() {
        Map<String, String> f = parser.parse(
                "ts=1 severity=ERROR device=door-lock-07 msg=\"Latch jammed\" code=E41").orElseThrow();
        assertEquals("Latch jammed", f.get("msg"));
        assertEquals("E41", f.get("code"));
    }

    @Test
    void quotedValueAtEndOfLineAndEmptyQuotedValue() {
        Map<String, String> f = parser.parse("ts=1 a=\"\" msg=\"Heartbeat OK\"").orElseThrow();
        assertEquals("", f.get("a"));
        assertEquals("Heartbeat OK", f.get("msg"));
    }

    @Test
    void toleratesExtraWhitespaceBetweenPairs() {
        Map<String, String> f = parser.parse("  ts=1    sensor=x\treading=3  ").orElseThrow();
        assertEquals("x", f.get("sensor"));
        assertEquals("3", f.get("reading"));
    }

    @Test
    void lastDuplicateKeyWins() {
        assertEquals("2", parser.parse("ts=1 k=1 k=2").orElseThrow().get("k"));
    }

    @Test
    void rejectsCorruptedLineWithTokensWithoutEquals() {
        assertTrue(parser.parse("ts=2024-05-10T08:00:50Z ###CORRUPTED LINE### !!!").isEmpty());
    }

    @Test
    void rejectsUnterminatedQuote() {
        assertTrue(parser.parse("ts=1 severity=INFO device=d msg=\"never closed").isEmpty());
    }

    @Test
    void rejectsEmptyKeyAndTextGluedToClosingQuote() {
        assertTrue(parser.parse("ts=1 =value").isEmpty());
        assertTrue(parser.parse("ts=1 msg=\"a b\"c").isEmpty());
    }

    @Test
    void blankOrNullLineYieldsEmpty() {
        assertEquals(Optional.empty(), parser.parse(""));
        assertEquals(Optional.empty(), parser.parse("   "));
        assertEquals(Optional.empty(), parser.parse(null));
    }
}
