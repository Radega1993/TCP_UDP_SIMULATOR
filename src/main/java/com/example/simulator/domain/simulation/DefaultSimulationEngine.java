package com.example.simulator.domain.simulation;

import com.example.simulator.domain.network.Endpoint;
import com.example.simulator.domain.network.NetworkConditions;
import com.example.simulator.domain.protocol.PacketKind;
import com.example.simulator.domain.protocol.PacketStatus;
import com.example.simulator.domain.protocol.ProtocolType;
import com.example.simulator.domain.protocol.tcp.TcpState;
import com.example.simulator.domain.scenario.Scenario;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class DefaultSimulationEngine implements SimulationEngine {
    private static final long PACKET_TRAVEL_MS = 1200L;
    private static final long UDP_SEND_GAP_MS = 700L;
    private static final long TCP_TIMEOUT_MS = 2200L;

    @Override
    public SimulationResult run(Scenario scenario) {
        TimelineBuilder timeline = new TimelineBuilder();
        if (scenario.getProtocol() == ProtocolType.TCP) {
            runTcpScenario(scenario, timeline);
        } else {
            runUdpScenario(scenario, timeline);
        }
        return timeline.toResult(scenario);
    }

    private void runTcpScenario(Scenario scenario, TimelineBuilder timeline) {
        TcpState clientState = TcpState.CLOSED;
        TcpState serverState = TcpState.CLOSED;
        String deliveredMessage = "";

        timeline.log("[TCP] Inicio de simulación.");

        serverState = TcpState.LISTEN;
        timeline.stateChanged(Endpoint.SERVER, serverState);
        clientState = TcpState.SYN_SENT;
        timeline.stateChanged(Endpoint.CLIENT, clientState);
        timeline.log("[TCP] 3-way handshake 1/3: Cliente envia SYN.");
        timeline.deliver(packet(ProtocolType.TCP, Endpoint.CLIENT, Endpoint.SERVER, PacketKind.SYN, 0, 0, "", false));

        serverState = TcpState.SYN_RECEIVED;
        timeline.stateChanged(Endpoint.SERVER, serverState);
        timeline.log("[TCP] 3-way handshake 2/3: Servidor envia SYN-ACK.");
        timeline.deliver(packet(ProtocolType.TCP, Endpoint.SERVER, Endpoint.CLIENT, PacketKind.SYN_ACK, 0, 1, "", false));

        clientState = TcpState.ESTABLISHED;
        serverState = TcpState.ESTABLISHED;
        timeline.stateChanged(Endpoint.CLIENT, clientState);
        timeline.stateChanged(Endpoint.SERVER, serverState);
        timeline.log("[TCP] 3-way handshake 3/3: Cliente envia ACK final.");
        timeline.log("[TCP] 3-way handshake completado: conexión ESTABLISHED.");
        timeline.deliver(packet(ProtocolType.TCP, Endpoint.CLIENT, Endpoint.SERVER, PacketKind.ACK, 0, 1, "", false));

        String[] segments = splitMessage(scenario.getMessage(), scenario.getFragmentSize());
        timeline.log("[TCP] Mensaje fragmentado en " + segments.length + " segmentos (tamano maximo " + scenario.getFragmentSize() + ").");

        NetworkDecisionPolicy policy = new NetworkDecisionPolicy(scenario.getNetworkConditions(), segments.length);
        int currentSeq = 1;

        for (int index = 0; index < segments.length; index++) {
            String payload = segments[index];
            int segmentNumber = index + 1;
            timeline.log("[TCP] Envio de segmento " + segmentNumber + "/" + segments.length + " SEQ=" + currentSeq + " payload=\"" + payload + "\".");

            Packet data = packet(ProtocolType.TCP, Endpoint.CLIENT, Endpoint.SERVER, PacketKind.DATA, currentSeq, 0, payload, false);
            boolean lose = policy.shouldLose(segmentNumber);
            if (lose) {
                timeline.lose(data);
                timeline.waitMillis(TCP_TIMEOUT_MS);
                timeline.log("[TCP] Timeout en segmento " + segmentNumber + "/" + segments.length + " (SEQ=" + currentSeq + "). Retransmisión.");
                Packet retransmission = packet(ProtocolType.TCP, Endpoint.CLIENT, Endpoint.SERVER, PacketKind.RETRANSMISSION, currentSeq, 0, payload, true);
                timeline.deliver(retransmission);
            } else {
                timeline.deliver(data);
            }

            int ackValue = currentSeq + payload.length();
            deliveredMessage = deliveredMessage + payload;
            timeline.log("[TCP] ACK del segmento " + segmentNumber + "/" + segments.length + ": ACK=" + ackValue + ".");
            timeline.deliver(packet(ProtocolType.TCP, Endpoint.SERVER, Endpoint.CLIENT, PacketKind.ACK, 0, ackValue, "", false));
            currentSeq = ackValue;
        }

        timeline.log("[TCP] Inicio del cierre de conexión (4-way close).");
        int finSeq = currentSeq;
        clientState = TcpState.FIN_WAIT_1;
        timeline.stateChanged(Endpoint.CLIENT, clientState);
        timeline.log("[TCP] Cierre 1/4: Cliente envia FIN.");
        timeline.deliver(packet(ProtocolType.TCP, Endpoint.CLIENT, Endpoint.SERVER, PacketKind.FIN, finSeq, 0, "", false));

        serverState = TcpState.CLOSE_WAIT;
        timeline.stateChanged(Endpoint.SERVER, serverState);
        timeline.log("[TCP] Cierre 2/4: Servidor responde ACK=" + (finSeq + 1) + ".");
        timeline.deliver(packet(ProtocolType.TCP, Endpoint.SERVER, Endpoint.CLIENT, PacketKind.ACK, 0, finSeq + 1, "", false));

        serverState = TcpState.LAST_ACK;
        timeline.stateChanged(Endpoint.SERVER, serverState);
        timeline.log("[TCP] Cierre 3/4: Servidor envia FIN.");
        timeline.deliver(packet(ProtocolType.TCP, Endpoint.SERVER, Endpoint.CLIENT, PacketKind.FIN, 1, finSeq + 1, "", false));

        clientState = TcpState.TIME_WAIT;
        timeline.stateChanged(Endpoint.CLIENT, clientState);
        timeline.log("[TCP] Cierre 4/4: Cliente responde ACK final.");
        timeline.deliver(packet(ProtocolType.TCP, Endpoint.CLIENT, Endpoint.SERVER, PacketKind.ACK, 0, 2, "", false));

        clientState = TcpState.CLOSED;
        serverState = TcpState.CLOSED;
        timeline.stateChanged(Endpoint.CLIENT, clientState);
        timeline.stateChanged(Endpoint.SERVER, serverState);
        timeline.log("[TCP] Conexión cerrada correctamente.");
        timeline.messageDelivered("TCP entregó: " + deliveredMessage + " y cerró la conexión.");
        timeline.completed(new SimulationSnapshot(
                ProtocolType.TCP,
                clientState,
                serverState,
                scenario.getMessage(),
                deliveredMessage,
                true
        ));
    }

    private void runUdpScenario(Scenario scenario, TimelineBuilder timeline) {
        StringBuilder delivered = new StringBuilder();
        String[] chunks = splitMessage(scenario.getMessage(), scenario.getFragmentSize());

        timeline.log("[UDP] Inicio de simulación sin conexión.");
        timeline.log("[UDP] Mensaje fragmentado en " + chunks.length + " datagramas (tamano maximo " + scenario.getFragmentSize() + ").");

        NetworkDecisionPolicy policy = new NetworkDecisionPolicy(scenario.getNetworkConditions(), chunks.length);
        for (int i = 0; i < chunks.length; i++) {
            String payload = chunks[i];
            Packet datagram = packet(ProtocolType.UDP, Endpoint.CLIENT, Endpoint.SERVER, PacketKind.UDP_DATAGRAM, i + 1, 0, payload, false);
            if (policy.shouldLose(i + 1)) {
                timeline.lose(datagram);
            } else {
                timeline.deliver(datagram);
                delivered.append(payload);
                timeline.log("[UDP] Servidor recibió datagrama #" + (i + 1) + " payload=\"" + payload + "\".");
                timeline.messageDelivered("UDP recibió: " + payload);
            }
            timeline.waitMillis(UDP_SEND_GAP_MS);
        }

        timeline.log("[UDP] Fin de simulación: no hay ACK ni retransmisión.");
        timeline.completed(new SimulationSnapshot(
                ProtocolType.UDP,
                TcpState.CLOSED,
                TcpState.CLOSED,
                scenario.getMessage(),
                delivered.toString(),
                true
        ));
    }

    private Packet packet(ProtocolType protocol, Endpoint from, Endpoint to, PacketKind kind, int seq, int ack,
                          String payload, boolean retransmission) {
        return new Packet(UUID.randomUUID().toString(), protocol, from, to, kind, seq, ack, payload, PacketStatus.CREATED, retransmission);
    }

    private String[] splitMessage(String message, int size) {
        List<String> parts = new ArrayList<>();
        for (int i = 0; i < message.length(); i += size) {
            parts.add(message.substring(i, Math.min(message.length(), i + size)));
        }
        return parts.toArray(new String[0]);
    }

    private static final class NetworkDecisionPolicy {
        private final NetworkConditions networkConditions;
        private final Random random = new Random(42L);
        private final int totalPackets;

        private NetworkDecisionPolicy(NetworkConditions networkConditions, int totalPackets) {
            this.networkConditions = networkConditions;
            this.totalPackets = totalPackets;
        }

        private boolean shouldLose(int packetIndex) {
            if (networkConditions.getForcedLossIndexes().contains(packetIndex)) {
                return true;
            }
            if (packetIndex > totalPackets) {
                return false;
            }
            return random.nextDouble() < networkConditions.getPacketLossRate();
        }
    }

    private static final class TimelineBuilder {
        private final SimulationClock clock = new SimulationClock();
        private final SimulationEventQueue queue = new SimulationEventQueue();
        private final SimulationScheduler scheduler = new SimulationScheduler(clock, queue);
        private final SimulationLog log = new SimulationLog();
        private SimulationSnapshot finalSnapshot;

        private void log(String message) {
            log.append(message);
            scheduler.scheduleNow(new SimulationEvent(clock.currentTimeMillis(), SimulationEventType.LOG, message, null, null, null));
        }

        private void stateChanged(Endpoint endpoint, TcpState state) {
            scheduler.scheduleNow(new SimulationEvent(clock.currentTimeMillis(), SimulationEventType.TCP_STATE_CHANGED, null, null, endpoint, state));
        }

        private void messageDelivered(String message) {
            scheduler.scheduleNow(new SimulationEvent(clock.currentTimeMillis(), SimulationEventType.MESSAGE_DELIVERED, message, null, null, null));
        }

        private void deliver(Packet packet) {
            Packet created = copyPacket(packet, PacketStatus.IN_TRANSIT);
            scheduler.scheduleNow(new SimulationEvent(clock.currentTimeMillis(), SimulationEventType.PACKET_CREATED, null, created, null, null));
            clock.advanceBy(PACKET_TRAVEL_MS);
            Packet delivered = copyPacket(packet, PacketStatus.DELIVERED);
            scheduler.scheduleNow(new SimulationEvent(clock.currentTimeMillis(), SimulationEventType.PACKET_DELIVERED, null, delivered, null, null));
        }

        private void lose(Packet packet) {
            Packet created = copyPacket(packet, PacketStatus.IN_TRANSIT);
            scheduler.scheduleNow(new SimulationEvent(clock.currentTimeMillis(), SimulationEventType.PACKET_CREATED, null, created, null, null));
            clock.advanceBy(PACKET_TRAVEL_MS);
            Packet lost = copyPacket(packet, PacketStatus.LOST);
            scheduler.scheduleNow(new SimulationEvent(clock.currentTimeMillis(), SimulationEventType.PACKET_LOST, null, lost, null, null));
            log(packet.label() + " se perdió en la red.");
        }

        private void waitMillis(long millis) {
            clock.advanceBy(millis);
        }

        private void completed(SimulationSnapshot snapshot) {
            this.finalSnapshot = snapshot;
            scheduler.scheduleNow(new SimulationEvent(clock.currentTimeMillis(), SimulationEventType.SCENARIO_COMPLETED, null, null, null, null));
        }

        private SimulationResult toResult(Scenario scenario) {
            return new SimulationResult(scenario, scheduler.drainInOrder(), finalSnapshot, log.entries());
        }

        private static Packet copyPacket(Packet packet, PacketStatus status) {
            return new Packet(
                    packet.getId(),
                    packet.getProtocolType(),
                    packet.getFrom(),
                    packet.getTo(),
                    packet.getKind(),
                    packet.getSeq(),
                    packet.getAck(),
                    packet.getPayload(),
                    status,
                    packet.isRetransmission()
            );
        }
    }
}
