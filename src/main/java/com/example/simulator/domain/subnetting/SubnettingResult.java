package com.example.simulator.domain.subnetting;

import java.util.List;

public record SubnettingResult(
        String baseNetwork,
        int baseCidr,
        int newCidr,
        String newMask,
        int borrowedBits,
        int generatedSubnets,
        long hostsPerSubnet,
        long increment,
        List<SubnetBlock> visibleSubnets
) {
}
