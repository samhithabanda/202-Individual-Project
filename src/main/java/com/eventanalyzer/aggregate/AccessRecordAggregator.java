package com.eventanalyzer.aggregate;

import com.eventanalyzer.model.AccessRecord;
import com.eventanalyzer.model.AccessResult;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Per door: decision-time min/max and nearest-rank percentiles (50th, 90th, 99th),
 * GRANTED vs. DENIED counts, and users with {@value #FLAG_THRESHOLD} or more denied attempts.
 */
public class AccessRecordAggregator extends TypedAggregator<AccessRecord> {

    public static final String NAME = "access";
    public static final int FLAG_THRESHOLD = 3;

    private final Map<String, DoorStats> doors = new TreeMap<>();

    public AccessRecordAggregator() {
        super(NAME, AccessRecord.class);
    }

    @Override
    protected void add(AccessRecord record) {
        doors.computeIfAbsent(record.door(), k -> new DoorStats()).add(record);
    }

    @Override
    public Map<String, Object> result() {
        Map<String, Object> out = new LinkedHashMap<>();
        doors.forEach((door, stats) -> out.put(door, stats.toMap()));
        return out;
    }

    /** Running totals for a single door. */
    private static final class DoorStats {
        private final List<Long> durations = new ArrayList<>();
        private final Map<AccessResult, Integer> results = new EnumMap<>(AccessResult.class);
        private final Map<String, Integer> deniedByUser = new TreeMap<>();

        void add(AccessRecord r) {
            durations.add(r.durationMs());
            results.merge(r.result(), 1, Integer::sum);
            if (r.result() == AccessResult.DENIED) {
                deniedByUser.merge(r.user(), 1, Integer::sum);
            }
        }

        Map<String, Object> toMap() {
            List<Long> sorted = new ArrayList<>(durations);
            sorted.sort(null);

            Map<String, Object> times = new LinkedHashMap<>();
            times.put("min", sorted.get(0));
            times.put("50_percentile", Statistics.percentileNearestRank(sorted, 50));
            times.put("90_percentile", Statistics.percentileNearestRank(sorted, 90));
            times.put("99_percentile", Statistics.percentileNearestRank(sorted, 99));
            times.put("max", sorted.get(sorted.size() - 1));

            Map<String, Object> resultCounts = new LinkedHashMap<>();
            for (AccessResult r : AccessResult.values()) {
                resultCounts.put(r.name(), results.getOrDefault(r, 0));
            }

            List<String> flagged = new ArrayList<>();
            deniedByUser.forEach((user, count) -> {
                if (count >= FLAG_THRESHOLD) {
                    flagged.add(user);
                }
            });

            Map<String, Object> out = new LinkedHashMap<>();
            out.put("decision_times", times);
            out.put("results", resultCounts);
            out.put("flagged_users", flagged);
            return out;
        }
    }
}
