package com.example.simulator.app;

import com.example.simulator.engine.SimulationEngine;
import com.example.simulator.engine.SimulationListener;
import com.example.simulator.model.*;
import com.example.simulator.model.PacketKind;
import com.example.simulator.ui.PacketNode;
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
import javafx.scene.shape.Line;
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


public class SimulatorApp extends Application implements SimulationListener {
    private Pane networkPane;
    private TextArea logArea;
    private TextArea packetDetailsArea;
    private TextArea clientDataArea;
    private TextArea serverDataArea;
    private VBox clientPacketList;
    private VBox serverPacketList;
    private Label clientStateLabel;
    private Label serverStateLabel;
    private Label statusLabel;
    private SimulationEngine engine;
    private ComboBox<ProtocolType> protocolSelector;
    private Slider lossSlider;
    private Slider speedSlider;
    private TextField messageField;
    private Stage primaryStage;
    private Spinner<Integer> widthSpinner;
    private Spinner<Integer> heightSpinner;
    private Spinner<Integer> fragmentSizeSpinner;
    private ComboBox<String> windowPresetSelector;
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
        engine = new SimulationEngine(this);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #f8fafc, #e2e8f0);");

        root.setTop(buildHeader());
        root.setCenter(buildCenter());
        root.setRight(buildRightPanel());
        root.setBottom(buildFooter());

        Scene scene = new Scene(root, 1380, 780);
        stage.setTitle("Simulador gráfico TCP y UDP - JavaFX");
        stage.setScene(scene);
        loadAppIcon(stage);
        stage.show();

