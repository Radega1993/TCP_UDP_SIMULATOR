package com.example.simulator.domain.simulation.tcp;

import com.example.simulator.domain.protocol.PacketKind;
import com.example.simulator.domain.scenario.Scenario;
import com.example.simulator.domain.simulation.Packet;
import com.example.simulator.domain.simulation.SimulationResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.dataPackets;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.run;
import static com.example.simulator.domain.simulation.tcp.TcpSimulationTestSupport.tcpScenario;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TcpFragmentationTest {
    @Test
    void shouldCreateSingleDataSegmentWhenMessageIsShorterThanFragment() {
        SimulationResult result = run(tcpScenario("HOLA", 8));

        List<Packet> data = dataPackets(result);

        assertEquals(1, data.size());
        assertEquals("HOLA", data.get(0).getPayload());
        assertEquals(1, data.get(0).getSeq());
    }

    @Test
    void shouldSplitHolaAlumnosIntoTwoSegmentsWithFragmentEight() {
        SimulationResult result = run(tcpScenario("HOLAALUMNOS", 8));

        List<String> payloads = dataPackets(result).stream()
                .map(Packet::getPayload)
                .toList();

        assertEquals(List.of("HOLAALUM", "NOS"), payloads);
        assertEquals("HOLAALUMNOS", result.getFinalSnapshot().getDeliveredMessage());
    }

    @Test
    void shouldCreateOneSegmentPerCharacterWhenFragmentIsOne() {
        SimulationResult result = run(tcpScenario("TCP", 1));

        assertEquals(List.of("T", "C", "P"), dataPackets(result).stream().map(Packet::getPayload).toList());
        assertEquals(List.of(1, 2, 3), dataPackets(result).stream().map(Packet::getSeq).toList());
    }

    @Test
    void shouldNormalizeInvalidFragmentSizeThroughScenarioRules() {
        Scenario scenario = tcpScenario("AB", 0);

        SimulationResult result = run(scenario);

        assertEquals(1, result.getScenario().getFragmentSize());
        assertEquals(List.of("A", "B"), dataPackets(result).stream().map(Packet::getPayload).toList());
    }

    @Test
    void shouldPreservePayloadOrderAfterFragmentation() {
        SimulationResult result = run(tcpScenario("ABCDEFGHIJK", 3));

        String rebuiltFromDataSegments = dataPackets(result).stream()
                .filter(packet -> packet.getKind() == PacketKind.DATA)
                .map(Packet::getPayload)
                .reduce("", String::concat);

        assertEquals("ABCDEFGHIJK", rebuiltFromDataSegments);
        assertEquals("ABCDEFGHIJK", result.getFinalSnapshot().getDeliveredMessage());
    }
}
