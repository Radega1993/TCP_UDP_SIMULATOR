package com.example.simulator.domain.subnetting;

public record VlsmAssignment(
        int order,
        String name,
        int requiredHosts,
        int cidr,
        String mask,
        long blockSize,
        long usableHosts,
        String network,
        String firstHost,
        String lastHost,
        String broadcast,
        String explanation
) {
}
