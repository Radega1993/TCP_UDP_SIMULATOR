package com.example.simulator.domain.simulation.tcp;

import com.example.simulator.domain.network.Endpoint;
import com.example.simulator.domain.protocol.PacketKind;
import com.example.simulator.domain.protocol.tcp.TcpState;
import com.example.simulator.domain.simulation.SimulationEvent;
import com.example.simulator.domain.simulation.SimulationResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.createdPackets;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.firstIndex;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.run;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.tcpScenario;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TcpConnectionCloseTest {
    @Test
    void shouldCloseOnlyAfterAllDataIsAcknowledged() {
        SimulationResult result = run(tcpScenario("ABCDEFGH", 2));

        long firstFinTime = result.getEvents().stream()
                .filter(event -> event.getPacket() != null && event.getPacket().getKind() == PacketKind.FIN)
                .mapToLong(SimulationEvent::getTimestampMillis)
                .min()
                .orElseThrow();
        long finalAckForDataTime = createdPackets(result, PacketKind.ACK).stream()
                .filter(packet -> packet.getFrom() == Endpoint.SERVER)
                .filter(packet -> packet.getAck() == 9)
                .findFirst()
                .map(packet -> result.getEvents().stream()
                        .filter(event -> event.getPacket() == packet)
                        .findFirst()
                        .map(SimulationEvent::getTimestampMillis)
                        .orElse(0L))
                .orElse(0L);

        assertTrue(finalAckForDataTime <= firstFinTime);
    }

    @Test
    void shouldEmitFinAckFinAckCloseSequence() {
        SimulationResult result = run(tcpScenario("AB", 2));

        List<PacketKind> closingPackets = createdPackets(result, PacketKind.FIN).stream()
                .map(packet -> packet.getKind())
                .toList();

        assertEquals(2, closingPackets.size());
        assertTrue(createdPackets(result, PacketKind.ACK).stream()
                .anyMatch(packet -> packet.getFrom() == Endpoint.SERVER && packet.getAck() > 1));
        assertTrue(createdPackets(result, PacketKind.ACK).stream()
                .anyMatch(packet -> packet.getFrom() == Endpoint.CLIENT && packet.getAck() == 2));
    }

    @Test
    void shouldEndBothEndpointsClosedAndNoBytesInFlight() {
        SimulationResult result = run(tcpScenario("ABCD", 2));

        assertEquals(TcpState.CLOSED, result.getFinalSnapshot().getClientState());
        assertEquals(TcpState.CLOSED, result.getFinalSnapshot().getServerState());
        assertEquals(0, result.getFinalSnapshot().getFlowControlSnapshot().getBytesInFlight());
    }

    @Test
    void shouldNotSendDataAfterFirstFin() {
        SimulationResult result = run(tcpScenario("ABCDEFGH", 2));

        int firstFinIndex = firstIndex(result, event -> event.getPacket() != null
                && event.getPacket().getKind() == PacketKind.FIN);

        assertTrue(result.getEvents().subList(firstFinIndex, result.getEvents().size()).stream()
                .noneMatch(event -> event.getPacket() != null && event.getPacket().getKind() == PacketKind.DATA));
    }
}
