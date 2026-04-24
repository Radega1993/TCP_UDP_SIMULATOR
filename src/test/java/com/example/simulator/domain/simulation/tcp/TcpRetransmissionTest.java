package com.example.simulator.domain.simulation.tcp;

import com.example.simulator.domain.protocol.PacketKind;
import com.example.simulator.domain.simulation.Packet;
import com.example.simulator.domain.simulation.SimulationEventType;
import com.example.simulator.domain.simulation.SimulationResult;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.createdPackets;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.lostPackets;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.network;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.run;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.tcpScenario;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TcpRetransmissionTest {
    @Test
    void shouldNotRetransmitWhenThereIsNoLoss() {
        SimulationResult result = run(tcpScenario("ABCDEFGH", 2));

        assertTrue(createdPackets(result, PacketKind.RETRANSMISSION).isEmpty());
        assertTrue(result.getEvents().stream().noneMatch(event -> event.getType() == SimulationEventType.PACKET_LOST));
    }

    @Test
    void shouldRetransmitLostDataSegmentAfterTimeout() {
        SimulationResult result = run(tcpScenario("ABCD", 2, 4, 4,
                network(0.0, 900L, 0L, 0.0, 0.0, 0, Set.of(1))));

        Packet lost = lostPackets(result, PacketKind.DATA).get(0);
        Packet retransmission = createdPackets(result, PacketKind.RETRANSMISSION).get(0);

        assertEquals(lost.getSeq(), retransmission.getSeq());
        assertEquals(lost.getPayload(), retransmission.getPayload());
        assertTrue(retransmission.isRetransmission());
        assertEquals("ABCD", result.getFinalSnapshot().getDeliveredMessage());
    }

    @Test
    void shouldLogPacketLostTimeoutAndRetransmissionInLogicalOrder() {
        SimulationResult result = run(tcpScenario("ABCD", 2, 4, 4,
                network(0.0, 900L, 0L, 0.0, 0.0, 0, Set.of(1))));

        int lostIndex = TcpSimulationTestSupport.firstIndex(result, event ->
                event.getType() == SimulationEventType.PACKET_LOST);
        int timeoutIndex = TcpSimulationTestSupport.firstIndex(result, event ->
                event.getMessage() != null && event.getMessage().contains("Timeout en segmento"));
        int retransmissionIndex = TcpSimulationTestSupport.firstIndex(result, event ->
                event.getPacket() != null && event.getPacket().getKind() == PacketKind.RETRANSMISSION);

        assertTrue(lostIndex < timeoutIndex);
        assertTrue(timeoutIndex < retransmissionIndex);
    }

    @Test
    void shouldNotRetransmitAlreadyAcknowledgedSegments() {
        SimulationResult result = run(tcpScenario("ABCDEFGH", 2, 8, 8,
                network(0.0, 900L, 0L, 0.0, 0.0, 0, Set.of(3))));

        assertFalse(createdPackets(result, PacketKind.RETRANSMISSION).stream()
                .anyMatch(packet -> packet.getSeq() == 1 || packet.getSeq() == 3));
    }
}
