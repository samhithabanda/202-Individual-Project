package com.eventanalyzer.aggregate;

import com.eventanalyzer.model.EventRecord;

import java.util.Objects;

/**
 * Template Method base class for aggregators that handle exactly one record type.
 * <p>
 * {@link #accept(EventRecord)} is the fixed algorithm (type check + cast); subclasses only
 * implement the type-specific step {@link #add(EventRecord)} and {@link #result()}.
 *
 * @param <T> the record type this aggregator consumes
 */
public abstract class TypedAggregator<T extends EventRecord> implements Aggregator {

    private final Class<T> recordType;
    private final String name;

    protected TypedAggregator(String name, Class<T> recordType) {
        this.name = Objects.requireNonNull(name);
        this.recordType = Objects.requireNonNull(recordType);
    }

    @Override
    public final String name() {
        return name;
    }

    @Override
    public final void accept(EventRecord record) {
        if (recordType.isInstance(record)) {
            add(recordType.cast(record));
        }
    }

    /** Adds one record of the supported type to the running aggregation. */
    protected abstract void add(T record);
}
