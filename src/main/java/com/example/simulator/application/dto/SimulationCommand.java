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
    private final long baseLatencyMillis;
    private final long jitterMillis;
    private final double duplicationRate;
    private final double reorderingRate;
    private final int bandwidthPacketsPerSecond;
    private final int tcpWindowSizeBytes;
    private final int tcpReceiverBufferBytes;

    public SimulationCommand(ProtocolType protocolType, String message, int fragmentSize, double packetLossRate) {
        this(protocolType, message, fragmentSize, packetLossRate, 1200L, 0L, 0.0, 0.0, 0, 24, 24);
    }

    public SimulationCommand(ProtocolType protocolType, String message, int fragmentSize, double packetLossRate,
                             long baseLatencyMillis, long jitterMillis, double duplicationRate,
                             double reorderingRate, int bandwidthPacketsPerSecond) {
        this(protocolType, message, fragmentSize, packetLossRate, baseLatencyMillis, jitterMillis,
                duplicationRate, reorderingRate, bandwidthPacketsPerSecond, 24, 24);
    }

    public SimulationCommand(ProtocolType protocolType, String message, int fragmentSize, double packetLossRate,
                             long baseLatencyMillis, long jitterMillis, double duplicationRate,
                             double reorderingRate, int bandwidthPacketsPerSecond,
                             int tcpWindowSizeBytes, int tcpReceiverBufferBytes) {
        this.protocolType = protocolType;
        this.message = message;
        this.fragmentSize = fragmentSize;
        this.packetLossRate = packetLossRate;
        this.baseLatencyMillis = baseLatencyMillis;
        this.jitterMillis = jitterMillis;
        this.duplicationRate = duplicationRate;
        this.reorderingRate = reorderingRate;
        this.bandwidthPacketsPerSecond = bandwidthPacketsPerSecond;
        this.tcpWindowSizeBytes = Math.max(1, tcpWindowSizeBytes);
        this.tcpReceiverBufferBytes = Math.max(1, tcpReceiverBufferBytes);
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

    public long getBaseLatencyMillis() {
        return baseLatencyMillis;
    }

    public long getJitterMillis() {
        return jitterMillis;
    }

    public double getDuplicationRate() {
        return duplicationRate;
    }

    public double getReorderingRate() {
        return reorderingRate;
    }

    public int getBandwidthPacketsPerSecond() {
        return bandwidthPacketsPerSecond;
    }

    public int getTcpWindowSizeBytes() {
        return tcpWindowSizeBytes;
    }

    public int getTcpReceiverBufferBytes() {
        return tcpReceiverBufferBytes;
    }

    public Scenario toAdHocScenario() {
        return new Scenario(
                "ad-hoc",
                "Escenario interactivo",
                protocolType,
                message,
                fragmentSize,
                tcpWindowSizeBytes,
                tcpReceiverBufferBytes,
                new NetworkConditions(packetLossRate, baseLatencyMillis, jitterMillis, duplicationRate,
                        reorderingRate, bandwidthPacketsPerSecond, java.util.Set.of()),
                List.of(protocolType.name().toLowerCase())
        );
    }
}
