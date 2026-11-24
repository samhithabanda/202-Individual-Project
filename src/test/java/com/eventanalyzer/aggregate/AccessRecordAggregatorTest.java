package com.eventanalyzer.aggregate;

import com.eventanalyzer.model.AccessRecord;
import com.eventanalyzer.model.AccessResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.eventanalyzer.model.AccessResult.DENIED;
import static com.eventanalyzer.model.AccessResult.GRANTED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccessRecordAggregatorTest {

    @Test
    @SuppressWarnings("unchecked")
    void computesDecisionTimesResultsAndFlaggedUsersPerDoor() {
        AccessRecordAggregator agg = new AccessRecordAggregator();
        // door-A: 8 attempts, durations 12,15,20,22,30,45,50,60
        add(agg, "user-42", "door-A", DENIED, 30);
        add(agg, "user-42", "door-A", DENIED, 45);
        add(agg, "user-42", "door-A", DENIED, 60);
        add(agg, "user-17", "door-A", DENIED, 12);
        add(agg, "user-17", "door-A", DENIED, 15);   // only 2 denials -> not flagged
        add(agg, "user-17", "door-A", GRANTED, 20);
        add(agg, "user-08", "door-A", GRANTED, 22);
        add(agg, "user-08", "door-A", GRANTED, 50);
        // Denials on another door do not count toward door-A
        add(agg, "user-17", "door-B", DENIED, 5);

        Map<String, Object> doorA = (Map<String, Object>) agg.result().get("door-A");
        Map<String, Object> times = (Map<String, Object>) doorA.get("decision_times");
        assertEquals(12L, times.get("min"));
        assertEquals(22L, times.get("50_percentile")); // ceil(4.0)-1 = index 3
        assertEquals(60L, times.get("90_percentile")); // ceil(7.2)-1 = index 7
        assertEquals(60L, times.get("99_percentile"));
        assertEquals(60L, times.get("max"));
        assertEquals(Map.of("GRANTED", 3, "DENIED", 5), doorA.get("results"));
        assertEquals(List.of("user-42"), doorA.get("flagged_users"));

        Map<String, Object> doorB = (Map<String, Object>) agg.result().get("door-B");
        assertEquals(Map.of("GRANTED", 0, "DENIED", 1), doorB.get("results"));
        assertEquals(List.of(), doorB.get("flagged_users"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void flaggedUsersAreSortedAndThresholdIsInclusive() {
        AccessRecordAggregator agg = new AccessRecordAggregator();
        for (int i = 0; i < 3; i++) {
            add(agg, "zed", "door-X", DENIED, 10);
            add(agg, "amy", "door-X", DENIED, 10);
        }
        Map<String, Object> door = (Map<String, Object>) agg.result().get("door-X");
        assertEquals(List.of("amy", "zed"), door.get("flagged_users"));
    }

    @Test
    void emptyAggregatorProducesEmptyMap() {
        assertTrue(new AccessRecordAggregator().result().isEmpty());
    }

    private static void add(AccessRecordAggregator agg, String user, String door, AccessResult r, long ms) {
        agg.accept(new AccessRecord(user, door, r, ms));
    }
}
