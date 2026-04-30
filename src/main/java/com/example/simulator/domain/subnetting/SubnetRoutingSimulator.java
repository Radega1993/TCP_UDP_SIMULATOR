package com.example.simulator.domain.subnetting;

import java.util.ArrayList;
import java.util.List;

public final class SubnetRoutingSimulator {
    private SubnetRoutingSimulator() {
    }

    public static SubnetRoutingResult simulate(VlsmResult vlsm, int initialTtl) {
        if (vlsm == null || vlsm.assignments().size() < 2) {
            throw new IllegalArgumentException("La integración con routing necesita al menos dos subredes VLSM.");
        }
        if (initialTtl < 1 || initialTtl > 255) {
            throw new IllegalArgumentException("El TTL debe estar entre 1 y 255.");
        }

        VlsmAssignment source = vlsm.assignments().get(0);
        VlsmAssignment destination = vlsm.assignments().get(1);
        String sourceSubnet = source.network() + "/" + source.cidr();
        String destinationSubnet = destination.network() + "/" + destination.cidr();
        String sourceHost = source.firstHost();
        String destinationHost = destination.firstHost();
        String sourceGateway = source.lastHost();
        String destinationGateway = destination.lastHost();

        List<RawHop> rawHops = List.of(
                new RawHop("Cliente " + source.name(), sourceSubnet, sourceGateway, "eth0",
                        "El host detecta que el destino no está en su subred y entrega al gateway."),
                new RawHop("Router R1", sourceSubnet, "10.10.10.2", "eth1",
                        "R1 consulta su tabla y sale por el enlace de tránsito hacia R2."),
                new RawHop("Router R2", "10.10.10.0/30", destinationGateway, "eth2",
                        "R2 ya conoce la red destino y reenvía hacia la interfaz conectada a esa subred."),
                new RawHop("Servidor " + destination.name(), destinationSubnet, "Entrega final", "NIC",
                        "El paquete entra en la subred destino y llega al host final.")
        );

        ArrayList<SubnetRoutingHop> hops = new ArrayList<>();
        int ttl = initialTtl;
        boolean expired = false;
        String expiredAt = "";
        for (int index = 0; index < rawHops.size(); index++) {
            RawHop rawHop = rawHops.get(index);
            int before = ttl;
            int after = Math.max(0, ttl - 1);
            boolean delivered = after > 0 || index == rawHops.size() - 1;
            if (after == 0 && index < rawHops.size() - 1) {
                expired = true;
                expiredAt = rawHop.device();
                delivered = false;
            }
            hops.add(new SubnetRoutingHop(
                    index + 1,
                    rawHop.device(),
                    rawHop.network(),
                    rawHop.nextHop(),
                    rawHop.networkInterface(),
                    before,
                    after,
                    delivered,
                    expired ? "TTL llega a 0: el paquete se descarta aquí." : rawHop.detail()
            ));
            ttl = after;
            if (expired) {
                break;
            }
        }

        List<SubnetRoutingRoute> routes = List.of(
                new SubnetRoutingRoute(source.network(), source.cidr(), "Directo", "eth0", false),
                new SubnetRoutingRoute("10.10.10.0", 30, "Directo", "eth1", false),
                new SubnetRoutingRoute(destination.network(), destination.cidr(), "10.10.10.2", "eth1", true),
                new SubnetRoutingRoute("0.0.0.0", 0, sourceGateway, "eth0", false)
        );

        return new SubnetRoutingResult(
                sourceSubnet,
                destinationSubnet,
                sourceHost,
                destinationHost,
                sourceGateway,
                destinationGateway,
                expired,
                expiredAt,
                ttl,
                List.copyOf(hops),
                routes
        );
    }

    private record RawHop(String device, String network, String nextHop, String networkInterface, String detail) {
    }
}
