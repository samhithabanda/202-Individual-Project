package com.eventanalyzer.classify;

import com.eventanalyzer.core.AnalyzerFactory;
import com.eventanalyzer.model.AccessRecord;
import com.eventanalyzer.model.AccessResult;
import com.eventanalyzer.model.DeviceEvent;
import com.eventanalyzer.model.EventRecord;
import com.eventanalyzer.model.SensorReading;
import com.eventanalyzer.model.Severity;
import com.eventanalyzer.parser.KeyValueLineParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecordClassifierTest {

    private final KeyValueLineParser parser = new KeyValueLineParser();
    private final RecordClassifier chain = new AnalyzerFactory().createClassifierChain();

    private Optional<EventRecord> classify(String line) {
        return parser.parse(line).flatMap(chain::classify);
    }

    @Test
    void classifiesSensorReadingAndIgnoresExtraKeys() {
        assertEquals(new SensorReading("temperature_celsius", 21.5),
                classify("ts=1 sensor=temperature_celsius device=thermostat-01 reading=21.5").orElseThrow());
    }

    @Test
    void classifiesDeviceEvent() {
        assertEquals(new DeviceEvent("door-lock-07", Severity.ERROR),
                classify("ts=1 severity=ERROR device=door-lock-07 msg=\"Latch jammed\" code=E41").orElseThrow());
    }

    @Test
    void classifiesAccessRecord() {
        assertEquals(new AccessRecord("user-42", "door-A", AccessResult.DENIED, 30),
                classify("ts=1 user=user-42 door=door-A result=DENIED duration_ms=30").orElseThrow());
    }

    @Test
    void acceptsNegativeAndDecimalSensorReadings() {
        assertEquals(-4.25, ((SensorReading) classify("ts=1 sensor=t reading=-4.25").orElseThrow()).reading());
        assertEquals(1000.0, ((SensorReading) classify("ts=1 sensor=t reading=1e3").orElseThrow()).reading());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ts=1 sensor=temperature_celsius device=thermostat-01 reading=abc", // non-numeric reading
            "ts=1 sensor=temperature_celsius reading=NaN",                      // not a finite number
            "ts=1 sensor=temperature_celsius",                                  // missing reading
            "ts=1 severity=info device=d1",                                     // lowercase severity
            "ts=1 severity=DEBUG device=d1",                                    // unknown severity
            "ts=1 severity=INFO",                                               // missing device
            "ts=1 user=u door=d result=DENIED",                                 // missing duration_ms
            "ts=1 user=u door=d result=MAYBE duration_ms=5",                    // invalid result
            "ts=1 user=u door=d result=GRANTED duration_ms=12.5",               // non-integer duration
            "ts=1 user=u door=d result=GRANTED duration_ms=-3",                 // negative duration
            "ts=1 msg=\"nothing useful\""                                       // no classification keys
    })
    void rejectsLinesWithMissingKeysOrInvalidValues(String line) {
        assertTrue(classify(line).isEmpty(), line);
    }

    @Test
    void firstMatchingClassifierInChainWins() {
        // Has both sensor and device-event keys: the sensor classifier is first in the chain.
        assertTrue(classify("ts=1 sensor=s reading=1 severity=INFO device=d").orElseThrow()
                instanceof SensorReading);
        // Invalid reading falls through to the next link (device event).
        assertTrue(classify("ts=1 sensor=s reading=x severity=INFO device=d").orElseThrow()
                instanceof DeviceEvent);
    }
}
