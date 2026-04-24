package com.example.simulator.domain.simulation.tcp;

import com.example.simulator.domain.network.Endpoint;
import com.example.simulator.domain.network.NetworkConditions;
import com.example.simulator.domain.protocol.PacketKind;
import com.example.simulator.domain.protocol.ProtocolType;
import com.example.simulator.domain.scenario.Scenario;
import com.example.simulator.domain.simulation.DefaultSimulationEngine;
import com.example.simulator.domain.simulation.Packet;
import com.example.simulator.domain.simulation.SimulationEvent;
import com.example.simulator.domain.simulation.SimulationEventType;
import com.example.simulator.domain.simulation.SimulationResult;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

final class TcpSimulationTestSupport {
    static final DefaultSimulationEngine ENGINE = new DefaultSimulationEngine();

    private TcpSimulationTestSupport() {
    }

    static Scenario tcpScenario(String message, int fragmentSize) {
        return tcpScenario(message, fragmentSize, 24, 24, NetworkConditions.none());
    }

    static Scenario tcpScenario(String message, int fragmentSize, int sendWindowBytes, int receiverBufferBytes,
                                NetworkConditions conditions) {
        return new Scenario(
                "tcp-test",
                "TCP test",
                ProtocolType.TCP,
                message,
                fragmentSize,
                sendWindowBytes,
                receiverBufferBytes,
                conditions,
                List.of("tcp")
        );
    }

    static NetworkConditions network(double lossRate, long latencyMillis, long jitterMillis,
                                     double duplicationRate, double reorderingRate,
                                     int bandwidthPacketsPerSecond, Set<Integer> forcedLossIndexes) {
        return new NetworkConditions(
                lossRate,
                latencyMillis,
                jitterMillis,
                duplicationRate,
                reorderingRate,
                bandwidthPacketsPerSecond,
                forcedLossIndexes
        );
    }

    static SimulationResult run(Scenario scenario) {
        return ENGINE.run(scenario);
    }

    static List<SimulationEvent> eventsOf(SimulationResult result, SimulationEventType type) {
        return result.getEvents().stream()
                .filter(event -> event.getType() == type)
                .toList();
    }

    static List<Packet> createdPackets(SimulationResult result, PacketKind kind) {
        return packets(result, SimulationEventType.PACKET_CREATED, packet -> packet.getKind() == kind);
    }

    static List<Packet> deliveredPackets(SimulationResult result, PacketKind kind) {
        return packets(result, SimulationEventType.PACKET_DELIVERED, packet -> packet.getKind() == kind);
    }

    static List<Packet> lostPackets(SimulationResult result, PacketKind kind) {
        return packets(result, SimulationEventType.PACKET_LOST, packet -> packet.getKind() == kind);
    }

    static List<Packet> serverAckPackets(SimulationResult result) {
        return createdPackets(result, PacketKind.ACK).stream()
                .filter(packet -> packet.getFrom() == Endpoint.SERVER)
                .toList();
    }

    static List<Packet> dataPackets(SimulationResult result) {
        return createdPackets(result, PacketKind.DATA);
    }

    static long firstTimestamp(SimulationResult result, Predicate<SimulationEvent> predicate) {
        return result.getEvents().stream()
                .filter(predicate)
                .map(SimulationEvent::getTimestampMillis)
                .min(Comparator.naturalOrder())
                .orElseThrow();
    }

    static int firstIndex(SimulationResult result, Predicate<SimulationEvent> predicate) {
        List<SimulationEvent> events = result.getEvents();
        for (int i = 0; i < events.size(); i++) {
            if (predicate.test(events.get(i))) {
                return i;
            }
        }
        throw new AssertionError("No matching event found");
    }

    static boolean logContains(SimulationResult result, String text) {
        return result.getLogEntries().stream().anyMatch(entry -> entry.contains(text));
    }

    private static List<Packet> packets(SimulationResult result, SimulationEventType type, Predicate<Packet> predicate) {
        return result.getEvents().stream()
                .filter(event -> event.getType() == type)
                .map(SimulationEvent::getPacket)
                .filter(packet -> packet != null && packet.getProtocolType() == ProtocolType.TCP)
                .filter(predicate)
                .toList();
    }
}
