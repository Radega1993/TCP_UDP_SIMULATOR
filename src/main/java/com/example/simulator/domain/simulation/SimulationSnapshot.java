package com.example.simulator.domain.simulation;

import com.example.simulator.domain.protocol.ProtocolType;
import com.example.simulator.domain.protocol.tcp.TcpState;

public class SimulationSnapshot {
    private final ProtocolType protocol;
    private final TcpState clientState;
    private final TcpState serverState;
    private final String sentMessage;
    private final String deliveredMessage;
    private final boolean completed;
    private final FlowControlSnapshot flowControlSnapshot;
    private final CongestionSnapshot congestionSnapshot;

    public SimulationSnapshot(ProtocolType protocol, TcpState clientState, TcpState serverState,
                              String sentMessage, String deliveredMessage, boolean completed) {
        this(protocol, clientState, serverState, sentMessage, deliveredMessage, completed, null, null);
    }

    public SimulationSnapshot(ProtocolType protocol, TcpState clientState, TcpState serverState,
                              String sentMessage, String deliveredMessage, boolean completed,
                              FlowControlSnapshot flowControlSnapshot) {
        this(protocol, clientState, serverState, sentMessage, deliveredMessage, completed, flowControlSnapshot, null);
    }

    public SimulationSnapshot(ProtocolType protocol, TcpState clientState, TcpState serverState,
                              String sentMessage, String deliveredMessage, boolean completed,
                              FlowControlSnapshot flowControlSnapshot,
                              CongestionSnapshot congestionSnapshot) {
        this.protocol = protocol;
        this.clientState = clientState;
        this.serverState = serverState;
        this.sentMessage = sentMessage;
        this.deliveredMessage = deliveredMessage;
        this.completed = completed;
        this.flowControlSnapshot = flowControlSnapshot;
        this.congestionSnapshot = congestionSnapshot;
    }

    public ProtocolType getProtocol() {
        return protocol;
    }

    public TcpState getClientState() {
        return clientState;
    }

    public TcpState getServerState() {
        return serverState;
    }

    public String getSentMessage() {
        return sentMessage;
    }

    public String getDeliveredMessage() {
        return deliveredMessage;
    }

    public boolean isCompleted() {
        return completed;
    }

    public FlowControlSnapshot getFlowControlSnapshot() {
        return flowControlSnapshot;
    }

    public CongestionSnapshot getCongestionSnapshot() {
        return congestionSnapshot;
    }
}
