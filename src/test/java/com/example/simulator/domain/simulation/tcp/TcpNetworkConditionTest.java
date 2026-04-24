package com.example.simulator.domain.simulation.tcp;

import com.example.simulator.domain.protocol.PacketKind;
import com.example.simulator.domain.simulation.Packet;
import com.example.simulator.domain.simulation.SimulationEvent;
import com.example.simulator.domain.simulation.SimulationEventType;
import com.example.simulator.domain.simulation.SimulationResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.deliveredPackets;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.network;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.run;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.tcpScenario;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TcpNetworkConditionTest {
    @Test
    void bandwidthZeroShouldNotPreventSuccessfulDelivery() {
        SimulationResult result = run(tcpScenario("ABCD", 2, 8, 8,
                network(0.0, 500L, 0L, 0.0, 0.0, 0, Set.of())));

        List<SimulationEvent> dataCreated = result.getEvents().stream()
                .filter(event -> event.getType() == SimulationEventType.PACKET_CREATED)
                .filter(event -> event.getPacket() != null && event.getPacket().getKind() == PacketKind.DATA)
                .toList();

        assertEquals(2, dataCreated.size());
        assertTrue(dataCreated.get(1).getTimestampMillis() > dataCreated.get(0).getTimestampMillis());
        assertEquals("ABCD", result.getFinalSnapshot().getDeliveredMessage());
    }

    @Test
    void lowBandwidthShouldSpaceClientToServerPacketsWithoutCorruptingMessage() {
        SimulationResult result = run(tcpScenario("ABCD", 2, 8, 8,
                network(0.0, 500L, 0L, 0.0, 0.0, 1, Set.of())));

        List<SimulationEvent> dataCreated = result.getEvents().stream()
                .filter(event -> event.getType() == SimulationEventType.PACKET_CREATED)
                .filter(event -> event.getPacket() != null && event.getPacket().getKind() == PacketKind.DATA)
                .toList();

        assertEquals(2, dataCreated.size());
        assertTrue(dataCreated.get(1).getTimestampMillis() - dataCreated.get(0).getTimestampMillis() >= 1000L);
        assertEquals("ABCD", result.getFinalSnapshot().getDeliveredMessage());
    }

    @Test
    void shouldRecordReorderingAndTolerateItWithoutCorruptingFinalMessage() {
        SimulationResult result = run(tcpScenario("ABCDEFGHIJKL", 2, 12, 12,
                network(0.0, 1200L, 0L, 0.0, 1.0, 0, Set.of())));

        assertTrue(result.getLogEntries().stream().anyMatch(entry -> entry.contains("fuera de orden")));
        assertEquals("ABCDEFGHIJKL", result.getFinalSnapshot().getDeliveredMessage());
    }

    @Test
    void shouldTolerateDuplicationWithoutDuplicatingDeliveredPayload() {
        SimulationResult result = run(tcpScenario("ABCD", 2, 8, 8,
                network(0.0, 600L, 0L, 1.0, 0.0, 0, Set.of())));

        long deliveredDataEvents = deliveredPackets(result, PacketKind.DATA).size();

        assertTrue(deliveredDataEvents > 2);
        assertEquals("ABCD", result.getFinalSnapshot().getDeliveredMessage());
    }

    @Test
    void shouldUseMinimumLatencyWhenConfiguredLatencyIsZero() {
        SimulationResult result = run(tcpScenario("AB", 2, 8, 8,
                network(0.0, 0L, 0L, 0.0, 0.0, 0, Set.of())));

        SimulationEvent created = result.getEvents().stream()
                .filter(event -> event.getType() == SimulationEventType.PACKET_CREATED)
                .findFirst()
                .orElseThrow();
        SimulationEvent delivered = result.getEvents().stream()
                .filter(event -> event.getType() == SimulationEventType.PACKET_DELIVERED)
                .findFirst()
                .orElseThrow();

        assertEquals(80L, delivered.getTimestampMillis() - created.getTimestampMillis());
    }
}
