package com.example.simulator.domain.simulation;

import com.example.simulator.domain.network.NetworkConditions;
import com.example.simulator.domain.protocol.PacketKind;
import com.example.simulator.domain.protocol.ProtocolType;
import com.example.simulator.domain.protocol.tcp.CongestionPhase;
import com.example.simulator.domain.protocol.tcp.TcpState;
import com.example.simulator.domain.scenario.Scenario;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultSimulationEngineTest {
    private final DefaultSimulationEngine engine = new DefaultSimulationEngine();

    @Test
    void runsTcpScenarioWithoutJavaFx() {
        Scenario scenario = new Scenario(
                "tcp-test",
                "TCP test",
                ProtocolType.TCP,
                "HOLA",
                2,
                NetworkConditions.none(),
                List.of("tcp")
        );

        SimulationResult result = engine.run(scenario);

        assertTrue(result.getEvents().stream().anyMatch(event -> event.getType() == SimulationEventType.SCENARIO_COMPLETED));
        assertEquals(TcpState.CLOSED, result.getFinalSnapshot().getClientState());
        assertEquals(TcpState.CLOSED, result.getFinalSnapshot().getServerState());
        assertEquals("HOLA", result.getFinalSnapshot().getDeliveredMessage());
    }

    @Test
    void retransmitsWhenConfiguredPacketIsLost() {
        Scenario scenario = new Scenario(
                "tcp-retry",
                "TCP retry",
                ProtocolType.TCP,
                "ABCD",
                2,
                new NetworkConditions(0.0, 1200L, 0L, 0.0, 0.0, 0, Set.of(1)),
                List.of("tcp")
        );

        SimulationResult result = engine.run(scenario);

        assertTrue(result.getEvents().stream().anyMatch(event ->
                event.getPacket() != null && event.getPacket().getKind() == PacketKind.RETRANSMISSION));
        assertEquals("ABCD", result.getFinalSnapshot().getDeliveredMessage());
    }

    @Test
    void tcpScenarioContainsHandshakeAndAckEvents() {
        Scenario scenario = new Scenario(
                "tcp-handshake",
                "TCP class demo",
                ProtocolType.TCP,
                "HOLA",
                4,
                NetworkConditions.none(),
                List.of("tcp")
        );

        SimulationResult result = engine.run(scenario);

        assertTrue(result.getEvents().stream().anyMatch(event ->
                event.getPacket() != null && event.getPacket().getKind() == PacketKind.SYN));
        assertTrue(result.getEvents().stream().anyMatch(event ->
                event.getPacket() != null && event.getPacket().getKind() == PacketKind.SYN_ACK));
        assertTrue(result.getEvents().stream().anyMatch(event ->
                event.getPacket() != null && event.getPacket().getKind() == PacketKind.ACK));
    }

    @Test
    void appliesConfiguredLatencyToPacketTimeline() {
        Scenario scenario = new Scenario(
                "udp-latency",
                "UDP latency",
                ProtocolType.UDP,
                "ABC",
                3,
                new NetworkConditions(0.0, 500L, 0L, 0.0, 0.0, 0, Set.of()),
                List.of("udp")
        );

        SimulationResult result = engine.run(scenario);

        SimulationEvent created = result.getEvents().stream()
                .filter(event -> event.getType() == SimulationEventType.PACKET_CREATED)
                .findFirst()
                .orElseThrow();
        SimulationEvent delivered = result.getEvents().stream()
                .filter(event -> event.getType() == SimulationEventType.PACKET_DELIVERED)
                .findFirst()
                .orElseThrow();

        assertEquals(500L, delivered.getTimestampMillis() - created.getTimestampMillis());
    }

    @Test
    void duplicatesUdpPacketsWhenConfigured() {
        Scenario scenario = new Scenario(
                "udp-dup",
                "UDP duplication",
                ProtocolType.UDP,
                "ABC",
                3,
                new NetworkConditions(0.0, 600L, 0L, 1.0, 0.0, 0, Set.of()),
                List.of("udp")
        );

        SimulationResult result = engine.run(scenario);

        long createdCount = result.getEvents().stream()
                .filter(event -> event.getType() == SimulationEventType.PACKET_CREATED)
                .count();
        long deliveredCount = result.getEvents().stream()
                .filter(event -> event.getType() == SimulationEventType.PACKET_DELIVERED)
                .count();

        assertEquals(2L, createdCount);
        assertEquals(2L, deliveredCount);
    }

    @Test
    void reordersUdpDeliveriesWhenConfigured() {
        Scenario scenario = new Scenario(
                "udp-reorder",
                "UDP reorder",
                ProtocolType.UDP,
                "ABCDEFGHI",
                3,
                new NetworkConditions(0.0, 1200L, 0L, 0.0, 1.0, 0, Set.of()),
                List.of("udp")
        );

        SimulationResult result = engine.run(scenario);

        List<Integer> deliveredSeq = result.getEvents().stream()
                .filter(event -> event.getType() == SimulationEventType.PACKET_DELIVERED)
                .map(event -> event.getPacket().getSeq())
                .toList();

        assertFalse(deliveredSeq.isEmpty());
        assertEquals(List.of(2, 1, 3), deliveredSeq);
    }

    @Test
    void tcpUsesSlidingWindowAndEmitsFlowControlSnapshots() {
        Scenario scenario = new Scenario(
                "tcp-window",
                "TCP window",
                ProtocolType.TCP,
                "ABCDEFGHIJKL",
                2,
                6,
                6,
                NetworkConditions.none(),
                List.of("tcp")
        );

        SimulationResult result = engine.run(scenario);

        long sentBeforeFirstAck = result.getEvents().stream()
                .takeWhile(event -> !(event.getPacket() != null
                        && event.getPacket().getKind() == PacketKind.ACK
                        && event.getPacket().getFrom() == com.example.simulator.domain.network.Endpoint.SERVER))
                .filter(event -> event.getType() == SimulationEventType.PACKET_CREATED)
                .filter(event -> event.getPacket().getKind() == PacketKind.DATA)
                .count();

        assertTrue(sentBeforeFirstAck >= 3L);
        assertTrue(result.getEvents().stream().anyMatch(event -> event.getType() == SimulationEventType.FLOW_CONTROL_UPDATED));
        assertEquals(0, result.getFinalSnapshot().getFlowControlSnapshot().getBytesInFlight());
        assertEquals("ABCDEFGHIJKL", result.getFinalSnapshot().getDeliveredMessage());
    }

    @Test
    void tcpEmitsCumulativeAcknowledgements() {
        Scenario scenario = new Scenario(
                "tcp-cumulative-ack",
                "TCP cumulative ack",
                ProtocolType.TCP,
                "ABCDEFGH",
                2,
                6,
                6,
                NetworkConditions.none(),
                List.of("tcp")
        );

        SimulationResult result = engine.run(scenario);

        List<Integer> ackValues = result.getEvents().stream()
                .filter(event -> event.getType() == SimulationEventType.PACKET_CREATED)
                .filter(event -> event.getPacket() != null)
                .filter(event -> event.getPacket().getKind() == PacketKind.ACK)
                .filter(event -> event.getPacket().getFrom() == com.example.simulator.domain.network.Endpoint.SERVER)
                .map(event -> event.getPacket().getAck())
                .filter(ack -> ack > 1)
                .toList();

        assertTrue(ackValues.contains(3));
        assertTrue(ackValues.contains(5));
        assertTrue(ackValues.contains(7));
        assertTrue(ackValues.contains(9));
    }

    @Test
    void congestionFallsToSlowStartOnTimeout() {
        Scenario scenario = new Scenario(
                "tcp-timeout",
                "TCP timeout",
                ProtocolType.TCP,
                "ABCDEFGHIJKL",
                2,
                4,
                4,
                new NetworkConditions(0.0, 900L, 0L, 0.0, 0.0, 0, Set.of(2)),
                List.of("tcp", "congestion")
        );

        SimulationResult result = engine.run(scenario);

        assertTrue(result.getEvents().stream().anyMatch(event ->
                event.getType() == SimulationEventType.CONGESTION_UPDATED
                        && event.getCongestionSnapshot() != null
                        && "Timeout y caída de cwnd".equals(event.getCongestionSnapshot().getReason())));
        assertEquals(CongestionPhase.CONGESTION_AVOIDANCE, result.getFinalSnapshot().getCongestionSnapshot().getPhase());
    }

    @Test
    void congestionTriggersFastRetransmitOnDuplicateAck() {
        Scenario scenario = new Scenario(
                "tcp-fast-retransmit",
                "TCP duplicate ack",
                ProtocolType.TCP,
                "ABCDEFGHIJKLMNOP",
                2,
                16,
                16,
                new NetworkConditions(0.0, 850L, 0L, 0.0, 0.0, 0, Set.of(2)),
                List.of("tcp", "congestion")
        );

        SimulationResult result = engine.run(scenario);

        assertTrue(result.getEvents().stream().anyMatch(event ->
                event.getType() == SimulationEventType.CONGESTION_UPDATED
                        && event.getCongestionSnapshot() != null
                        && event.getCongestionSnapshot().getReason().contains("Duplicate ACK")));
        assertTrue(result.getLogEntries().stream().anyMatch(log -> log.contains("Fast retransmit activado")));
        assertTrue(result.getEvents().stream().anyMatch(event ->
                event.getPacket() != null && event.getPacket().getKind() == PacketKind.RETRANSMISSION));
    }
}
