package com.example.simulator.domain.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class SimulationEventQueue {
    private final PriorityQueue<ScheduledSimulationEvent> queue = new PriorityQueue<>();

    public void add(ScheduledSimulationEvent event) {
        queue.add(event);
    }

    public boolean hasNext() {
        return !queue.isEmpty();
    }

    public ScheduledSimulationEvent poll() {
        return queue.poll();
    }

    public List<SimulationEvent> drainInOrder() {
        List<SimulationEvent> events = new ArrayList<>();
        while (hasNext()) {
            events.add(poll().event());
        }
        return events;
    }
}
