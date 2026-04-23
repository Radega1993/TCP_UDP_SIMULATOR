package com.example.simulator.app;

import com.example.simulator.application.dto.SimulationCommand;
import com.example.simulator.domain.network.Endpoint;
import com.example.simulator.domain.protocol.PacketKind;
import com.example.simulator.domain.protocol.PacketStatus;
import com.example.simulator.domain.protocol.ProtocolType;
import com.example.simulator.domain.protocol.tcp.TcpState;
import com.example.simulator.domain.scenario.Scenario;
import com.example.simulator.domain.simulation.Packet;
import com.example.simulator.presentation.playback.JavaFxSimulationPlayer;
import com.example.simulator.presentation.playback.SimulationPlaybackListener;
import com.example.simulator.presentation.viewmodel.SimulationViewModel;
import com.example.simulator.ui.ControlPanel;
import com.example.simulator.ui.DashboardCard;
import com.example.simulator.ui.EventLogPanel;
import com.example.simulator.ui.MailboxPanel;
import com.example.simulator.ui.MessageSummaryPanel;
import com.example.simulator.ui.NetworkCanvas;
import com.example.simulator.ui.PacketNode;
import com.example.simulator.ui.SimulatorHeader;
import com.example.simulator.ui.StatePanel;
import com.example.simulator.ui.UiTheme;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class SimulatorApp extends Application implements SimulationPlaybackListener {
    private Pane networkPane;
    private NetworkCanvas networkCanvas;
    private TextArea logArea;
    private TextArea packetDetailsArea;
    private TextArea clientDataArea;
    private TextArea serverDataArea;
    private TextArea theoryArea;
    private VBox clientPacketList;
    private VBox serverPacketList;
    private Label clientStateLabel;
    private Label serverStateLabel;
    private Label statusLabel;
    private SimulationViewModel viewModel;
    private JavaFxSimulationPlayer player;
    private ComboBox<ProtocolType> protocolSelector;
    private ComboBox<Scenario> scenarioSelector;
    private Slider lossSlider;
    private Slider speedSlider;
    private TextField messageField;
    private Stage primaryStage;
    private Spinner<Integer> widthSpinner;
    private Spinner<Integer> heightSpinner;
    private Spinner<Integer> fragmentSizeSpinner;
    private ComboBox<String> windowPresetSelector;
    private HBox reviewControlsBox;
    private Button reviewStepBackButton;
    private Button reviewStepForwardButton;
    private Button reviewFirstButton;
    private Button reviewLastButton;
    private Button playPauseButton;
    private Button liveStepButton;
    private Label stepIndicatorLabel;
    private List<Scenario> scenarios = List.of();
    private ControlPanel controlPanel;
    private final List<UiSnapshot> uiSnapshots = new ArrayList<>();
    private int currentSnapshotIndex = -1;
    private boolean restoringSnapshot = false;
    private final Map<String, PacketNode> packetNodes = new HashMap<>();
    private final Map<String, double[]> packetTravel = new HashMap<>();
    private final Map<String, Integer> packetLane = new HashMap<>();
    private final boolean[] c2sLaneBusy = new boolean[6];
    private final boolean[] s2cLaneBusy = new boolean[6];
    private final Map<Integer, String> udpSentChunks = new LinkedHashMap<>();
    private final Map<Integer, String> udpDeliveredChunks = new LinkedHashMap<>();
    private final Set<Integer> udpLostChunks = new LinkedHashSet<>();
    private final Set<Integer> tcpDeliveredSeqs = new LinkedHashSet<>();
    private final Map<Integer, String> tcpSentSegments = new LinkedHashMap<>();
    private final Map<Integer, String> tcpReceivedSegments = new LinkedHashMap<>();
    private String currentMessage = "";
    private String tcpDeliveredMessage = "";
    private ProtocolType currentProtocol = ProtocolType.TCP;
    private long scenarioStartMillis = System.currentTimeMillis();
    private boolean scenarioStartRequested = false;
    private String pendingMessage = "";
    private ProtocolType pendingProtocol = ProtocolType.TCP;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        Bootstrap bootstrap = new Bootstrap();
        viewModel = bootstrap.createSimulationViewModel();
        player = new JavaFxSimulationPlayer(this);
        scenarios = viewModel.availableScenarios();

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(16));
        root.setStyle(UiTheme.APP_BACKGROUND);

        root.setTop(buildHeader());
        root.setCenter(buildCenter());
        root.setRight(buildRightPanel());
        root.setBottom(buildFooter());

        Scene scene = new Scene(root, 1500, 860);
        stage.setTitle("Simulador gráfico TCP y UDP - JavaFX");
        stage.setScene(scene);
        loadAppIcon(stage);
        stage.setMaximized(true);
        stage.show();

        onReset();
    }

    private VBox buildHeader() {
        VBox box = new VBox(new SimulatorHeader());
        box.setPadding(new Insets(0, 0, 18, 0));
        return box;
    }

    private VBox buildCenter() {
        networkCanvas = new NetworkCanvas();
        networkPane = networkCanvas;

        MailboxPanel clientHistoryCard = new MailboxPanel("Buzón cliente", "Salida del emisor");
        MailboxPanel serverHistoryCard = new MailboxPanel("Buzón servidor", "Llegadas al receptor");
        clientPacketList = clientHistoryCard.getItemsBox();
        serverPacketList = serverHistoryCard.getItemsBox();
        clientHistoryCard.setPrefWidth(236);
        clientHistoryCard.setMinWidth(220);
        serverHistoryCard.setPrefWidth(236);
        serverHistoryCard.setMinWidth(220);

        HBox centerRow = new HBox(14, clientHistoryCard, networkCanvas, serverHistoryCard);
        HBox.setHgrow(networkCanvas, Priority.ALWAYS);
        VBox.setVgrow(centerRow, Priority.ALWAYS);

        VBox box = new VBox(14, centerRow, buildControls());
        return box;
    }

    private Node buildRightPanel() {
        VBox panel = new VBox(16);
        panel.setPadding(new Insets(0, 0, 0, 14));
        panel.setPrefWidth(390);
        panel.setMinWidth(360);

        StatePanel statePanel = new StatePanel();
        clientStateLabel = statePanel.getClientStateLabel();
        serverStateLabel = statePanel.getServerStateLabel();
        statusLabel = statePanel.getStatusLabel();

        packetDetailsArea = new TextArea();
        packetDetailsArea.setEditable(false);
        packetDetailsArea.setWrapText(true);
        packetDetailsArea.setPrefRowCount(6);
        packetDetailsArea.setText("Selecciona un paquete para ver su detalle.");
        packetDetailsArea.setStyle(UiTheme.MUTED_TEXT_SURFACE);
        VBox detailsCard = card("Detalle del paquete", "Inspección rápida del paquete seleccionado.", wrapInset(packetDetailsArea));

        VBox legend = new VBox(6,
                legendRow(Color.web("#93c5fd"), "Azul claro: TCP SYN"),
                legendRow(Color.web("#60a5fa"), "Azul medio: TCP SYN-ACK"),
                legendRow(Color.web("#86efac"), "Verde: TCP ACK"),
                legendRow(Color.web("#7dd3fc"), "Celeste: TCP DATA"),
                legendRow(Color.web("#c4b5fd"), "Morado: UDP"),
                legendRow(Color.web("#fecaca"), "Rojo: Perdido"),
                legendRow(Color.web("#fed7aa"), "Naranja: Retransmitido")
        );
        legend.setPadding(new Insets(6));
        VBox legendCard = card("Leyenda visual", "Código cromático de los paquetes y eventos principales.", wrapInset(legend));

        theoryArea = new TextArea();
        theoryArea.setEditable(false);
        theoryArea.setWrapText(true);
        theoryArea.setPrefRowCount(11);
        theoryArea.setStyle(UiTheme.TEXT_SURFACE);
        theoryArea.setText(viewModel.helpTextForContext("*", protocolSelector != null ? protocolSelector.getValue() : ProtocolType.TCP));
        VBox theoryCard = card("Teoría y ayuda", "Apoyo docente contextual para interpretar lo que ves en la simulación.", wrapInset(theoryArea));
        VBox.setVgrow(theoryArea, Priority.ALWAYS);

        EventLogPanel eventLogPanel = new EventLogPanel();
        logArea = eventLogPanel.getLogArea();

        panel.getChildren().addAll(statePanel, detailsCard, legendCard, theoryCard, eventLogPanel);
        VBox.setVgrow(eventLogPanel, Priority.ALWAYS);
        return panel;
    }

    private VBox buildFooter() {
        Label hint = new Label("Consejo didáctico: ejecuta la misma palabra en TCP y UDP con una pérdida del 30-40% para comparar la fiabilidad.");
        hint.setStyle("-fx-text-fill: #617487; -fx-font-size: 12px;");
        MessageSummaryPanel clientCard = new MessageSummaryPanel("Cliente: mensaje enviado", "Salida didáctica del emisor");
        MessageSummaryPanel serverCard = new MessageSummaryPanel("Servidor: mensaje recibido", "Resultado visible en destino");
        clientDataArea = clientCard.getTextArea();
        serverDataArea = serverCard.getTextArea();
        HBox messages = new HBox(16, clientCard, serverCard);
        HBox.setHgrow(clientCard, Priority.ALWAYS);
        HBox.setHgrow(serverCard, Priority.ALWAYS);
        VBox box = new VBox(10, messages, hint);
        box.setPadding(new Insets(14, 0, 0, 0));
        return box;
    }

    private VBox buildControls() {
        controlPanel = new ControlPanel(scenarios);
        protocolSelector = controlPanel.getProtocolSelector();
        scenarioSelector = controlPanel.getScenarioSelector();
        messageField = controlPanel.getMessageField();
        lossSlider = controlPanel.getLossSlider();
        speedSlider = controlPanel.getSpeedSlider();
        fragmentSizeSpinner = controlPanel.getFragmentSizeSpinner();
        widthSpinner = controlPanel.getWidthSpinner();
        heightSpinner = controlPanel.getHeightSpinner();
        windowPresetSelector = controlPanel.getWindowPresetSelector();
        playPauseButton = controlPanel.getPlayPauseButton();
        liveStepButton = controlPanel.getLiveStepButton();
        reviewFirstButton = controlPanel.getReviewFirstButton();
        reviewStepBackButton = controlPanel.getReviewStepBackButton();
        reviewStepForwardButton = controlPanel.getReviewStepForwardButton();
        reviewLastButton = controlPanel.getReviewLastButton();
        stepIndicatorLabel = controlPanel.getStepIndicatorLabel();
        reviewControlsBox = controlPanel.getReviewControlsBox();

        scenarioSelector.setOnAction(event -> applyScenarioSelection(scenarioSelector.getValue()));

        controlPanel.getRunButton().setOnAction(event -> startSimulation());
        controlPanel.getResetButton().setOnAction(event -> {
            scenarioStartRequested = false;
            player.stop();
            viewModel.reset();
            onReset();
        });
        playPauseButton.setOnAction(event -> togglePlaybackPause());
        liveStepButton.setOnAction(event -> {
            player.stepForward();
            updatePlaybackButtons();
        });
        controlPanel.getApplySizeButton().setOnAction(event -> applyWindowSize());
        controlPanel.getPresetSizeButton().setOnAction(event -> applyWindowPreset());
        reviewFirstButton.setOnAction(event -> goToFirstStep());
        reviewStepBackButton.setOnAction(event -> goToPreviousStep());
        reviewStepForwardButton.setOnAction(event -> goToNextStep());
        reviewLastButton.setOnAction(event -> goToLastStep());
        protocolSelector.setOnAction(event -> refreshTheoryPanel());
        updatePlaybackButtons();
        return controlPanel;
    }

    private void startSimulation() {
        Scenario selectedScenario = scenarioSelector.getValue();
        String inputMessage = normalizeMessage(messageField.getText());
        scenarioStartRequested = true;
        pendingMessage = inputMessage;
        pendingProtocol = protocolSelector.getValue();
        var result = selectedScenario != null
                ? viewModel.start(selectedScenario)
                : viewModel.start(new SimulationCommand(
                        protocolSelector.getValue(),
                        inputMessage,
                        fragmentSizeSpinner.getValue(),
                        lossSlider.getValue() / 100.0
                ));

        if (result.getEvents().isEmpty()) {
            statusLabel.setText("La simulación no generó eventos.");
            return;
        }

        if (selectedScenario != null) {
            pendingMessage = selectedScenario.getMessage();
            pendingProtocol = selectedScenario.getProtocol();
        } else {
            pendingProtocol = protocolSelector.getValue();
        }
        player.load(result, speedSlider.getValue());
        player.play();
        updatePlaybackButtons();
        statusLabel.setText("Simulación en vivo. Eventos cargados: " + result.getEvents().size());
        refreshTheoryPanel();
    }

    private void togglePlaybackPause() {
        if (!player.hasRemainingEvents()) {
            updatePlaybackButtons();
            return;
        }
        if (player.isPaused()) {
            player.play();
        } else {
            player.pause();
        }
        updatePlaybackButtons();
    }

    private void updatePlaybackButtons() {
        if (playPauseButton == null || liveStepButton == null) {
            return;
        }
        boolean hasRemaining = player != null && player.hasRemainingEvents();
        boolean paused = player == null || player.isPaused();
        playPauseButton.setDisable(!hasRemaining);
        liveStepButton.setDisable(!hasRemaining);
        playPauseButton.setText(paused ? "Reanudar" : "Pausar");
    }

    private void applyScenarioSelection(Scenario scenario) {
        if (scenario == null) {
            refreshTheoryPanel();
            return;
        }
        protocolSelector.setValue(scenario.getProtocol());
        messageField.setText(scenario.getMessage());
        fragmentSizeSpinner.getValueFactory().setValue(scenario.getFragmentSize());
        lossSlider.setValue(scenario.getNetworkConditions().getPacketLossRate() * 100.0);
        refreshTheoryPanel();
    }

    private void refreshTheoryPanel() {
        if (theoryArea == null || protocolSelector == null) {
            return;
        }
        Scenario selectedScenario = scenarioSelector != null ? scenarioSelector.getValue() : null;
        String scenarioId = selectedScenario == null ? "*" : selectedScenario.getId();
        theoryArea.setText(viewModel.helpTextForContext(scenarioId, protocolSelector.getValue()));
    }

    private VBox card(String title, Node content) {
        DashboardCard card = new DashboardCard(title);
        card.setContent(content);
        return card;
    }

    private VBox card(String title, String subtitle, Node content) {
        DashboardCard card = new DashboardCard(title, subtitle);
        card.setContent(content);
        return card;
    }

    private TextArea readonlyArea(int rows) {
        TextArea area = new TextArea();
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefRowCount(rows);
        area.setStyle(UiTheme.MONO + "-fx-control-inner-background: #f8fbfe; -fx-background-insets: 0; -fx-background-radius: 16;");
        return area;
    }

    private HBox legendRow(Color color, String text) {
        Rectangle swatch = new Rectangle(16, 16);
        swatch.setArcWidth(6);
        swatch.setArcHeight(6);
        swatch.setFill(color);
        swatch.setStroke(Color.web("#4a6075"));
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #203246; -fx-font-size: 12px;");
        HBox row = new HBox(8, swatch, label);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private VBox wrapInset(Node content) {
        VBox wrapper = new VBox(content);
        wrapper.setPadding(new Insets(6));
        wrapper.setStyle(UiTheme.PANEL_INSET_TINT);
        VBox.setVgrow(content, Priority.ALWAYS);
        return wrapper;
    }

    private String normalizeMessage(String raw) {
        if (raw == null || raw.isBlank()) {
            return "HOLA";
        }
        return raw.trim();
    }

    private void loadAppIcon(Stage stage) {
        try (InputStream stream = SimulatorApp.class.getResourceAsStream("/icono.png")) {
            if (stream != null) {
                stage.getIcons().add(new Image(stream));
                return;
            }
        } catch (Exception ignored) {
        }

        try (InputStream fileStream = new FileInputStream("icono.png")) {
            stage.getIcons().add(new Image(fileStream));
        } catch (Exception ignored) {
            // If icon cannot be loaded, app still starts normally.
        }
    }

    private void applyWindowSize() {
        if (primaryStage == null) {
            return;
        }
        int width = widthSpinner.getValue();
        int height = heightSpinner.getValue();
        primaryStage.setWidth(width);
        primaryStage.setHeight(height);
        statusLabel.setText("Tamaño de ventana aplicado: " + width + "x" + height);
    }

    private void applyWindowPreset() {
        String preset = windowPresetSelector.getValue();
        if (preset == null || !preset.contains("x")) {
            return;
        }
        String[] parts = preset.split("x");
        try {
            int width = Integer.parseInt(parts[0]);
            int height = Integer.parseInt(parts[1]);
            widthSpinner.getValueFactory().setValue(width);
            heightSpinner.getValueFactory().setValue(height);
            applyWindowSize();
        } catch (NumberFormatException ignored) {
            statusLabel.setText("Preset inválido: " + preset);
        }
    }

    private void goToPreviousStep() {
        if (uiSnapshots.isEmpty() || currentSnapshotIndex <= 0) {
            return;
        }
        restoreSnapshot(currentSnapshotIndex - 1);
    }

    private void goToNextStep() {
        if (!uiSnapshots.isEmpty() && currentSnapshotIndex < uiSnapshots.size() - 1) {
            restoreSnapshot(currentSnapshotIndex + 1);
        }
    }

    private void goToFirstStep() {
        if (uiSnapshots.isEmpty()) {
            return;
        }
        restoreSnapshot(0);
    }

    private void goToLastStep() {
        if (uiSnapshots.isEmpty()) {
            return;
        }
        restoreSnapshot(uiSnapshots.size() - 1);
    }

    private void updateStepIndicator() {
        if (stepIndicatorLabel == null) {
            return;
        }
        int current = uiSnapshots.isEmpty() ? 0 : currentSnapshotIndex + 1;
        int total = uiSnapshots.size();
        stepIndicatorLabel.setText("Paso: " + current + "/" + total);
        if (reviewStepBackButton != null) {
            reviewStepBackButton.setDisable(current <= 1);
        }
        if (reviewFirstButton != null) {
            reviewFirstButton.setDisable(current <= 1);
        }
        if (reviewStepForwardButton != null) {
            reviewStepForwardButton.setDisable(total == 0 || current >= total);
        }
        if (reviewLastButton != null) {
            reviewLastButton.setDisable(total == 0 || current >= total);
        }
    }

    private void recordSnapshot() {
        if (restoringSnapshot) {
            return;
        }
        if (reviewControlsBox == null) {
            return;
        }
        UiSnapshot snapshot = captureSnapshot();
        if (currentSnapshotIndex >= 0 && currentSnapshotIndex < uiSnapshots.size() - 1) {
            uiSnapshots.subList(currentSnapshotIndex + 1, uiSnapshots.size()).clear();
        }
        uiSnapshots.add(snapshot);
        currentSnapshotIndex = uiSnapshots.size() - 1;
        updateStepIndicator();
    }

    private UiSnapshot captureSnapshot() {
        List<PacketVisualSnapshot> activePackets = new ArrayList<>();
        for (Map.Entry<String, PacketNode> entry : packetNodes.entrySet()) {
            PacketNode node = entry.getValue();
            if (node == null) {
                continue;
            }
            Packet packet = node.getPacket();
            activePackets.add(new PacketVisualSnapshot(
                    copyPacket(packet),
                    node.getLayoutX(),
                    node.getLayoutY(),
                    node.getTranslateX(),
                    node.getTranslateY()
            ));
        }
        return new UiSnapshot(
                clientStateLabel.getText(),
                serverStateLabel.getText(),
                statusLabel.getText(),
                logArea.getText(),
                packetDetailsArea.getText(),
                clientDataArea.getText(),
                serverDataArea.getText(),
                captureSideList(clientPacketList),
                captureSideList(serverPacketList),
                activePackets
        );
    }

    private List<SideEntrySnapshot> captureSideList(VBox box) {
        List<SideEntrySnapshot> entries = new ArrayList<>();
        if (box == null) {
            return entries;
        }
        for (Node node : box.getChildren()) {
            if (node instanceof Label label) {
                entries.add(SideEntrySnapshot.separator(label.getText()));
            } else if (node instanceof PacketNode packetNode) {
                entries.add(SideEntrySnapshot.packet(copyPacket(packetNode.getPacket())));
            }
        }
        return entries;
    }

    private void restoreSnapshot(int targetIndex) {
        if (targetIndex < 0 || targetIndex >= uiSnapshots.size()) {
            return;
        }
        UiSnapshot snapshot = uiSnapshots.get(targetIndex);
        restoringSnapshot = true;
        try {
            clientStateLabel.setText(snapshot.clientState);
            serverStateLabel.setText(snapshot.serverState);
            statusLabel.setText(snapshot.status);
            logArea.setText(snapshot.logText);
            packetDetailsArea.setText(snapshot.packetDetailsText);
            clientDataArea.setText(snapshot.clientDataText);
            serverDataArea.setText(snapshot.serverDataText);

            restoreSideList(clientPacketList, snapshot.clientEntries);
            restoreSideList(serverPacketList, snapshot.serverEntries);
            restoreNetworkPackets(snapshot.activePackets);
            currentSnapshotIndex = targetIndex;
            updateStepIndicator();
        } finally {
            restoringSnapshot = false;
        }
    }

    private void restoreSideList(VBox targetBox, List<SideEntrySnapshot> entries) {
        if (targetBox == null) {
            return;
        }
        targetBox.getChildren().clear();
        for (SideEntrySnapshot entry : entries) {
            if (entry.separatorText != null) {
                Label separator = new Label(entry.separatorText);
                separator.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #475569;");
                separator.setMaxWidth(Double.MAX_VALUE);
                separator.setAlignment(Pos.CENTER);
                separator.setPadding(new Insets(4, 0, 2, 0));
                targetBox.getChildren().add(separator);
                continue;
            }
            if (entry.packet != null) {
                PacketNode archived = new PacketNode(entry.packet);
                archived.setScaleX(0.85);
                archived.setScaleY(0.85);
                archived.setOnMouseClicked(event -> showPacketDetails(entry.packet));
                if (entry.packet.getStatus() == PacketStatus.DELIVERED) {
                    archived.markDelivered();
                } else if (entry.packet.getStatus() == PacketStatus.LOST) {
                    archived.markLost();
                }
                targetBox.getChildren().add(archived);
            }
        }
    }

    private void restoreNetworkPackets(List<PacketVisualSnapshot> activePackets) {
        networkPane.getChildren().removeIf(node -> node instanceof PacketNode);
        packetNodes.clear();
        packetTravel.clear();
        packetLane.clear();
        clearLaneState();
        for (PacketVisualSnapshot visual : activePackets) {
            Packet packet = visual.packet;
            PacketNode node = new PacketNode(packet);
            node.setLayoutX(visual.layoutX);
            node.setLayoutY(visual.layoutY);
            node.setTranslateX(visual.translateX);
            node.setTranslateY(visual.translateY);
            node.setOnMouseClicked(event -> showPacketDetails(packet));
            if (packet.getStatus() == PacketStatus.DELIVERED) {
                node.markDelivered();
            } else if (packet.getStatus() == PacketStatus.LOST) {
                node.markLost();
            }
            networkPane.getChildren().add(node);
            packetNodes.put(packet.getId(), node);
        }
    }

    private Packet copyPacket(Packet packet) {
        return new Packet(
                packet.getId(),
                packet.getProtocolType(),
                packet.getFrom(),
                packet.getTo(),
                packet.getKind(),
                packet.getSeq(),
                packet.getAck(),
                packet.getPayload(),
                packet.getStatus(),
                packet.isRetransmission()
        );
    }

    @Override
    public void onLog(String message) {
        runOnFxThread(() -> {
            logArea.appendText(formatLog(message) + "\n");
        });
    }

    @Override
    public void onPacketCreated(Packet packet) {
        runOnFxThread(() -> {
            PacketNode node = new PacketNode(packet);
            double startX = packet.getFrom() == Endpoint.CLIENT ? 200 : 640;
            double endX = packet.getTo() == Endpoint.SERVER ? 640 : 200;
            int lane = allocateLane(packet);
            double y = laneToY(packet, lane);
            node.setLayoutX(startX);
            node.setLayoutY(y);
            node.setOnMouseClicked(event -> showPacketDetails(packet));
            networkPane.getChildren().add(node);
            packetNodes.put(packet.getId(), node);
            packetTravel.put(packet.getId(), new double[]{startX, endX});
            packetLane.put(packet.getId(), lane);

            TranslateTransition transition = new TranslateTransition(Duration.millis(1000), node);
            transition.setToX(endX - startX);
            transition.play();

            if (packet.isRetransmission() || packet.getKind() == PacketKind.RETRANSMISSION) {
                node.markRetransmitted();
            }
            statusLabel.setText("En tránsito: " + packet.label());
            showPacketDetails(packet);
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
                schedulePacketCleanup(packet);
            }
            statusLabel.setText("Entregado: " + packet.label());
            showPacketDetails(packet);
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
                schedulePacketCleanup(packet);
            }
            statusLabel.setText("Perdido: " + packet.label());
            showPacketDetails(packet);
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
        runOnFxThread(() -> {
            statusLabel.setText(message);
        });
    }

    @Override
    public void onScenarioCompleted() {
        runOnFxThread(() -> {
            recordSnapshot();
            if (reviewControlsBox != null) {
                reviewControlsBox.setManaged(true);
                reviewControlsBox.setVisible(true);
            }
            if (!uiSnapshots.isEmpty()) {
                currentSnapshotIndex = uiSnapshots.size() - 1;
            }
            updateStepIndicator();
            updatePlaybackButtons();
            statusLabel.setText("Simulación terminada. Usa los controles de revisión por pasos.");
        });
    }

    @Override
    public void onReset() {
        runOnFxThread(() -> {
            if (!restoringSnapshot) {
                uiSnapshots.clear();
                currentSnapshotIndex = -1;
                updateStepIndicator();
                if (reviewControlsBox != null) {
                    reviewControlsBox.setVisible(false);
                    reviewControlsBox.setManaged(false);
                }
            }
            networkPane.getChildren().removeIf(node -> node instanceof PacketNode);
            packetNodes.clear();
            packetTravel.clear();
            packetLane.clear();
            clearLaneState();
            udpSentChunks.clear();
            udpDeliveredChunks.clear();
            udpLostChunks.clear();
            tcpDeliveredSeqs.clear();
            tcpSentSegments.clear();
            tcpReceivedSegments.clear();
            tcpDeliveredMessage = "";
            logArea.clear();
            packetDetailsArea.setText("Selecciona un paquete para ver su detalle.");
            clientDataArea.clear();
            serverDataArea.clear();
            statusLabel.setText("Listo para iniciar");
            refreshTheoryPanel();
            updatePlaybackButtons();
            if (scenarioStartRequested) {
                scenarioStartMillis = System.currentTimeMillis();
                currentMessage = pendingMessage;
                currentProtocol = pendingProtocol;
                addSimulationSeparator(clientPacketList, currentProtocol);
                addSimulationSeparator(serverPacketList, currentProtocol);
                int fragmentSize = fragmentSizeSpinner != null ? fragmentSizeSpinner.getValue() : 8;
                clientDataArea.setText(
                        "Protocolo: " + currentProtocol + "\n" +
                        "Tamaño de fragmento: " + fragmentSize + "\n" +
                        "Mensaje a enviar:\n\"" + currentMessage + "\""
                );
                if (currentProtocol == ProtocolType.TCP) {
                    serverDataArea.setText(
                            "Protocolo: TCP\n" +
                            "Estado conexión: iniciando 3-way handshake\n" +
                            "Mensaje recibido:\n\"\""
                    );
                } else {
                    serverDataArea.setText("Protocolo: UDP\nDatagramas recibidos: []\nDatagramas perdidos: []\nMensaje recibido:\n\"\"");
                }
                scenarioStartRequested = false;
            } else {
                currentMessage = "";
                currentProtocol = protocolSelector != null ? protocolSelector.getValue() : ProtocolType.TCP;
                clientDataArea.setText("Sin simulación activa.");
                serverDataArea.setText("Sin simulación activa.");
                if (clientPacketList != null) {
                    clientPacketList.getChildren().clear();
                }
                if (serverPacketList != null) {
                    serverPacketList.getChildren().clear();
                }
            }
            recordSnapshot();
        });
    }

    private void runOnFxThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }

    private void showPacketDetails(Packet packet) {
        String payload = packet.getPayload() == null || packet.getPayload().isBlank() ? "-" : packet.getPayload();
        double elapsed = (System.currentTimeMillis() - scenarioStartMillis) / 1000.0;
        packetDetailsArea.setText(
                "Protocolo: " + packet.getProtocolType() + "\n" +
                "Tipo: " + packet.getKind() + "\n" +
                "Origen: " + packet.getFrom() + "\n" +
                "Destino: " + packet.getTo() + "\n" +
                "SEQ: " + packet.getSeq() + "\n" +
                "ACK: " + packet.getAck() + "\n" +
                "Payload: " + payload + "\n" +
                "Estado: " + packet.getStatus() + "\n" +
                String.format("Tiempo: %.1f s", elapsed)
        );
    }

    private void updateMessagePanelsOnCreated(Packet packet) {
        if (packet.getFrom() != Endpoint.CLIENT) {
            return;
        }
        if (packet.getProtocolType() == ProtocolType.TCP
                && (packet.getKind() == PacketKind.DATA || packet.getKind() == PacketKind.RETRANSMISSION)) {
            if (packet.getKind() == PacketKind.DATA) {
                tcpSentSegments.put(packet.getSeq(), packet.getPayload());
            }
            int fragmentSize = fragmentSizeSpinner != null ? fragmentSizeSpinner.getValue() : 8;
            clientDataArea.setText(
                    "Protocolo: TCP\n" +
                    "Tamaño de fragmento: " + fragmentSize + "\n" +
                    "Mensaje a enviar:\n\"" + currentMessage + "\"\n\n" +
                    "3-way handshake: SYN -> SYN-ACK -> ACK\n" +
                    "Cierre: FIN -> ACK -> FIN -> ACK\n" +
                    "Segmentos enviados: " + tcpSentSegments.keySet() + "\n" +
                    "Último segmento:\nSEQ=" + packet.getSeq() + " payload=\"" + shorten(packet.getPayload(), 24) + "\""
            );
            return;
        }
        if (packet.getProtocolType() == ProtocolType.TCP) {
            if (packet.getKind() == PacketKind.SYN || packet.getKind() == PacketKind.SYN_ACK || packet.getKind() == PacketKind.ACK) {
                serverDataArea.setText(
                        "Protocolo: TCP\n" +
                        "3-way handshake: en progreso\n" +
                        "Cierre: pendiente\n" +
                        "Mensaje recibido:\n\"" + tcpDeliveredMessage + "\""
                );
            }
            if (packet.getKind() == PacketKind.FIN) {
                serverDataArea.setText(
                        "Protocolo: TCP\n" +
                        "3-way handshake: completado\n" +
                        "Cierre: FIN observado (" + packet.getFrom() + " -> " + packet.getTo() + ")\n" +
                        "Mensaje recibido:\n\"" + tcpDeliveredMessage + "\""
                );
            }
            if (packet.getKind() == PacketKind.ACK && packet.getAck() == 2) {
                serverDataArea.setText(
                        "Protocolo: TCP\n" +
                        "3-way handshake: completado\n" +
                        "Cierre: completado (FIN/ACK/FIN/ACK)\n" +
                        "Segmentos confirmados: " + tcpDeliveredSeqs + "\n" +
                        "Mensaje recibido:\n\"" + tcpDeliveredMessage + "\""
                );
            }
        }
        if (packet.getProtocolType() == ProtocolType.UDP && packet.getKind() == PacketKind.UDP_DATAGRAM) {
            udpSentChunks.put(packet.getSeq(), packet.getPayload());
            clientDataArea.setText(
                    "Protocolo: UDP\n" +
                    "Mensaje a enviar:\n\"" + currentMessage + "\"\n\n" +
                    "Datagramas enviados: " + udpSentChunks.keySet()
            );
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
            serverDataArea.setText(
                    "Protocolo: TCP\n" +
                    "3-way handshake: completado\n" +
                    "Cierre: en espera\n" +
                    "Segmentos confirmados: " + tcpDeliveredSeqs + "\n" +
                    "Mensaje recibido:\n\"" + tcpDeliveredMessage + "\""
            );
            return;
        }
        if (packet.getProtocolType() == ProtocolType.UDP && packet.getKind() == PacketKind.UDP_DATAGRAM) {
            udpDeliveredChunks.put(packet.getSeq(), packet.getPayload());
            udpLostChunks.remove(packet.getSeq());
            serverDataArea.setText(
                    "Protocolo: UDP\n" +
                    "Datagramas recibidos: " + udpDeliveredChunks.keySet() + "\n" +
                    "Datagramas perdidos: " + udpLostChunks + "\n" +
                    "Mensaje recibido:\n\"" + buildUdpReceivedPreview() + "\""
            );
        }
    }

    private void updateMessagePanelsOnLost(Packet packet) {
        if (packet.getProtocolType() == ProtocolType.UDP && packet.getKind() == PacketKind.UDP_DATAGRAM) {
            udpLostChunks.add(packet.getSeq());
            serverDataArea.setText(
                    "Protocolo: UDP\n" +
                    "Datagramas recibidos: " + udpDeliveredChunks.keySet() + "\n" +
                    "Datagramas perdidos: " + udpLostChunks + "\n" +
                    "Mensaje recibido:\n\"" + buildUdpReceivedPreview() + "\""
            );
        }
    }

    private String buildUdpReceivedPreview() {
        if (udpSentChunks.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Integer id : udpSentChunks.keySet()) {
            if (udpDeliveredChunks.containsKey(id)) {
                builder.append(udpDeliveredChunks.get(id));
            } else {
                builder.append("___");
            }
        }
        return builder.toString();
    }

    private String buildTcpReceivedMessage() {
        if (tcpReceivedSegments.isEmpty()) {
            return "";
        }
        List<Integer> orderedSeq = new ArrayList<>(tcpReceivedSegments.keySet());
        Collections.sort(orderedSeq);
        StringBuilder builder = new StringBuilder();
        for (Integer seq : orderedSeq) {
            builder.append(tcpReceivedSegments.get(seq));
        }
        return builder.toString();
    }

    private String shorten(String text, int max) {
        if (text == null) {
            return "";
        }
        if (text.length() <= max) {
            return text;
        }
        return text.substring(0, max) + "...";
    }

    private String formatLog(String raw) {
        String lower = raw.toLowerCase();
        String prefix;
        if (lower.contains("perdido")) {
            prefix = "[LOST]";
        } else if (lower.contains("ack")) {
            prefix = "[ACK]";
        } else if (lower.contains("retrans")) {
            prefix = "[RETRY]";
        } else if (lower.contains("recib")) {
            prefix = "[RECV]";
        } else if (lower.contains("estado") || lower.contains("handshake") || lower.contains("conexión")) {
            prefix = "[STATE]";
        } else {
            prefix = "[SEND]";
        }
        return prefix + " " + raw;
    }

    private void addSimulationSeparator(VBox targetList, ProtocolType protocol) {
        if (targetList == null) {
            return;
        }
        Label separator = new Label("Nueva simulación (" + protocol + ")");
        separator.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #475569;");
        separator.setMaxWidth(Double.MAX_VALUE);
        separator.setAlignment(Pos.CENTER);
        separator.setPadding(new Insets(4, 0, 2, 0));
        targetList.getChildren().add(separator);
    }

    private void archivePacketCard(Packet packet, Endpoint side) {
        PacketNode archived = new PacketNode(packet);
        archived.setScaleX(0.85);
        archived.setScaleY(0.85);
        archived.setOnMouseClicked(event -> showPacketDetails(packet));

        if (packet.getStatus() == PacketStatus.DELIVERED) {
            archived.markDelivered();
        } else if (packet.getStatus() == PacketStatus.LOST) {
            archived.markLost();
        }

        VBox targetList = side == Endpoint.CLIENT ? clientPacketList : serverPacketList;
        if (targetList != null) {
            targetList.getChildren().add(archived);
        }
    }

    private int allocateLane(Packet packet) {
        boolean[] lanes = packet.getFrom() == Endpoint.CLIENT ? c2sLaneBusy : s2cLaneBusy;
        for (int i = 0; i < lanes.length; i++) {
            if (!lanes[i]) {
                lanes[i] = true;
                return i;
            }
        }
        return 0;
    }

    private double laneToY(Packet packet, int lane) {
        double base = packet.getFrom() == Endpoint.CLIENT ? 116 : 298;
        return base + lane * 30;
    }

    private void releaseLane(Packet packet) {
        Integer lane = packetLane.remove(packet.getId());
        if (lane == null) {
            return;
        }
        boolean[] lanes = packet.getFrom() == Endpoint.CLIENT ? c2sLaneBusy : s2cLaneBusy;
        if (lane >= 0 && lane < lanes.length) {
            lanes[lane] = false;
        }
    }

    private void clearLaneState() {
        for (int i = 0; i < c2sLaneBusy.length; i++) {
            c2sLaneBusy[i] = false;
            s2cLaneBusy[i] = false;
        }
    }

    private void schedulePacketCleanup(Packet packet) {
        PacketNode node = packetNodes.get(packet.getId());
        if (node == null) {
            releaseLane(packet);
            return;
        }
        PauseTransition cleanupWait = new PauseTransition(Duration.millis(700));
        cleanupWait.setOnFinished(event -> {
            networkPane.getChildren().remove(node);
            packetNodes.remove(packet.getId());
            packetTravel.remove(packet.getId());
            releaseLane(packet);
        });
        cleanupWait.play();
    }

    private static class UiSnapshot {
        private final String clientState;
        private final String serverState;
        private final String status;
        private final String logText;
        private final String packetDetailsText;
        private final String clientDataText;
        private final String serverDataText;
        private final List<SideEntrySnapshot> clientEntries;
        private final List<SideEntrySnapshot> serverEntries;
        private final List<PacketVisualSnapshot> activePackets;

        private UiSnapshot(String clientState, String serverState, String status, String logText, String packetDetailsText,
                           String clientDataText, String serverDataText, List<SideEntrySnapshot> clientEntries,
                           List<SideEntrySnapshot> serverEntries, List<PacketVisualSnapshot> activePackets) {
            this.clientState = clientState;
            this.serverState = serverState;
            this.status = status;
            this.logText = logText;
            this.packetDetailsText = packetDetailsText;
            this.clientDataText = clientDataText;
            this.serverDataText = serverDataText;
            this.clientEntries = clientEntries;
            this.serverEntries = serverEntries;
            this.activePackets = activePackets;
        }
    }

    private static class SideEntrySnapshot {
        private final String separatorText;
        private final Packet packet;

        private SideEntrySnapshot(String separatorText, Packet packet) {
            this.separatorText = separatorText;
            this.packet = packet;
        }

        private static SideEntrySnapshot separator(String text) {
            return new SideEntrySnapshot(text, null);
        }

        private static SideEntrySnapshot packet(Packet packet) {
            return new SideEntrySnapshot(null, packet);
        }
    }

    private static class PacketVisualSnapshot {
        private final Packet packet;
        private final double layoutX;
        private final double layoutY;
        private final double translateX;
        private final double translateY;

        private PacketVisualSnapshot(Packet packet, double layoutX, double layoutY, double translateX, double translateY) {
            this.packet = packet;
            this.layoutX = layoutX;
            this.layoutY = layoutY;
            this.translateX = translateX;
            this.translateY = translateY;
        }
    }
}
