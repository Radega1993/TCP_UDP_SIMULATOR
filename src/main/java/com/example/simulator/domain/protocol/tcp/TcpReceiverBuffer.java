package com.example.simulator.domain.protocol.tcp;

public class TcpReceiverBuffer {
    private final int capacityBytes;
    private int usedBytes;

    public TcpReceiverBuffer(int capacityBytes) {
        this.capacityBytes = Math.max(1, capacityBytes);
    }

    public int capacityBytes() {
        return capacityBytes;
    }

    public int usedBytes() {
        return usedBytes;
    }

    public int availableBytes() {
        return Math.max(0, capacityBytes - usedBytes);
    }

    public boolean canAccept(int bytes) {
        return bytes <= availableBytes();
    }

    public void reserve(int bytes) {
        usedBytes = Math.min(capacityBytes, usedBytes + Math.max(0, bytes));
    }

    public void release(int bytes) {
        usedBytes = Math.max(0, usedBytes - Math.max(0, bytes));
    }
}
