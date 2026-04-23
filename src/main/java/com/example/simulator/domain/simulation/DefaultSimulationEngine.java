package com.example.simulator.domain.simulation;

import com.example.simulator.domain.network.Endpoint;
import com.example.simulator.domain.network.NetworkConditions;
import com.example.simulator.domain.protocol.PacketKind;
import com.example.simulator.domain.protocol.PacketStatus;
import com.example.simulator.domain.protocol.ProtocolType;
import com.example.simulator.domain.protocol.tcp.CongestionPhase;
import com.example.simulator.domain.protocol.tcp.TcpReceiverBuffer;
import com.example.simulator.domain.protocol.tcp.TcpCongestionControl;
import com.example.simulator.domain.protocol.tcp.TcpSlidingWindow;
import com.example.simulator.domain.protocol.tcp.TcpState;
import com.example.simulator.domain.scenario.Scenario;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class DefaultSimulationEngine implements SimulationEngine {
    private static final long DEFAULT_SEND_GAP_MS = 700L;
    private static final long TCP_TIMEOUT_MS = 2200L;
    private static final long DUPLICATE_EXTRA_DELAY_MS = 140L;
    private static final long REORDER_ADVANCE_MS = 180L;
    private static final long MIN_LATENCY_MS = 80L;
    private static final long TCP_PIPELINE_SEND_GAP_MS = 180L;

    @Override
    public SimulationResult run(Scenario scenario) {
        TimelineBuilder timeline = new TimelineBuilder();
        NetworkEffectPlanner networkPlanner = new NetworkEffectPlanner(scenario.getNetworkConditions());
        if (scenario.getProtocol() == ProtocolType.TCP) {
            runTcpScenario(scenario, timeline, networkPlanner);
        } else {
            runUdpScenario(scenario, timeline, networkPlanner);
        }
        return timeline.toResult(scenario);
    }

    private void runTcpScenario(Scenario scenario, TimelineBuilder timeline, NetworkEffectPlanner networkPlanner) {
        TcpState clientState = TcpState.CLOSED;
        TcpState serverState = TcpState.CLOSED;
        int packetIndex = 1;

        timeline.log("[TCP] Inicio de simulación.");
        timeline.log(describeConditions(scenario.getNetworkConditions()));

        serverState = TcpState.LISTEN;
        timeline.stateChanged(Endpoint.SERVER, serverState);
        clientState = TcpState.SYN_SENT;
        timeline.stateChanged(Endpoint.CLIENT, clientState);
        timeline.log("[TCP] 3-way handshake 1/3: Cliente envia SYN.");
        TransmissionOutcome synOutcome = timeline.transmit(
                packet(ProtocolType.TCP, Endpoint.CLIENT, Endpoint.SERVER, PacketKind.SYN, 0, 0, "", false),
                networkPlanner.plan(Endpoint.CLIENT, timeline.currentTime(), packetIndex++, false)
        );
        timeline.advanceTo(synOutcome.primaryArrivalTime());

        serverState = TcpState.SYN_RECEIVED;
        timeline.stateChanged(Endpoint.SERVER, serverState);
        timeline.log("[TCP] 3-way handshake 2/3: Servidor envia SYN-ACK.");
        TransmissionOutcome synAckOutcome = timeline.transmit(
                packet(ProtocolType.TCP, Endpoint.SERVER, Endpoint.CLIENT, PacketKind.SYN_ACK, 0, 1, "", false),
                networkPlanner.plan(Endpoint.SERVER, timeline.currentTime(), packetIndex++, false)
        );
        timeline.advanceTo(synAckOutcome.primaryArrivalTime());

        clientState = TcpState.ESTABLISHED;
        serverState = TcpState.ESTABLISHED;
        timeline.stateChanged(Endpoint.CLIENT, clientState);
        timeline.stateChanged(Endpoint.SERVER, serverState);
        timeline.log("[TCP] 3-way handshake 3/3: Cliente envia ACK final.");
        timeline.log("[TCP] 3-way handshake completado: conexión ESTABLISHED.");
        TransmissionOutcome ackHandshakeOutcome = timeline.transmit(
                packet(ProtocolType.TCP, Endpoint.CLIENT, Endpoint.SERVER, PacketKind.ACK, 0, 1, "", false),
                networkPlanner.plan(Endpoint.CLIENT, timeline.currentTime(), packetIndex++, false)
        );
        timeline.advanceTo(ackHandshakeOutcome.primaryArrivalTime());

        String[] segments = splitMessage(scenario.getMessage(), scenario.getFragmentSize());
        timeline.log("[TCP] Mensaje fragmentado en " + segments.length + " segmentos (tamano maximo "
                + scenario.getFragmentSize() + ").");
        LossDecisionPolicy lossPolicy = new LossDecisionPolicy(scenario.getNetworkConditions(), segments.length);
        TcpSlidingWindow slidingWindow = new TcpSlidingWindow(scenario.getTcpWindowSizeBytes());
        TcpReceiverBuffer receiverBuffer = new TcpReceiverBuffer(scenario.getTcpReceiverBufferBytes());
        TcpCongestionControl congestionControl = new TcpCongestionControl(scenario.getFragmentSize(), scenario.getTcpWindowSizeBytes());
        TreeMap<Integer, OutstandingSegment> bufferedSegments = new TreeMap<>();
        List<OutstandingSegment> outstandingSegments = new ArrayList<>();
        StringBuilder deliveredMessage = new StringBuilder();
        long sendCursor = timeline.currentTime();
        int currentSeq = 1;
        int nextSegmentIndex = 0;
        int nextExpectedByte = 1;
        int advertisedWindow = receiverBuffer.availableBytes();
        int highestAckValue = 1;
        int congestionStep = 0;

        timeline.log("[TCP] Ventana de envío configurada en " + scenario.getTcpWindowSizeBytes() + " bytes.");
        timeline.log("[TCP] Buffer de recepción configurado en " + scenario.getTcpReceiverBufferBytes() + " bytes.");
        timeline.log("[TCP][CONG] cwnd inicial=" + congestionControl.currentWindowBytes()
                + " ssthresh=" + congestionControl.slowStartThresholdBytes()
                + " fase=" + congestionControl.phase() + ".");
        timeline.flowControlUpdated(buildFlowSnapshot(slidingWindow, receiverBuffer, advertisedWindow, scenario.getMessage()), timeline.currentTime());
        timeline.congestionUpdated(buildCongestionSnapshot(
                congestionControl,
                slidingWindow.bytesInFlight(),
                congestionControl.effectiveWindowBytes(advertisedWindow),
                ++congestionStep,
                "Inicio de TCP",
                false
        ), timeline.currentTime());

        while (nextSegmentIndex < segments.length || !outstandingSegments.isEmpty()) {
            sendCursor = Math.max(sendCursor, timeline.currentTime());
            int effectiveWindow = congestionControl.effectiveWindowBytes(advertisedWindow);
            while (nextSegmentIndex < segments.length
                    && slidingWindow.canSend(segments[nextSegmentIndex].length(), effectiveWindow)) {
                String payload = segments[nextSegmentIndex];
                int segmentNumber = nextSegmentIndex + 1;
                timeline.log("[TCP] Envio en ventana del segmento " + segmentNumber + "/" + segments.length
                        + " SEQ=" + currentSeq + " payload=\"" + payload + "\"."
                        + " Ventana efectiva=" + effectiveWindow + " bytes.");

                Packet data = packet(ProtocolType.TCP, Endpoint.CLIENT, Endpoint.SERVER, PacketKind.DATA,
                        currentSeq, 0, payload, false, effectiveWindow);
                boolean lose = lossPolicy.shouldLose(segmentNumber);
                TransmissionPlan firstPlan = networkPlanner.plan(Endpoint.CLIENT, sendCursor, packetIndex++, lose);
                timeline.transmit(data, firstPlan);
                OutstandingSegment outstanding = new OutstandingSegment(
                        segmentNumber,
                        currentSeq,
                        payload,
                        sendCursor,
                        firstPlan.arrivalTime(),
                        firstPlan.lost(),
                        false,
                        false
                );
                outstandingSegments.add(outstanding);
                slidingWindow.markSent(payload.length());
                timeline.flowControlUpdated(buildFlowSnapshot(slidingWindow, receiverBuffer, advertisedWindow, scenario.getMessage()), sendCursor);
                timeline.congestionUpdated(buildCongestionSnapshot(
                        congestionControl,
                        slidingWindow.bytesInFlight(),
                        congestionControl.effectiveWindowBytes(advertisedWindow),
                        ++congestionStep,
                        "Envío SEQ=" + currentSeq,
                        false
                ), sendCursor);
                sendCursor += TCP_PIPELINE_SEND_GAP_MS;
                currentSeq += payload.length();
                nextSegmentIndex++;
                effectiveWindow = congestionControl.effectiveWindowBytes(advertisedWindow);
            }

            OutstandingSegment nextArrival = outstandingSegments.stream()
                    .filter(segment -> !segment.acknowledged && !segment.arrivalProcessed && !segment.lost)
                    .min(Comparator.comparingLong(segment -> segment.arrivalTime))
                    .orElse(null);
            OutstandingSegment nextTimeout = outstandingSegments.stream()
                    .filter(segment -> !segment.acknowledged && !segment.timeoutProcessed && segment.lost)
                    .min(Comparator.comparingLong(segment -> segment.sendTime + TCP_TIMEOUT_MS))
                    .orElse(null);
            PendingAck pendingAck = outstandingSegments.stream()
                    .map(segment -> segment.pendingAck)
                    .filter(ack -> ack != null && !ack.processed)
                    .min(Comparator.comparingLong(ack -> ack.arrivalTime))
                    .orElse(null);

            long nextTime = Long.MAX_VALUE;
            if (nextArrival != null) {
                nextTime = Math.min(nextTime, nextArrival.arrivalTime);
            }
            if (nextTimeout != null) {
                nextTime = Math.min(nextTime, nextTimeout.sendTime + TCP_TIMEOUT_MS);
            }
            if (pendingAck != null) {
                nextTime = Math.min(nextTime, pendingAck.arrivalTime);
            }
            if (nextTime == Long.MAX_VALUE) {
                break;
            }
            timeline.advanceTo(nextTime);

            if (nextArrival != null && nextArrival.arrivalTime == nextTime) {
                nextArrival.arrivalProcessed = true;
                int payloadBytes = nextArrival.payload.length();
                if (!receiverBuffer.canAccept(payloadBytes)) {
                    timeline.log("[TCP] Buffer receptor lleno. Segmento SEQ=" + nextArrival.seq + " fuera de ventana de recepción.");
                    Packet ackPacket = packet(ProtocolType.TCP, Endpoint.SERVER, Endpoint.CLIENT, PacketKind.ACK,
                            0, nextExpectedByte, "", false, receiverBuffer.availableBytes());
                    TransmissionPlan ackPlan = networkPlanner.plan(Endpoint.SERVER, nextTime, packetIndex++, false);
                    timeline.transmit(ackPacket, ackPlan);
                    nextArrival.pendingAck = new PendingAck(ackPlan.arrivalTime(), nextExpectedByte, receiverBuffer.availableBytes());
                } else {
                    receiverBuffer.reserve(payloadBytes);
                    bufferedSegments.putIfAbsent(nextArrival.seq, nextArrival);
                    timeline.log("[TCP] Buffer receptor ocupa " + receiverBuffer.usedBytes() + "/" + receiverBuffer.capacityBytes()
                            + " bytes tras recibir SEQ=" + nextArrival.seq + ".");

                    while (bufferedSegments.containsKey(nextExpectedByte)) {
                        OutstandingSegment inOrder = bufferedSegments.remove(nextExpectedByte);
                        deliveredMessage.append(inOrder.payload);
                        nextExpectedByte += inOrder.payload.length();
                        receiverBuffer.release(inOrder.payload.length());
                    }

                    advertisedWindow = receiverBuffer.availableBytes();
                    timeline.log("[TCP] ACK acumulativo emitido: ACK=" + nextExpectedByte
                            + " ventana anunciada=" + advertisedWindow + ".");
                    Packet ackPacket = packet(ProtocolType.TCP, Endpoint.SERVER, Endpoint.CLIENT, PacketKind.ACK,
                            0, nextExpectedByte, "", false, advertisedWindow);
                    TransmissionPlan ackPlan = networkPlanner.plan(Endpoint.SERVER, nextTime, packetIndex++, false);
                    timeline.transmit(ackPacket, ackPlan);
                    nextArrival.pendingAck = new PendingAck(ackPlan.arrivalTime(), nextExpectedByte, advertisedWindow);
                }
                timeline.flowControlUpdated(buildFlowSnapshot(slidingWindow, receiverBuffer, advertisedWindow, scenario.getMessage()), nextTime);
                timeline.congestionUpdated(buildCongestionSnapshot(
                        congestionControl,
                        slidingWindow.bytesInFlight(),
                        congestionControl.effectiveWindowBytes(advertisedWindow),
                        ++congestionStep,
                        "Recepción y ACK acumulativo",
                        false
                ), nextTime);
                continue;
            }

            if (pendingAck != null && pendingAck.arrivalTime == nextTime) {
                pendingAck.processed = true;
                advertisedWindow = pendingAck.advertisedWindow;
                if (pendingAck.ackValue > highestAckValue) {
                    int acknowledgedBytes = pendingAck.ackValue - highestAckValue;
                    highestAckValue = pendingAck.ackValue;
                    slidingWindow.markAcknowledgedUpTo(pendingAck.ackValue - 1);
                    for (OutstandingSegment segment : outstandingSegments) {
                        if (!segment.acknowledged && segment.seq + segment.payload.length() <= pendingAck.ackValue) {
                            segment.acknowledged = true;
                        }
                    }
                    outstandingSegments.removeIf(segment -> segment.acknowledged);
                    CongestionPhase previousPhase = congestionControl.phase();
                    congestionControl.onAcknowledgement(acknowledgedBytes);
                    timeline.log("[TCP] ACK acumulativo recibido en cliente: ACK=" + pendingAck.ackValue
                            + " ventana disponible=" + advertisedWindow + ".");
                    if (previousPhase != congestionControl.phase()) {
                        timeline.log("[TCP][CONG] Cambio de fase: " + previousPhase + " -> " + congestionControl.phase() + ".");
                    }
                    timeline.log("[TCP][CONG] cwnd=" + congestionControl.currentWindowBytes()
                            + " ssthresh=" + congestionControl.slowStartThresholdBytes() + ".");
                    timeline.flowControlUpdated(buildFlowSnapshot(slidingWindow, receiverBuffer, advertisedWindow, scenario.getMessage()), nextTime);
                    timeline.congestionUpdated(buildCongestionSnapshot(
                            congestionControl,
                            slidingWindow.bytesInFlight(),
                            congestionControl.effectiveWindowBytes(advertisedWindow),
                            ++congestionStep,
                            "ACK nuevo " + pendingAck.ackValue,
                            false
                    ), nextTime);
                } else if (pendingAck.ackValue == highestAckValue) {
                    int duplicateCount = congestionControl.onDuplicateAck();
                    timeline.log("[TCP][CONG] Duplicate ACK detectado: ACK=" + pendingAck.ackValue
                            + " count=" + duplicateCount + ".");

                    if (duplicateCount >= 3) {
                        OutstandingSegment missingSegment = outstandingSegments.stream()
                                .filter(segment -> !segment.acknowledged && segment.seq == pendingAck.ackValue)
                                .findFirst()
                                .orElse(null);
                        if (missingSegment != null && !missingSegment.fastRetransmitted) {
                            congestionControl.onFastRetransmit();
                            timeline.log("[TCP][CONG] Fast retransmit activado para SEQ=" + missingSegment.seq
                                    + ". cwnd=" + congestionControl.currentWindowBytes()
                                    + " ssthresh=" + congestionControl.slowStartThresholdBytes() + ".");
                            Packet retransmission = packet(ProtocolType.TCP, Endpoint.CLIENT, Endpoint.SERVER,
                                    PacketKind.RETRANSMISSION, missingSegment.seq, 0, missingSegment.payload, true,
                                    congestionControl.effectiveWindowBytes(advertisedWindow));
                            TransmissionPlan retryPlan = networkPlanner.plan(Endpoint.CLIENT, nextTime, packetIndex++, false);
                            timeline.transmit(retransmission, retryPlan);
                            missingSegment.fastRetransmitted = true;
                            missingSegment.lost = false;
                            missingSegment.arrivalProcessed = false;
                            missingSegment.timeoutProcessed = false;
                            missingSegment.arrivalTime = retryPlan.arrivalTime();
                            missingSegment.sendTime = nextTime;
                            sendCursor = Math.max(sendCursor, nextTime + TCP_PIPELINE_SEND_GAP_MS);
                        }
                    }
                    timeline.congestionUpdated(buildCongestionSnapshot(
                            congestionControl,
                            slidingWindow.bytesInFlight(),
                            congestionControl.effectiveWindowBytes(advertisedWindow),
                            ++congestionStep,
                            "Duplicate ACK x" + duplicateCount,
                            duplicateCount >= 3
                    ), nextTime);
                }
                continue;
            }

            if (nextTimeout != null && nextTimeout.sendTime + TCP_TIMEOUT_MS == nextTime) {
                nextTimeout.timeoutProcessed = true;
                congestionControl.onTimeout();
                timeline.log("[TCP] Timeout en segmento " + nextTimeout.segmentNumber + "/" + segments.length
                        + " (SEQ=" + nextTimeout.seq + "). Retransmisión.");
                timeline.log("[TCP][CONG] Timeout detectado. cwnd=" + congestionControl.currentWindowBytes()
                        + " ssthresh=" + congestionControl.slowStartThresholdBytes()
                        + " fase=" + congestionControl.phase() + ".");
                Packet retransmission = packet(ProtocolType.TCP, Endpoint.CLIENT, Endpoint.SERVER,
                        PacketKind.RETRANSMISSION, nextTimeout.seq, 0, nextTimeout.payload, true,
                        congestionControl.effectiveWindowBytes(advertisedWindow));
                TransmissionPlan retryPlan = networkPlanner.plan(Endpoint.CLIENT, nextTime, packetIndex++, false);
                timeline.transmit(retransmission, retryPlan);
                nextTimeout.lost = false;
                nextTimeout.arrivalProcessed = false;
                nextTimeout.timeoutProcessed = false;
                nextTimeout.arrivalTime = retryPlan.arrivalTime();
                nextTimeout.sendTime = nextTime;
                nextTimeout.fastRetransmitted = false;
                timeline.flowControlUpdated(buildFlowSnapshot(slidingWindow, receiverBuffer, advertisedWindow, scenario.getMessage()), nextTime);
                timeline.congestionUpdated(buildCongestionSnapshot(
                        congestionControl,
                        slidingWindow.bytesInFlight(),
                        congestionControl.effectiveWindowBytes(advertisedWindow),
                        ++congestionStep,
                        "Timeout y caída de cwnd",
                        true
                ), nextTime);
            }
        }

        timeline.log("[TCP] Inicio del cierre de conexión (4-way close).");
        int finSeq = nextExpectedByte;
        clientState = TcpState.FIN_WAIT_1;
        timeline.stateChanged(Endpoint.CLIENT, clientState);
        timeline.log("[TCP] Cierre 1/4: Cliente envia FIN.");
        TransmissionOutcome finClientOutcome = timeline.transmit(
                packet(ProtocolType.TCP, Endpoint.CLIENT, Endpoint.SERVER, PacketKind.FIN, finSeq, 0, "", false),
                networkPlanner.plan(Endpoint.CLIENT, timeline.currentTime(), packetIndex++, false)
        );
        timeline.advanceTo(finClientOutcome.primaryArrivalTime());

        serverState = TcpState.CLOSE_WAIT;
        timeline.stateChanged(Endpoint.SERVER, serverState);
        timeline.log("[TCP] Cierre 2/4: Servidor responde ACK=" + (finSeq + 1) + ".");
        TransmissionOutcome finAckOutcome = timeline.transmit(
                packet(ProtocolType.TCP, Endpoint.SERVER, Endpoint.CLIENT, PacketKind.ACK, 0, finSeq + 1, "", false),
                networkPlanner.plan(Endpoint.SERVER, timeline.currentTime(), packetIndex++, false)
        );
        timeline.advanceTo(finAckOutcome.primaryArrivalTime());

        serverState = TcpState.LAST_ACK;
        timeline.stateChanged(Endpoint.SERVER, serverState);
        timeline.log("[TCP] Cierre 3/4: Servidor envia FIN.");
        TransmissionOutcome serverFinOutcome = timeline.transmit(
                packet(ProtocolType.TCP, Endpoint.SERVER, Endpoint.CLIENT, PacketKind.FIN, 1, finSeq + 1, "", false),
                networkPlanner.plan(Endpoint.SERVER, timeline.currentTime(), packetIndex++, false)
        );
        timeline.advanceTo(serverFinOutcome.primaryArrivalTime());

        clientState = TcpState.TIME_WAIT;
        timeline.stateChanged(Endpoint.CLIENT, clientState);
        timeline.log("[TCP] Cierre 4/4: Cliente responde ACK final.");
        TransmissionOutcome finalAckOutcome = timeline.transmit(
                packet(ProtocolType.TCP, Endpoint.CLIENT, Endpoint.SERVER, PacketKind.ACK, 0, 2, "", false),
                networkPlanner.plan(Endpoint.CLIENT, timeline.currentTime(), packetIndex++, false)
        );
        timeline.advanceTo(finalAckOutcome.primaryArrivalTime());

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
                deliveredMessage.toString(),
                true,
                buildFlowSnapshot(slidingWindow, receiverBuffer, advertisedWindow, scenario.getMessage()),
                buildCongestionSnapshot(
                        congestionControl,
                        slidingWindow.bytesInFlight(),
                        congestionControl.effectiveWindowBytes(advertisedWindow),
                        ++congestionStep,
                        "Final de la simulación TCP",
                        false
                )
        ));
    }

    private void runUdpScenario(Scenario scenario, TimelineBuilder timeline, NetworkEffectPlanner networkPlanner) {
        StringBuilder delivered = new StringBuilder();
        String[] chunks = splitMessage(scenario.getMessage(), scenario.getFragmentSize());
        LossDecisionPolicy lossPolicy = new LossDecisionPolicy(scenario.getNetworkConditions(), chunks.length);
        long sendCursor = timeline.currentTime();
        long latestArrival = timeline.currentTime();

        timeline.log("[UDP] Inicio de simulación sin conexión.");
        timeline.log(describeConditions(scenario.getNetworkConditions()));
        timeline.log("[UDP] Mensaje fragmentado en " + chunks.length + " datagramas (tamano maximo "
                + scenario.getFragmentSize() + ").");

        for (int i = 0; i < chunks.length; i++) {
            int packetIndex = i + 1;
            String payload = chunks[i];
            boolean lose = lossPolicy.shouldLose(packetIndex);
            Packet datagram = packet(ProtocolType.UDP, Endpoint.CLIENT, Endpoint.SERVER, PacketKind.UDP_DATAGRAM, packetIndex, 0, payload, false);
            TransmissionOutcome outcome = timeline.transmit(
                    datagram,
                    networkPlanner.plan(Endpoint.CLIENT, sendCursor, packetIndex, lose)
            );

            if (lose) {
                timeline.logAt(outcome.primaryArrivalTime(), "[UDP] Datagrama #" + packetIndex + " se perdió antes de llegar al servidor.");
            } else {
                delivered.append(payload);
                timeline.logAt(outcome.primaryArrivalTime(), "[UDP] Servidor recibió datagrama #" + packetIndex
                        + " payload=\"" + payload + "\".");
                timeline.messageDeliveredAt(outcome.primaryArrivalTime(), "UDP recibió: " + payload);
            }

            latestArrival = Math.max(latestArrival, outcome.latestRelevantTime());
            sendCursor += DEFAULT_SEND_GAP_MS;
        }

        timeline.advanceTo(latestArrival);
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
        return packet(protocol, from, to, kind, seq, ack, payload, retransmission, 0);
    }

    private Packet packet(ProtocolType protocol, Endpoint from, Endpoint to, PacketKind kind, int seq, int ack,
                          String payload, boolean retransmission, int windowSize) {
        return new Packet(UUID.randomUUID().toString(), protocol, from, to, kind, seq, ack, payload, PacketStatus.CREATED, retransmission, windowSize);
    }

    private String[] splitMessage(String message, int size) {
        List<String> parts = new ArrayList<>();
        for (int i = 0; i < message.length(); i += size) {
            parts.add(message.substring(i, Math.min(message.length(), i + size)));
        }
        return parts.toArray(new String[0]);
    }

    private String describeConditions(NetworkConditions conditions) {
        return "[NET] Condiciones: pérdida=" + Math.round(conditions.getPacketLossRate() * 100.0) + "%, "
                + "latencia=" + conditions.getBaseLatencyMillis() + " ms, "
                + "jitter=" + conditions.getJitterMillis() + " ms, "
                + "duplicación=" + Math.round(conditions.getDuplicationRate() * 100.0) + "%, "
                + "reordenación=" + Math.round(conditions.getReorderingRate() * 100.0) + "%, "
                + "ancho de banda="
                + (conditions.getBandwidthPacketsPerSecond() <= 0 ? "sin límite" : conditions.getBandwidthPacketsPerSecond() + " pkt/s");
    }

    private static final class LossDecisionPolicy {
        private final NetworkConditions networkConditions;
        private final Random random = new Random(42L);
        private final int totalPackets;

        private LossDecisionPolicy(NetworkConditions networkConditions, int totalPackets) {
            this.networkConditions = networkConditions;
            this.totalPackets = totalPackets;
        }

        private boolean shouldLose(int packetIndex) {
            Set<Integer> forcedLossIndexes = networkConditions.getForcedLossIndexes();
            if (forcedLossIndexes.contains(packetIndex)) {
                return true;
            }
            if (packetIndex > totalPackets) {
                return false;
            }
            return random.nextDouble() < networkConditions.getPacketLossRate();
        }
    }

    private static final class NetworkEffectPlanner {
        private final NetworkConditions conditions;
        private final Random random = new Random(7L);
        private long clientToServerBandwidthCursor = 0L;
        private long serverToClientBandwidthCursor = 0L;
        private long lastClientToServerArrival = Long.MIN_VALUE;
        private long lastServerToClientArrival = Long.MIN_VALUE;

        private NetworkEffectPlanner(NetworkConditions conditions) {
            this.conditions = conditions;
        }

        private TransmissionPlan plan(Endpoint from, long requestedSendTime, int packetIndex, boolean lose) {
            long sendTime = applyBandwidth(from, requestedSendTime);
            long latency = computeLatency();
            long arrivalTime = sendTime + latency;
            boolean reordered = false;
            long previousArrival = previousArrival(from);

            if (shouldReorder() && previousArrival != Long.MIN_VALUE) {
                long reorderedArrival = Math.max(sendTime + MIN_LATENCY_MS, previousArrival - REORDER_ADVANCE_MS);
                if (reorderedArrival < arrivalTime) {
                    arrivalTime = reorderedArrival;
                    reordered = true;
                }
            }

            updatePreviousArrival(from, arrivalTime);

            boolean duplicated = !lose && shouldDuplicate();
            long duplicateArrivalTime = duplicated ? arrivalTime + Math.max(DUPLICATE_EXTRA_DELAY_MS, conditions.getJitterMillis()) : -1L;

            return new TransmissionPlan(packetIndex, sendTime, arrivalTime, lose, duplicated, duplicateArrivalTime, reordered, arrivalTime - sendTime);
        }

        private long applyBandwidth(Endpoint from, long requestedSendTime) {
            if (conditions.getBandwidthPacketsPerSecond() <= 0) {
                return requestedSendTime;
            }
            long slotMillis = Math.max(1L, Math.round(1000.0 / conditions.getBandwidthPacketsPerSecond()));
            if (from == Endpoint.CLIENT) {
                long sendTime = Math.max(requestedSendTime, clientToServerBandwidthCursor);
                clientToServerBandwidthCursor = sendTime + slotMillis;
                return sendTime;
            }
            long sendTime = Math.max(requestedSendTime, serverToClientBandwidthCursor);
            serverToClientBandwidthCursor = sendTime + slotMillis;
            return sendTime;
        }

        private long computeLatency() {
            long base = Math.max(MIN_LATENCY_MS, conditions.getBaseLatencyMillis());
            long jitter = conditions.getJitterMillis();
            if (jitter <= 0L) {
                return base;
            }
            long variation = (long) Math.floor((random.nextDouble() * ((jitter * 2) + 1)) - jitter);
            return Math.max(MIN_LATENCY_MS, base + variation);
        }

        private boolean shouldDuplicate() {
            return random.nextDouble() < conditions.getDuplicationRate();
        }

        private boolean shouldReorder() {
            return random.nextDouble() < conditions.getReorderingRate();
        }

        private long previousArrival(Endpoint from) {
            return from == Endpoint.CLIENT ? lastClientToServerArrival : lastServerToClientArrival;
        }

        private void updatePreviousArrival(Endpoint from, long arrivalTime) {
            if (from == Endpoint.CLIENT) {
                lastClientToServerArrival = arrivalTime;
            } else {
                lastServerToClientArrival = arrivalTime;
            }
        }
    }

    private static final class TimelineBuilder {
        private final SimulationClock clock = new SimulationClock();
        private final SimulationEventQueue queue = new SimulationEventQueue();
        private final SimulationScheduler scheduler = new SimulationScheduler(clock, queue);
        private final SimulationLog log = new SimulationLog();
        private SimulationSnapshot finalSnapshot;
        private long latestScheduledTime = 0L;

        private long currentTime() {
            return clock.currentTimeMillis();
        }

        private void advanceBy(long millis) {
            clock.advanceBy(millis);
        }

        private void advanceTo(long targetMillis) {
            clock.advanceTo(targetMillis);
        }

        private void log(String message) {
            logAt(clock.currentTimeMillis(), message);
        }

        private void logAt(long timestampMillis, String message) {
            log.append(message);
            scheduleAt(timestampMillis, new SimulationEvent(timestampMillis, SimulationEventType.LOG, message, null, null, null));
        }

        private void stateChanged(Endpoint endpoint, TcpState state) {
            long now = clock.currentTimeMillis();
            scheduleAt(now, new SimulationEvent(now, SimulationEventType.TCP_STATE_CHANGED, null, null, endpoint, state));
        }

        private void messageDelivered(String message) {
            messageDeliveredAt(clock.currentTimeMillis(), message);
        }

        private void messageDeliveredAt(long timestampMillis, String message) {
            scheduleAt(timestampMillis, new SimulationEvent(timestampMillis, SimulationEventType.MESSAGE_DELIVERED, message, null, null, null));
        }

        private void flowControlUpdated(FlowControlSnapshot snapshot, long timestampMillis) {
            scheduleAt(timestampMillis, new SimulationEvent(timestampMillis, SimulationEventType.FLOW_CONTROL_UPDATED,
                    null, null, null, null, snapshot));
        }

        private void congestionUpdated(CongestionSnapshot snapshot, long timestampMillis) {
            scheduleAt(timestampMillis, new SimulationEvent(timestampMillis, SimulationEventType.CONGESTION_UPDATED,
                    null, null, null, null, null, snapshot));
        }

        private TransmissionOutcome transmit(Packet packet, TransmissionPlan plan) {
            Packet created = copyPacket(packet, PacketStatus.IN_TRANSIT);
            scheduleAt(plan.sendTime(), new SimulationEvent(plan.sendTime(), SimulationEventType.PACKET_CREATED, null, created, null, null));

            if (plan.reordered()) {
                logAt(plan.sendTime(), "[NET] " + packet.label() + " llegará fuera de orden.");
            }
            if (plan.latencyMillis() != 1200L) {
                logAt(plan.sendTime(), "[NET] " + packet.label() + " usa latencia efectiva de " + plan.latencyMillis() + " ms.");
            }

            if (plan.lost()) {
                Packet lost = copyPacket(packet, PacketStatus.LOST);
                scheduleAt(plan.arrivalTime(), new SimulationEvent(plan.arrivalTime(), SimulationEventType.PACKET_LOST, null, lost, null, null));
                logAt(plan.arrivalTime(), packet.label() + " se perdió en la red.");
                return new TransmissionOutcome(plan.arrivalTime(), plan.arrivalTime());
            }

            Packet delivered = copyPacket(packet, PacketStatus.DELIVERED);
            scheduleAt(plan.arrivalTime(), new SimulationEvent(plan.arrivalTime(), SimulationEventType.PACKET_DELIVERED, null, delivered, null, null));

            long latest = plan.arrivalTime();
            if (plan.duplicated()) {
                Packet duplicateCreated = duplicateOf(packet);
                Packet duplicateDelivered = copyPacket(duplicateCreated, PacketStatus.DELIVERED);
                long duplicateSendTime = Math.min(plan.duplicateArrivalTime(), plan.sendTime() + DUPLICATE_EXTRA_DELAY_MS);
                scheduleAt(duplicateSendTime, new SimulationEvent(duplicateSendTime, SimulationEventType.PACKET_CREATED, null,
                        copyPacket(duplicateCreated, PacketStatus.IN_TRANSIT), null, null));
                scheduleAt(plan.duplicateArrivalTime(), new SimulationEvent(plan.duplicateArrivalTime(), SimulationEventType.PACKET_DELIVERED, null,
                        duplicateDelivered, null, null));
                logAt(duplicateSendTime, "[NET] " + packet.label() + " se ha duplicado en la red.");
                latest = Math.max(latest, plan.duplicateArrivalTime());
            }
            return new TransmissionOutcome(plan.arrivalTime(), latest);
        }

        private void completed(SimulationSnapshot snapshot) {
            this.finalSnapshot = snapshot;
            long completionTime = Math.max(clock.currentTimeMillis(), latestScheduledTime) + 1L;
            scheduleAt(completionTime, new SimulationEvent(completionTime, SimulationEventType.SCENARIO_COMPLETED, null, null, null, null));
        }

        private SimulationResult toResult(Scenario scenario) {
            return new SimulationResult(scenario, scheduler.drainInOrder(), finalSnapshot, log.entries());
        }

        private void scheduleAt(long timestampMillis, SimulationEvent event) {
            latestScheduledTime = Math.max(latestScheduledTime, timestampMillis);
            scheduler.scheduleAt(timestampMillis, event);
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
                    packet.isRetransmission(),
                    packet.getWindowSize()
            );
        }

        private static Packet duplicateOf(Packet packet) {
            return new Packet(
                    packet.getId() + "-dup-" + UUID.randomUUID(),
                    packet.getProtocolType(),
                    packet.getFrom(),
                    packet.getTo(),
                    packet.getKind(),
                    packet.getSeq(),
                    packet.getAck(),
                    packet.getPayload(),
                    PacketStatus.CREATED,
                    packet.isRetransmission(),
                    packet.getWindowSize()
            );
        }
    }

    private FlowControlSnapshot buildFlowSnapshot(TcpSlidingWindow slidingWindow, TcpReceiverBuffer receiverBuffer,
                                                  int advertisedWindow, String message) {
        return new FlowControlSnapshot(
                slidingWindow.configuredWindowBytes(),
                slidingWindow.bytesSent(),
                slidingWindow.bytesAcknowledged(),
                slidingWindow.bytesInFlight(),
                slidingWindow.bytesPending(message),
                receiverBuffer.capacityBytes(),
                receiverBuffer.usedBytes(),
                advertisedWindow
        );
    }

    private CongestionSnapshot buildCongestionSnapshot(TcpCongestionControl congestionControl, int bytesInFlight,
                                                       int effectiveWindowBytes, int step, String reason,
                                                       boolean lossEvent) {
        return new CongestionSnapshot(
                congestionControl.phase(),
                congestionControl.currentWindowBytes(),
                congestionControl.slowStartThresholdBytes(),
                congestionControl.duplicateAckCount(),
                effectiveWindowBytes,
                bytesInFlight,
                reason,
                new CwndHistoryPoint(step, congestionControl.currentWindowBytes(), reason, lossEvent)
        );
    }

    private record TransmissionPlan(int packetIndex, long sendTime, long arrivalTime, boolean lost, boolean duplicated,
                                    long duplicateArrivalTime, boolean reordered, long latencyMillis) {
    }

    private record TransmissionOutcome(long primaryArrivalTime, long latestRelevantTime) {
    }

    private static final class OutstandingSegment {
        private final int segmentNumber;
        private final int seq;
        private final String payload;
        private long sendTime;
        private long arrivalTime;
        private boolean lost;
        private boolean acknowledged;
        private boolean arrivalProcessed;
        private boolean timeoutProcessed;
        private boolean fastRetransmitted;
        private PendingAck pendingAck;

        private OutstandingSegment(int segmentNumber, int seq, String payload, long sendTime, long arrivalTime,
                                   boolean lost, boolean acknowledged, boolean arrivalProcessed) {
            this.segmentNumber = segmentNumber;
            this.seq = seq;
            this.payload = payload;
            this.sendTime = sendTime;
            this.arrivalTime = arrivalTime;
            this.lost = lost;
            this.acknowledged = acknowledged;
            this.arrivalProcessed = arrivalProcessed;
        }
    }

    private static final class PendingAck {
        private final long arrivalTime;
        private final int ackValue;
        private final int advertisedWindow;
        private boolean processed;

        private PendingAck(long arrivalTime, int ackValue, int advertisedWindow) {
            this.arrivalTime = arrivalTime;
            this.ackValue = ackValue;
            this.advertisedWindow = advertisedWindow;
        }
    }
}
