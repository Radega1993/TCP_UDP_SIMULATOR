package com.example.simulator.domain.simulation;

public class FlowControlSnapshot {
    private final int windowSizeBytes;
    private final int bytesSent;
    private final int bytesAcknowledged;
    private final int bytesInFlight;
    private final int bytesPending;
    private final int receiverBufferCapacity;
    private final int receiverBufferUsed;
    private final int receiverAdvertisedWindow;

    public FlowControlSnapshot(int windowSizeBytes, int bytesSent, int bytesAcknowledged, int bytesInFlight,
                               int bytesPending, int receiverBufferCapacity, int receiverBufferUsed,
                               int receiverAdvertisedWindow) {
        this.windowSizeBytes = windowSizeBytes;
        this.bytesSent = bytesSent;
        this.bytesAcknowledged = bytesAcknowledged;
        this.bytesInFlight = bytesInFlight;
        this.bytesPending = bytesPending;
        this.receiverBufferCapacity = receiverBufferCapacity;
        this.receiverBufferUsed = receiverBufferUsed;
        this.receiverAdvertisedWindow = receiverAdvertisedWindow;
    }

    public int getWindowSizeBytes() {
        return windowSizeBytes;
    }

    public int getBytesSent() {
        return bytesSent;
    }

    public int getBytesAcknowledged() {
        return bytesAcknowledged;
    }

    public int getBytesInFlight() {
        return bytesInFlight;
    }

    public int getBytesPending() {
        return bytesPending;
    }

    public int getReceiverBufferCapacity() {
        return receiverBufferCapacity;
    }

    public int getReceiverBufferUsed() {
        return receiverBufferUsed;
    }

    public int getReceiverAdvertisedWindow() {
        return receiverAdvertisedWindow;
    }
}
