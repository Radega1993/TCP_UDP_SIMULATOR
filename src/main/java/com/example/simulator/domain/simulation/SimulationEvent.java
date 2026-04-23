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

    public SimulationEvent(long timestampMillis, SimulationEventType type, String message, Packet packet,
                           Endpoint endpoint, TcpState tcpState) {
        this.timestampMillis = timestampMillis;
        this.type = type;
        this.message = message;
        this.packet = packet;
        this.endpoint = endpoint;
        this.tcpState = tcpState;
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
}
