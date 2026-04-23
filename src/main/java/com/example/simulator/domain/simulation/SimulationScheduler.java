package com.example.simulator.domain.simulation;

import java.util.List;

public class SimulationScheduler {
    private final SimulationClock clock;
    private final SimulationEventQueue queue;
    private long nextSequence = 0L;

    public SimulationScheduler(SimulationClock clock, SimulationEventQueue queue) {
        this.clock = clock;
        this.queue = queue;
    }

    public void scheduleNow(SimulationEvent event) {
        scheduleAt(clock.currentTimeMillis(), event);
    }

    public void scheduleAfter(long delayMillis, SimulationEvent event) {
        scheduleAt(clock.currentTimeMillis() + Math.max(0L, delayMillis), event);
    }

    public void scheduleAt(long targetTimeMillis, SimulationEvent event) {
        queue.add(new ScheduledSimulationEvent(targetTimeMillis, nextSequence++, event));
    }

    public List<SimulationEvent> drainInOrder() {
        return queue.drainInOrder();
    }
}
