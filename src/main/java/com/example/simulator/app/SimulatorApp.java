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
import com.example.simulator.ui.HomeFooterHint;
import com.example.simulator.ui.HomeHeroHeader;
import com.example.simulator.ui.HomeModeCard;
import com.example.simulator.ui.LayersLearningView;
import com.example.simulator.ui.MailboxPanel;
import com.example.simulator.ui.MessageSummaryPanel;
import com.example.simulator.ui.NetworkCanvas;
import com.example.simulator.ui.PacketNode;
import com.example.simulator.ui.QuickAccessChip;
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
import javafx.scene.transform.Scale;
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
    private static final double TCP_METRIC_CARD_HEIGHT = 220;
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
    private VBox appHeader;
    private Button tcpTopPlayPauseButton;
    private Button tcpTopStepButton;
    private Button tcpTopResetButton;
    private Label tcpTopProtocolBadge;
    private ViewModeToggle tcpTopViewModeToggle;
    private Stage configurationStage;
    private VBox workspaceContent;
    private DashboardCard workspaceIntroCard;
    private StackPane appContentStack;
    private Node homeView;
    private Node simpleModeView;
    private Node layersModeView;
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
    private HBox receiverBufferCells;
    private Label receiverBufferUsageLabel;
    private Label receiverAdvertisedWindowLabel;
    private ViewModeToggle viewModeToggle;
    private StyledButton workspaceConfigButton;
    private FlowPane workspaceLegendPane;
    private SimulationViewMode currentViewMode = SimulationViewMode.DIAGRAM;
    private FlowControlSnapshot latestFlowControlSnapshot;
    private CongestionSnapshot latestCongestionSnapshot;
    private final List<CwndHistoryPoint> congestionHistory = new ArrayList<>();
    private WorkspaceScreen currentScreen = WorkspaceScreen.HOME;
    private LayersLearningView layersLearningView;
    private Packet lastInspectablePacket;

    private enum WorkspaceScreen {
        HOME,
        TCP,
        UDP,
        COMPARE,
        LAYERS
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
        appHeader = new VBox(new SimulatorHeader());
        appHeader.setPadding(new Insets(0, 0, 16, 0));
        return appHeader;
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
        HomeHeroHeader headerCard = new HomeHeroHeader(buildHomeHeaderActions());

        DashboardCard introCard = new DashboardCard("INICIO", "Elige una experiencia de simulación",
                "Accede directamente al modo TCP, al modo UDP o a la comparación paralela entre ambos.");
        introCard.setPadding(new Insets(16, 18, 16, 18));
        introCard.setStyle("-fx-background-color: #ffffff;"
                + "-fx-background-radius: 18;"
                + "-fx-border-radius: 18;"
                + "-fx-border-color: #d9e2ec;"
                + "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.06), 18, 0.18, 0, 6);");
        introCard.setContent(buildHomeIntroContent());

        HomeModeCard tcpCard = new HomeModeCard(
                "PROTOCOLO",
                "TCP",
                "Explora handshake, ACK, retransmisión y cierre con una vista centrada en fiabilidad.",
                "Ideal para explicar conexión previa, confirmaciones y recuperación de pérdidas.",
                "/icons/tcp.png",
                "<>",
                "#4F8EF7",
                "#60a5fa",
                false
        );
        tcpCard.setOnMouseClicked(event -> openSimpleWorkspace(ProtocolType.TCP));

        HomeModeCard udpCard = new HomeModeCard(
                "PROTOCOLO",
                "UDP",
                "Observa datagramas, pérdidas y simplicidad de envío sin conexión ni ACK.",
                "Ideal para explicar baja sobrecarga, rapidez y ausencia de recuperación nativa.",
                "/icons/udp.png",
                ">>",
                "#8A63D2",
                "#b794f4",
                false
        );
        udpCard.setOnMouseClicked(event -> openSimpleWorkspace(ProtocolType.UDP));

        HomeModeCard comparisonCard = new HomeModeCard(
                "COMPARACIÓN",
                "TCP vs UDP",
                "Lanza ambos protocolos en paralelo con la misma red y el mismo mensaje.",
                "Ideal para ver en una sola demo la diferencia entre fiabilidad y simplicidad.",
                "/icons/compare.png",
                "<|>",
                "#4F8EF7",
                "#8A63D2",
                true
        );
        comparisonCard.setOnMouseClicked(event -> openComparisonWorkspace());

        HomeModeCard layersCard = new HomeModeCard(
                "CAPAS",
                "Ver modelo TCP/IP y OSI",
                "Explora capas, encapsulación, cabeceras simplificadas y equivalencias entre modelos.",
                "Ideal para conectar lo que ves en la simulación con la arquitectura completa de red.",
                "/icons/layers.png",
                "[#]",
                "#F08A24",
                "#f6ad55",
                false
        );
        layersCard.setOnMouseClicked(event -> openLayersWorkspace());

        GridPane cards = new GridPane();
        cards.setHgap(20);
        cards.setVgap(20);
        for (int column = 0; column < 4; column++) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setPercentWidth(25);
            columnConstraints.setHgrow(Priority.ALWAYS);
            columnConstraints.setFillWidth(true);
            cards.getColumnConstraints().add(columnConstraints);
        }
        cards.add(tcpCard, 0, 0);
        cards.add(udpCard, 1, 0);
        cards.add(comparisonCard, 2, 0);
        cards.add(layersCard, 3, 0);
        for (Node card : List.of(comparisonCard, tcpCard, udpCard, layersCard)) {
            GridPane.setHgrow(card, Priority.ALWAYS);
            GridPane.setFillWidth(card, true);
        }

        DashboardCard quickAccess = new DashboardCard("ACCESOS DIRECTOS", "Escenarios rápidos",
                "Lanza ejemplos frecuentes para clase sin pasar por toda la configuración.");
        quickAccess.setPadding(new Insets(16, 18, 18, 18));
        quickAccess.setContent(buildQuickAccessPanel());

        HomeFooterHint footerHint = new HomeFooterHint(
                "Consejo didáctico",
                "Ejecuta la misma palabra en TCP y UDP con una pérdida del 30–40% para comparar fiabilidad y simplicidad en una sola demostración.",
                "Escenarios cargados: " + scenarios.size() + "   •   Vista por defecto: Diagrama   •   Modo destacado: TCP vs UDP"
        );

        VBox home = new VBox(20, headerCard, introCard, cards, quickAccess, footerHint);
        home.setPadding(new Insets(0, 0, 18, 0));
        return home;
    }

    private Label headerMetric(String text, String accent) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + accent + ";"
                + "-fx-background-color: #f6f9fd;"
                + "-fx-background-radius: 999;"
                + "-fx-border-radius: 999;"
                + "-fx-border-color: #dce6ef;"
                + "-fx-padding: 7 10 7 10;");
        return label;
    }

    private Node buildHomeHeaderActions() {
        StyledButton helpButton = new StyledButton("Ayuda", StyledButton.Kind.TERTIARY);
        helpButton.setOnAction(event -> openTextModal(
                "Ayuda de inicio",
                "Guía rápida para orientarte en la portada del simulador.",
                """
                        TCP: explica conexión, ACK y retransmisión.
                        UDP: explica datagramas, pérdidas y baja sobrecarga.
                        TCP vs UDP: compara ambos protocolos con la misma red.
                        Modelo TCP/IP y OSI: conecta la simulación con la teoría de capas.
                        """,
                false
        ));

        StyledButton lastModeButton = new StyledButton("Última simulación", StyledButton.Kind.TERTIARY);
        lastModeButton.setOnAction(event -> {
            if (currentScreen == WorkspaceScreen.TCP) {
                openSimpleWorkspace(ProtocolType.TCP);
            } else if (currentScreen == WorkspaceScreen.UDP) {
                openSimpleWorkspace(ProtocolType.UDP);
            } else if (currentScreen == WorkspaceScreen.COMPARE) {
                openComparisonWorkspace();
            } else if (currentScreen == WorkspaceScreen.LAYERS) {
                openLayersWorkspace();
            } else {
                openComparisonWorkspace();
            }
        });

        StyledButton aboutButton = new StyledButton("Acerca de", StyledButton.Kind.TERTIARY);
        aboutButton.setOnAction(event -> openTextModal(
                "Acerca de",
                "Resumen del producto.",
                """
                        Herramienta educativa de escritorio para explicar TCP, UDP, comparación entre protocolos,
                        congestión, control de flujo, capas, encapsulación y estructura de paquetes.
                        """,
                false
        ));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(10, spacer, helpButton, lastModeButton, aboutButton);
        row.setAlignment(Pos.CENTER_RIGHT);
        return row;
    }

    private Node buildHomeIntroContent() {
        Label body = new Label("Selecciona un modo principal o entra por un escenario rápido. La comparación entre TCP y UDP está pensada como demo central para clase.");
        body.setWrapText(true);
        body.setStyle("-fx-font-size: 13px; -fx-text-fill: #486581;");

        HBox steps = new HBox(10,
                headerMetric("1. Elige protocolo", "#4F8EF7"),
                headerMetric("2. Ajusta escenario", "#5BAA4A"),
                headerMetric("3. Explica resultados", "#8A63D2")
        );
        steps.setAlignment(Pos.CENTER_LEFT);
        return new VBox(10, body, steps);
    }

    private Node buildQuickAccessPanel() {
        FlowPane row = new FlowPane(12, 12);
        row.setPrefWrapLength(1220);

        QuickAccessChip handshake = new QuickAccessChip("[]", "Handshake TCP", "#4F8EF7", "/icons/tcp.png");
        handshake.setOnMouseClicked(event -> openScenarioShortcut("tcp-handshake", SimulationMode.SIMPLE));

        QuickAccessChip retransmission = new QuickAccessChip("!!", "Pérdida y retransmisión", "#4F8EF7", "/icons/tcp.png");
        retransmission.setOnMouseClicked(event -> openScenarioShortcut("tcp-congestion-timeout", SimulationMode.SIMPLE));

        QuickAccessChip udpBasic = new QuickAccessChip(">>", "UDP básico", "#8A63D2", "/icons/udp.png");
        udpBasic.setOnMouseClicked(event -> openScenarioShortcut("udp-basic", SimulationMode.SIMPLE));

        QuickAccessChip comparison = new QuickAccessChip("<|>", "Comparación TCP vs UDP", "#4F8EF7", "/icons/compare.png");
        comparison.setOnMouseClicked(event -> openScenarioShortcut("tcp-handshake", SimulationMode.COMPARE));

        QuickAccessChip window = new QuickAccessChip("<>", "Ventana deslizante", "#5BAA4A", "/icons/tcp.png");
        window.setOnMouseClicked(event -> openScenarioShortcut("tcp-congestion-growth", SimulationMode.SIMPLE));

        QuickAccessChip congestion = new QuickAccessChip("^^", "Congestión TCP", "#F08A24", "/icons/tcp.png");
        congestion.setOnMouseClicked(event -> openScenarioShortcut("tcp-congestion-duplicate-ack", SimulationMode.SIMPLE));

        QuickAccessChip layers = new QuickAccessChip("[#]", "Modelo OSI / TCP-IP", "#F08A24", "/icons/layers.png");
        layers.setOnMouseClicked(event -> openLayersWorkspace());

        row.getChildren().addAll(handshake, retransmission, udpBasic, comparison, window, congestion, layers);
        return row;
    }

    private void openScenarioShortcut(String scenarioId, SimulationMode mode) {
        Scenario scenario = findScenarioById(scenarioId);
        if (scenario == null) {
            return;
        }
        scenarioSelector.setValue(scenario);
        applyScenarioSelection(scenario);
        modeSelector.setValue(mode);
        if (mode == SimulationMode.COMPARE) {
            openComparisonWorkspace();
        } else {
            openSimpleWorkspace(scenario.getProtocol());
        }
    }

    private Scenario findScenarioById(String scenarioId) {
        for (Scenario scenario : scenarios) {
            if (scenario.getId().equals(scenarioId)) {
                return scenario;
            }
        }
        return null;
    }

    private VBox buildWorkspaceContent() {
        simpleModeView = buildSimpleModeView();
        comparisonModeView = new ComparisonModeView(details -> openTextModal(
                "Detalle del paquete",
                "Lectura puntual del paquete seleccionado.",
                details,
                true
        ));
        layersLearningView = new LayersLearningView(details -> openTextModal(
                "Estructura interna del paquete",
                "Vista didáctica de encapsulación y cabeceras simplificadas.",
                details,
                true
        ));
        layersModeView = layersLearningView;
        comparisonModeView.setVisible(false);
        comparisonModeView.setManaged(false);
        comparisonModeView.setViewMode(currentViewMode);
        layersModeView.setVisible(false);
        layersModeView.setManaged(false);

        modeContentStack = new StackPane(simpleModeView, comparisonModeView, layersModeView);
        workspaceIntroCard = new DashboardCard("ESPACIO DE TRABAJO", "Simulación activa",
                "La configuración se abre en modal y la barra inferior concentra la ejecución y navegación.");
        workspaceIntroCard.setStyle(UiTheme.HERO_CARD);
        workspaceIntroCard.setContent(buildWorkspaceIntroBar());

        VBox content = new VBox(18, workspaceIntroCard, modeContentStack);
        content.setPadding(new Insets(0, 0, 12, 0));
        return content;
    }

    private Node buildWorkspaceIntroBar() {
        workspaceConfigButton = new StyledButton("Configurar simulación", StyledButton.Kind.SOFT);
        workspaceConfigButton.setOnAction(event -> openConfigurationModal());
        viewModeToggle = new ViewModeToggle();
        viewModeToggle.setOnModeChanged(this::applySimulationViewMode);

        workspaceLegendPane = new FlowPane(12, 8,
                compactLegendChip(Color.web("#93c5fd"), "TCP SYN"),
                compactLegendChip(Color.web("#60a5fa"), "TCP SYN-ACK"),
                compactLegendChip(Color.web("#86efac"), "TCP ACK"),
                compactLegendChip(Color.web("#7dd3fc"), "TCP DATA"),
                compactLegendChip(Color.web("#c4b5fd"), "UDP"),
                compactLegendChip(Color.web("#fecaca"), "Perdido"),
                compactLegendChip(Color.web("#fed7aa"), "Retransmitido")
        );
        workspaceLegendPane.setAlignment(Pos.CENTER_RIGHT);
        workspaceLegendPane.setPrefWrapLength(860);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(16, workspaceConfigButton, viewModeToggle, spacer, workspaceLegendPane);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(2, 0, 0, 0));
        updateWorkspaceToolbar();
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
        if (appHeader != null) {
            appHeader.setVisible(true);
            appHeader.setManaged(true);
        }
        if (comparisonModeView != null) {
            comparisonModeView.stop();
        }
        if (player != null) {
            player.stop();
        }
        updateWorkspaceToolbar();
        updateBottomBarForScreen();
    }

    private void openSimpleWorkspace(ProtocolType protocolType) {
        currentScreen = protocolType == ProtocolType.TCP ? WorkspaceScreen.TCP : WorkspaceScreen.UDP;
        if (tcpTopProtocolBadge != null) {
            tcpTopProtocolBadge.setText("⌁ " + protocolType.name() + " activo");
            tcpTopProtocolBadge.setStyle(protocolType == ProtocolType.TCP ? tcpBadgeStyle() : udpBadgeStyle());
        }
        modeSelector.setValue(SimulationMode.SIMPLE);
        protocolSelector.setDisable(false);
        protocolSelector.setValue(protocolType);
        switchSimulationMode(SimulationMode.SIMPLE);
        refreshTheoryPanel();
        updateLayersLearningContext();
        revealWorkspace();
        onReset();
    }

    private void openComparisonWorkspace() {
        currentScreen = WorkspaceScreen.COMPARE;
        modeSelector.setValue(SimulationMode.COMPARE);
        switchSimulationMode(SimulationMode.COMPARE);
        refreshTheoryPanel();
        updateLayersLearningContext();
        revealWorkspace();
    }

    private void openLayersWorkspace() {
        currentScreen = WorkspaceScreen.LAYERS;
        refreshTheoryPanel();
        updateLayersLearningContext();
        revealWorkspace();
        if (simpleModeView != null) {
            simpleModeView.setVisible(false);
            simpleModeView.setManaged(false);
        }
        if (comparisonModeView != null) {
            comparisonModeView.setVisible(false);
            comparisonModeView.setManaged(false);
            comparisonModeView.stop();
        }
        if (layersModeView != null) {
            layersModeView.setVisible(true);
            layersModeView.setManaged(true);
        }
        if (player != null) {
            player.stop();
        }
        updatePlaybackButtons();
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
            boolean useEmbeddedTcpBar = currentScreen == WorkspaceScreen.TCP || currentScreen == WorkspaceScreen.UDP;
            bottomControlBar.setVisible(!useEmbeddedTcpBar);
            bottomControlBar.setManaged(!useEmbeddedTcpBar);
        }
        if (appHeader != null) {
            boolean useEmbeddedTcpBar = currentScreen == WorkspaceScreen.TCP || currentScreen == WorkspaceScreen.UDP;
            appHeader.setVisible(!useEmbeddedTcpBar);
            appHeader.setManaged(!useEmbeddedTcpBar);
        }
        updateWorkspaceToolbar();
        updateBottomBarForScreen();
    }

    private void updateWorkspaceToolbar() {
        if (workspaceConfigButton == null || workspaceLegendPane == null || viewModeToggle == null) {
            return;
        }
        boolean showSimulationTools = currentScreen == WorkspaceScreen.TCP
                || currentScreen == WorkspaceScreen.UDP
                || currentScreen == WorkspaceScreen.COMPARE;
        boolean embeddedTcpLayout = currentScreen == WorkspaceScreen.TCP || currentScreen == WorkspaceScreen.UDP;
        if (workspaceIntroCard != null) {
            workspaceIntroCard.setVisible(!embeddedTcpLayout);
            workspaceIntroCard.setManaged(!embeddedTcpLayout);
        }
        workspaceConfigButton.setVisible(showSimulationTools);
        workspaceConfigButton.setManaged(showSimulationTools);
        viewModeToggle.setVisible(showSimulationTools);
        viewModeToggle.setManaged(showSimulationTools);
        workspaceLegendPane.setVisible(showSimulationTools);
        workspaceLegendPane.setManaged(showSimulationTools);
    }

    private void updateBottomBarForScreen() {
        if (bottomControlBar == null) {
            return;
        }
        boolean simulationWorkspace = currentScreen == WorkspaceScreen.TCP
                || currentScreen == WorkspaceScreen.UDP
                || currentScreen == WorkspaceScreen.COMPARE;
        bottomControlBar.getRunButton().setDisable(!simulationWorkspace);
        bottomControlBar.getPlayPauseButton().setDisable(!simulationWorkspace);
        bottomControlBar.getStepButton().setDisable(!simulationWorkspace);
        bottomControlBar.getResetButton().setDisable(!simulationWorkspace);
        bottomControlBar.getReviewFirstButton().setDisable(!simulationWorkspace);
        bottomControlBar.getReviewStepBackButton().setDisable(!simulationWorkspace);
        bottomControlBar.getReviewStepForwardButton().setDisable(!simulationWorkspace);
        bottomControlBar.getReviewLastButton().setDisable(!simulationWorkspace);
        bottomControlBar.getReviewBox().setManaged(simulationWorkspace && !uiSnapshots.isEmpty());
        bottomControlBar.getReviewBox().setVisible(simulationWorkspace && !uiSnapshots.isEmpty());
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
        Node topbar = buildTcpWorkspaceTopbar();
        Node sidebar = buildTcpSidebar();
        VBox centerColumn = buildSimpleCenter();
        Node rightPanel = buildRightPanel();
        Node legend = buildTcpLegendBar();

        GridPane layout = new GridPane();
        layout.setHgap(14);
        layout.setVgap(14);
        layout.setAlignment(Pos.TOP_LEFT);
        layout.setStyle("-fx-background-color: #f3f7fb;");

        ColumnConstraints left = new ColumnConstraints(300, 300, 300);
        ColumnConstraints center = new ColumnConstraints(620, 960, Double.MAX_VALUE);
        center.setHgrow(Priority.ALWAYS);
        ColumnConstraints right = new ColumnConstraints(430, 430, 430);
        layout.getColumnConstraints().setAll(left, center, right);
        layout.add(sidebar, 0, 0);
        layout.add(centerColumn, 1, 0);
        layout.add(rightPanel, 2, 0);
        GridPane.setHgrow(centerColumn, Priority.ALWAYS);
        centerColumn.setMaxWidth(Double.MAX_VALUE);

        VBox shell = new VBox(14, topbar, layout, legend);
        shell.setPadding(new Insets(0, 0, 14, 0));
        shell.setStyle("-fx-background-color: #f3f7fb;");
        mainContentRow = new FlowPane();
        mainContentRow.getChildren().setAll(shell);
        mainContentRow.setStyle("-fx-background-color: #f3f7fb;");
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

        Node networkCanvasViewport = buildNetworkCanvasViewport();
        HBox simulationLane = new HBox(12, clientHistoryCard, networkCanvasViewport, serverHistoryCard);
        simulationLane.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(networkCanvasViewport, Priority.ALWAYS);

        simulationRow = new FlowPane();
        simulationRow.getChildren().setAll(simulationLane);

        DashboardCard simulationCard = new DashboardCard(null, "Diagrama temporal", "Intercambio ordenado de mensajes entre cliente y servidor");
        simulationCard.setStyle(tcpCardStyle());
        simulationCard.setPadding(new Insets(16, 18, 18, 18));
        simulationCard.setMinHeight(500);
        simulationCard.setPrefHeight(500);
        simulationCard.setMaxHeight(500);
        StackPane simulationModeStack = new StackPane(simulationRow, sequenceDiagramView);
        simulationModeStack.setAlignment(Pos.TOP_CENTER);
        simulationModeStack.setMaxWidth(Double.MAX_VALUE);
        StackPane.setAlignment(sequenceDiagramView, Pos.TOP_CENTER);
        StackPane.setAlignment(simulationRow, Pos.TOP_CENTER);
        simulationCard.setContent(simulationModeStack);
        simulationCard.setMaxWidth(Double.MAX_VALUE);
        clipToBounds(simulationCard);

        Node metrics = buildTcpMetricGrid();
        VBox box = new VBox(12, simulationCard, metrics);
        box.setFillWidth(true);
        box.setMaxWidth(Double.MAX_VALUE);
        box.setPrefWidth(960);
        applySimulationViewMode(currentViewMode);
        return box;
    }

    private Node buildNetworkCanvasViewport() {
        Pane viewport = new Pane(networkCanvas);
        viewport.setMinSize(552, 410);
        viewport.setPrefSize(552, 410);
        viewport.setMaxSize(552, 410);
        Rectangle clip = new Rectangle(552, 410);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        viewport.setClip(clip);
        networkCanvas.getTransforms().setAll(new Scale(0.78, 0.92, 0, 0));
        networkCanvas.setLayoutX(2);
        networkCanvas.setLayoutY(8);
        return viewport;
    }

    private Node buildTcpWorkspaceTopbar() {
        Label menu = new Label("☰");
        menu.setAlignment(Pos.CENTER);
        menu.setOnMouseClicked(event -> showHomeScreen());
        menu.setStyle("-fx-min-width: 34; -fx-min-height: 34; -fx-alignment: center;"
                + "-fx-background-color: #e8f1ff; -fx-background-radius: 10;"
                + "-fx-text-fill: #2f80ed; -fx-font-size: 17px; -fx-font-weight: bold; -fx-cursor: hand;");

        Label brand = new Label("Simulador visual de TCP y UDP");
        brand.setStyle("-fx-font-size: 17px; -fx-font-weight: 800; -fx-text-fill: #102a43;");
        HBox brandBox = new HBox(12, menu, brand);
        brandBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(brandBox, Priority.ALWAYS);

        tcpTopProtocolBadge = new Label("⌁ TCP activo");
        tcpTopProtocolBadge.setStyle(tcpBadgeStyle());

        tcpTopResetButton = topGhostButton("↻ Reiniciar");
        tcpTopResetButton.setOnAction(event -> resetActiveMode());
        tcpTopPlayPauseButton = topGhostButton("Ⅱ Pausar");
        tcpTopPlayPauseButton.setOnAction(event -> togglePlaybackPause());
        tcpTopStepButton = topGhostButton("▷ Paso a paso");
        tcpTopStepButton.setOnAction(event -> {
            player.stepForward();
            updatePlaybackButtons();
        });

        tcpTopViewModeToggle = new ViewModeToggle();
        tcpTopViewModeToggle.setValue(currentViewMode);
        tcpTopViewModeToggle.setOnModeChanged(this::applySimulationViewMode);

        Label speedLabel = new Label("Velocidad");
        speedLabel.setStyle("-fx-text-fill: #627d98; -fx-font-size: 13px; -fx-font-weight: 600;");
        ComboBox<String> speed = new ComboBox<>();
        speed.getItems().setAll("0.5x", "1.0x", "1.5x", "2.0x", "3.0x");
        speed.setValue("1.0x");
        speed.setPrefWidth(88);
        speed.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d9e5f2; -fx-border-radius: 0; -fx-background-radius: 0;");
        speed.setOnAction(event -> {
            String value = speed.getValue();
            if (value != null && speedSlider != null) {
                speedSlider.setValue(Double.parseDouble(value.replace("x", "")));
            }
        });
        HBox speedBox = new HBox(10, speedLabel, speed);
        speedBox.setAlignment(Pos.CENTER_LEFT);
        speedBox.setPadding(new Insets(0, 0, 0, 12));
        speedBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d9e5f2;"
                + "-fx-background-radius: 10; -fx-border-radius: 10;");

        HBox topbar = new HBox(14, brandBox, tcpTopProtocolBadge, tcpTopViewModeToggle, tcpTopResetButton, tcpTopPlayPauseButton, tcpTopStepButton, speedBox);
        topbar.setAlignment(Pos.CENTER_LEFT);
        topbar.setPadding(new Insets(0, 18, 0, 18));
        topbar.setMinHeight(58);
        topbar.setStyle("-fx-background-color: rgba(255,255,255,0.95);"
                + "-fx-border-color: transparent transparent #d9e5f2 transparent;"
                + "-fx-border-width: 0 0 1 0;");
        return topbar;
    }

    private String tcpBadgeStyle() {
        return "-fx-background-color: #e3fcef; -fx-border-color: #bff0d4;"
                + "-fx-background-radius: 999; -fx-border-radius: 999;"
                + "-fx-text-fill: #0f7a3f; -fx-font-size: 13px; -fx-font-weight: 700;"
                + "-fx-padding: 7 14 7 14;";
    }

    private String udpBadgeStyle() {
        return "-fx-background-color: #f1e8ff; -fx-border-color: #dac7ff;"
                + "-fx-background-radius: 999; -fx-border-radius: 999;"
                + "-fx-text-fill: #6b35b5; -fx-font-size: 13px; -fx-font-weight: 700;"
                + "-fx-padding: 7 14 7 14;";
    }

    private Button topGhostButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d9e5f2;"
                + "-fx-background-radius: 10; -fx-border-radius: 10;"
                + "-fx-text-fill: #102a43; -fx-font-weight: 700; -fx-padding: 9 14 9 14;");
        return button;
    }

    private Node buildTcpSidebar() {
        controlPanel.setPrefWidth(300);
        controlPanel.setMaxWidth(300);
        return controlPanel;
    }

    private Node buildTcpMetricGrid() {
        slidingWindowPanel = new SlidingWindowPanel();
        congestionPanel = new CongestionPanel();
        DashboardCard bufferCard = new DashboardCard(null, "Buffer de recepción", "(Servidor)");
        bufferCard.setStyle(tcpCardStyle());
        bufferCard.setMinHeight(TCP_METRIC_CARD_HEIGHT);
        bufferCard.setPrefHeight(TCP_METRIC_CARD_HEIGHT);
        bufferCard.setMaxHeight(TCP_METRIC_CARD_HEIGHT);
        bufferCard.setContent(buildReceiverBufferGraphic());
        clipToBounds(bufferCard);

        DashboardCard transitCard = new DashboardCard(null, "Paquetes en tránsito", null);
        transitCard.setStyle(tcpCardStyle());
        transitCard.setMinHeight(TCP_METRIC_CARD_HEIGHT);
        transitCard.setPrefHeight(TCP_METRIC_CARD_HEIGHT);
        transitCard.setMaxHeight(TCP_METRIC_CARD_HEIGHT);
        transitCard.setContent(new VBox(8, buildTransitLine(), metricText("0 paquetes en tránsito\nLatencia actual: 1200 ms   Jitter: 0 ms   Pérdida: 20%")));
        clipToBounds(transitCard);

        slidingWindowPanel.setStyle(tcpCardStyle());
        slidingWindowPanel.setMinHeight(TCP_METRIC_CARD_HEIGHT);
        slidingWindowPanel.setPrefHeight(TCP_METRIC_CARD_HEIGHT);
        slidingWindowPanel.setMaxHeight(TCP_METRIC_CARD_HEIGHT);
        clipToBounds(slidingWindowPanel);
        congestionPanel.setStyle(tcpCardStyle());
        congestionPanel.setMinHeight(TCP_METRIC_CARD_HEIGHT);
        congestionPanel.setPrefHeight(TCP_METRIC_CARD_HEIGHT);
        congestionPanel.setMaxHeight(TCP_METRIC_CARD_HEIGHT);
        clipToBounds(congestionPanel);
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(50);
        c1.setHgrow(Priority.ALWAYS);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(50);
        c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().setAll(c1, c2);
        grid.add(slidingWindowPanel, 0, 0);
        grid.add(bufferCard, 1, 0);
        grid.add(congestionPanel, 0, 1);
        grid.add(transitCard, 1, 1);
        return grid;
    }

    private Node byteRow(int ackCount, int emptyCount) {
        HBox row = new HBox(6);
        for (int i = 0; i < ackCount; i++) {
            row.getChildren().add(byteCell("#6fcf97", "#68c38f"));
        }
        for (int i = 0; i < emptyCount; i++) {
            row.getChildren().add(byteCell("#edf2f7", "#d9e5f2"));
        }
        return row;
    }

    private Node buildReceiverBufferGraphic() {
        receiverBufferCells = new HBox(6);
        receiverBufferCells.setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < 12; i++) {
            receiverBufferCells.getChildren().add(byteCell("#edf2f7", "#d9e5f2"));
        }

        receiverBufferUsageLabel = metricText("Buffer: 0 / 24 bytes");
        receiverAdvertisedWindowLabel = metricText("Ventana anunciada: 24 bytes");
        Label hint = new Label("El verde representa bytes ocupando el buffer del servidor; al liberar datos, vuelve a crecer la ventana anunciada.");
        hint.setWrapText(true);
        hint.setStyle("-fx-text-fill: #627d98; -fx-font-size: 12px;");

        return new VBox(8,
                receiverBufferCells,
                legendInline("Datos en buffer", "Espacio libre"),
                receiverBufferUsageLabel,
                receiverAdvertisedWindowLabel,
                hint
        );
    }

    private void updateReceiverBufferGraphic(FlowControlSnapshot snapshot) {
        if (receiverBufferCells == null || snapshot == null) {
            return;
        }
        int capacity = Math.max(1, snapshot.getReceiverBufferCapacity());
        int used = Math.max(0, Math.min(capacity, snapshot.getReceiverBufferUsed()));
        int advertised = Math.max(0, snapshot.getReceiverAdvertisedWindow());
        int cellCount = receiverBufferCells.getChildren().size();

        for (int i = 0; i < cellCount; i++) {
            double cellStart = i * (capacity / (double) cellCount);
            double cellEnd = (i + 1) * (capacity / (double) cellCount);
            boolean occupied = cellStart < used && cellEnd > 0;
            String fill = occupied ? "#6fcf97" : "#edf2f7";
            String stroke = occupied ? "#68c38f" : "#d9e5f2";
            receiverBufferCells.getChildren().get(i).setStyle("-fx-background-color: " + fill + "; -fx-border-color: " + stroke + ";"
                    + "-fx-background-radius: 5; -fx-border-radius: 5;");
        }

        if (receiverBufferUsageLabel != null) {
            receiverBufferUsageLabel.setText("Buffer: " + used + " / " + capacity + " bytes");
        }
        if (receiverAdvertisedWindowLabel != null) {
            receiverAdvertisedWindowLabel.setText("Ventana anunciada: " + advertised + " bytes");
        }
    }

    private Region byteCell(String fill, String stroke) {
        Region cell = new Region();
        cell.setMinSize(22, 22);
        cell.setPrefSize(22, 22);
        cell.setMaxSize(22, 22);
        cell.setStyle("-fx-background-color: " + fill + "; -fx-border-color: " + stroke + ";"
                + "-fx-background-radius: 5; -fx-border-radius: 5;");
        return cell;
    }

    private Node legendInline(String first, String second) {
        Label a = new Label("■ " + first);
        Label b = new Label("■ " + second);
        a.setStyle("-fx-text-fill: #627d98; -fx-font-size: 12px;");
        b.setStyle("-fx-text-fill: #627d98; -fx-font-size: 12px;");
        return new HBox(14, a, b);
    }

    private Label metricText(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle("-fx-text-fill: #627d98; -fx-font-size: 13px; -fx-line-spacing: 4px;");
        return label;
    }

    private Node buildTransitLine() {
        Label client = new Label("▣ Cliente");
        Label cloud = new Label("☁");
        Label server = new Label("▤ Servidor");
        client.setWrapText(false);
        server.setWrapText(false);
        client.setMinWidth(118);
        server.setMinWidth(128);
        client.setStyle("-fx-text-fill: #2f80ed; -fx-font-size: 20px; -fx-font-weight: 800; -fx-alignment: center;");
        cloud.setStyle("-fx-text-fill: #a9c8e8; -fx-font-size: 34px;");
        server.setStyle("-fx-text-fill: #2f80ed; -fx-font-size: 20px; -fx-font-weight: 800; -fx-alignment: center;");
        HBox line = new HBox(28, client, cloud, server);
        line.setAlignment(Pos.CENTER);
        line.setMinHeight(46);
        line.setPrefHeight(46);
        return line;
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
        VBox panel = new VBox(12);
        panel.setPrefWidth(430);
        panel.setMinWidth(430);
        panel.setMaxWidth(430);

        EventLogPanel eventLogPanel = new EventLogPanel();
        eventLogPanel.setStyle(tcpCardStyle());
        eventLogPanel.setPreferredHeight(300);
        logArea = eventLogPanel.getLogArea();

        StatePanel statePanel = new StatePanel();
        statePanel.setStyle(tcpCardStyle());
        statePanel.setMinHeight(190);
        statePanel.setPrefHeight(190);
        statePanel.setMaxHeight(190);
        clipToBounds(statePanel);
        clientStateLabel = statePanel.getClientStateLabel();
        serverStateLabel = statePanel.getServerStateLabel();
        statusLabel = statePanel.getStatusLabel();

        HBox messageCard = new HBox(14);
        messageCard.setAlignment(Pos.CENTER_LEFT);
        messageCard.setPadding(new Insets(17));
        messageCard.setStyle("-fx-background-color: linear-gradient(to bottom, #f1fff7, #e3fcef);"
                + "-fx-background-radius: 16; -fx-border-radius: 16;"
                + "-fx-border-color: #c6f2d8; -fx-effect: dropshadow(gaussian, rgba(16,42,67,0.07), 30, 0.20, 0, 10);");
        messageCard.setMinHeight(96);
        messageCard.setPrefHeight(96);
        messageCard.setMaxHeight(96);
        clipToBounds(messageCard);
        Label successIcon = new Label("✓");
        successIcon.setAlignment(Pos.CENTER);
        successIcon.setStyle("-fx-min-width: 44; -fx-min-height: 44; -fx-background-color: #26ad61;"
                + "-fx-background-radius: 999; -fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: 800;");
        clientDataArea = new TextArea();
        serverDataArea = new TextArea();
        clientDataArea.setVisible(false);
        clientDataArea.setManaged(false);
        serverDataArea.setVisible(false);
        serverDataArea.setManaged(false);
        serverDataArea.setEditable(false);
        serverDataArea.setWrapText(true);
        serverDataArea.setPrefRowCount(3);
        serverDataArea.setStyle("-fx-control-inner-background: transparent; -fx-background-color: transparent;"
                + "-fx-border-color: transparent; -fx-text-fill: #0f7a3f; -fx-font-size: 13px;");
        Label successTitle = new Label("Simulación completada");
        successTitle.setStyle("-fx-text-fill: #063b21; -fx-font-size: 16px; -fx-font-weight: 800;");
        Label successSubtitle = new Label("El mensaje se entregó correctamente.");
        successSubtitle.setStyle("-fx-text-fill: #2f6f4c; -fx-font-size: 13px;");
        Label successText = new Label("Sin simulación activa.");
        successText.setWrapText(true);
        successText.setMaxWidth(330);
        successText.setStyle("-fx-text-fill: #0f7a3f; -fx-font-size: 13px; -fx-font-weight: 700;");
        serverDataArea.textProperty().addListener((obs, oldText, text) -> successText.setText(text == null || text.isBlank()
                ? "Sin simulación activa."
                : text.replace('\n', ' ')));
        VBox successCopy = new VBox(4, successTitle, successSubtitle, successText);
        successCopy.setAlignment(Pos.CENTER_LEFT);
        messageCard.getChildren().setAll(successIcon, successCopy);
        messageCard.setMaxWidth(Double.MAX_VALUE);

        panel.getChildren().addAll(statePanel, eventLogPanel, messageCard);
        return panel;
    }

    private Node buildTcpLegendBar() {
        HBox legend = new HBox(24,
                legendItem("↗", "SYN", "Inicio de conexión", "#2f80ed", "#e8f1ff"),
                legendItem("↙", "SYN-ACK", "Respuesta del servidor", "#2f80ed", "#e8f1ff"),
                legendItem("↔", "ACK", "Confirmación", "#26ad61", "#e3fcef"),
                legendItem("▣", "DATA", "Datos de aplicación", "#2f80ed", "#e8f1ff"),
                legendItem("↯", "FIN", "Cierre", "#f2994a", "#fff1df"),
                legendItem("✖", "Pérdida", "Paquete perdido", "#ee5a5a", "#ffe9e9"),
                legendItem("↻", "Retrans.", "Retransmisión", "#f2994a", "#fff5db")
        );
        Label tip = new Label("ⓘ Los ACK son acumulativos: indican el siguiente byte esperado.");
        tip.setWrapText(true);
        tip.setMaxWidth(290);
        tip.setStyle("-fx-background-color: #e8f1ff; -fx-border-color: #cfe0f4;"
                + "-fx-background-radius: 12; -fx-border-radius: 12;"
                + "-fx-text-fill: #486581; -fx-font-size: 12px; -fx-padding: 10 12 10 12;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        legend.getChildren().addAll(spacer, tip);
        legend.setAlignment(Pos.CENTER_LEFT);
        legend.setPadding(new Insets(10, 14, 10, 14));
        legend.setMinHeight(56);
        legend.setPrefHeight(56);
        legend.setMaxHeight(56);
        legend.setStyle(tcpCardStyle());
        return legend;
    }

    private Node legendItem(String icon, String title, String text, String color, String bg) {
        Label iconLabel = new Label(icon);
        iconLabel.setAlignment(Pos.CENTER);
        iconLabel.setStyle("-fx-min-width: 22; -fx-min-height: 22; -fx-background-color: " + bg + ";"
                + "-fx-background-radius: 7; -fx-text-fill: " + color + "; -fx-font-weight: 800;");
        Label label = new Label(title + "  " + text);
        label.setStyle("-fx-text-fill: #627d98; -fx-font-size: 12px;");
        return new HBox(8, iconLabel, label);
    }

    private String tcpCardStyle() {
        return "-fx-background-color: #ffffff;"
                + "-fx-background-radius: 16;"
                + "-fx-border-radius: 16;"
                + "-fx-border-color: #d9e5f2;"
                + "-fx-border-width: 1;"
                + "-fx-effect: dropshadow(gaussian, rgba(16,42,67,0.07), 30, 0.20, 0, 10);";
    }

    private void clipToBounds(Region region) {
        Rectangle clip = new Rectangle();
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        clip.widthProperty().bind(region.widthProperty());
        clip.heightProperty().bind(region.heightProperty());
        region.setClip(clip);
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
        controlPanel.getRunButton().setOnAction(event -> startSimulation());
        controlPanel.getPlayPauseButton().setOnAction(event -> togglePlaybackPause());
        controlPanel.getLiveStepButton().setOnAction(event -> {
            player.stepForward();
            updatePlaybackButtons();
        });
        controlPanel.getResetButton().setOnAction(event -> resetActiveMode());
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
        if (currentScreen == WorkspaceScreen.LAYERS) {
            playPauseButton.setDisable(true);
            liveStepButton.setDisable(true);
            playPauseButton.setText("Pausar");
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
        if (tcpTopPlayPauseButton != null) {
            tcpTopPlayPauseButton.setDisable(!hasRemaining);
            tcpTopPlayPauseButton.setText(paused ? "▷ Reanudar" : "Ⅱ Pausar");
        }
        if (tcpTopStepButton != null) {
            tcpTopStepButton.setDisable(!hasRemaining);
        }
    }

    private void switchSimulationMode(SimulationMode mode) {
        boolean compare = mode == SimulationMode.COMPARE;
        boolean layers = currentScreen == WorkspaceScreen.LAYERS;
        if (simpleModeView != null) {
            simpleModeView.setVisible(!compare && !layers);
            simpleModeView.setManaged(!compare && !layers);
        }
        if (comparisonModeView != null) {
            comparisonModeView.setVisible(compare && !layers);
            comparisonModeView.setManaged(compare && !layers);
        }
        if (layersModeView != null) {
            layersModeView.setVisible(layers);
            layersModeView.setManaged(layers);
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
        updateWorkspaceToolbar();
        updateBottomBarForScreen();
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
            updateLayersLearningContext();
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
        updateLayersLearningContext();
    }

    private void refreshTheoryPanel() {
        if (protocolSelector == null || viewModel == null) {
            return;
        }
        if (currentScreen == WorkspaceScreen.LAYERS) {
            currentTheoryText = """
                    Encapsulación
                    Cuando una aplicación envía datos, cada capa añade su propia cabecera. El resultado final es una unidad de información preparada para atravesar la red.

                    Capa de transporte
                    La capa de transporte se encarga de la comunicación extremo a extremo. Aquí trabajan TCP y UDP.

                    Capa de red
                    La capa de red se ocupa del direccionamiento lógico y del encaminamiento. Aquí se encuentra IP.

                    Cabeceras
                    Las cabeceras contienen metadatos necesarios para que cada protocolo pueda realizar su función.

                    Modelos
                    El modelo TCP/IP describe la pila usada realmente en Internet. El modelo OSI ayuda a estudiar funciones y responsabilidades con más detalle.
                    """;
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
            updateReceiverBufferGraphic(snapshot);
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
                if (currentScreen == WorkspaceScreen.UDP || currentProtocol == ProtocolType.UDP) {
                    slidingWindowPanel.showUnavailable();
                } else {
                    slidingWindowPanel.reset();
                }
            }
            updateReceiverBufferGraphic(new FlowControlSnapshot(24, 0, 0, 0, 0, 24, 0, 24));
            if (sequenceDiagramView != null) {
                sequenceDiagramView.reset();
            }
            if (congestionPanel != null) {
                if (currentScreen == WorkspaceScreen.UDP || currentProtocol == ProtocolType.UDP) {
                    congestionPanel.showUnavailable();
                } else {
                    congestionPanel.reset();
                }
            }
            statusLabel.setText("Listo para iniciar");
            refreshTheoryPanel();
            updatePlaybackButtons();
            if (scenarioStartRequested) {
                scenarioStartMillis = System.currentTimeMillis();
                currentMessage = pendingMessage;
                currentProtocol = pendingProtocol;
                updateLayersLearningContext();
                addSimulationSeparator(clientPacketList, currentProtocol);
                addSimulationSeparator(serverPacketList, currentProtocol);
                int fragmentSize = fragmentSizeSpinner != null ? fragmentSizeSpinner.getValue() : 8;
                clientDataArea.setText(
                        "Protocolo: " + currentProtocol + "\n" +
                        "Tamaño de fragmento: " + fragmentSize + "\n" +
                        "Mensaje a enviar:\n\"" + currentMessage + "\""
                );
                if (currentProtocol == ProtocolType.TCP) {
                    FlowControlSnapshot initialFlow = new FlowControlSnapshot(
                            tcpWindowSizeSpinner.getValue(),
                            0,
                            0,
                            0,
                            currentMessage.length(),
                            tcpReceiverBufferSpinner.getValue(),
                            0,
                            tcpReceiverBufferSpinner.getValue()
                    );
                    if (slidingWindowPanel != null) {
                        slidingWindowPanel.update(initialFlow);
                    }
                    updateReceiverBufferGraphic(initialFlow);
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
                lastInspectablePacket = null;
                updateLayersLearningContext();
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
        lastInspectablePacket = copyPacket(packet);
        updateLayersLearningContext();
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
            case LAYERS -> "Guía docente sobre modelos TCP/IP, OSI, encapsulación y cabeceras.";
            default -> "Contexto docente asociado al modo de trabajo activo.";
        };
        openTextModal("Teoría", subtitle, currentTheoryText, false);
    }

    private void updateLayersLearningContext() {
        if (layersLearningView == null) {
            return;
        }
        ProtocolType protocol = currentProtocol;
        if (protocolSelector != null && protocolSelector.getValue() != null && currentScreen != WorkspaceScreen.COMPARE) {
            protocol = protocolSelector.getValue();
        }
        String message = currentMessage == null || currentMessage.isBlank()
                ? normalizeMessage(messageField != null ? messageField.getText() : "HOLA")
                : currentMessage;
        layersLearningView.updatePacketContext(lastInspectablePacket, protocol, message);
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
