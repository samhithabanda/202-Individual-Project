package com.eventanalyzer.classify;

import com.eventanalyzer.model.AccessRecord;
import com.eventanalyzer.model.AccessResult;
import com.eventanalyzer.model.EventRecord;

import java.util.Map;
import java.util.Optional;

/**
 * Recognises lines that have all of {@code user}, {@code door}, {@code result}
 * (GRANTED/DENIED) and an integer {@code duration_ms}.
 */
public class AccessRecordClassifier extends RecordClassifier {

    @Override
    protected Optional<EventRecord> tryClassify(Map<String, String> fields) {
        Optional<String> user = requireText(fields, "user");
        Optional<String> door = requireText(fields, "door");
        Optional<AccessResult> result = AccessResult.parse(fields.get("result"));
        Optional<Long> duration = requireNonNegativeInteger(fields, "duration_ms");
        if (user.isEmpty() || door.isEmpty() || result.isEmpty() || duration.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new AccessRecord(user.get(), door.get(), result.get(), duration.get()));
    }
}
