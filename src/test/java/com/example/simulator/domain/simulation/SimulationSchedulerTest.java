package com.example.simulator.domain.simulation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimulationSchedulerTest {
    @Test
    void drainsEventsInTimestampOrder() {
        SimulationClock clock = new SimulationClock();
        SimulationEventQueue queue = new SimulationEventQueue();
        SimulationScheduler scheduler = new SimulationScheduler(clock, queue);

        scheduler.scheduleAfter(10, new SimulationEvent(10, SimulationEventType.LOG, "later", null, null, null));
        scheduler.scheduleNow(new SimulationEvent(0, SimulationEventType.LOG, "now", null, null, null));
        scheduler.scheduleAfter(5, new SimulationEvent(5, SimulationEventType.LOG, "middle", null, null, null));

        List<SimulationEvent> events = scheduler.drainInOrder();

        assertEquals(List.of("now", "middle", "later"), events.stream().map(SimulationEvent::getMessage).toList());
    }
}
