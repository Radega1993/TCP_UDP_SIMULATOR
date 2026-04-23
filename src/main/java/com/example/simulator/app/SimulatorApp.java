package com.example.simulator.app;

import com.example.simulator.application.dto.ComparisonCommand;
import com.example.simulator.application.dto.ProtocolComparisonResult;
import com.example.simulator.application.dto.SimulationCommand;
import com.example.simulator.domain.network.Endpoint;
import com.example.simulator.domain.network.NetworkConditions;
import com.example.simulator.domain.protocol.PacketKind;
import com.example.simulator.domain.protocol.PacketStatus;
import com.example.simulator.domain.protocol.ProtocolType;
import com.example.simulator.domain.protocol.tcp.TcpState;
import com.example.simulator.domain.scenario.Scenario;
import com.example.simulator.domain.simulation.CongestionSnapshot;
import com.example.simulator.domain.simulation.CwndHistoryPoint;
import com.example.simulator.domain.simulation.FlowControlSnapshot;
import com.example.simulator.domain.simulation.Packet;
import com.example.simulator.presentation.playback.JavaFxSimulationPlayer;
import com.example.simulator.presentation.playback.SimulationPlaybackListener;
import com.example.simulator.presentation.viewmodel.ComparisonViewModel;
import com.example.simulator.presentation.viewmodel.SimulationMode;
import com.example.simulator.presentation.viewmodel.SimulationViewModel;
import com.example.simulator.ui.BottomControlBar;
import com.example.simulator.ui.ComparisonModeView;
import com.example.simulator.ui.CongestionPanel;
import com.example.simulator.ui.ControlPanel;
import com.example.simulator.ui.DashboardCard;
import com.example.simulator.ui.EventLogPanel;
import com.example.simulator.ui.MailboxPanel;
import com.example.simulator.ui.MessageSummaryPanel;
import com.example.simulator.ui.ModeLaunchCard;
import com.example.simulator.ui.NetworkCanvas;
import com.example.simulator.ui.PacketNode;
import com.example.simulator.ui.SequenceDiagramView;
import com.example.simulator.ui.SimulationViewMode;
import com.example.simulator.ui.SimulatorHeader;
import com.example.simulator.ui.SlidingWindowPanel;
import com.example.simulator.ui.StyledButton;
import com.example.simulator.ui.StatePanel;
import com.example.simulator.ui.UiTheme;
import com.example.simulator.ui.ViewModeToggle;
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
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.Modality;
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
import javafx.scene.layout.*;


public class SimulatorApp extends Application implements SimulationPlaybackListener {
    private Pane networkPane;
    private NetworkCanvas networkCanvas;
    private SequenceDiagramView sequenceDiagramView;
    private TextArea logArea;
    private TextArea clientDataArea;
    private TextArea serverDataArea;
    private VBox clientPacketList;
    private VBox serverPacketList;
    private Label clientStateLabel;
    private Label serverStateLabel;
    private Label statusLabel;
    private SimulationViewModel viewModel;
    private ComparisonViewModel comparisonViewModel;
    private JavaFxSimulationPlayer player;
    private ComparisonModeView comparisonModeView;
    private BottomControlBar bottomControlBar;
    private Stage configurationStage;
    private VBox workspaceContent;
    private StackPane appContentStack;
    private Node homeView;
    private Node simpleModeView;
    private StackPane modeContentStack;
    private ComboBox<SimulationMode> modeSelector;
    private ComboBox<ProtocolType> protocolSelector;
    private ComboBox<Scenario> scenarioSelector;
    private Slider lossSlider;
    private Slider latencySlider;
    private Slider jitterSlider;
    private Slider duplicationSlider;
    private Slider reorderingSlider;
    private Slider speedSlider;
    private TextField messageField;
    private Stage primaryStage;
    private Spinner<Integer> fragmentSizeSpinner;
    private Spinner<Integer> bandwidthSpinner;
    private Spinner<Integer> tcpWindowSizeSpinner;
    private Spinner<Integer> tcpReceiverBufferSpinner;
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
    private FlowPane simulationRow;
    private FlowPane mainContentRow;
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
    private String lastPacketDetailsText = "Haz clic en un paquete para abrir su detalle.";
    private String currentTheoryText = "";
    private SlidingWindowPanel slidingWindowPanel;
    private CongestionPanel congestionPanel;
    private ViewModeToggle viewModeToggle;
    private SimulationViewMode currentViewMode = SimulationViewMode.DIAGRAM;
    private FlowControlSnapshot latestFlowControlSnapshot;
    private CongestionSnapshot latestCongestionSnapshot;
    private final List<CwndHistoryPoint> congestionHistory = new ArrayList<>();
    private WorkspaceScreen currentScreen = WorkspaceScreen.HOME;

