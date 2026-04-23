package com.example.simulator.application.dto;

import com.example.simulator.domain.network.NetworkConditions;
import com.example.simulator.domain.protocol.ProtocolType;
import com.example.simulator.domain.scenario.Scenario;

import java.util.List;

public class ComparisonCommand {
    private final String message;
    private final int fragmentSize;
    private final NetworkConditions networkConditions;
    private final int tcpWindowSizeBytes;
    private final int tcpReceiverBufferBytes;

    public ComparisonCommand(String message, int fragmentSize, NetworkConditions networkConditions) {
        this(message, fragmentSize, networkConditions, 24, 24);
    }

    public ComparisonCommand(String message, int fragmentSize, NetworkConditions networkConditions,
                             int tcpWindowSizeBytes, int tcpReceiverBufferBytes) {
        this.message = message;
        this.fragmentSize = Math.max(1, fragmentSize);
        this.networkConditions = networkConditions;
        this.tcpWindowSizeBytes = Math.max(1, tcpWindowSizeBytes);
        this.tcpReceiverBufferBytes = Math.max(1, tcpReceiverBufferBytes);
    }

    public String getMessage() {
        return message;
    }

    public int getFragmentSize() {
        return fragmentSize;
    }

    public NetworkConditions getNetworkConditions() {
        return networkConditions;
    }

    public int getTcpWindowSizeBytes() {
        return tcpWindowSizeBytes;
    }

    public int getTcpReceiverBufferBytes() {
        return tcpReceiverBufferBytes;
    }

    public SimulationCommand toSimulationCommand(ProtocolType protocolType) {
        return new SimulationCommand(
                protocolType,
                message,
                fragmentSize,
                networkConditions.getPacketLossRate(),
                networkConditions.getBaseLatencyMillis(),
                networkConditions.getJitterMillis(),
                networkConditions.getDuplicationRate(),
                networkConditions.getReorderingRate(),
                networkConditions.getBandwidthPacketsPerSecond(),
                tcpWindowSizeBytes,
                tcpReceiverBufferBytes
        );
    }

    public Scenario toScenario(ProtocolType protocolType) {
        return new Scenario(
                "compare-" + protocolType.name().toLowerCase(),
                "Comparación " + protocolType,
                protocolType,
                message,
                fragmentSize,
                tcpWindowSizeBytes,
                tcpReceiverBufferBytes,
                networkConditions,
                List.of("comparison", "protocol-" + protocolType.name().toLowerCase())
        );
    }

    public static ComparisonCommand fromScenario(Scenario scenario) {
        return new ComparisonCommand(
                scenario.getMessage(),
                scenario.getFragmentSize(),
                scenario.getNetworkConditions(),
                scenario.getTcpWindowSizeBytes(),
                scenario.getTcpReceiverBufferBytes()
        );
    }
}
