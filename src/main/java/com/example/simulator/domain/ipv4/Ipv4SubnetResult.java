package com.example.simulator.domain.ipv4;

public record Ipv4SubnetResult(
        String sourceIp,
        String destinationIp,
        int cidr,
        String decimalMask,
        String sourceNetwork,
        String destinationNetwork,
        String sourceBroadcast,
        String destinationBroadcast,
        String sourceHostRange,
        String destinationHostRange,
        boolean sameNetwork,
        boolean needsRouter
) {
}
