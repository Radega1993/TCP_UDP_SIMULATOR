package com.example.simulator.domain.simulation;

public class SimulationClock {
    private long currentTimeMillis;

    public long currentTimeMillis() {
        return currentTimeMillis;
    }

    public void advanceBy(long millis) {
        currentTimeMillis += Math.max(0L, millis);
    }

    public void advanceTo(long targetMillis) {
        currentTimeMillis = Math.max(currentTimeMillis, targetMillis);
    }

    public void reset() {
        currentTimeMillis = 0L;
    }
}
