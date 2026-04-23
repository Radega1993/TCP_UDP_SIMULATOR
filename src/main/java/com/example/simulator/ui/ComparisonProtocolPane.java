package com.example.simulator.ui;

import com.example.simulator.domain.network.Endpoint;
import com.example.simulator.domain.protocol.PacketKind;
import com.example.simulator.domain.protocol.PacketStatus;
import com.example.simulator.domain.protocol.ProtocolType;
import com.example.simulator.domain.protocol.tcp.TcpState;
import com.example.simulator.domain.simulation.Packet;
import com.example.simulator.domain.simulation.SimulationResult;
import com.example.simulator.presentation.playback.JavaFxSimulationPlayer;
import com.example.simulator.presentation.playback.SimulationPlaybackListener;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class ComparisonProtocolPane extends DashboardCard implements SimulationPlaybackListener {
    private static final double COMPARISON_CANVAS_SCALE = 0.72;
    private static final double PACKET_START_X = 150;
    private static final double PACKET_END_X = 500;
    private static final double PACKET_Y_CLIENT_TO_SERVER = 206;
    private static final double PACKET_Y_SERVER_TO_CLIENT = 248;
    private final ProtocolType protocol;
    private final JavaFxSimulationPlayer player;
    private final Consumer<String> packetDetailsOpener;
    private final NetworkCanvas networkCanvas = new NetworkCanvas();
    private final Pane networkPane = networkCanvas;
    private final SequenceDiagramView sequenceDiagramView;
    private final StackPane simulationModeStack;
    private final DashboardCard simulationCard;
    private final VBox clientPacketList;
    private final VBox serverPacketList;
    private final Label clientStateLabel;
    private final Label serverStateLabel;
    private final Label statusLabel;
    private final TextArea sentArea;
    private final TextArea receivedArea;
    private final TextArea logArea;
    private final Map<String, PacketNode> packetNodes = new HashMap<>();
    private final Map<String, double[]> packetTravel = new HashMap<>();
    private final Map<Integer, String> udpSentChunks = new LinkedHashMap<>();
    private final Map<Integer, String> udpDeliveredChunks = new LinkedHashMap<>();
    private final Set<Integer> udpLostChunks = new LinkedHashSet<>();
    private final Set<Integer> tcpDeliveredSeqs = new LinkedHashSet<>();
    private final Map<Integer, String> tcpSentSegments = new LinkedHashMap<>();
    private final Map<Integer, String> tcpReceivedSegments = new LinkedHashMap<>();
    private String currentMessage = "";
    private String tcpDeliveredMessage = "";

    public ComparisonProtocolPane(ProtocolType protocol, TextArea logArea, Consumer<String> packetDetailsOpener) {
        super(protocol == ProtocolType.TCP ? "COMPARACIÓN IZQUIERDA" : "COMPARACIÓN DERECHA",
                protocol == ProtocolType.TCP ? "TCP" : "UDP",
                protocol == ProtocolType.TCP ? "Fiabilidad, ACK y retransmisión" : "Ligero, rápido y sin garantías");
        this.protocol = protocol;
        this.logArea = logArea;
        this.packetDetailsOpener = packetDetailsOpener;
        this.player = new JavaFxSimulationPlayer(this);
        setStyle(UiTheme.HERO_CARD);

        Label chip = new Label(protocol == ProtocolType.TCP ? "Con control y recuperación" : "Con entrega simple");
        chip.setStyle(UiTheme.CHIP);

        MailboxPanel clientCard = new MailboxPanel("Cliente", "Salida del emisor");
        MailboxPanel serverCard = new MailboxPanel("Servidor", "Llegadas al receptor");
        clientPacketList = clientCard.getItemsBox();
        serverPacketList = serverCard.getItemsBox();
        clientCard.setViewportHeight(250);
        serverCard.setViewportHeight(250);
        clientCard.setPrefWidth(118);
        serverCard.setPrefWidth(118);
        clientCard.setMinWidth(110);
        serverCard.setMinWidth(110);

        simulationCard = new DashboardCard("SIMULACIÓN", "Escena del protocolo", "Cliente, red y servidor en una vista espejo.");
        simulationCard.setPrefHeight(400);
        simulationCard.setMinHeight(400);
        simulationCard.setMaxHeight(400);
        StackPane simulationStage = new StackPane(networkCanvas);
        simulationStage.setAlignment(Pos.CENTER);
        simulationStage.setPrefWidth(520);
        simulationStage.setMinWidth(520);
        simulationStage.setMaxWidth(520);
        simulationStage.setPrefHeight(320);
        simulationStage.setMinHeight(320);
        simulationStage.setMaxHeight(320);
        simulationStage.setStyle(UiTheme.PANEL_INSET);

        Rectangle clip = new Rectangle(520, 320);
        simulationStage.setClip(clip);
        simulationStage.layoutBoundsProperty().addListener((obs, oldBounds, bounds) -> {
            clip.setWidth(bounds.getWidth());
            clip.setHeight(bounds.getHeight());
        });

        networkCanvas.setScaleX(COMPARISON_CANVAS_SCALE);
        networkCanvas.setScaleY(COMPARISON_CANVAS_SCALE);
        networkCanvas.setPrefWidth(700);
        networkCanvas.setMinWidth(700);
        networkCanvas.setPrefHeight(430);
        networkCanvas.setMinHeight(430);

        HBox networkRow = new HBox(14, clientCard, simulationStage, serverCard);
        networkRow.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(simulationStage, Priority.ALWAYS);
        sequenceDiagramView = new SequenceDiagramView(
                protocol == ProtocolType.TCP ? "Diagrama secuencial TCP" : "Diagrama secuencial UDP",
                packetDetailsOpener
        );
        sequenceDiagramView.setVisible(false);
        sequenceDiagramView.setManaged(false);
        simulationModeStack = new StackPane(networkRow, sequenceDiagramView);
        simulationCard.setContent(simulationModeStack);

        StatePanel statePanel = new StatePanel();
        clientStateLabel = statePanel.getClientStateLabel();
        serverStateLabel = statePanel.getServerStateLabel();
        statusLabel = statePanel.getStatusLabel();

        MessageSummaryPanel sentPanel = new MessageSummaryPanel("Mensaje base", "Entrada compartida");
        MessageSummaryPanel receivedPanel = new MessageSummaryPanel("Resultado", "Mensaje recibido");
        sentArea = sentPanel.getTextArea();
        receivedArea = receivedPanel.getTextArea();
        sentArea.setPrefRowCount(4);
        receivedArea.setPrefRowCount(4);
        sentPanel.setPrefHeight(180);
        receivedPanel.setPrefHeight(180);

        sentPanel.setPrefWidth(250);
        receivedPanel.setPrefWidth(250);

        HBox messages = new HBox(10, sentPanel, receivedPanel);
        HBox.setHgrow(sentPanel, Priority.ALWAYS);
        HBox.setHgrow(receivedPanel, Priority.ALWAYS);
        sentPanel.setMaxWidth(Double.MAX_VALUE);
        receivedPanel.setMaxWidth(Double.MAX_VALUE);

        getContentBox().getChildren().addAll(chip, simulationCard, statePanel, messages);
    }

    public void loadAndPlay(SimulationResult result, double speedFactor) {
        currentMessage = result.getScenario().getMessage();
        player.load(result, speedFactor);
        player.play();
    }

    public void pause() {
        player.pause();
    }

    public void play() {
        player.play();
    }

    public void stepForward() {
        player.stepForward();
    }

    public void stop() {
        player.stop();
        onReset();
    }

    public void setViewMode(SimulationViewMode mode) {
        boolean scene = mode == SimulationViewMode.SCENE;
        simulationModeStack.getChildren().get(0).setVisible(scene);
        simulationModeStack.getChildren().get(0).setManaged(scene);
        sequenceDiagramView.setVisible(!scene);
        sequenceDiagramView.setManaged(!scene);
        simulationCard.setTitle(scene ? "Escena del protocolo" : "Diagrama temporal",
                scene ? "Cliente, red y servidor en una vista espejo." : "Intercambio de mensajes ordenado en el tiempo.");
    }

    public boolean hasRemainingEvents() {
        return player.hasRemainingEvents();
    }

    public boolean isPaused() {
        return player.isPaused();
    }

    @Override
    public void onLog(String message) {
        runOnFxThread(() -> {
            logArea.appendText(message + "\n");
            sequenceDiagramView.addLogEvent(message, protocol);
        });
    }

    @Override
    public void onPacketCreated(Packet packet) {
        runOnFxThread(() -> {
            PacketNode node = new PacketNode(packet);
            double startX = packet.getFrom() == Endpoint.CLIENT ? PACKET_START_X : PACKET_END_X;
            double endX = packet.getTo() == Endpoint.SERVER ? PACKET_END_X : PACKET_START_X;
            double y = packet.getFrom() == Endpoint.CLIENT ? PACKET_Y_CLIENT_TO_SERVER : PACKET_Y_SERVER_TO_CLIENT;
            node.setLayoutX(startX);
            node.setLayoutY(y);
            node.setOnMouseClicked(event -> packetDetailsOpener.accept(buildPacketDetails(packet)));
            networkPane.getChildren().add(node);
            packetNodes.put(packet.getId(), node);
            packetTravel.put(packet.getId(), new double[]{startX, endX});
            sequenceDiagramView.addPacket(packet);

            TranslateTransition transition = new TranslateTransition(Duration.millis(950), node);
            transition.setToX(endX - startX);
            transition.play();

            if (packet.isRetransmission() || packet.getKind() == PacketKind.RETRANSMISSION) {
                node.markRetransmitted();
            }
            statusLabel.setText("En tránsito: " + packet.label());
            updateMessagePanelsOnCreated(packet);
        });
    }

    @Override
    public void onPacketDelivered(Packet packet) {
        runOnFxThread(() -> {
            PacketNode node = packetNodes.get(packet.getId());
            if (node != null) {
                double[] travel = packetTravel.get(packet.getId());
                if (travel != null) {
                    node.setTranslateX(travel[1] - travel[0]);
                }
                node.markDelivered();
                ScaleTransition pulse = new ScaleTransition(Duration.millis(220), node);
                pulse.setFromX(1.0);
                pulse.setFromY(1.0);
                pulse.setToX(1.08);
                pulse.setToY(1.08);
                pulse.setCycleCount(2);
                pulse.setAutoReverse(true);
                pulse.play();
            }
            statusLabel.setText("Entregado: " + packet.label());
            sequenceDiagramView.markPacketDelivered(packet);
            updateMessagePanelsOnDelivered(packet);
            archivePacketCard(packet, packet.getTo());
        });
    }

    @Override
    public void onPacketLost(Packet packet) {
        runOnFxThread(() -> {
            PacketNode node = packetNodes.get(packet.getId());
            if (node != null) {
                double[] travel = packetTravel.get(packet.getId());
                if (travel != null) {
                    node.setTranslateX((travel[1] - travel[0]) * 0.52);
                }
                node.markLost();
                FadeTransition fade = new FadeTransition(Duration.millis(500), node);
                fade.setFromValue(0.8);
                fade.setToValue(0.45);
                fade.play();
            }
            statusLabel.setText("Perdido: " + packet.label());
            sequenceDiagramView.markPacketLost(packet);
            updateMessagePanelsOnLost(packet);
            archivePacketCard(packet, packet.getFrom());
        });
    }

    @Override
    public void onTcpStateChanged(Endpoint endpoint, TcpState newState) {
        runOnFxThread(() -> {
            if (endpoint == Endpoint.CLIENT) {
                clientStateLabel.setText("Cliente: " + newState);
            } else {
                serverStateLabel.setText("Servidor: " + newState);
            }
        });
    }

    @Override
    public void onMessageDelivered(String message) {
        runOnFxThread(() -> statusLabel.setText(message));
    }

    @Override
    public void onScenarioCompleted() {
        runOnFxThread(() -> statusLabel.setText(protocol + " completó la simulación."));
    }

    @Override
    public void onReset() {
        runOnFxThread(() -> {
            networkPane.getChildren().removeIf(node -> node instanceof PacketNode);
            packetNodes.clear();
            packetTravel.clear();
            clientPacketList.getChildren().clear();
            serverPacketList.getChildren().clear();
            logArea.clear();
            udpSentChunks.clear();
            udpDeliveredChunks.clear();
            udpLostChunks.clear();
            tcpDeliveredSeqs.clear();
            tcpSentSegments.clear();
            tcpReceivedSegments.clear();
            tcpDeliveredMessage = "";
            sequenceDiagramView.reset();
            sentArea.setText("Mensaje compartido:\n\"" + currentMessage + "\"");
            receivedArea.setText("Aún sin entrega.");
            clientStateLabel.setText("Cliente: CLOSED");
            serverStateLabel.setText("Servidor: CLOSED");
            statusLabel.setText("Listo para comparar");
        });
    }

    private void updateMessagePanelsOnCreated(Packet packet) {
        if (packet.getFrom() != Endpoint.CLIENT) {
            return;
        }
        sentArea.setText("Mensaje compartido:\n\"" + currentMessage + "\"\n\nProtocolo: " + protocol);
        if (packet.getProtocolType() == ProtocolType.TCP
                && (packet.getKind() == PacketKind.DATA || packet.getKind() == PacketKind.RETRANSMISSION)
                && packet.getKind() == PacketKind.DATA) {
            tcpSentSegments.put(packet.getSeq(), packet.getPayload());
        }
        if (packet.getProtocolType() == ProtocolType.UDP && packet.getKind() == PacketKind.UDP_DATAGRAM) {
            udpSentChunks.put(packet.getSeq(), packet.getPayload());
        }
    }

    private void updateMessagePanelsOnDelivered(Packet packet) {
        if (packet.getTo() != Endpoint.SERVER) {
            return;
        }
        if (packet.getProtocolType() == ProtocolType.TCP
                && (packet.getKind() == PacketKind.DATA || packet.getKind() == PacketKind.RETRANSMISSION)) {
            if (!tcpDeliveredSeqs.contains(packet.getSeq())) {
                tcpDeliveredSeqs.add(packet.getSeq());
                tcpReceivedSegments.put(packet.getSeq(), packet.getPayload());
                tcpDeliveredMessage = buildTcpReceivedMessage();
            }
            receivedArea.setText("TCP recibió:\n\"" + tcpDeliveredMessage + "\"\n\nACK y orden gestionados por el protocolo.");
            return;
        }
        if (packet.getProtocolType() == ProtocolType.UDP && packet.getKind() == PacketKind.UDP_DATAGRAM) {
            udpDeliveredChunks.put(packet.getSeq(), packet.getPayload());
            udpLostChunks.remove(packet.getSeq());
            receivedArea.setText("UDP recibió:\n\"" + buildUdpReceivedPreview() + "\"\n\nSin ACK ni retransmisión.");
        }
    }

    private void updateMessagePanelsOnLost(Packet packet) {
        if (packet.getProtocolType() == ProtocolType.UDP && packet.getKind() == PacketKind.UDP_DATAGRAM) {
            udpLostChunks.add(packet.getSeq());
            receivedArea.setText("UDP recibió:\n\"" + buildUdpReceivedPreview() + "\"\n\nDatagramas perdidos: " + udpLostChunks);
        }
    }

    private String buildUdpReceivedPreview() {
        if (udpSentChunks.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Integer id : udpSentChunks.keySet()) {
            builder.append(udpDeliveredChunks.getOrDefault(id, "___"));
        }
        return builder.toString();
    }

    private String buildTcpReceivedMessage() {
        if (tcpReceivedSegments.isEmpty()) {
            return "";
        }
        List<Integer> orderedSeq = new ArrayList<>(tcpReceivedSegments.keySet());
        orderedSeq.sort(Integer::compareTo);
        StringBuilder builder = new StringBuilder();
        for (Integer seq : orderedSeq) {
            builder.append(tcpReceivedSegments.get(seq));
        }
        return builder.toString();
    }

    private void archivePacketCard(Packet packet, Endpoint side) {
        Packet archivedPacket = copyPacket(packet);
        PacketNode archived = new PacketNode(archivedPacket);
        archived.setScaleX(0.76);
        archived.setScaleY(0.76);
        archived.setOnMouseClicked(event -> packetDetailsOpener.accept(buildPacketDetails(archivedPacket)));

        if (packet.getStatus() == PacketStatus.DELIVERED) {
            archived.markDelivered();
        } else if (packet.getStatus() == PacketStatus.LOST) {
            archived.markLost();
        }

        VBox targetList = side == Endpoint.CLIENT ? clientPacketList : serverPacketList;
        targetList.getChildren().add(archived);
    }

    private Packet copyPacket(Packet packet) {
        return new Packet(packet.getId(), packet.getProtocolType(), packet.getFrom(), packet.getTo(), packet.getKind(),
                packet.getSeq(), packet.getAck(), packet.getPayload(), packet.getStatus(), packet.isRetransmission());
    }

    private String buildPacketDetails(Packet packet) {
        String payload = packet.getPayload() == null || packet.getPayload().isBlank() ? "-" : packet.getPayload();
        return "Protocolo: " + packet.getProtocolType() + "\n"
                + "Tipo: " + packet.getKind() + "\n"
                + "Origen: " + packet.getFrom() + "\n"
                + "Destino: " + packet.getTo() + "\n"
                + "SEQ: " + packet.getSeq() + "\n"
                + "ACK: " + packet.getAck() + "\n"
                + "Payload: " + payload + "\n"
                + "Estado: " + packet.getStatus();
    }

    private void runOnFxThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }
}
