package com.example.simulator.domain.simulation;

public class CwndHistoryPoint {
    private final int step;
    private final int cwndBytes;
    private final String label;
    private final boolean lossEvent;

    public CwndHistoryPoint(int step, int cwndBytes, String label, boolean lossEvent) {
        this.step = step;
        this.cwndBytes = cwndBytes;
        this.label = label;
        this.lossEvent = lossEvent;
    }

    public int getStep() {
        return step;
    }

    public int getCwndBytes() {
        return cwndBytes;
    }

    public String getLabel() {
        return label;
    }

    public boolean isLossEvent() {
        return lossEvent;
    }
}
