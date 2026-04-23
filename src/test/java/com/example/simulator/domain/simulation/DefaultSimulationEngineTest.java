package com.example.simulator.domain.simulation;

import com.example.simulator.domain.network.NetworkConditions;
import com.example.simulator.domain.protocol.PacketKind;
import com.example.simulator.domain.protocol.ProtocolType;
import com.example.simulator.domain.protocol.tcp.TcpState;
import com.example.simulator.domain.scenario.Scenario;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
                new NetworkConditions(0.0, Set.of(1)),
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
}
