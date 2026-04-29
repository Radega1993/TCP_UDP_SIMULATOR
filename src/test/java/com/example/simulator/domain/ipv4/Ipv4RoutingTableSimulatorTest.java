package com.example.simulator.domain.ipv4;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Ipv4RoutingTableSimulatorTest {
    @Test
    void selectsMostSpecificMatchingRoute() {
        Ipv4RouteSelection selection = Ipv4RoutingTableSimulator.select("192.168.2.20", List.of(
                route("0.0.0.0", 0, "192.168.1.1", "eth0"),
                route("192.168.0.0", 16, "192.168.1.254", "eth0"),
                route("192.168.2.0", 24, "Directo", "eth1")
        ));

        assertEquals("192.168.2.0", selection.selectedRoute().destination());
        assertEquals(24, selection.selectedRoute().cidr());
        assertTrue(selection.explanation().contains("/24"));
    }

    @Test
    void fallsBackToDefaultRouteWhenOnlyDefaultMatches() {
        Ipv4RouteSelection selection = Ipv4RoutingTableSimulator.select("10.10.10.10", List.of(
                route("192.168.0.0", 16, "192.168.1.254", "eth0"),
                route("0.0.0.0", 0, "192.168.1.1", "eth0")
        ));

        assertEquals("0.0.0.0", selection.selectedRoute().destination());
        assertEquals(0, selection.selectedRoute().cidr());
    }

    @Test
    void buildsThreeRouterTriangleTables() {
        Ipv4SubnetResult subnet = Ipv4SubnetCalculator.calculate("192.168.1.10", "192.168.2.20", 24);
        List<Ipv4RouterTable> routers = Ipv4RoutingTableSimulator.buildTriangleTables(subnet, "192.168.1.1");

        assertEquals(3, routers.size());
        assertEquals("R1", routers.get(0).id());
        assertEquals("192.168.2.0", routers.get(0).selection().selectedRoute().destination());
        assertEquals("Directo", routers.get(2).selection().selectedRoute().gateway());
    }

    private static Ipv4RouteEntry route(String destination, int cidr, String gateway, String networkInterface) {
        return new Ipv4RouteEntry(destination, cidr, "", gateway, networkInterface, false, false);
    }
}
