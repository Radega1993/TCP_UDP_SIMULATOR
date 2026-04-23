package com.example.simulator.domain.simulation;

public record ScheduledSimulationEvent(long scheduledTimeMillis, long sequence, SimulationEvent event)
        implements Comparable<ScheduledSimulationEvent> {

    @Override
    public int compareTo(ScheduledSimulationEvent other) {
        int timeComparison = Long.compare(scheduledTimeMillis, other.scheduledTimeMillis);
        if (timeComparison != 0) {
            return timeComparison;
        }
        return Long.compare(sequence, other.sequence);
    }
}