    private enum WorkspaceScreen {
        HOME,
        TCP,
        UDP,
        COMPARE
    }

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        Bootstrap bootstrap = new Bootstrap();
        viewModel = bootstrap.createSimulationViewModel();
        comparisonViewModel = bootstrap.createComparisonViewModel();
        player = new JavaFxSimulationPlayer(this);
        scenarios = viewModel.availableScenarios();

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(16));
        root.setStyle(UiTheme.APP_BACKGROUND);

        root.setTop(buildHeader());
        root.setCenter(buildApplicationContent());
        root.setBottom(buildBottomBar());

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
        box.setPadding(new Insets(0, 0, 16, 0));
        return box;
    }

    private Node buildApplicationContent() {
        buildControls();
        homeView = buildHomeView();
        workspaceContent = buildWorkspaceContent();

        appContentStack = new StackPane(homeView, workspaceContent);
        showHomeScreen();

        ScrollPane scrollPane = new ScrollPane(appContentStack);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.viewportBoundsProperty().addListener((obs, oldBounds, bounds) -> {
            if (mainContentRow != null) {
                mainContentRow.setPrefWrapLength(Math.max(900, bounds.getWidth() - 18));
            }
            if (simulationRow != null) {
                simulationRow.setPrefWrapLength(Math.max(860, bounds.getWidth() - 450));
            }
        });
        return scrollPane;
    }

    private Node buildBottomBar() {
        bottomControlBar = new BottomControlBar();
        bottomControlBar.getHomeButton().setOnAction(event -> showHomeScreen());
        bottomControlBar.getTheoryButton().setOnAction(event -> openTheoryModal());
        bottomControlBar.getRunButton().setOnAction(event -> startSimulation());
        bottomControlBar.getResetButton().setOnAction(event -> resetActiveMode());
        bottomControlBar.getPlayPauseButton().setOnAction(event -> togglePlaybackPause());
        bottomControlBar.getStepButton().setOnAction(event -> {
            if (isComparisonMode()) {
                comparisonModeView.stepForward();
            } else {
                player.stepForward();
            }
            updatePlaybackButtons();
        });
        bottomControlBar.getReviewFirstButton().setOnAction(event -> goToFirstStep());
        bottomControlBar.getReviewStepBackButton().setOnAction(event -> goToPreviousStep());
        bottomControlBar.getReviewStepForwardButton().setOnAction(event -> goToNextStep());
        bottomControlBar.getReviewLastButton().setOnAction(event -> goToLastStep());

        playPauseButton = bottomControlBar.getPlayPauseButton();
        liveStepButton = bottomControlBar.getStepButton();
        reviewFirstButton = bottomControlBar.getReviewFirstButton();
        reviewStepBackButton = bottomControlBar.getReviewStepBackButton();
        reviewStepForwardButton = bottomControlBar.getReviewStepForwardButton();
        reviewLastButton = bottomControlBar.getReviewLastButton();
        stepIndicatorLabel = bottomControlBar.getStepIndicatorLabel();
        reviewControlsBox = bottomControlBar.getReviewBox();
        bottomControlBar.setVisible(false);
        bottomControlBar.setManaged(false);
        updatePlaybackButtons();
        return bottomControlBar;
    }

    private Node buildHomeView() {
        DashboardCard intro = new DashboardCard("INICIO", "Elige una experiencia de simulación",
                "Accede directamente al modo TCP, al modo UDP o a la comparación paralela entre ambos.");
        intro.setStyle(UiTheme.HERO_CARD);

        ModeLaunchCard tcpCard = new ModeLaunchCard("PROTOCOLO", "TCP",
                "Explora handshake, ACK, retransmisión y cierre con una vista centrada en fiabilidad.",
                "Ideal para explicar conexión previa, confirmaciones y recuperación de pérdidas.");
        tcpCard.setOnMouseClicked(event -> openSimpleWorkspace(ProtocolType.TCP));

        ModeLaunchCard udpCard = new ModeLaunchCard("PROTOCOLO", "UDP",
                "Observa datagramas, pérdidas y simplicidad de envío sin conexión ni ACK.",
                "Ideal para explicar baja sobrecarga, rapidez y ausencia de recuperación nativa.");
        udpCard.setOnMouseClicked(event -> openSimpleWorkspace(ProtocolType.UDP));

        ModeLaunchCard comparisonCard = new ModeLaunchCard("COMPARACIÓN", "TCP vs UDP",
                "Lanza ambos protocolos en paralelo con la misma red y el mismo mensaje.",
                "Ideal para ver en una sola demo la diferencia entre fiabilidad y simplicidad.");
        comparisonCard.setOnMouseClicked(event -> openComparisonWorkspace());

        FlowPane cards = new FlowPane(22, 22, tcpCard, udpCard, comparisonCard);
        cards.setAlignment(Pos.TOP_LEFT);
        cards.setPrefWrapLength(1300);

        VBox home = new VBox(22, intro, cards);
        home.setPadding(new Insets(0, 0, 18, 0));
        return home;
    }

    private VBox buildWorkspaceContent() {
        simpleModeView = buildSimpleModeView();
        comparisonModeView = new ComparisonModeView(details -> openTextModal(
                "Detalle del paquete",
                "Lectura puntual del paquete seleccionado.",
                details,
                true
        ));
        comparisonModeView.setVisible(false);
        comparisonModeView.setManaged(false);
        comparisonModeView.setViewMode(currentViewMode);

        modeContentStack = new StackPane(simpleModeView, comparisonModeView);
        DashboardCard workspaceIntro = new DashboardCard("ESPACIO DE TRABAJO", "Simulación activa",
                "La configuración se abre en modal y la barra inferior concentra la ejecución y navegación.");
        workspaceIntro.setStyle(UiTheme.HERO_CARD);
        workspaceIntro.setContent(buildWorkspaceIntroBar());

        VBox content = new VBox(18, workspaceIntro, modeContentStack);
        content.setPadding(new Insets(0, 0, 12, 0));
        return content;
    }

    private Node buildWorkspaceIntroBar() {
        StyledButton configButton = new StyledButton("Configurar simulación", StyledButton.Kind.SOFT);
        configButton.setOnAction(event -> openConfigurationModal());
        viewModeToggle = new ViewModeToggle();
        viewModeToggle.setOnModeChanged(this::applySimulationViewMode);

        FlowPane compactLegend = new FlowPane(12, 8,
                compactLegendChip(Color.web("#93c5fd"), "TCP SYN"),
                compactLegendChip(Color.web("#60a5fa"), "TCP SYN-ACK"),
                compactLegendChip(Color.web("#86efac"), "TCP ACK"),
                compactLegendChip(Color.web("#7dd3fc"), "TCP DATA"),
                compactLegendChip(Color.web("#c4b5fd"), "UDP"),
                compactLegendChip(Color.web("#fecaca"), "Perdido"),
                compactLegendChip(Color.web("#fed7aa"), "Retransmitido")
        );
        compactLegend.setAlignment(Pos.CENTER_RIGHT);
        compactLegend.setPrefWrapLength(860);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(16, configButton, viewModeToggle, spacer, compactLegend);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(2, 0, 0, 0));
        return row;
    }

    private void applySimulationViewMode(SimulationViewMode mode) {
        currentViewMode = mode;
        if (sequenceDiagramView != null) {
            sequenceDiagramView.setVisible(mode == SimulationViewMode.DIAGRAM);
            sequenceDiagramView.setManaged(mode == SimulationViewMode.DIAGRAM);
        }
        if (simulationRow != null) {
            simulationRow.setVisible(mode == SimulationViewMode.SCENE);
            simulationRow.setManaged(mode == SimulationViewMode.SCENE);
        }
        if (comparisonModeView != null) {
            comparisonModeView.setViewMode(mode);
        }
    }

    private HBox compactLegendChip(Color color, String text) {
        Rectangle swatch = new Rectangle(12, 12);
        swatch.setArcWidth(5);
        swatch.setArcHeight(5);
        swatch.setFill(color);
        swatch.setStroke(Color.web("#5c7087"));

        Label label = new Label(text);
        label.setStyle("-fx-font-size: 11px; -fx-text-fill: #304559;");

        HBox chip = new HBox(6, swatch, label);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setPadding(new Insets(4, 8, 4, 8));
        chip.setStyle("-fx-background-color: rgba(248,251,254,0.9);"
                + "-fx-background-radius: 999; -fx-border-radius: 999; -fx-border-color: #dbe4ee;");
        return chip;
    }

    private void showHomeScreen() {
        currentScreen = WorkspaceScreen.HOME;
        if (homeView != null) {
            homeView.setVisible(true);
            homeView.setManaged(true);
        }
        if (workspaceContent != null) {
            workspaceContent.setVisible(false);
            workspaceContent.setManaged(false);
        }
        if (bottomControlBar != null) {
            bottomControlBar.setVisible(false);
            bottomControlBar.setManaged(false);
        }
        if (comparisonModeView != null) {
            comparisonModeView.stop();
        }
        if (player != null) {
            player.stop();
        }
    }

    private void openSimpleWorkspace(ProtocolType protocolType) {
        currentScreen = protocolType == ProtocolType.TCP ? WorkspaceScreen.TCP : WorkspaceScreen.UDP;
        modeSelector.setValue(SimulationMode.SIMPLE);
        protocolSelector.setDisable(false);
        protocolSelector.setValue(protocolType);
        switchSimulationMode(SimulationMode.SIMPLE);
        refreshTheoryPanel();
        revealWorkspace();
        onReset();
    }

    private void openComparisonWorkspace() {
        currentScreen = WorkspaceScreen.COMPARE;
        modeSelector.setValue(SimulationMode.COMPARE);
        switchSimulationMode(SimulationMode.COMPARE);
        refreshTheoryPanel();
        revealWorkspace();
    }

    private void revealWorkspace() {
        if (homeView != null) {
            homeView.setVisible(false);
            homeView.setManaged(false);
        }
        if (workspaceContent != null) {
            workspaceContent.setVisible(true);
            workspaceContent.setManaged(true);
        }
        if (bottomControlBar != null) {
            bottomControlBar.setVisible(true);
            bottomControlBar.setManaged(true);
        }
    }

    private void openConfigurationModal() {
        if (configurationStage == null) {
            configurationStage = new Stage();
            configurationStage.initOwner(primaryStage);
            configurationStage.initModality(Modality.NONE);
            configurationStage.setTitle("Configuración de simulación");

            DashboardCard modalShell = new DashboardCard("CONFIGURACIÓN", "Parámetros de la simulación",
                    "Ajusta escenario, red y visualización sin ocupar espacio fijo en la pantalla.");
            modalShell.setStyle(UiTheme.HERO_CARD);
            modalShell.setContent(controlPanel);

            ScrollPane scrollPane = new ScrollPane(modalShell);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

            Label titleLabel = new Label("Configurar simulación");
            titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #102033;");

            Label subtitleLabel = new Label("Revisa escenario, red, ventana TCP y visualización en un único panel.");
            subtitleLabel.setWrapText(true);
            subtitleLabel.setStyle(UiTheme.SUBTITLE);

            StyledButton closeButton = new StyledButton("Cerrar", StyledButton.Kind.SOFT);
            closeButton.setOnAction(event -> configurationStage.close());

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox footer = new HBox(12, spacer, closeButton);
            footer.setAlignment(Pos.CENTER_RIGHT);
            footer.setPadding(new Insets(14, 0, 0, 0));

            BorderPane content = new BorderPane();
            content.setTop(new VBox(8, titleLabel, subtitleLabel));
            content.setCenter(scrollPane);
            content.setBottom(footer);
            content.setPadding(new Insets(20));
            content.setStyle(UiTheme.APP_BACKGROUND);

            Scene scene = new Scene(content, 1180, 900);
            configurationStage.setScene(scene);
            configurationStage.setMinWidth(1080);
            configurationStage.setMinHeight(820);
        }
        if (currentScreen == WorkspaceScreen.TCP || currentScreen == WorkspaceScreen.UDP) {
            protocolSelector.setDisable(true);
            protocolSelector.setValue(currentScreen == WorkspaceScreen.TCP ? ProtocolType.TCP : ProtocolType.UDP);
        } else {
            protocolSelector.setDisable(true);
        }
        configurationStage.show();
        configurationStage.toFront();
    }

    private Node buildSimpleModeView() {
        VBox centerColumn = buildSimpleCenter();
        Node leftPanel = buildLeftPanel();
        Node rightPanel = buildRightPanel();
        HBox row = new HBox(18, leftPanel, centerColumn, rightPanel);
        row.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(centerColumn, Priority.ALWAYS);
        centerColumn.setMaxWidth(Double.MAX_VALUE);
        mainContentRow = new FlowPane();
        mainContentRow.getChildren().setAll(row);
        return mainContentRow;
    }

    private VBox buildSimpleCenter() {
        networkCanvas = new NetworkCanvas();
        networkPane = networkCanvas;
        sequenceDiagramView = new SequenceDiagramView("Diagrama secuencial", this::openTextDetailFromDiagram);
        sequenceDiagramView.setVisible(false);
        sequenceDiagramView.setManaged(false);

        MailboxPanel clientHistoryCard = new MailboxPanel("Buzón cliente", "Salida del emisor");
        MailboxPanel serverHistoryCard = new MailboxPanel("Buzón servidor", "Llegadas al receptor");
        clientPacketList = clientHistoryCard.getItemsBox();
        serverPacketList = serverHistoryCard.getItemsBox();
        clientHistoryCard.setPrefWidth(176);
        clientHistoryCard.setMinWidth(164);
        serverHistoryCard.setPrefWidth(176);
        serverHistoryCard.setMinWidth(164);

        HBox simulationLane = new HBox(14, clientHistoryCard, networkCanvas, serverHistoryCard);
        simulationLane.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(networkCanvas, Priority.ALWAYS);
        networkCanvas.setMaxWidth(Double.MAX_VALUE);

        simulationRow = new FlowPane();
        simulationRow.getChildren().setAll(simulationLane);

        DashboardCard simulationCard = new DashboardCard("SIMULACIÓN", "Escenario de red", "La zona principal de observación para seguir el tránsito de paquetes.");
        simulationCard.setStyle(UiTheme.HERO_CARD);
        StackPane simulationModeStack = new StackPane(simulationRow, sequenceDiagramView);
        simulationCard.setContent(simulationModeStack);
        simulationCard.setMaxWidth(Double.MAX_VALUE);

        VBox box = new VBox(14, simulationCard);
        box.setFillWidth(true);
        box.setMaxWidth(Double.MAX_VALUE);
        box.setPrefWidth(1100);
        applySimulationViewMode(currentViewMode);
        return box;
    }

    private void openTextDetailFromDiagram(String details) {
        lastPacketDetailsText = details;
        openPacketDetailsModal();
    }

    private Node buildLeftPanel() {
        VBox panel = new VBox(14);
        panel.setPadding(new Insets(0, 0, 0, 2));
        panel.setPrefWidth(290);
        panel.setMinWidth(270);

        StatePanel statePanel = new StatePanel();
        clientStateLabel = statePanel.getClientStateLabel();
        serverStateLabel = statePanel.getServerStateLabel();
        statusLabel = statePanel.getStatusLabel();

        slidingWindowPanel = new SlidingWindowPanel();
        congestionPanel = new CongestionPanel();

        panel.getChildren().addAll(statePanel, slidingWindowPanel, congestionPanel);
        return panel;
    }

    private Node buildRightPanel() {
        VBox panel = new VBox(14);
        panel.setPadding(new Insets(0, 0, 0, 2));
        panel.setPrefWidth(350);
        panel.setMinWidth(320);

        EventLogPanel eventLogPanel = new EventLogPanel();
        logArea = eventLogPanel.getLogArea();

        Label hint = new Label("Consejo didáctico: compara el texto enviado y el recibido para ver el efecto de la red.");
        hint.setWrapText(true);
        hint.setStyle("-fx-text-fill: #617487; -fx-font-size: 12px;");

        MessageSummaryPanel clientCard = new MessageSummaryPanel("Cliente", "Mensaje enviado");
        MessageSummaryPanel serverCard = new MessageSummaryPanel("Servidor", "Mensaje recibido");
        clientDataArea = clientCard.getTextArea();
        serverDataArea = serverCard.getTextArea();

        clientCard.setPrefWidth(150);
        serverCard.setPrefWidth(150);

        HBox messagePanels = new HBox(10, clientCard, serverCard);
        HBox.setHgrow(clientCard, Priority.ALWAYS);
        HBox.setHgrow(serverCard, Priority.ALWAYS);
        clientCard.setMaxWidth(Double.MAX_VALUE);
        serverCard.setMaxWidth(Double.MAX_VALUE);

        DashboardCard messageCard = new DashboardCard("RESULTADO", "Mensajes y reconstrucción", "Vista paralela del mensaje en origen y destino.");
        messageCard.setContent(new VBox(10, messagePanels, hint));
        messageCard.setMaxWidth(Double.MAX_VALUE);

        panel.getChildren().addAll(eventLogPanel, messageCard);
        return panel;
    }

    private void buildControls() {
        controlPanel = new ControlPanel(scenarios);
        controlPanel.hideActionsSection();
        modeSelector = controlPanel.getModeSelector();
        protocolSelector = controlPanel.getProtocolSelector();
        scenarioSelector = controlPanel.getScenarioSelector();
        messageField = controlPanel.getMessageField();
        lossSlider = controlPanel.getLossSlider();
        latencySlider = controlPanel.getLatencySlider();
        jitterSlider = controlPanel.getJitterSlider();
        duplicationSlider = controlPanel.getDuplicationSlider();
        reorderingSlider = controlPanel.getReorderingSlider();
        speedSlider = controlPanel.getSpeedSlider();
        fragmentSizeSpinner = controlPanel.getFragmentSizeSpinner();
        bandwidthSpinner = controlPanel.getBandwidthSpinner();
        tcpWindowSizeSpinner = controlPanel.getTcpWindowSizeSpinner();
        tcpReceiverBufferSpinner = controlPanel.getTcpReceiverBufferSpinner();

        scenarioSelector.setOnAction(event -> applyScenarioSelection(scenarioSelector.getValue()));
        protocolSelector.setOnAction(event -> refreshTheoryPanel());
        updatePlaybackButtons();
    }

    private void startSimulation() {
        if (isComparisonMode()) {
            startComparison();
            return;
        }
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
                        lossSlider.getValue() / 100.0,
                        Math.round(latencySlider.getValue()),
                        Math.round(jitterSlider.getValue()),
                        duplicationSlider.getValue() / 100.0,
                        reorderingSlider.getValue() / 100.0,
                        bandwidthSpinner.getValue(),
                        tcpWindowSizeSpinner.getValue(),
                        tcpReceiverBufferSpinner.getValue()
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

    private void startComparison() {
        Scenario selectedScenario = scenarioSelector.getValue();
        ComparisonCommand command = selectedScenario != null
                ? ComparisonCommand.fromScenario(selectedScenario)
                : new ComparisonCommand(
                        normalizeMessage(messageField.getText()),
                        fragmentSizeSpinner.getValue(),
                        new NetworkConditions(
                                lossSlider.getValue() / 100.0,
                                Math.round(latencySlider.getValue()),
                                Math.round(jitterSlider.getValue()),
                                duplicationSlider.getValue() / 100.0,
                                reorderingSlider.getValue() / 100.0,
                                bandwidthSpinner.getValue(),
                                Set.of()
                        ),
                        tcpWindowSizeSpinner.getValue(),
                        tcpReceiverBufferSpinner.getValue()
                );
        ProtocolComparisonResult result = comparisonViewModel.startComparison(command);
        comparisonModeView.start(result.getTcpResult(), result.getUdpResult(), result.getSummary(), speedSlider.getValue());
        updatePlaybackButtons();
    }

    private void togglePlaybackPause() {
        if (isComparisonMode()) {
            if (!comparisonModeView.hasRemainingEvents()) {
                updatePlaybackButtons();
                return;
            }
            if (comparisonModeView.isPaused()) {
                comparisonModeView.play();
            } else {
                comparisonModeView.pause();
            }
            updatePlaybackButtons();
            return;
        }
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
        boolean hasRemaining = isComparisonMode()
                ? comparisonModeView != null && comparisonModeView.hasRemainingEvents()
                : player != null && player.hasRemainingEvents();
        boolean paused = isComparisonMode()
                ? comparisonModeView == null || comparisonModeView.isPaused()
                : player == null || player.isPaused();
        playPauseButton.setDisable(!hasRemaining);
        liveStepButton.setDisable(!hasRemaining);
        playPauseButton.setText(paused ? "Reanudar" : "Pausar");
    }

    private void switchSimulationMode(SimulationMode mode) {
        boolean compare = mode == SimulationMode.COMPARE;
        if (simpleModeView != null) {
            simpleModeView.setVisible(!compare);
            simpleModeView.setManaged(!compare);
        }
        if (comparisonModeView != null) {
            comparisonModeView.setVisible(compare);
            comparisonModeView.setManaged(compare);
        }
        if (protocolSelector != null) {
            protocolSelector.setDisable(compare || currentScreen == WorkspaceScreen.TCP || currentScreen == WorkspaceScreen.UDP);
        }
        if (reviewControlsBox != null) {
            boolean showReview = !compare && !uiSnapshots.isEmpty();
            reviewControlsBox.setManaged(showReview);
            reviewControlsBox.setVisible(showReview);
        }
        if (bottomControlBar != null) {
            bottomControlBar.getRunButton().setText(compare ? "Iniciar comparación" : "Iniciar");
        }
        updatePlaybackButtons();
    }

    private void resetActiveMode() {
        scenarioStartRequested = false;
        if (isComparisonMode()) {
            if (comparisonModeView != null) {
                comparisonModeView.stop();
            }
            updatePlaybackButtons();
            return;
        }
        player.stop();
        viewModel.reset();
        onReset();
    }

    private boolean isComparisonMode() {
        return modeSelector != null && modeSelector.getValue() == SimulationMode.COMPARE;
    }

    private void applyScenarioSelection(Scenario scenario) {
        if (scenario == null) {
            refreshTheoryPanel();
            return;
        }
        protocolSelector.setValue(scenario.getProtocol());
        messageField.setText(scenario.getMessage());
        fragmentSizeSpinner.getValueFactory().setValue(scenario.getFragmentSize());
        tcpWindowSizeSpinner.getValueFactory().setValue(scenario.getTcpWindowSizeBytes());
        tcpReceiverBufferSpinner.getValueFactory().setValue(scenario.getTcpReceiverBufferBytes());
        lossSlider.setValue(scenario.getNetworkConditions().getPacketLossRate() * 100.0);
        latencySlider.setValue(scenario.getNetworkConditions().getBaseLatencyMillis());
        jitterSlider.setValue(scenario.getNetworkConditions().getJitterMillis());
        duplicationSlider.setValue(scenario.getNetworkConditions().getDuplicationRate() * 100.0);
        reorderingSlider.setValue(scenario.getNetworkConditions().getReorderingRate() * 100.0);
        bandwidthSpinner.getValueFactory().setValue(scenario.getNetworkConditions().getBandwidthPacketsPerSecond());
        refreshTheoryPanel();
    }

    private void refreshTheoryPanel() {
        if (protocolSelector == null || viewModel == null) {
            return;
        }
        if (currentScreen == WorkspaceScreen.COMPARE || isComparisonMode()) {
            currentTheoryText = viewModel.detailedTheoryForComparison();
            return;
        }
        ProtocolType protocol = protocolSelector.getValue();
        if (protocol == null) {
            currentTheoryText = viewModel.helpTextForContext("*", null);
            return;
        }
        currentTheoryText = viewModel.detailedTheoryForProtocol(protocol);
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
        wrapper.setPadding(new Insets(8));
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
                lastPacketDetailsText,
                clientDataArea.getText(),
                serverDataArea.getText(),
                latestFlowControlSnapshot,
                latestCongestionSnapshot,
                List.copyOf(congestionHistory),
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
            lastPacketDetailsText = snapshot.packetDetailsText;
            clientDataArea.setText(snapshot.clientDataText);
            serverDataArea.setText(snapshot.serverDataText);
            latestFlowControlSnapshot = snapshot.flowControlSnapshot;
            if (slidingWindowPanel != null) {
                slidingWindowPanel.update(snapshot.flowControlSnapshot);
            }
            latestCongestionSnapshot = snapshot.congestionSnapshot;
            congestionHistory.clear();
            congestionHistory.addAll(snapshot.congestionHistory);
            if (congestionPanel != null) {
                congestionPanel.restore(snapshot.congestionSnapshot, snapshot.congestionHistory);
            }

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
                archived.setOnMouseClicked(event -> openPacketDetails(entry.packet));
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
            node.setOnMouseClicked(event -> openPacketDetails(packet));
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
                packet.isRetransmission(),
                packet.getWindowSize()
        );
    }

    @Override
    public void onLog(String message) {
        runOnFxThread(() -> {
            logArea.appendText(formatLog(message) + "\n");
            if (sequenceDiagramView != null) {
                sequenceDiagramView.addLogEvent(message, currentProtocol);
            }
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
            node.setOnMouseClicked(event -> openPacketDetails(packet));
            networkPane.getChildren().add(node);
            packetNodes.put(packet.getId(), node);
            packetTravel.put(packet.getId(), new double[]{startX, endX});
            packetLane.put(packet.getId(), lane);
            if (sequenceDiagramView != null) {
                sequenceDiagramView.addPacket(packet);
            }

            TranslateTransition transition = new TranslateTransition(Duration.millis(1000), node);
            transition.setToX(endX - startX);
            transition.play();

            if (packet.isRetransmission() || packet.getKind() == PacketKind.RETRANSMISSION) {
                node.markRetransmitted();
            }
            statusLabel.setText("En tránsito: " + packet.label());
            rememberPacketDetails(packet);
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
            rememberPacketDetails(packet);
            if (sequenceDiagramView != null) {
                sequenceDiagramView.markPacketDelivered(packet);
            }
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
            rememberPacketDetails(packet);
            if (sequenceDiagramView != null) {
                sequenceDiagramView.markPacketLost(packet);
            }
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
    public void onFlowControlUpdated(FlowControlSnapshot snapshot) {
        runOnFxThread(() -> {
            latestFlowControlSnapshot = snapshot;
            if (slidingWindowPanel != null) {
                slidingWindowPanel.update(snapshot);
            }
        });
    }

    @Override
    public void onCongestionUpdated(CongestionSnapshot snapshot) {
        runOnFxThread(() -> {
            latestCongestionSnapshot = snapshot;
            if (snapshot != null && snapshot.getHistoryPoint() != null) {
                if (congestionHistory.isEmpty()
                        || congestionHistory.get(congestionHistory.size() - 1).getStep() != snapshot.getHistoryPoint().getStep()) {
                    congestionHistory.add(snapshot.getHistoryPoint());
                }
            }
            if (congestionPanel != null) {
                congestionPanel.update(snapshot);
            }
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
            latestFlowControlSnapshot = null;
            latestCongestionSnapshot = null;
            congestionHistory.clear();
            logArea.clear();
            lastPacketDetailsText = "Haz clic en un paquete para abrir su detalle.";
            clientDataArea.clear();
            serverDataArea.clear();
            if (slidingWindowPanel != null) {
                slidingWindowPanel.reset();
            }
            if (sequenceDiagramView != null) {
                sequenceDiagramView.reset();
            }
            if (congestionPanel != null) {
                congestionPanel.reset();
            }
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
                    if (slidingWindowPanel != null) {
                        slidingWindowPanel.update(new FlowControlSnapshot(
                                tcpWindowSizeSpinner.getValue(),
                                0,
                                0,
                                0,
                                currentMessage.length(),
                                tcpReceiverBufferSpinner.getValue(),
                                0,
                                tcpReceiverBufferSpinner.getValue()
                        ));
                    }
                    if (congestionPanel != null) {
                        congestionPanel.reset();
                    }
                    serverDataArea.setText(
                        "Protocolo: TCP\n" +
                        "Estado conexión: iniciando 3-way handshake\n" +
                            "Mensaje recibido:\n\"\""
                    );
                } else {
                    if (slidingWindowPanel != null) {
                        slidingWindowPanel.showUnavailable();
                    }
                    if (congestionPanel != null) {
                        congestionPanel.showUnavailable();
                    }
                    serverDataArea.setText("Protocolo: UDP\nDatagramas recibidos: []\nDatagramas perdidos: []\nMensaje recibido:\n\"\"");
                }
                scenarioStartRequested = false;
            } else {
                currentMessage = "";
                currentProtocol = protocolSelector != null ? protocolSelector.getValue() : ProtocolType.TCP;
                if (slidingWindowPanel != null) {
                    if (currentProtocol == ProtocolType.TCP) {
                        slidingWindowPanel.reset();
                    } else {
                        slidingWindowPanel.showUnavailable();
                    }
                }
                if (congestionPanel != null) {
                    if (currentProtocol == ProtocolType.TCP) {
                        congestionPanel.reset();
                    } else {
                        congestionPanel.showUnavailable();
                    }
                }
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

    private void rememberPacketDetails(Packet packet) {
        String payload = packet.getPayload() == null || packet.getPayload().isBlank() ? "-" : packet.getPayload();
        double elapsed = (System.currentTimeMillis() - scenarioStartMillis) / 1000.0;
        lastPacketDetailsText =
                "Protocolo: " + packet.getProtocolType() + "\n" +
                "Tipo: " + packet.getKind() + "\n" +
                "Origen: " + packet.getFrom() + "\n" +
                "Destino: " + packet.getTo() + "\n" +
                "SEQ: " + packet.getSeq() + "\n" +
                "ACK: " + packet.getAck() + "\n" +
                "WIN: " + packet.getWindowSize() + "\n" +
                "Payload: " + payload + "\n" +
                "Estado: " + packet.getStatus() + "\n" +
                String.format("Tiempo: %.1f s", elapsed);
    }

    private void openPacketDetails(Packet packet) {
        rememberPacketDetails(packet);
        openPacketDetailsModal();
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
        archived.setOnMouseClicked(event -> openPacketDetails(packet));

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

    private void openPacketDetailsModal() {
        openTextModal("Detalle del paquete", "Lectura puntual del paquete seleccionado.", lastPacketDetailsText, true);
    }

    private void openTheoryModal() {
        String subtitle = switch (currentScreen) {
            case TCP -> "Guía docente completa para explicar TCP usando la simulación.";
            case UDP -> "Guía docente completa para explicar UDP usando la simulación.";
            case COMPARE -> "Guía docente para comparar TCP y UDP con la misma red y el mismo mensaje.";
            default -> "Contexto docente asociado al modo de trabajo activo.";
        };
        openTextModal("Teoría", subtitle, currentTheoryText, false);
    }

    private void openTextModal(String title, String subtitle, String content, boolean monospaced) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.NONE);
        dialog.setTitle(title);
        dialog.setResizable(true);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #102033;");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setWrapText(true);
        subtitleLabel.setStyle(UiTheme.SUBTITLE);

        TextArea body = new TextArea(content == null || content.isBlank() ? "No hay contenido disponible todavía." : content);
        body.setEditable(false);
        body.setWrapText(true);
        body.setStyle((monospaced ? UiTheme.MUTED_TEXT_SURFACE : UiTheme.TEXT_SURFACE));
        VBox.setVgrow(body, Priority.ALWAYS);

        Button closeButton = new Button("Cerrar");
        closeButton.setStyle(UiTheme.SOFT_BUTTON);
        closeButton.setOnAction(event -> dialog.close());

        VBox contentBox = new VBox(12, titleLabel, subtitleLabel, body, closeButton);
        contentBox.setPadding(new Insets(22));
        contentBox.setStyle(UiTheme.APP_BACKGROUND);
        VBox.setVgrow(body, Priority.ALWAYS);

        Scene modalScene = new Scene(contentBox, 820, 620);
        dialog.setScene(modalScene);
        dialog.setMinWidth(680);
        dialog.setMinHeight(480);
        dialog.show();
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
        private final FlowControlSnapshot flowControlSnapshot;
        private final CongestionSnapshot congestionSnapshot;
        private final List<CwndHistoryPoint> congestionHistory;
        private final List<SideEntrySnapshot> clientEntries;
        private final List<SideEntrySnapshot> serverEntries;
        private final List<PacketVisualSnapshot> activePackets;

        private UiSnapshot(String clientState, String serverState, String status, String logText, String packetDetailsText,
                           String clientDataText, String serverDataText, FlowControlSnapshot flowControlSnapshot,
                           CongestionSnapshot congestionSnapshot, List<CwndHistoryPoint> congestionHistory,
                           List<SideEntrySnapshot> clientEntries,
                           List<SideEntrySnapshot> serverEntries, List<PacketVisualSnapshot> activePackets) {
            this.clientState = clientState;
            this.serverState = serverState;
            this.status = status;
            this.logText = logText;
            this.packetDetailsText = packetDetailsText;
            this.clientDataText = clientDataText;
            this.serverDataText = serverDataText;
            this.flowControlSnapshot = flowControlSnapshot;
            this.congestionSnapshot = congestionSnapshot;
            this.congestionHistory = congestionHistory;
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
