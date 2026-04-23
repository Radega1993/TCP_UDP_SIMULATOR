package com.example.simulator.domain.simulation;

import com.example.simulator.domain.network.Endpoint;
import com.example.simulator.domain.protocol.tcp.TcpState;

public class SimulationEvent {
    private final long timestampMillis;
    private final SimulationEventType type;
    private final String message;
    private final Packet packet;
    private final Endpoint endpoint;
    private final TcpState tcpState;
    private final FlowControlSnapshot flowControlSnapshot;
    private final CongestionSnapshot congestionSnapshot;

    public SimulationEvent(long timestampMillis, SimulationEventType type, String message, Packet packet,
                           Endpoint endpoint, TcpState tcpState) {
        this(timestampMillis, type, message, packet, endpoint, tcpState, null, null);
    }

    public SimulationEvent(long timestampMillis, SimulationEventType type, String message, Packet packet,
                           Endpoint endpoint, TcpState tcpState, FlowControlSnapshot flowControlSnapshot) {
        this(timestampMillis, type, message, packet, endpoint, tcpState, flowControlSnapshot, null);
    }

    public SimulationEvent(long timestampMillis, SimulationEventType type, String message, Packet packet,
                           Endpoint endpoint, TcpState tcpState, FlowControlSnapshot flowControlSnapshot,
                           CongestionSnapshot congestionSnapshot) {
        this.timestampMillis = timestampMillis;
        this.type = type;
        this.message = message;
        this.packet = packet;
        this.endpoint = endpoint;
        this.tcpState = tcpState;
        this.flowControlSnapshot = flowControlSnapshot;
        this.congestionSnapshot = congestionSnapshot;
    }

    public long getTimestampMillis() {
        return timestampMillis;
    }

    public SimulationEventType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public Packet getPacket() {
        return packet;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public TcpState getTcpState() {
        return tcpState;
    }

    public FlowControlSnapshot getFlowControlSnapshot() {
        return flowControlSnapshot;
    }

    public CongestionSnapshot getCongestionSnapshot() {
        return congestionSnapshot;
    }
}
