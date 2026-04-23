package com.example.simulator.domain.simulation;

import com.example.simulator.domain.protocol.tcp.CongestionPhase;

public class CongestionSnapshot {
    private final CongestionPhase phase;
    private final int cwndBytes;
    private final int slowStartThresholdBytes;
    private final int duplicateAckCount;
    private final int effectiveWindowBytes;
    private final int bytesInFlight;
    private final String reason;
    private final CwndHistoryPoint historyPoint;

    public CongestionSnapshot(CongestionPhase phase, int cwndBytes, int slowStartThresholdBytes,
                              int duplicateAckCount, int effectiveWindowBytes, int bytesInFlight,
                              String reason, CwndHistoryPoint historyPoint) {
        this.phase = phase;
        this.cwndBytes = cwndBytes;
        this.slowStartThresholdBytes = slowStartThresholdBytes;
        this.duplicateAckCount = duplicateAckCount;
        this.effectiveWindowBytes = effectiveWindowBytes;
        this.bytesInFlight = bytesInFlight;
        this.reason = reason;
        this.historyPoint = historyPoint;
    }

    public CongestionPhase getPhase() {
        return phase;
    }

    public int getCwndBytes() {
        return cwndBytes;
    }

    public int getSlowStartThresholdBytes() {
        return slowStartThresholdBytes;
    }

    public int getDuplicateAckCount() {
        return duplicateAckCount;
    }

    public int getEffectiveWindowBytes() {
        return effectiveWindowBytes;
    }

    public int getBytesInFlight() {
        return bytesInFlight;
    }

    public String getReason() {
        return reason;
    }

    public CwndHistoryPoint getHistoryPoint() {
        return historyPoint;
    }
}
