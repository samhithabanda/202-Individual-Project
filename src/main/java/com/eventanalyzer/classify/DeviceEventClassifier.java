package com.eventanalyzer.classify;

import com.eventanalyzer.model.DeviceEvent;
import com.eventanalyzer.model.EventRecord;
import com.eventanalyzer.model.Severity;

import java.util.Map;
import java.util.Optional;

/** Recognises lines with a {@code device} key and a valid {@code severity} (CRITICAL/ERROR/WARN/INFO). */
public class DeviceEventClassifier extends RecordClassifier {

    @Override
    protected Optional<EventRecord> tryClassify(Map<String, String> fields) {
        Optional<String> device = requireText(fields, "device");
        Optional<Severity> severity = Severity.parse(fields.get("severity"));
        if (device.isEmpty() || severity.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new DeviceEvent(device.get(), severity.get()));
    }
}
