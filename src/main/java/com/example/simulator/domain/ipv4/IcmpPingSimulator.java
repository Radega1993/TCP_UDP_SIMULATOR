package com.example.simulator.domain.ipv4;

import java.util.ArrayList;
import java.util.List;

public final class IcmpPingSimulator {
    private IcmpPingSimulator() {
    }

    public static IcmpPingResult simulate(Ipv4SubnetResult subnet, String gatewayIp, int initialTtl) {
        if (initialTtl < 1 || initialTtl > 255) {
            throw new IllegalArgumentException("El TTL debe estar entre 1 y 255.");
        }
        String gateway = gatewayIp == null ? "" : gatewayIp.trim();
        boolean hasGateway = !gateway.isBlank();
        boolean gatewayReachable = hasGateway
                && Ipv4SubnetCalculator.calculate(subnet.sourceIp(), gateway, subnet.cidr()).sameNetwork();
        List<IcmpPingEvent> events = new ArrayList<>();

        if (subnet.sameNetwork()) {
            events.add(event(1, IcmpMessageType.ECHO_REQUEST, "Cliente", subnet.sourceIp(), "Servidor", subnet.destinationIp(),
                    initialTtl, initialTtl, "El cliente pregunta si el destino responde."));
            events.add(event(2, IcmpMessageType.ECHO_REPLY, "Servidor", subnet.destinationIp(), "Cliente", subnet.sourceIp(),
                    64, 64, "El servidor responde al origen."));
            return new IcmpPingResult(
                    IcmpMessageType.ECHO_REPLY,
                    "Reply from " + subnet.destinationIp() + " time=8ms TTL=64",
                    "Ping correcto: el destino está en la misma red y responde.",
                    true,
                    List.copyOf(events)
            );
        }

        if (!hasGateway) {
            events.add(event(1, IcmpMessageType.ECHO_REQUEST, "Cliente", subnet.sourceIp(), "Red destino", subnet.destinationIp(),
                    initialTtl, initialTtl, "El cliente intenta salir de la LAN."));
            events.add(event(2, IcmpMessageType.DESTINATION_UNREACHABLE, "Cliente", subnet.sourceIp(), "Cliente", subnet.sourceIp(),
                    initialTtl, initialTtl, "No hay gateway configurado para llegar a otra red."));
            return unreachable(subnet.sourceIp(), "Destination host unreachable: falta gateway.", events);
        }

        if (!gatewayReachable) {
            events.add(event(1, IcmpMessageType.ECHO_REQUEST, "Cliente", subnet.sourceIp(), "Gateway", gateway,
                    initialTtl, initialTtl, "El cliente intenta entregar el ping al gateway."));
            events.add(event(2, IcmpMessageType.DESTINATION_UNREACHABLE, "Cliente", subnet.sourceIp(), "Cliente", subnet.sourceIp(),
                    initialTtl, initialTtl, "El gateway no pertenece a la red origen."));
            return unreachable(subnet.sourceIp(), "Destination host unreachable: gateway no alcanzable.", events);
        }

        events.add(event(1, IcmpMessageType.ECHO_REQUEST, "Cliente", subnet.sourceIp(), "Gateway", gateway,
                initialTtl, initialTtl, "El cliente entrega el ping a su puerta de enlace."));
        int ttlAfterRouter = Math.max(0, initialTtl - 1);
        events.add(event(2, IcmpMessageType.ECHO_REQUEST, "Router", gateway, "Servidor", subnet.destinationIp(),
                initialTtl, ttlAfterRouter, "El router decrementa TTL y reenvía hacia la red destino."));

        if (ttlAfterRouter == 0) {
            events.add(event(3, IcmpMessageType.TTL_EXCEEDED, "Router", gateway, "Cliente", subnet.sourceIp(),
                    64, 64, "El router descarta el paquete y avisa al origen."));
            return new IcmpPingResult(
                    IcmpMessageType.TTL_EXCEEDED,
                    "TTL expired in transit from " + gateway,
                    "TTL exceeded: el paquete murió en el router antes de llegar al destino.",
                    false,
                    List.copyOf(events)
            );
        }

        events.add(event(3, IcmpMessageType.ECHO_REPLY, "Servidor", subnet.destinationIp(), "Cliente", subnet.sourceIp(),
                64, 63, "El servidor devuelve una respuesta ICMP."));
        return new IcmpPingResult(
                IcmpMessageType.ECHO_REPLY,
                "Reply from " + subnet.destinationIp() + " time=20ms TTL=63",
                "Ping correcto: el gateway encaminó la solicitud y volvió un Echo Reply.",
                true,
                List.copyOf(events)
        );
    }

    private static IcmpPingResult unreachable(String sourceIp, String summary, List<IcmpPingEvent> events) {
        return new IcmpPingResult(
                IcmpMessageType.DESTINATION_UNREACHABLE,
                "Destination unreachable from " + sourceIp,
                summary,
                false,
                List.copyOf(events)
        );
    }

    private static IcmpPingEvent event(int step, IcmpMessageType type, String fromDevice, String fromIp,
                                       String toDevice, String toIp, int ttlBefore, int ttlAfter, String description) {
        return new IcmpPingEvent(step, type, fromDevice, fromIp, toDevice, toIp, ttlBefore, ttlAfter, description);
    }
}
