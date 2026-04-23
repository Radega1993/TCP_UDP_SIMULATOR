package com.example.simulator.application.dto;

public class ComparisonSummary {
    private final boolean tcpDeliveredComplete;
    private final boolean udpDeliveredComplete;
    private final boolean tcpRetransmitted;
    private final boolean udpRetransmitted;
    private final boolean tcpOrdered;
    private final boolean udpOrdered;
    private final int tcpLostPackets;
    private final int udpLostPackets;
    private final int tcpPacketCount;
    private final int udpPacketCount;

    public ComparisonSummary(boolean tcpDeliveredComplete, boolean udpDeliveredComplete,
                             boolean tcpRetransmitted, boolean udpRetransmitted,
                             boolean tcpOrdered, boolean udpOrdered,
                             int tcpLostPackets, int udpLostPackets,
                             int tcpPacketCount, int udpPacketCount) {
        this.tcpDeliveredComplete = tcpDeliveredComplete;
        this.udpDeliveredComplete = udpDeliveredComplete;
        this.tcpRetransmitted = tcpRetransmitted;
        this.udpRetransmitted = udpRetransmitted;
        this.tcpOrdered = tcpOrdered;
        this.udpOrdered = udpOrdered;
        this.tcpLostPackets = tcpLostPackets;
        this.udpLostPackets = udpLostPackets;
        this.tcpPacketCount = tcpPacketCount;
        this.udpPacketCount = udpPacketCount;
    }

    public boolean isTcpDeliveredComplete() {
        return tcpDeliveredComplete;
    }

    public boolean isUdpDeliveredComplete() {
        return udpDeliveredComplete;
    }

    public boolean isTcpRetransmitted() {
        return tcpRetransmitted;
    }

    public boolean isUdpRetransmitted() {
        return udpRetransmitted;
    }

    public boolean isTcpOrdered() {
        return tcpOrdered;
    }

    public boolean isUdpOrdered() {
        return udpOrdered;
    }

    public int getTcpLostPackets() {
        return tcpLostPackets;
    }

    public int getUdpLostPackets() {
        return udpLostPackets;
    }

    public int getTcpPacketCount() {
        return tcpPacketCount;
    }

    public int getUdpPacketCount() {
        return udpPacketCount;
    }
}
