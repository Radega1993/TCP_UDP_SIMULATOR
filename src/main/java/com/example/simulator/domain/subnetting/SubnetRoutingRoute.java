package com.example.simulator.domain.subnetting;

public record SubnetRoutingRoute(
        String destination,
        int cidr,
        String gateway,
        String networkInterface,
        boolean selected
) {
    public String destinationCidr() {
        return destination + "/" + cidr;
    }
}
