package com.example.simulator.application.dto;

import com.example.simulator.domain.network.NetworkConditions;
import com.example.simulator.domain.protocol.ProtocolType;
import com.example.simulator.domain.scenario.Scenario;

import java.util.List;

public class SimulationCommand {
    private final ProtocolType protocolType;
    private final String message;
    private final int fragmentSize;
    private final double packetLossRate;

    public SimulationCommand(ProtocolType protocolType, String message, int fragmentSize, double packetLossRate) {
        this.protocolType = protocolType;
        this.message = message;
        this.fragmentSize = fragmentSize;
        this.packetLossRate = packetLossRate;
    }

    public ProtocolType getProtocolType() {
        return protocolType;
    }

    public String getMessage() {
        return message;
    }

    public int getFragmentSize() {
        return fragmentSize;
    }

    public double getPacketLossRate() {
        return packetLossRate;
    }

    public Scenario toAdHocScenario() {
        return new Scenario(
                "ad-hoc",
                "Escenario interactivo",
                protocolType,
                message,
                fragmentSize,
                new NetworkConditions(packetLossRate, java.util.Set.of()),
                List.of(protocolType.name().toLowerCase())
        );
    }
}
