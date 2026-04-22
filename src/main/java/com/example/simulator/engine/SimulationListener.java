package com.example.simulator.engine;

import com.example.simulator.model.Endpoint;
import com.example.simulator.model.Packet;
import com.example.simulator.model.TcpState;

public interface SimulationListener {
    void onLog(String message);
    void onPacketCreated(Packet packet);
    void onPacketDelivered(Packet packet);
    void onPacketLost(Packet packet);
    void onTcpStateChanged(Endpoint endpoint, TcpState newState);
    void onMessageDelivered(String message);
    void onScenarioCompleted();
    void onReset();
}
