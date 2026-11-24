package com.eventanalyzer.aggregate;

import com.eventanalyzer.model.DeviceEvent;
import com.eventanalyzer.model.Severity;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static com.eventanalyzer.model.Severity.CRITICAL;
import static com.eventanalyzer.model.Severity.ERROR;
import static com.eventanalyzer.model.Severity.INFO;
import static com.eventanalyzer.model.Severity.WARN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeviceEventAggregatorTest {

    @Test
    @SuppressWarnings("unchecked")
    void countsBySeverityAndByDevice() {
        DeviceEventAggregator agg = new DeviceEventAggregator();
        add(agg, "thermostat-01", INFO, INFO, INFO, WARN);
        add(agg, "door-lock-07", ERROR, ERROR, CRITICAL);

        Map<String, Object> result = agg.result();
        Map<String, Object> bySeverity = (Map<String, Object>) result.get("by_severity");
        assertEquals(Map.of("CRITICAL", 1, "ERROR", 2, "WARN", 1, "INFO", 3), bySeverity);

        Map<String, Object> byDevice = (Map<String, Object>) result.get("by_device");
        assertEquals(Map.of("total", 4, "most_frequent_severity", "INFO"), byDevice.get("thermostat-01"));
        assertEquals(Map.of("total", 3, "most_frequent_severity", "ERROR"), byDevice.get("door-lock-07"));
    }

    @Test
    void tieOnMostFrequentSeverityPicksHighestSeverity() {
        Map<Severity, Integer> counts = new EnumMap<>(Severity.class);
        counts.put(INFO, 2);
        counts.put(WARN, 2);
        assertEquals(WARN, DeviceEventAggregator.mostFrequent(counts));

        counts.put(CRITICAL, 2);
        assertEquals(CRITICAL, DeviceEventAggregator.mostFrequent(counts));

        counts.put(INFO, 3);
        assertEquals(INFO, DeviceEventAggregator.mostFrequent(counts));
    }

    @Test
    void emptyAggregatorProducesEmptyMap() {
        assertTrue(new DeviceEventAggregator().result().isEmpty());
    }

    private static void add(DeviceEventAggregator agg, String device, Severity... severities) {
        for (Severity s : severities) {
            agg.accept(new DeviceEvent(device, s));
        }
    }
}
