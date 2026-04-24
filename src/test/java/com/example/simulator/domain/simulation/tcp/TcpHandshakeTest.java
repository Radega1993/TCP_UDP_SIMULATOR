package com.example.simulator.domain.simulation.tcp;

import com.example.simulator.domain.network.Endpoint;
import com.example.simulator.domain.protocol.PacketKind;
import com.example.simulator.domain.protocol.tcp.TcpState;
import com.example.simulator.domain.simulation.SimulationEventType;
import com.example.simulator.domain.simulation.SimulationResult;
import org.junit.jupiter.api.Test;

import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.createdPackets;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.firstIndex;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.run;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.tcpScenario;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TcpHandshakeTest {
    @Test
    void shouldCompleteThreeWayHandshakeBeforeSendingData() {
        SimulationResult result = run(tcpScenario("HOLA", 2));

        int synIndex = firstIndex(result, event -> event.getPacket() != null
                && event.getPacket().getKind() == PacketKind.SYN);
        int synAckIndex = firstIndex(result, event -> event.getPacket() != null
                && event.getPacket().getKind() == PacketKind.SYN_ACK);
        int finalAckIndex = firstIndex(result, event -> event.getPacket() != null
                && event.getPacket().getKind() == PacketKind.ACK
                && event.getPacket().getFrom() == Endpoint.CLIENT);
        int firstDataIndex = firstIndex(result, event -> event.getPacket() != null
                && event.getPacket().getKind() == PacketKind.DATA);

        assertTrue(synIndex < synAckIndex);
        assertTrue(synAckIndex < finalAckIndex);
        assertTrue(finalAckIndex < firstDataIndex);
    }

    @Test
    void shouldExposeExpectedHandshakeStateTransitions() {
        SimulationResult result = run(tcpScenario("HOLA", 4));

        assertTrue(result.getEvents().stream().anyMatch(event ->
                event.getType() == SimulationEventType.TCP_STATE_CHANGED
                        && event.getEndpoint() == Endpoint.SERVER
                        && event.getTcpState() == TcpState.LISTEN));
        assertTrue(result.getEvents().stream().anyMatch(event ->
                event.getType() == SimulationEventType.TCP_STATE_CHANGED
                        && event.getEndpoint() == Endpoint.CLIENT
                        && event.getTcpState() == TcpState.SYN_SENT));
        assertTrue(result.getEvents().stream().anyMatch(event ->
                event.getType() == SimulationEventType.TCP_STATE_CHANGED
                        && event.getEndpoint() == Endpoint.SERVER
                        && event.getTcpState() == TcpState.SYN_RECEIVED));
        assertTrue(result.getEvents().stream().anyMatch(event ->
                event.getType() == SimulationEventType.TCP_STATE_CHANGED
                        && event.getEndpoint() == Endpoint.CLIENT
                        && event.getTcpState() == TcpState.ESTABLISHED));
        assertTrue(result.getEvents().stream().anyMatch(event ->
                event.getType() == SimulationEventType.TCP_STATE_CHANGED
                        && event.getEndpoint() == Endpoint.SERVER
                        && event.getTcpState() == TcpState.ESTABLISHED));
    }

    @Test
    void shouldGenerateSynSynAckAndAckWithExpectedAckNumbers() {
        SimulationResult result = run(tcpScenario("HOLA", 4));

        assertEquals(1, createdPackets(result, PacketKind.SYN).size());
        assertEquals(1, createdPackets(result, PacketKind.SYN_ACK).size());
        assertEquals(1, createdPackets(result, PacketKind.SYN_ACK).get(0).getAck());
        assertTrue(createdPackets(result, PacketKind.ACK).stream()
                .anyMatch(packet -> packet.getFrom() == Endpoint.CLIENT && packet.getAck() == 1));
    }
}
