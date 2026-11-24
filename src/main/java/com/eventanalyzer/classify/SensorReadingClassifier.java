package com.eventanalyzer.classify;

import com.eventanalyzer.model.EventRecord;
import com.eventanalyzer.model.SensorReading;

import java.util.Map;
import java.util.Optional;

/** Recognises lines that have a {@code sensor} key and a numeric {@code reading} key. */
public class SensorReadingClassifier extends RecordClassifier {

    @Override
    protected Optional<EventRecord> tryClassify(Map<String, String> fields) {
        Optional<String> sensor = requireText(fields, "sensor");
        Optional<Double> reading = requireDecimal(fields, "reading");
        if (sensor.isEmpty() || reading.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new SensorReading(sensor.get(), reading.get()));
    }
}
