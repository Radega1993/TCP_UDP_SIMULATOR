package com.example.simulator.application.dto;

import com.example.simulator.domain.simulation.SimulationResult;

public class ProtocolComparisonResult {
    private final SimulationResult tcpResult;
    private final SimulationResult udpResult;
    private final ComparisonSummary summary;

    public ProtocolComparisonResult(SimulationResult tcpResult, SimulationResult udpResult, ComparisonSummary summary) {
        this.tcpResult = tcpResult;
        this.udpResult = udpResult;
        this.summary = summary;
    }

    public SimulationResult getTcpResult() {
        return tcpResult;
    }

    public SimulationResult getUdpResult() {
        return udpResult;
    }

    public ComparisonSummary getSummary() {
        return summary;
    }
}
