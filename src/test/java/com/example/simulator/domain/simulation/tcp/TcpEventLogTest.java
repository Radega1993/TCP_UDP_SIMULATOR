package com.example.simulator.domain.simulation.tcp;

import com.example.simulator.domain.protocol.PacketKind;
import com.example.simulator.domain.protocol.ProtocolType;
import com.example.simulator.domain.simulation.Packet;
import com.example.simulator.domain.simulation.SimulationEvent;
import com.example.simulator.domain.simulation.SimulationEventType;
import com.example.simulator.domain.simulation.SimulationResult;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.eventsOf;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.network;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.run;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.tcpScenario;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TcpEventLogTest {
    @Test
    void shouldGenerateEducationalEventsForSendDeliveryLossAckRetransmissionAndStateChanges() {
        SimulationResult result = run(tcpScenario("ABCDEFGH", 2, 8, 8,
                network(0.0, 900L, 0L, 0.0, 0.0, 0, Set.of(2))));

        assertFalse(eventsOf(result, SimulationEventType.PACKET_CREATED).isEmpty());
        assertFalse(eventsOf(result, SimulationEventType.PACKET_DELIVERED).isEmpty());
        assertFalse(eventsOf(result, SimulationEventType.PACKET_LOST).isEmpty());
        assertFalse(eventsOf(result, SimulationEventType.TCP_STATE_CHANGED).isEmpty());
        assertFalse(eventsOf(result, SimulationEventType.FLOW_CONTROL_UPDATED).isEmpty());
        assertFalse(eventsOf(result, SimulationEventType.CONGESTION_UPDATED).isEmpty());
        assertFalse(eventsOf(result, SimulationEventType.MESSAGE_DELIVERED).isEmpty());
        assertFalse(eventsOf(result, SimulationEventType.SCENARIO_COMPLETED).isEmpty());
    }

    @Test
    void shouldKeepEventTimestampsMonotonicAfterSchedulerDrain() {
        SimulationResult result = run(tcpScenario("ABCDEFGH", 2));

        long previous = Long.MIN_VALUE;
        for (SimulationEvent event : result.getEvents()) {
            assertTrue(event.getTimestampMillis() >= previous);
            previous = event.getTimestampMillis();
        }
    }

    @Test
    void packetEventsShouldContainDataRequiredByUi() {
        SimulationResult result = run(tcpScenario("ABCD", 2));

        for (SimulationEvent event : result.getEvents()) {
            Packet packet = event.getPacket();
            if (packet == null) {
                continue;
            }
            assertNotNull(packet.getId());
            assertEqualsTcp(packet.getProtocolType());
            assertNotNull(packet.getFrom());
            assertNotNull(packet.getTo());
            assertNotNull(packet.getKind());
            assertNotNull(packet.getPayload());
        }
    }

    @Test
    void shouldLogHandshakeFragmentationRetransmissionAndCloseNarrative() {
        SimulationResult result = run(tcpScenario("ABCD", 2, 4, 4,
                network(0.0, 900L, 0L, 0.0, 0.0, 0, Set.of(1))));

        assertTrue(TcpSimulationTestSupport.logContains(result, "3-way handshake"));
        assertTrue(TcpSimulationTestSupport.logContains(result, "Mensaje fragmentado"));
        assertTrue(TcpSimulationTestSupport.logContains(result, "Retransmisión"));
        assertTrue(TcpSimulationTestSupport.logContains(result, "Conexión cerrada"));
    }

    @Test
    void ackEventsShouldCarryAckAndWindowValues() {
        SimulationResult result = run(tcpScenario("ABCD", 2));

        assertTrue(TcpSimulationTestSupport.serverAckPackets(result).stream()
                .filter(packet -> packet.getKind() == PacketKind.ACK)
                .anyMatch(packet -> packet.getAck() > 1 && packet.getWindowSize() > 0));
    }

    private static void assertEqualsTcp(ProtocolType protocolType) {
        assertTrue(protocolType == ProtocolType.TCP);
    }
}
