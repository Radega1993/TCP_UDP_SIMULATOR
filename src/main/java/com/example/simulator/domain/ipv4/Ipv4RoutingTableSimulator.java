package com.example.simulator.domain.ipv4;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class Ipv4RoutingTableSimulator {
    private Ipv4RoutingTableSimulator() {
    }

    public static Ipv4RouteSelection buildTeachingTable(Ipv4SubnetResult subnet, String gateway) {
        String safeGateway = gateway == null || gateway.isBlank() ? "Sin gateway" : gateway.trim();
        List<Ipv4RouteEntry> routes = new ArrayList<>();

        routes.add(route(subnet.sourceNetwork(), subnet.cidr(), "Directo", "eth0"));
        if (!subnet.destinationNetwork().equals(subnet.sourceNetwork())) {
            routes.add(route(subnet.destinationNetwork(), subnet.cidr(), "Directo", "eth1"));
        }

        if (subnet.cidr() > 16) {
            String aggregate = networkFor(subnet.destinationIp(), 16);
            if (routes.stream().noneMatch(route -> route.destination().equals(aggregate) && route.cidr() == 16)) {
                routes.add(route(aggregate, 16, safeGateway, "eth0"));
            }
        }

        routes.add(route("0.0.0.0", 0, safeGateway, "eth0"));
        return select(subnet.destinationIp(), routes);
    }

    public static List<Ipv4RouterTable> buildTriangleTables(Ipv4SubnetResult subnet, String gateway) {
        String safeGateway = gateway == null || gateway.isBlank() ? "Sin gateway" : gateway.trim();
        String destinationAggregate = subnet.cidr() > 16 ? networkFor(subnet.destinationIp(), 16) : subnet.destinationNetwork();
        String sourceAggregate = subnet.cidr() > 16 ? networkFor(subnet.sourceIp(), 16) : subnet.sourceNetwork();

        List<Ipv4RouteEntry> routerA = distinct(List.of(
                route(subnet.sourceNetwork(), subnet.cidr(), "Directo", "eth0"),
                route(subnet.destinationNetwork(), subnet.cidr(), "10.0.13.2", "eth2"),
                route(destinationAggregate, Math.min(16, subnet.cidr()), "10.0.12.2", "eth1"),
                route("0.0.0.0", 0, safeGateway, "eth0")
        ));

        List<Ipv4RouteEntry> routerB = distinct(List.of(
                route(sourceAggregate, Math.min(16, subnet.cidr()), "10.0.12.1", "eth1"),
                route(destinationAggregate, Math.min(16, subnet.cidr()), "10.0.23.2", "eth2"),
                route("172.16.0.0", 12, "Directo", "eth0"),
                route("0.0.0.0", 0, "10.0.12.1", "eth1")
        ));

        List<Ipv4RouteEntry> routerC = distinct(List.of(
                route(subnet.destinationNetwork(), subnet.cidr(), "Directo", "eth0"),
                route(destinationAggregate, Math.min(16, subnet.cidr()), "Directo", "eth0"),
                route(subnet.sourceNetwork(), subnet.cidr(), "10.0.13.1", "eth1"),
                route("0.0.0.0", 0, "10.0.23.1", "eth2")
        ));

        return List.of(
                new Ipv4RouterTable("R1", "Router A", "Gateway de la red origen", select(subnet.destinationIp(), routerA)),
                new Ipv4RouterTable("R2", "Router B", "Router intermedio con rutas agregadas", select(subnet.destinationIp(), routerB)),
                new Ipv4RouterTable("R3", "Router C", "Router conectado a la red destino", select(subnet.destinationIp(), routerC))
        );
    }

    public static Ipv4RouteSelection select(String destinationIp, List<Ipv4RouteEntry> table) {
        long destination = Ipv4SubnetCalculator.parseIpv4(destinationIp);
        Ipv4RouteEntry selected = table.stream()
                .filter(route -> matches(destination, route))
                .max(Comparator.comparingInt(Ipv4RouteEntry::cidr))
                .orElseThrow(() -> new IllegalArgumentException("La tabla de rutas no tiene ruta por defecto ni coincidencias."));

        List<Ipv4RouteEntry> renderedRoutes = table.stream()
                .map(route -> route.withState(matches(destination, route), sameRoute(route, selected)))
                .toList();
        long matches = renderedRoutes.stream().filter(Ipv4RouteEntry::matches).count();
        String explanation = "Coinciden " + matches + " rutas, pero gana " + selected.destinationCidr()
                + " porque tiene el prefijo más largo: /" + selected.cidr() + ".";
        return new Ipv4RouteSelection(formatIpv4(destination), selected.withState(true, true), renderedRoutes, explanation);
    }

    private static Ipv4RouteEntry route(String destination, int cidr, String gateway, String networkInterface) {
        return new Ipv4RouteEntry(destination, cidr, decimalMask(cidr), gateway, networkInterface, false, false);
    }

    private static List<Ipv4RouteEntry> distinct(List<Ipv4RouteEntry> routes) {
        List<Ipv4RouteEntry> unique = new ArrayList<>();
        for (Ipv4RouteEntry route : routes) {
            boolean exists = unique.stream().anyMatch(candidate -> candidate.destination().equals(route.destination())
                    && candidate.cidr() == route.cidr()
                    && candidate.gateway().equals(route.gateway())
                    && candidate.networkInterface().equals(route.networkInterface()));
            if (!exists) {
                unique.add(route);
            }
        }
        return unique;
    }

    private static boolean matches(long destination, Ipv4RouteEntry route) {
        long mask = maskFor(route.cidr());
        long network = Ipv4SubnetCalculator.parseIpv4(route.destination());
        return (destination & mask) == (network & mask);
    }

    private static boolean sameRoute(Ipv4RouteEntry left, Ipv4RouteEntry right) {
        return left.destination().equals(right.destination()) && left.cidr() == right.cidr()
                && left.gateway().equals(right.gateway()) && left.networkInterface().equals(right.networkInterface());
    }

    private static String networkFor(String ip, int cidr) {
        long value = Ipv4SubnetCalculator.parseIpv4(ip);
        return formatIpv4(value & maskFor(cidr));
    }

    private static String decimalMask(int cidr) {
        return formatIpv4(maskFor(cidr));
    }

    private static long maskFor(int cidr) {
        if (cidr < 0 || cidr > 32) {
            throw new IllegalArgumentException("El CIDR debe estar entre /0 y /32.");
        }
        if (cidr == 0) {
            return 0;
        }
        return (0xFFFFFFFFL << (32 - cidr)) & 0xFFFFFFFFL;
    }

    private static String formatIpv4(long value) {
        return ((value >>> 24) & 0xFF) + "."
                + ((value >>> 16) & 0xFF) + "."
                + ((value >>> 8) & 0xFF) + "."
                + (value & 0xFF);
    }
}
