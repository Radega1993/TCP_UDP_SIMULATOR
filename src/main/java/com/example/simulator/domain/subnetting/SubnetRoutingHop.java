package com.example.simulator.domain.subnetting;

public record SubnetRoutingHop(
        int hop,
        String device,
        String network,
        String nextHop,
        String networkInterface,
        int ttlBefore,
        int ttlAfter,
        boolean delivered,
        String detail
) {
}