        engine.reset();
    }

    private VBox buildHeader() {
        Label title = new Label("Simulador gráfico de TCP y UDP");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Label subtitle = new Label("Visualiza handshake, envío de datos, ACKs, pérdidas y retransmisiones");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #334155;");

        VBox box = new VBox(4, title, subtitle);
        box.setPadding(new Insets(0, 0, 16, 0));
        return box;
    }

    private VBox buildCenter() {
        networkPane = new Pane();
        networkPane.setPrefSize(900, 520);
        networkPane.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 18; -fx-background-radius: 18;");

        Rectangle clientBox = nodeBox(70, 190, "Cliente");
        Rectangle serverBox = nodeBox(690, 190, "Servidor");
        Label clientLabel = nodeLabel(120, 260, "Cliente");
        Label serverLabel = nodeLabel(740, 260, "Servidor");

        Line line = new Line(220, 220, 690, 220);
        line.setStroke(Color.web("#94a3b8"));
        line.setStrokeWidth(4);

        Label netLabel = new Label("Red");
        netLabel.setLayoutX(435);
        netLabel.setLayoutY(188);
        netLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #475569;");

        networkPane.getChildren().addAll(line, clientBox, serverBox, clientLabel, serverLabel, netLabel);

        clientPacketList = packetListContainer();
        serverPacketList = packetListContainer();
        ScrollPane clientScroll = packetListScroll(clientPacketList);
        ScrollPane serverScroll = packetListScroll(serverPacketList);

        VBox clientHistoryCard = card("Buzón Cliente", clientScroll);
        clientHistoryCard.setPrefWidth(250);
        VBox serverHistoryCard = card("Buzón Servidor", serverScroll);
        serverHistoryCard.setPrefWidth(250);

        HBox centerRow = new HBox(12, clientHistoryCard, networkPane, serverHistoryCard);
        HBox.setHgrow(networkPane, Priority.ALWAYS);
        VBox.setVgrow(clientScroll, Priority.ALWAYS);
        VBox.setVgrow(serverScroll, Priority.ALWAYS);

        VBox box = new VBox(12, centerRow, buildControls());
        return box;
    }

    private Node buildRightPanel() {
        VBox panel = new VBox(14);
        panel.setPadding(new Insets(0, 0, 0, 16));
        panel.setPrefWidth(420);

        clientStateLabel = stateChip("Cliente: CLOSED");
        serverStateLabel = stateChip("Servidor: CLOSED");
        statusLabel = new Label("Listo para iniciar");
        statusLabel.setWrapText(true);
        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #1e293b;");

        VBox statesCard = card("Estados", new VBox(10, clientStateLabel, serverStateLabel, statusLabel));

        packetDetailsArea = new TextArea();
        packetDetailsArea.setEditable(false);
        packetDetailsArea.setWrapText(true);
        packetDetailsArea.setPrefRowCount(9);
        packetDetailsArea.setText("Selecciona un paquete para ver su detalle.");
        VBox detailsCard = card("Detalle del paquete", packetDetailsArea);

        VBox legend = new VBox(6,
                legendRow(Color.web("#93c5fd"), "Azul claro: TCP SYN"),
                legendRow(Color.web("#60a5fa"), "Azul medio: TCP SYN-ACK"),
                legendRow(Color.web("#86efac"), "Verde: TCP ACK"),
                legendRow(Color.web("#7dd3fc"), "Celeste: TCP DATA"),
                legendRow(Color.web("#c4b5fd"), "Morado: UDP"),
                legendRow(Color.web("#fecaca"), "Rojo: Perdido"),
                legendRow(Color.web("#fed7aa"), "Naranja: Retransmitido")
        );
        VBox legendCard = card("Leyenda visual", legend);

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefRowCount(16);
        logArea.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 12px;");
        VBox logCard = card("Registro de eventos", logArea);
        VBox.setVgrow(logArea, Priority.ALWAYS);

        panel.getChildren().addAll(statesCard, detailsCard, legendCard, logCard);
        VBox.setVgrow(logCard, Priority.ALWAYS);
        return panel;
    }

    private VBox buildFooter() {
        Label hint = new Label("Consejo didáctico: ejecuta la misma palabra en TCP y UDP con una pérdida del 30-40% para comparar la fiabilidad.");
        hint.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px;");
        clientDataArea = readonlyArea(5);
        serverDataArea = readonlyArea(5);
        VBox clientCard = card("Cliente: mensaje enviado", clientDataArea);
        VBox serverCard = card("Servidor: mensaje recibido", serverDataArea);
        HBox messages = new HBox(12, clientCard, serverCard);
        HBox.setHgrow(clientCard, Priority.ALWAYS);
        HBox.setHgrow(serverCard, Priority.ALWAYS);
        VBox box = new VBox(10, messages, hint);
        box.setPadding(new Insets(14, 0, 0, 0));
        return box;
    }

    private VBox buildControls() {
        protocolSelector = new ComboBox<>();
        protocolSelector.getItems().addAll(ProtocolType.TCP, ProtocolType.UDP);
        protocolSelector.setValue(ProtocolType.TCP);

        messageField = new TextField("HOLAALUMNOS");

        lossSlider = new Slider(0, 100, 20);
        lossSlider.setShowTickLabels(true);
        lossSlider.setShowTickMarks(true);
        lossSlider.setMajorTickUnit(25);

        speedSlider = new Slider(0.5, 3.0, 1.0);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(0.5);

        widthSpinner = new Spinner<>(980, 2000, 1380, 20);
        widthSpinner.setEditable(true);
        heightSpinner = new Spinner<>(680, 1400, 780, 20);
        heightSpinner.setEditable(true);
        fragmentSizeSpinner = new Spinner<>(1, 16, 8, 1);
        fragmentSizeSpinner.setEditable(true);
        windowPresetSelector = new ComboBox<>();
        windowPresetSelector.getItems().addAll("1280x720", "1366x768", "1600x900", "1920x1080");
        windowPresetSelector.setValue("1366x768");

        Button runButton = new Button("Iniciar simulación");
        Button resetButton = new Button("Reiniciar");
        Button applySizeButton = new Button("Aplicar tamaño");
        Button presetSizeButton = new Button("Usar preset");

        runButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
        resetButton.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #0f172a; -fx-font-weight: bold; -fx-background-radius: 10;");
        applySizeButton.setStyle("-fx-background-color: #0ea5e9; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
        presetSizeButton.setStyle("-fx-background-color: #38bdf8; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
        runButton.setPrefHeight(38);
        resetButton.setPrefHeight(38);
        applySizeButton.setPrefHeight(38);
        presetSizeButton.setPrefHeight(38);

        runButton.setOnAction(event -> {
            String inputMessage = normalizeMessage(messageField.getText());
            scenarioStartRequested = true;
            pendingMessage = inputMessage;
            pendingProtocol = protocolSelector.getValue();
            engine.setProtocolType(protocolSelector.getValue());
            engine.setPacketLossRate(lossSlider.getValue() / 100.0);
            engine.setSpeedFactor(speedSlider.getValue());
            engine.setFragmentSize(fragmentSizeSpinner.getValue());
            engine.runScenario(inputMessage);
        });
        resetButton.setOnAction(event -> {
            scenarioStartRequested = false;
            engine.reset();
        });
        applySizeButton.setOnAction(event -> applyWindowSize());
        presetSizeButton.setOnAction(event -> applyWindowPreset());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Protocolo"), 0, 0);
        grid.add(protocolSelector, 1, 0);
        grid.add(new Label("Mensaje"), 0, 1);
        grid.add(messageField, 1, 1);
        grid.add(new Label("Pérdida (%)"), 0, 2);
        grid.add(lossSlider, 1, 2);
        grid.add(new Label("Velocidad"), 0, 3);
        grid.add(speedSlider, 1, 3);
        grid.add(new Label("Fragmento (1,2,3...)"), 0, 4);
        grid.add(fragmentSizeSpinner, 1, 4);
        grid.add(new Label("Ancho ventana"), 0, 5);
        grid.add(widthSpinner, 1, 5);
        grid.add(new Label("Alto ventana"), 0, 6);
        grid.add(heightSpinner, 1, 6);
        grid.add(new Label("Preset ventana"), 0, 7);
        grid.add(windowPresetSelector, 1, 7);

        HBox buttons = new HBox(10, runButton, resetButton, applySizeButton, presetSizeButton);
        buttons.setAlignment(Pos.CENTER_LEFT);

        VBox cardContent = new VBox(12, grid, buttons);
        return card("Controles", cardContent);
    }

    private VBox card(String title, javafx.scene.Node content) {
        Label heading = new Label(title);
        heading.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        VBox card = new VBox(12, heading, content);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 18; -fx-background-radius: 18;");
        return card;
    }

    private Rectangle nodeBox(double x, double y, String text) {
        Rectangle rect = new Rectangle(150, 70);
        rect.setX(x);
        rect.setY(y);
        rect.setArcWidth(22);
        rect.setArcHeight(22);
        rect.setFill(Color.web("#dbeafe"));
        rect.setStroke(Color.web("#2563eb"));
        rect.setStrokeWidth(2.0);
        return rect;
    }

    private Label nodeLabel(double x, double y, String text) {
        Label label = new Label(text);
        label.setLayoutX(x);
        label.setLayoutY(y);
        label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        return label;
    }

    private Label stateChip(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 999; -fx-padding: 8 12 8 12; -fx-font-weight: bold;");
        return label;
    }

    private TextArea readonlyArea(int rows) {
        TextArea area = new TextArea();
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefRowCount(rows);
        area.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 12px;");
        return area;
    }

    private HBox legendRow(Color color, String text) {
        Rectangle swatch = new Rectangle(16, 16);
        swatch.setArcWidth(6);
        swatch.setArcHeight(6);
        swatch.setFill(color);
        swatch.setStroke(Color.web("#334155"));
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 12px;");
        HBox row = new HBox(8, swatch, label);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private VBox packetListContainer() {
        VBox box = new VBox(8);
        box.setFillWidth(true);
        return box;
    }

    private ScrollPane packetListScroll(VBox content) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(460);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return scroll;
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

    @Override
    public void onLog(String message) {
        Platform.runLater(() -> logArea.appendText(formatLog(message) + "\n"));
    }

    @Override
    public void onPacketCreated(Packet packet) {
        Platform.runLater(() -> {
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
        Platform.runLater(() -> {
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
        Platform.runLater(() -> {
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
        Platform.runLater(() -> {
            if (endpoint == Endpoint.CLIENT) {
                clientStateLabel.setText("Cliente: " + newState);
            } else {
                serverStateLabel.setText("Servidor: " + newState);
            }
        });
    }

    @Override
    public void onMessageDelivered(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }

    @Override
    public void onReset() {
        Platform.runLater(() -> {
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
        });
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
}
