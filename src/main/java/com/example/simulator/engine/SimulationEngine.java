package com.example.simulator.engine;

import com.example.simulator.model.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

public class SimulationEngine {
    private final SimulationListener listener;
    private final List<Timeline> runningTimelines = new ArrayList<>();
    private ProtocolType protocolType = ProtocolType.TCP;
    private double packetLossRate = 0.20;
    private double speedFactor = 1.0;
    private int tcpSegmentSize = 8;
    private int udpDatagramSize = 3;
    private boolean stepMode = false;
    private final Deque<Runnable> pendingStepActions = new ArrayDeque<>();
    private TcpState clientState = TcpState.CLOSED;
    private TcpState serverState = TcpState.CLOSED;
    private int nextSeq = 1;

    public SimulationEngine(SimulationListener listener) {
        this.listener = listener;
    }

    public void setProtocolType(ProtocolType protocolType) {
        this.protocolType = protocolType;
    }

    public void setPacketLossRate(double packetLossRate) {
        this.packetLossRate = packetLossRate;
    }

    public void setSpeedFactor(double speedFactor) {
        this.speedFactor = speedFactor;
    }

    public void setFragmentSize(int size) {
        int safe = Math.max(1, size);
        this.tcpSegmentSize = safe;
        this.udpDatagramSize = safe;
    }

    public void setStepMode(boolean stepMode) {
        this.stepMode = stepMode;
        if (!stepMode) {
            pendingStepActions.clear();
        }
    }

    public void stepForward() {
        Runnable next = pendingStepActions.pollFirst();
        if (next == null) {
            return;
        }
        Platform.runLater(next);
    }

    public int getPendingStepCount() {
        return pendingStepActions.size();
    }

    public boolean isStepMode() {
        return stepMode;
    }

    public void reset() {
        runningTimelines.forEach(Timeline::stop);
        runningTimelines.clear();
        pendingStepActions.clear();
        nextSeq = 1;
        clientState = TcpState.CLOSED;
        serverState = TcpState.CLOSED;
        listener.onReset();
        listener.onTcpStateChanged(Endpoint.CLIENT, clientState);
        listener.onTcpStateChanged(Endpoint.SERVER, serverState);
        listener.onLog("Simulación reiniciada.");
    }

    public void runScenario(String message) {
        if (message == null || message.isBlank()) {
            message = "HOLA";
        }
        reset();
        if (protocolType == ProtocolType.TCP) {
            runTcpScenario(message.trim());
        } else {
            runUdpScenario(message.trim());
        }
    }

    private void runTcpScenario(String message) {
        listener.onLog("[TCP] Inicio de simulación.");
        serverState = TcpState.LISTEN;
        listener.onTcpStateChanged(Endpoint.SERVER, serverState);
        clientState = TcpState.SYN_SENT;
        listener.onTcpStateChanged(Endpoint.CLIENT, clientState);
        listener.onLog("[TCP] 3-way handshake 1/3: Cliente envia SYN.");

        Packet syn = packet(ProtocolType.TCP, Endpoint.CLIENT, Endpoint.SERVER, PacketKind.SYN, 0, 0, "", false);
        animateSend(syn, false, () -> {
            serverState = TcpState.SYN_RECEIVED;
            listener.onTcpStateChanged(Endpoint.SERVER, serverState);
            listener.onLog("[TCP] 3-way handshake 2/3: Servidor envia SYN-ACK.");

            Packet synAck = packet(ProtocolType.TCP, Endpoint.SERVER, Endpoint.CLIENT, PacketKind.SYN_ACK, 0, 1, "", false);
            animateSend(synAck, false, () -> {
                clientState = TcpState.ESTABLISHED;
                serverState = TcpState.ESTABLISHED;
                listener.onTcpStateChanged(Endpoint.CLIENT, clientState);
                listener.onTcpStateChanged(Endpoint.SERVER, serverState);
                listener.onLog("[TCP] 3-way handshake 3/3: Cliente envia ACK final.");
                listener.onLog("[TCP] 3-way handshake completado: conexión ESTABLISHED.");

                Packet ack = packet(ProtocolType.TCP, Endpoint.CLIENT, Endpoint.SERVER, PacketKind.ACK, 0, 1, "", false);
                animateSend(ack, false, () -> sendTcpData(message));
            });
        });
    }

