package com.example.simulator.domain.protocol.tcp;

public class TcpCongestionControl {
    private final int mssBytes;
    private int cwndBytes;
    private int slowStartThresholdBytes;
    private int duplicateAckCount;
    private CongestionPhase phase;

    public TcpCongestionControl(int mssBytes, int configuredWindowBytes) {
        this.mssBytes = Math.max(1, mssBytes);
        this.cwndBytes = this.mssBytes;
        this.slowStartThresholdBytes = Math.max(this.mssBytes * 4, Math.max(this.mssBytes, configuredWindowBytes / 2));
        this.phase = CongestionPhase.SLOW_START;
    }

    public int currentWindowBytes() {
        return cwndBytes;
    }

    public int slowStartThresholdBytes() {
        return slowStartThresholdBytes;
    }

    public int duplicateAckCount() {
        return duplicateAckCount;
    }

    public CongestionPhase phase() {
        return phase;
    }

    public int effectiveWindowBytes(int receiverWindowBytes) {
        return Math.max(1, Math.min(cwndBytes, receiverWindowBytes));
    }

    public void onAcknowledgement(int acknowledgedBytes) {
        int acked = Math.max(1, acknowledgedBytes);
        duplicateAckCount = 0;
        if (phase == CongestionPhase.FAST_RETRANSMIT) {
            cwndBytes = Math.max(slowStartThresholdBytes, mssBytes);
            phase = CongestionPhase.CONGESTION_AVOIDANCE;
            return;
        }
        if (phase == CongestionPhase.SLOW_START) {
            cwndBytes += acked;
            if (cwndBytes >= slowStartThresholdBytes) {
                phase = CongestionPhase.CONGESTION_AVOIDANCE;
            }
            return;
        }
        int additiveGrowth = Math.max(1, (mssBytes * acked) / Math.max(mssBytes, cwndBytes));
        cwndBytes += additiveGrowth;
    }

    public int onDuplicateAck() {
        duplicateAckCount++;
        return duplicateAckCount;
    }

    public void onFastRetransmit() {
        slowStartThresholdBytes = Math.max(mssBytes, cwndBytes / 2);
        cwndBytes = Math.max(slowStartThresholdBytes, mssBytes);
        phase = CongestionPhase.FAST_RETRANSMIT;
        duplicateAckCount = 0;
    }

    public void onTimeout() {
        slowStartThresholdBytes = Math.max(mssBytes, cwndBytes / 2);
        cwndBytes = mssBytes;
        duplicateAckCount = 0;
        phase = CongestionPhase.SLOW_START;
    }
}
