package com.example.simulator.domain.subnetting;

import java.util.List;

public record VlsmResult(
        String baseNetwork,
        int baseCidr,
        long baseBlockSize,
        long usedAddresses,
        long freeAddresses,
        List<VlsmAssignment> assignments
) {
}
