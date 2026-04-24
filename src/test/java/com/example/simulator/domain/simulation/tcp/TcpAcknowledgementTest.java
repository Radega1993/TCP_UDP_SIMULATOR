package com.example.simulator.domain.simulation.tcp;

import com.example.simulator.domain.network.NetworkConditions;
import com.example.simulator.domain.simulation.Packet;
import com.example.simulator.domain.simulation.SimulationResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.network;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.run;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.serverAckPackets;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.tcpScenario;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TcpAcknowledgementTest {
    @Test
    void shouldGenerateCumulativeAckForInOrderSegments() {
        SimulationResult result = run(tcpScenario("ABCDEFGH", 2, 8, 8, NetworkConditions.none()));

        List<Integer> ackValues = serverAckPackets(result).stream()
                .map(Packet::getAck)
                .filter(ack -> ack > 1)
                .toList();

        assertTrue(ackValues.containsAll(List.of(3, 5, 7, 9)));
    }

    @Test
    void shouldNotAdvanceAckBeyondMissingSegment() {
        SimulationResult result = run(tcpScenario("ABCDEFGH", 2, 8, 8,
                network(0.0, 900L, 0L, 0.0, 0.0, 0, Set.of(2))));

        List<Integer> ackValues = serverAckPackets(result).stream()
                .map(Packet::getAck)
                .filter(ack -> ack > 1)
                .toList();

        assertTrue(ackValues.contains(3));
        assertTrue(ackValues.stream().filter(ack -> ack == 3).count() >= 2);
        assertTrue(ackValues.contains(9));
    }

    @Test
    void shouldDetectDuplicateAckInEducationalLog() {
        SimulationResult result = run(tcpScenario("ABCDEFGHIJKLMNOP", 2, 16, 16,
                network(0.0, 850L, 0L, 0.0, 0.0, 0, Set.of(2))));

        assertTrue(result.getLogEntries().stream().anyMatch(entry -> entry.contains("Duplicate ACK detectado")));
    }

    @Test
    void shouldAllowOneAckToConfirmMultipleBufferedSegmentsAfterGapIsFilled() {
        SimulationResult result = run(tcpScenario("ABCDEFGH", 2, 8, 8,
                network(0.0, 900L, 0L, 0.0, 0.0, 0, Set.of(2))));

        assertTrue(serverAckPackets(result).stream().anyMatch(packet -> packet.getAck() == 9));
        assertEquals("ABCDEFGH", result.getFinalSnapshot().getDeliveredMessage());
    }
}
