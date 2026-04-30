package com.example.simulator.domain.subnetting;

import java.util.List;

public record SubnetRoutingResult(
        String sourceSubnet,
        String destinationSubnet,
        String sourceHost,
        String destinationHost,
        String sourceGateway,
        String destinationGateway,
        boolean ttlExpired,
        String expiredAt,
        int finalTtl,
        List<SubnetRoutingHop> hops,
        List<SubnetRoutingRoute> routes
) {
}
