package com.eventanalyzer.aggregate;

import com.eventanalyzer.model.SensorReading;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Groups readings by sensor name and reports count, minimum, median, average,
 * maximum and population standard deviation for each sensor.
 */
public class SensorReadingAggregator extends TypedAggregator<SensorReading> {

    public static final String NAME = "sensors";

    private final Map<String, List<Double>> readingsBySensor = new TreeMap<>();

    public SensorReadingAggregator() {
        super(NAME, SensorReading.class);
    }

    @Override
    protected void add(SensorReading record) {
        readingsBySensor.computeIfAbsent(record.sensor(), k -> new ArrayList<>()).add(record.reading());
    }

    @Override
    public Map<String, Object> result() {
        Map<String, Object> out = new LinkedHashMap<>();
        readingsBySensor.forEach((sensor, readings) -> {
            List<Double> sorted = new ArrayList<>(readings);
            sorted.sort(null);

            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("count", sorted.size());
            stats.put("minimum", Statistics.roundOneDecimal(sorted.get(0)));
            stats.put("median", Statistics.roundOneDecimal(Statistics.median(sorted)));
            stats.put("average", Statistics.roundOneDecimal(Statistics.mean(sorted)));
            stats.put("maximum", Statistics.roundOneDecimal(sorted.get(sorted.size() - 1)));
            stats.put("std_dev", Statistics.roundOneDecimal(Statistics.populationStdDev(sorted)));
            out.put(sensor, stats);
        });
        return out;
    }
}
