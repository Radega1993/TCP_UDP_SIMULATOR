package com.example.simulator.domain.ipv4;

import java.util.List;

public final class ArpSimulator {
    public static final String CLIENT_MAC = "AA:AA:AA:AA:AA:10";
    public static final String GATEWAY_MAC = "BB:BB:BB:BB:BB:01";
    public static final String SERVER_MAC = "CC:CC:CC:CC:CC:20";

    private ArpSimulator() {
    }

    public static ArpSimulationResult simulate(Ipv4SubnetResult subnet, String gateway) {
        String targetIp;
        String targetMac;
        String targetDevice;
        boolean resolvingGateway;

        if (subnet.sameNetwork()) {
            targetIp = subnet.destinationIp();
            targetMac = SERVER_MAC;
            targetDevice = "Servidor destino";
            resolvingGateway = false;
        } else {
            if (gateway == null || gateway.isBlank()) {
                throw new IllegalArgumentException("ARP necesita una IP de gateway para salir de la red local.");
            }
            targetIp = gateway.trim();
            targetMac = GATEWAY_MAC;
            targetDevice = "Router / Gateway";
            resolvingGateway = true;
        }

        String question = "Who has " + targetIp + "? Tell " + subnet.sourceIp();
        return new ArpSimulationResult(
                subnet.sourceIp(),
                CLIENT_MAC,
                targetIp,
                targetMac,
                targetDevice,
                resolvingGateway,
                question,
                List.of(
                        new ArpEvent(1, "ARP Request", question + " · destino MAC FF:FF:FF:FF:FF:FF", true),
                        new ArpEvent(2, "ARP Reply", targetIp + " is at " + targetMac, false),
                        new ArpEvent(3, "Cache ARP", subnet.sourceIp() + " guarda " + targetIp + " → " + targetMac, false)
                ),
                List.of(
                        new ArpCacheEntry(targetIp, targetMac, targetDevice)
                )
        );
    }
}
