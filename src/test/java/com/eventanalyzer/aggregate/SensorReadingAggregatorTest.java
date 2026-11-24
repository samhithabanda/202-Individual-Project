package com.eventanalyzer.aggregate;

import com.eventanalyzer.model.DeviceEvent;
import com.eventanalyzer.model.SensorReading;
import com.eventanalyzer.model.Severity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SensorReadingAggregatorTest {

    @Test
    void computesAllStatisticsPerSensor() {
        SensorReadingAggregator agg = new SensorReadingAggregator();
        agg.accept(new SensorReading("humidity_percent", 40));
        agg.accept(new SensorReading("humidity_percent", 50));
        for (double v : new double[]{18.5, 25.0, 21.0, 20.5}) {
            agg.accept(new SensorReading("temperature_celsius", v));
        }

        Map<String, Object> result = agg.result();
        assertEquals(2, result.size());

        @SuppressWarnings("unchecked")
        Map<String, Object> humidity = (Map<String, Object>) result.get("humidity_percent");
        assertEquals(2, humidity.get("count"));
        assertEquals(40L, humidity.get("minimum"));
        assertEquals(45L, humidity.get("median"));
        assertEquals(45L, humidity.get("average"));
        assertEquals(50L, humidity.get("maximum"));
        assertEquals(5L, humidity.get("std_dev"));

        @SuppressWarnings("unchecked")
        Map<String, Object> temp = (Map<String, Object>) result.get("temperature_celsius");
        assertEquals(4, temp.get("count"));
        assertEquals(new BigDecimal("18.5"), temp.get("minimum"));
        assertEquals(new BigDecimal("20.8"), temp.get("median"));   // (20.5 + 21.0) / 2 = 20.75
        assertEquals(new BigDecimal("21.3"), temp.get("average"));  // 85 / 4 = 21.25
        assertEquals(25L, temp.get("maximum"));
        assertEquals(new BigDecimal("2.4"), temp.get("std_dev"));   // sqrt(5.6875) = 2.3848...
    }

    @Test
    void groupsByArbitrarySensorNamesAndIgnoresOtherRecordTypes() {
        SensorReadingAggregator agg = new SensorReadingAggregator();
        agg.accept(new SensorReading("co2_ppm", 412));
        agg.accept(new DeviceEvent("d", Severity.INFO));
        assertEquals(1, agg.result().size());
        assertTrue(agg.result().containsKey("co2_ppm"));
    }

    @Test
    void emptyAggregatorProducesEmptyMap() {
        assertTrue(new SensorReadingAggregator().result().isEmpty());
    }
}