    private void sendTcpData(String message) {
        int initialSeq = nextSeq;
        nextSeq += message.length();
        String[] segments = splitMessage(message, tcpSegmentSize);
        listener.onLog("[TCP] Mensaje fragmentado en " + segments.length + " segmentos (tamano maximo " + tcpSegmentSize + ").");
        sendTcpSegment(segments, 0, initialSeq, message);
    }

    private void sendTcpSegment(String[] segments, int index, int seq, String fullMessage) {
        if (index >= segments.length) {
            startTcpClose(fullMessage);
            return;
        }
        String payload = segments[index];
        int segmentNumber = index + 1;
        listener.onLog("[TCP] Envio de segmento " + segmentNumber + "/" + segments.length + " SEQ=" + seq + " payload=\"" + payload + "\".");
        Packet data = packet(ProtocolType.TCP, Endpoint.CLIENT, Endpoint.SERVER, PacketKind.DATA, seq, 0, payload, false);
        boolean shouldLose = Math.random() < packetLossRate;

        animateSend(data, shouldLose, () -> {
            int ackValue = seq + payload.length();
            Packet ack = packet(ProtocolType.TCP, Endpoint.SERVER, Endpoint.CLIENT, PacketKind.ACK, 0, ackValue, "", false);
            listener.onLog("[TCP] ACK del segmento " + segmentNumber + "/" + segments.length + ": ACK=" + ackValue + ".");
            animateSend(ack, false, () -> sendTcpSegment(segments, index + 1, ackValue, fullMessage));
        });

        if (shouldLose) {
            schedule(2200, () -> {
                listener.onLog("[TCP] Timeout en segmento " + segmentNumber + "/" + segments.length + " (SEQ=" + seq + "). Retransmisión.");
                Packet retx = packet(ProtocolType.TCP, Endpoint.CLIENT, Endpoint.SERVER, PacketKind.RETRANSMISSION, seq, 0, payload, true);
                animateSend(retx, false, () -> {
                    int ackValue = seq + payload.length();
                    Packet ack = packet(ProtocolType.TCP, Endpoint.SERVER, Endpoint.CLIENT, PacketKind.ACK, 0, ackValue, "", false);
                    listener.onLog("[TCP] ACK tras retransmisión del segmento " + segmentNumber + "/" + segments.length + ": ACK=" + ackValue + ".");
                    animateSend(ack, false, () -> sendTcpSegment(segments, index + 1, ackValue, fullMessage));
                });
            });
        }
    }

    private void runUdpScenario(String message) {
        listener.onLog("[UDP] Inicio de simulación sin conexión.");
        String[] chunks = splitMessage(message, udpDatagramSize);
        listener.onLog("[UDP] Mensaje fragmentado en " + chunks.length + " datagramas (tamano maximo " + udpDatagramSize + ").");
        for (int i = 0; i < chunks.length; i++) {
            int index = i;
            schedule(i * 700L, () -> {
                String payload = chunks[index];
                Packet packet = packet(ProtocolType.UDP, Endpoint.CLIENT, Endpoint.SERVER, PacketKind.UDP_DATAGRAM, index + 1, 0, payload, false);
                boolean shouldLose = Math.random() < packetLossRate;
                animateSend(packet, shouldLose, () -> {
                    listener.onLog("[UDP] Servidor recibió datagrama #" + (index + 1) + " payload=\"" + payload + "\".");
                    listener.onMessageDelivered("UDP recibió: " + payload);
                });
            });
        }
        schedule(chunks.length * 700L + 900L, () -> {
            listener.onLog("[UDP] Fin de simulación: no hay ACK ni retransmisión.");
            listener.onScenarioCompleted();
        });
    }

