package com.example.simulator.domain.ipv4;

public record Ipv4RouteEntry(
        String destination,
        int cidr,
        String decimalMask,
        String gateway,
        String networkInterface,
        boolean matches,
        boolean selected
) {
    public String destinationCidr() {
        return destination + "/" + cidr;
    }

    public Ipv4RouteEntry withState(boolean matches, boolean selected) {
        return new Ipv4RouteEntry(destination, cidr, decimalMask, gateway, networkInterface, matches, selected);
    }
}
