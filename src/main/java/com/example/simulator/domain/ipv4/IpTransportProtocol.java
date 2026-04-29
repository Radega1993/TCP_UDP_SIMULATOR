package com.example.simulator.domain.ipv4;

public enum IpTransportProtocol {
    TCP("TCP", 6, 20),
    UDP("UDP", 17, 8);

    private final String displayName;
    private final int protocolNumber;
    private final int headerBytes;

    IpTransportProtocol(String displayName, int protocolNumber, int headerBytes) {
        this.displayName = displayName;
        this.protocolNumber = protocolNumber;
        this.headerBytes = headerBytes;
    }

    public String displayName() {
        return displayName;
    }

    public int protocolNumber() {
        return protocolNumber;
    }

    public int headerBytes() {
        return headerBytes;
    }
}
