package com.example.simulator.domain.simulation;

public enum SimulationEventType {
    LOG,
    PACKET_CREATED,
    PACKET_DELIVERED,
    PACKET_LOST,
    TCP_STATE_CHANGED,
    MESSAGE_DELIVERED,
    SCENARIO_COMPLETED
}
