package com.example.simulator.domain.protocol.tcp;

public class TcpSlidingWindow {
    private final int configuredWindowBytes;
    private int acknowledgedBytes;
    private int sentBytes;

    public TcpSlidingWindow(int configuredWindowBytes) {
        this.configuredWindowBytes = Math.max(1, configuredWindowBytes);
    }

    public int configuredWindowBytes() {
        return configuredWindowBytes;
    }

    public int bytesAcknowledged() {
        return acknowledgedBytes;
    }

    public int bytesSent() {
        return sentBytes;
    }

    public int bytesInFlight() {
        return Math.max(0, sentBytes - acknowledgedBytes);
    }

    public int bytesPending(String message) {
        return Math.max(0, message.length() - acknowledgedBytes);
    }

    public boolean canSend(int nextPayloadBytes, int advertisedWindowBytes) {
        int effectiveWindow = Math.max(0, Math.min(configuredWindowBytes, advertisedWindowBytes));
        return bytesInFlight() + nextPayloadBytes <= effectiveWindow;
    }

    public void markSent(int payloadBytes) {
        sentBytes += Math.max(0, payloadBytes);
    }

    public void markAcknowledgedUpTo(int bytes) {
        acknowledgedBytes = Math.max(acknowledgedBytes, Math.min(sentBytes, bytes));
    }
}
