package com.example.simulator.presentation.playback;

import com.example.simulator.domain.network.Endpoint;
import com.example.simulator.domain.protocol.tcp.TcpState;
import com.example.simulator.domain.simulation.CongestionSnapshot;
import com.example.simulator.domain.simulation.FlowControlSnapshot;
import com.example.simulator.domain.simulation.Packet;

public interface SimulationPlaybackListener {
    void onLog(String message);
    void onPacketCreated(Packet packet);
    void onPacketDelivered(Packet packet);
    void onPacketLost(Packet packet);
    void onTcpStateChanged(Endpoint endpoint, TcpState newState);
    void onMessageDelivered(String message);
    default void onFlowControlUpdated(FlowControlSnapshot snapshot) {
    }
    default void onCongestionUpdated(CongestionSnapshot snapshot) {
    }
    void onScenarioCompleted();
    void onReset();
}