    private void startTcpClose(String fullMessage) {
        listener.onLog("[TCP] Inicio del cierre de conexión (4-way close).");
        int finSeq = nextSeq++;
        clientState = TcpState.FIN_WAIT_1;
        listener.onTcpStateChanged(Endpoint.CLIENT, clientState);
        listener.onLog("[TCP] Cierre 1/4: Cliente envia FIN.");

        Packet fin = packet(ProtocolType.TCP, Endpoint.CLIENT, Endpoint.SERVER, PacketKind.FIN, finSeq, 0, "", false);
        animateSend(fin, false, () -> {
            serverState = TcpState.CLOSE_WAIT;
            listener.onTcpStateChanged(Endpoint.SERVER, serverState);
            listener.onLog("[TCP] Cierre 2/4: Servidor responde ACK=" + (finSeq + 1) + ".");

            Packet ackFromServer = packet(ProtocolType.TCP, Endpoint.SERVER, Endpoint.CLIENT, PacketKind.ACK, 0, finSeq + 1, "", false);
            animateSend(ackFromServer, false, () -> {
                serverState = TcpState.LAST_ACK;
                listener.onTcpStateChanged(Endpoint.SERVER, serverState);
                listener.onLog("[TCP] Cierre 3/4: Servidor envia FIN.");

                Packet serverFin = packet(ProtocolType.TCP, Endpoint.SERVER, Endpoint.CLIENT, PacketKind.FIN, 1, finSeq + 1, "", false);
                animateSend(serverFin, false, () -> {
                    clientState = TcpState.TIME_WAIT;
                    listener.onTcpStateChanged(Endpoint.CLIENT, clientState);
                    listener.onLog("[TCP] Cierre 4/4: Cliente responde ACK final.");

                    Packet finalAck = packet(ProtocolType.TCP, Endpoint.CLIENT, Endpoint.SERVER, PacketKind.ACK, 0, 2, "", false);
                    animateSend(finalAck, false, () -> {
                        clientState = TcpState.CLOSED;
                        serverState = TcpState.CLOSED;
                        listener.onTcpStateChanged(Endpoint.CLIENT, clientState);
                        listener.onTcpStateChanged(Endpoint.SERVER, serverState);
                        listener.onLog("[TCP] Conexión cerrada correctamente.");
                        listener.onMessageDelivered("TCP entregó: " + fullMessage + " y cerró la conexión.");
                        listener.onScenarioCompleted();
                    });
                });
            });
        });
    }

    private String[] splitMessage(String message, int size) {
        List<String> parts = new ArrayList<>();
        for (int i = 0; i < message.length(); i += size) {
            parts.add(message.substring(i, Math.min(message.length(), i + size)));
        }
        return parts.toArray(new String[0]);
    }

    private Packet packet(ProtocolType protocol, Endpoint from, Endpoint to, PacketKind kind, int seq, int ack, String payload, boolean retransmission) {
        return new Packet(UUID.randomUUID().toString(), protocol, from, to, kind, seq, ack, payload, PacketStatus.CREATED, retransmission);
    }

    private void animateSend(Packet packet, boolean lose, Runnable onDelivered) {
        packet.setStatus(PacketStatus.IN_TRANSIT);
        listener.onPacketCreated(packet);
        listener.onLog(packet.getFrom() + " -> " + packet.getTo() + " | " + packet.label());

        schedule(1200, () -> {
            if (lose) {
                packet.setStatus(PacketStatus.LOST);
                listener.onPacketLost(packet);
                listener.onLog(packet.label() + " se perdió en la red.");
                return;
            }
            packet.setStatus(PacketStatus.DELIVERED);
            listener.onPacketDelivered(packet);
            onDelivered.run();
        });
    }

    private void schedule(long millis, Runnable action) {
        if (stepMode) {
            pendingStepActions.addLast(action);
            return;
        }
        double adjustedMillis = Math.max(120, millis / speedFactor);
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(adjustedMillis), event -> Platform.runLater(action)));
        timeline.setCycleCount(1);
        runningTimelines.add(timeline);
        timeline.setOnFinished(e -> runningTimelines.remove(timeline));
        timeline.play();
    }
}
