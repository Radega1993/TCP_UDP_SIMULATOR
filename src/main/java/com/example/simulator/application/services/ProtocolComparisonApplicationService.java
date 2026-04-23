package com.example.simulator.application.services;

import com.example.simulator.application.dto.ComparisonCommand;
import com.example.simulator.application.dto.ComparisonSummary;
import com.example.simulator.application.dto.ProtocolComparisonResult;
import com.example.simulator.domain.protocol.PacketKind;
import com.example.simulator.domain.simulation.SimulationEvent;
import com.example.simulator.domain.simulation.SimulationEventType;
import com.example.simulator.domain.simulation.SimulationResult;

public class ProtocolComparisonApplicationService {
    private final SimulationApplicationService simulationService;

    public ProtocolComparisonApplicationService(SimulationApplicationService simulationService) {
        this.simulationService = simulationService;
    }

    public ProtocolComparisonResult compare(ComparisonCommand command) {
        SimulationResult tcpResult = simulationService.start(command.toScenario(com.example.simulator.domain.protocol.ProtocolType.TCP));
        SimulationResult udpResult = simulationService.start(command.toScenario(com.example.simulator.domain.protocol.ProtocolType.UDP));
        return new ProtocolComparisonResult(tcpResult, udpResult, summarize(tcpResult, udpResult));
    }

    private ComparisonSummary summarize(SimulationResult tcpResult, SimulationResult udpResult) {
        String tcpSent = tcpResult.getFinalSnapshot().getSentMessage();
        String udpSent = udpResult.getFinalSnapshot().getSentMessage();
        String tcpDelivered = tcpResult.getFinalSnapshot().getDeliveredMessage();
        String udpDelivered = udpResult.getFinalSnapshot().getDeliveredMessage();
        boolean tcpRetransmitted = containsRetransmission(tcpResult);
        boolean udpRetransmitted = containsRetransmission(udpResult);

        return new ComparisonSummary(
                tcpSent.equals(tcpDelivered),
                udpSent.equals(udpDelivered),
                tcpRetransmitted,
                udpRetransmitted,
                tcpSent.equals(tcpDelivered),
                udpSent.equals(udpDelivered),
                countEvents(tcpResult, SimulationEventType.PACKET_LOST),
                countEvents(udpResult, SimulationEventType.PACKET_LOST),
                countPacketEvents(tcpResult),
                countPacketEvents(udpResult)
        );
    }

    private boolean containsRetransmission(SimulationResult result) {
        return result.getEvents().stream()
                .map(SimulationEvent::getPacket)
                .filter(java.util.Objects::nonNull)
                .anyMatch(packet -> packet.isRetransmission() || packet.getKind() == PacketKind.RETRANSMISSION);
    }

    private int countEvents(SimulationResult result, SimulationEventType type) {
        return (int) result.getEvents().stream()
                .filter(event -> event.getType() == type)
                .count();
    }

    private int countPacketEvents(SimulationResult result) {
        return (int) result.getEvents().stream()
                .filter(event -> event.getPacket() != null
                        && (event.getType() == SimulationEventType.PACKET_CREATED
                        || event.getType() == SimulationEventType.PACKET_DELIVERED
                        || event.getType() == SimulationEventType.PACKET_LOST))
                .count();
    }
}
