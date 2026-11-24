package com.eventanalyzer.classify;

import com.eventanalyzer.model.EventRecord;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Chain of Responsibility link that decides which record type a parsed line belongs to.
 * <p>
 * {@link #classify(Map)} is a template method: it asks this link to {@link #tryClassify(Map)}
 * the fields and, if this link cannot, forwards the request to the next link. A line that
 * no link recognises yields {@link Optional#empty()} and is skipped by the caller.
 * <p>
 * Links are tried in the order they are chained, so the first matching type wins.
 */
public abstract class RecordClassifier {

    private static final Pattern DECIMAL =
            Pattern.compile("[+-]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?");
    private static final Pattern NON_NEGATIVE_INTEGER = Pattern.compile("\\+?\\d+");

    private RecordClassifier next;

    /**
     * Appends {@code nextClassifier} after this one and returns it, so chains can be built
     * fluently: {@code a.linkWith(b).linkWith(c)}.
     */
    public RecordClassifier linkWith(RecordClassifier nextClassifier) {
        this.next = nextClassifier;
        return nextClassifier;
    }

    /** Classifies the fields with this link, or delegates to the rest of the chain. */
    public final Optional<EventRecord> classify(Map<String, String> fields) {
        Optional<EventRecord> record = tryClassify(fields);
        if (record.isPresent()) {
            return record;
        }
        return next == null ? Optional.empty() : next.classify(fields);
    }

    /**
     * Attempts to build this link's record type from the fields.
     *
     * @return the record, or empty if required keys are missing or values are invalid
     */
    protected abstract Optional<EventRecord> tryClassify(Map<String, String> fields);

    // ---------- shared validation helpers for subclasses ----------

    /** Returns the value for {@code key} if present and not blank. */
    protected static Optional<String> requireText(Map<String, String> fields, String key) {
        String value = fields.get(key);
        return (value == null || value.isBlank()) ? Optional.empty() : Optional.of(value);
    }

    /** Returns the value for {@code key} parsed as a finite decimal number. */
    protected static Optional<Double> requireDecimal(Map<String, String> fields, String key) {
        String value = fields.get(key);
        if (value == null || !DECIMAL.matcher(value).matches()) {
            return Optional.empty();
        }
        double d = Double.parseDouble(value);
        return Double.isFinite(d) ? Optional.of(d) : Optional.empty();
    }

    /** Returns the value for {@code key} parsed as a non-negative whole number. */
    protected static Optional<Long> requireNonNegativeInteger(Map<String, String> fields, String key) {
        String value = fields.get(key);
        if (value == null || !NON_NEGATIVE_INTEGER.matcher(value).matches()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(value));
        } catch (NumberFormatException overflow) {
            return Optional.empty();
        }
    }
}
