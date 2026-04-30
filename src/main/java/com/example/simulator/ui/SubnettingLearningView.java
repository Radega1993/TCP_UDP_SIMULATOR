package com.example.simulator.ui;

import com.example.simulator.domain.ipv4.Ipv4SubnetCalculator;
import com.example.simulator.domain.subnetting.SubnetBlock;
import com.example.simulator.domain.subnetting.SubnettingCalculator;
import com.example.simulator.domain.subnetting.SubnettingMode;
import com.example.simulator.domain.subnetting.SubnettingResult;
import com.example.simulator.domain.subnetting.SubnetRoutingHop;
import com.example.simulator.domain.subnetting.SubnetRoutingResult;
import com.example.simulator.domain.subnetting.SubnetRoutingRoute;
import com.example.simulator.domain.subnetting.SubnetRoutingSimulator;
import com.example.simulator.domain.subnetting.VlsmAssignment;
import com.example.simulator.domain.subnetting.VlsmCalculator;
import com.example.simulator.domain.subnetting.VlsmNetworkRequest;
import com.example.simulator.domain.subnetting.VlsmResult;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubnettingLearningView extends VBox {
    private final TextField baseNetworkField = new TextField("192.168.1.0");
    private final Spinner<Integer> baseCidrSpinner = new Spinner<>(8, 30, 24);
    private final ToggleGroup modeGroup = new ToggleGroup();
    private final RadioButton bySubnets = new RadioButton("Número de subredes");
    private final RadioButton byHosts = new RadioButton("Hosts por red");
    private final ToggleGroup bitViewGroup = new ToggleGroup();
    private final ToggleButton binaryViewButton = new ToggleButton("Binario");
    private final ToggleButton decimalViewButton = new ToggleButton("Decimal");
    private final ToggleGroup practiceScenarioGroup = new ToggleGroup();
    private final ToggleButton officeScenarioButton = new ToggleButton("Oficina");
    private final ToggleButton companyScenarioButton = new ToggleButton("Empresa");
    private final ToggleGroup moduleTabGroup = new ToggleGroup();
    private final ToggleButton subnettingTabButton = new ToggleButton("Subnetting");
    private final ToggleButton vlsmTabButton = new ToggleButton("VLSM");
    private final StackPane moduleContentStack = new StackPane();
    private final Spinner<Integer> targetSpinner = new Spinner<>(1, 4096, 4);
    private final Label statusLabel = new Label();
    private final Label newMaskLabel = new Label();
    private final Label generatedSubnetsLabel = new Label();
    private final Label hostsPerSubnetLabel = new Label();
    private final Label incrementLabel = new Label();
    private final Label borrowedBitsLabel = new Label();
    private final FlowPane subnetBars = new FlowPane(8, 8);
    private final VBox binaryRows = new VBox(8);
    private final Label binaryLegendLabel = new Label();
    private final FlowPane practiceBlocks = new FlowPane(8, 8);
    private final VBox practiceTargets = new VBox(8);
    private final Label practiceFeedback = new Label();
    private final TextArea vlsmRequestsArea = new TextArea("Red A, 100\nRed B, 50\nRed C, 10");
    private final Label vlsmStatusLabel = new Label();
    private final Label vlsmUsageLabel = new Label();
    private final FlowPane vlsmTimeline = new FlowPane(8, 8);
    private final VBox vlsmRows = new VBox(8);
    private final VBox vlsmSteps = new VBox(8);
    private final Spinner<Integer> subnetRoutingTtlSpinner = new Spinner<>(1, 255, 4);
    private final Label subnetRoutingStatusLabel = new Label();
    private final Label subnetRoutingPathLabel = new Label();
    private final HBox subnetRoutingMap = new HBox(10);
    private final VBox subnetRoutingHops = new VBox(8);
    private final VBox subnetRoutingRoutes = new VBox(8);
    private final VBox subnetRows = new VBox(8);
    private final Label tableNote = new Label();
    private final Map<Integer, Node> subnetBarNodes = new HashMap<>();
    private final Map<String, String> practiceAssignments = new HashMap<>();
    private PracticeScenario activeScenario;

    public SubnettingLearningView(Runnable onHome, Runnable onTheory, Runnable onHelp) {
        setSpacing(14);
        setPadding(new Insets(0, 0, 14, 0));
        setStyle("-fx-background-color: #f4f7fb;");
        bySubnets.setToggleGroup(modeGroup);
        byHosts.setToggleGroup(modeGroup);
        binaryViewButton.setToggleGroup(bitViewGroup);
        decimalViewButton.setToggleGroup(bitViewGroup);
        officeScenarioButton.setToggleGroup(practiceScenarioGroup);
        companyScenarioButton.setToggleGroup(practiceScenarioGroup);
        subnettingTabButton.setToggleGroup(moduleTabGroup);
        vlsmTabButton.setToggleGroup(moduleTabGroup);
        bySubnets.setSelected(true);
        binaryViewButton.setSelected(true);
        officeScenarioButton.setSelected(true);
        subnettingTabButton.setSelected(true);
        activeScenario = officeScenario();
        baseCidrSpinner.setEditable(true);
        targetSpinner.setEditable(true);
        subnetRoutingTtlSpinner.setEditable(true);
        getChildren().addAll(buildTopbar(onHome, onTheory, onHelp), buildContent());
        addImmediateFeedback();
        renderPractice();
        calculateAndRender();
        calculateAndRenderVlsm();
    }

    public void resetInputs() {
        baseNetworkField.setText("192.168.1.0");
        baseCidrSpinner.getValueFactory().setValue(24);
        bySubnets.setSelected(true);
        targetSpinner.getValueFactory().setValue(4);
        calculateAndRender();
    }

    private Node buildTopbar(Runnable onHome, Runnable onTheory, Runnable onHelp) {
        StackPane menu = new StackPane(icon("/icons/menu.svg", 20));
        menu.setOnMouseClicked(event -> onHome.run());
        menu.setStyle("-fx-min-width: 34; -fx-min-height: 34; -fx-alignment: center;"
                + "-fx-background-color: #e8f1ff; -fx-background-radius: 10; -fx-cursor: hand;");
        Label brand = new Label("AulaRed");
        brand.setStyle("-fx-font-size: 17px; -fx-font-weight: 800; -fx-text-fill: #102a43;");
        HBox brandBox = new HBox(12, menu, brand);
        brandBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(brandBox, Priority.ALWAYS);
        Label modePill = pill("Módulo IP: Subnetting", "#e7f8ef", "#087f4f");
        Button reset = ghostButton("Reiniciar", "/icons/refresh.svg");
        reset.setOnAction(event -> resetInputs());
        Button theory = ghostButton("Teoría", "/icons/info.svg");
        theory.setOnAction(event -> onTheory.run());
        Button help = ghostButton("Ayuda", "/icons/help.svg");
        help.setOnAction(event -> onHelp.run());
        HBox topbar = new HBox(16, brandBox, modePill, statusLabel, new HBox(8, reset, theory, help));
        topbar.setAlignment(Pos.CENTER_LEFT);
        topbar.setPadding(new Insets(0, 18, 0, 18));
        topbar.setMinHeight(58);
        topbar.setStyle("-fx-background-color: #ffffff; -fx-border-color: transparent transparent #d9e6f2 transparent;"
                + "-fx-border-width: 0 0 1 0; -fx-effect: dropshadow(gaussian, rgba(17,42,67,0.04), 12, 0.2, 0, 2);");
        return topbar;
    }

    private Node buildContent() {
        GridPane layout = new GridPane();
        layout.setHgap(14);
        layout.setVgap(14);
        layout.setPadding(new Insets(0, 14, 0, 14));
        layout.getColumnConstraints().setAll(fixedColumn(330), growingColumn(840), fixedColumn(360));
        layout.add(buildConfigCard(), 0, 0);
        layout.add(buildTabbedModuleContent(), 1, 0);
        layout.add(buildTheoryCard(), 2, 0);
        return layout;
    }

    private Node buildTabbedModuleContent() {
        VBox subnettingContent = new VBox(14, buildVisualCard(), buildBinaryCard(), buildPracticeCard(), buildSubnetListCard());
        VBox vlsmContent = new VBox(14, buildVlsmCard(), buildSubnetRoutingCard());
        moduleContentStack.getChildren().setAll(subnettingContent, vlsmContent);
        configureModuleTabs(subnettingContent, vlsmContent);
        return new VBox(12, buildModuleTabs(), moduleContentStack);
    }

    private Node buildModuleTabs() {
        subnettingTabButton.setMinHeight(40);
        vlsmTabButton.setMinHeight(40);
        subnettingTabButton.setGraphic(icon("/icons/network.svg", 16));
        vlsmTabButton.setGraphic(icon("/icons/router.svg", 16));
        subnettingTabButton.setGraphicTextGap(8);
        vlsmTabButton.setGraphicTextGap(8);
        HBox tabs = new HBox(8, subnettingTabButton, vlsmTabButton);
        tabs.setAlignment(Pos.CENTER_LEFT);
        tabs.setPadding(new Insets(2, 0, 0, 0));
        tabs.setStyle("-fx-background-color: transparent;");
        return tabs;
    }

    private void configureModuleTabs(Node subnettingContent, Node vlsmContent) {
        moduleTabGroup.selectedToggleProperty().addListener((obs, old, selected) -> {
            if (selected == null) {
                moduleTabGroup.selectToggle(old);
                return;
            }
            showModuleTab(subnettingContent, vlsmContent);
        });
        showModuleTab(subnettingContent, vlsmContent);
    }

    private void showModuleTab(Node subnettingContent, Node vlsmContent) {
        boolean showSubnetting = subnettingTabButton.isSelected();
        subnettingContent.setVisible(showSubnetting);
        subnettingContent.setManaged(showSubnetting);
        vlsmContent.setVisible(!showSubnetting);
        vlsmContent.setManaged(!showSubnetting);
        subnettingTabButton.setStyle(toggleStyle(showSubnetting));
        vlsmTabButton.setStyle(toggleStyle(!showSubnetting));
    }

    private Node buildConfigCard() {
        DashboardCard card = new DashboardCard("CONFIGURACIÓN", "Subnetting básico", "Divide una red grande en redes más pequeñas.");
        card.setStyle(cardStyle());
        VBox content = new VBox(12,
                inputBlock("Red base", baseNetworkField, "/icons/ip.svg"),
                spinnerBlock("Máscara base", baseCidrSpinner, "CIDR inicial, por ejemplo /24."),
                modeBlock(),
                spinnerBlock("Objetivo", targetSpinner, "Subredes necesarias o hosts por subred."),
                primaryButton()
        );
        card.setContent(content);
        return card;
    }

    private Node buildVisualCard() {
        DashboardCard card = new DashboardCard("VISUAL", "Red original dividida", "Cada bloque representa una subred generada.");
        card.setStyle(cardStyle());
        subnetBars.setPrefWrapLength(860);
        card.setContent(new VBox(12, resultSummaryGrid(), subnetBars));
        return card;
    }

    private Node buildBinaryCard() {
        DashboardCard card = new DashboardCard("BINARIO", "Qué hace la máscara", "Verde = red. Azul = host.");
        card.setStyle(cardStyle());
        binaryViewButton.setMinHeight(34);
        decimalViewButton.setMinHeight(34);
        binaryViewButton.setStyle(toggleStyle(true));
        decimalViewButton.setStyle(toggleStyle(false));
        bitViewGroup.selectedToggleProperty().addListener((obs, old, selected) -> {
            if (selected == null) {
                bitViewGroup.selectToggle(old);
                return;
            }
            binaryViewButton.setStyle(toggleStyle(binaryViewButton.isSelected()));
            decimalViewButton.setStyle(toggleStyle(decimalViewButton.isSelected()));
            calculateAndRender();
        });
        HBox toggles = new HBox(8, binaryViewButton, decimalViewButton);
        toggles.setAlignment(Pos.CENTER_LEFT);
        HBox legend = new HBox(10, legendPill("Bits de red", "#e7f8ef", "#087f4f"),
                legendPill("Bits de host", "#eaf4ff", "#1d4ed8"), binaryLegendLabel);
        legend.setAlignment(Pos.CENTER_LEFT);
        binaryLegendLabel.setStyle("-fx-text-fill: #5f7390; -fx-font-size: 12px; -fx-font-weight: 800;");
        card.setContent(new VBox(10, toggles, binaryRows, legend));
        return card;
    }

    private Node buildPracticeCard() {
        DashboardCard card = new DashboardCard("PRÁCTICA", "Subnetting aplicado", "Arrastra cada bloque de red al departamento correcto.");
        card.setStyle(cardStyle());
        officeScenarioButton.setMinHeight(34);
        companyScenarioButton.setMinHeight(34);
        officeScenarioButton.setStyle(toggleStyle(true));
        companyScenarioButton.setStyle(toggleStyle(false));
        practiceScenarioGroup.selectedToggleProperty().addListener((obs, old, selected) -> {
            if (selected == null) {
                practiceScenarioGroup.selectToggle(old);
                return;
            }
            activeScenario = selected == companyScenarioButton ? companyScenario() : officeScenario();
            practiceAssignments.clear();
            officeScenarioButton.setStyle(toggleStyle(officeScenarioButton.isSelected()));
            companyScenarioButton.setStyle(toggleStyle(companyScenarioButton.isSelected()));
            renderPractice();
        });
        Button resetPractice = ghostButton("Limpiar", "/icons/refresh.svg");
        resetPractice.setOnAction(event -> {
            practiceAssignments.clear();
            renderPractice();
        });
        HBox controls = new HBox(8, officeScenarioButton, companyScenarioButton, resetPractice);
        controls.setAlignment(Pos.CENTER_LEFT);
        practiceBlocks.setPrefWrapLength(360);
        practiceFeedback.setWrapText(true);
        practiceFeedback.setStyle(practiceFeedbackStyle("#f0f6ff", "#1d4ed8"));
        VBox blocksPanel = new VBox(8, fieldLabel("Bloques de red"), practiceBlocks);
        VBox targetsPanel = new VBox(8, fieldLabel("Departamentos / redes"), practiceTargets);
        HBox practiceArea = new HBox(14, blocksPanel, targetsPanel);
        HBox.setHgrow(blocksPanel, Priority.ALWAYS);
        HBox.setHgrow(targetsPanel, Priority.ALWAYS);
        blocksPanel.setMinWidth(360);
        targetsPanel.setMinWidth(420);
        card.setContent(new VBox(10, controls, practiceArea, practiceFeedback));
        return card;
    }

    private Node buildVlsmCard() {
        DashboardCard card = new DashboardCard("VLSM", "Asignación automática", "Ordena por tamaño y reparte subredes con máscaras variables.");
        card.setStyle(cardStyle());
        vlsmRequestsArea.setMinHeight(100);
        vlsmRequestsArea.setPrefRowCount(4);
        vlsmRequestsArea.setWrapText(true);
        vlsmRequestsArea.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d9e6f2;"
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-font-family: 'Monospaced';"
                + "-fx-text-fill: #173452; -fx-font-weight: 800;");
        Button demo = ghostButton("Ejemplo", "/icons/refresh.svg");
        demo.setOnAction(event -> {
            vlsmRequestsArea.setText("Red A, 100\nRed B, 50\nRed C, 10");
            calculateAndRenderVlsm();
        });
        Button calculate = new Button("Asignar VLSM");
        calculate.setGraphic(icon("/icons/check.svg", 16));
        calculate.setGraphicTextGap(8);
        calculate.setMinHeight(38);
        calculate.setStyle("-fx-background-color: #173452; -fx-text-fill: white; -fx-font-weight: 900;"
                + "-fx-background-radius: 8; -fx-padding: 0 14 0 14;");
        calculate.setOnAction(event -> calculateAndRenderVlsm());
        HBox controls = new HBox(8, demo, calculate, vlsmStatusLabel);
        controls.setAlignment(Pos.CENTER_LEFT);
        vlsmUsageLabel.setWrapText(true);
        vlsmUsageLabel.setStyle("-fx-text-fill: #5f7390; -fx-font-size: 12px; -fx-font-weight: 900;");
        vlsmTimeline.setPrefWrapLength(820);
        vlsmRequestsArea.textProperty().addListener((obs, old, value) -> calculateAndRenderVlsm());
        VBox table = new VBox(8, vlsmTableHeader(), vlsmRows);
        VBox lower = new VBox(12, table, new VBox(8, fieldLabel("Paso a paso"), vlsmSteps));
        table.setMinWidth(610);
        card.setContent(new VBox(10,
                fieldLabel("Redes requeridas (nombre, hosts)"),
                vlsmRequestsArea,
                controls,
                vlsmUsageLabel,
                vlsmTimeline,
                lower
        ));
        return card;
    }

    private Node buildSubnetRoutingCard() {
        DashboardCard card = new DashboardCard("ROUTING", "Subredes conectadas", "Usa las subredes VLSM para ver rutas y TTL entre redes distintas.");
        card.setStyle(cardStyle());
        subnetRoutingTtlSpinner.setMinHeight(38);
        subnetRoutingTtlSpinner.setMaxWidth(120);
        subnetRoutingTtlSpinner.valueProperty().addListener((obs, old, value) -> calculateAndRenderVlsm());
        HBox controls = new HBox(10, fieldLabel("TTL"), subnetRoutingTtlSpinner, subnetRoutingStatusLabel);
        controls.setAlignment(Pos.CENTER_LEFT);
        subnetRoutingPathLabel.setWrapText(true);
        subnetRoutingPathLabel.setStyle("-fx-text-fill: #5f7390; -fx-font-size: 12px; -fx-font-weight: 900;");
        subnetRoutingMap.setAlignment(Pos.CENTER_LEFT);
        subnetRoutingMap.setMinHeight(136);
        HBox lists = new HBox(14,
                new VBox(8, fieldLabel("Saltos entre subredes"), subnetRoutingHops),
                new VBox(8, fieldLabel("Tabla de rutas del router"), subnetRoutingRoutes)
        );
        lists.setAlignment(Pos.TOP_LEFT);
        card.setContent(new VBox(10, controls, subnetRoutingPathLabel, subnetRoutingMap, lists));
        return card;
    }

    private Node buildSubnetListCard() {
        DashboardCard card = new DashboardCard("SUBREDES", "Tabla calculada", "Pasa el ratón por una fila para resaltarla en el diagrama.");
        card.setStyle(cardStyle());
        tableNote.setWrapText(true);
        tableNote.setStyle("-fx-text-fill: #5f7390; -fx-font-size: 12px; -fx-font-weight: 800;");
        card.setContent(new VBox(8, tableHeader(), subnetRows, tableNote));
        return card;
    }

    private Node buildTheoryCard() {
        DashboardCard card = new DashboardCard("TEORÍA", "Fundamentos", null);
        card.setStyle(cardStyle());
        card.setContent(new VBox(12,
                theoryItem("/icons/network.svg", "Qué es subnetting", "Dividir una red grande en redes pequeñas."),
                theoryItem("/icons/binary.svg", "Bits prestados", "Se toman bits de host y pasan a ser bits de red."),
                theoryItem("/icons/check.svg", "AND lógico", "La red se obtiene comparando IP y máscara bit a bit."),
                theoryItem("/icons/network.svg", "2^n subredes", "n son los bits que se prestan a la parte de red."),
                theoryItem("/icons/pc.svg", "2^h - 2 hosts", "h son los bits de host; se reservan red y broadcast."),
                theoryItem("/icons/router.svg", "VLSM", "Se asignan máscaras diferentes según los hosts que necesita cada red."),
                theoryItem("/icons/router.svg", "Por qué se usa", "Permite separar aulas, departamentos, VLANs o enlaces de routing.")
        ));
        return card;
    }

    private Node resultSummaryGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.getColumnConstraints().setAll(growingColumn(170), growingColumn(170), growingColumn(170));
        grid.add(summaryBox("Nueva máscara", newMaskLabel, "/icons/binary.svg"), 0, 0);
        grid.add(summaryBox("Subredes", generatedSubnetsLabel, "/icons/network.svg"), 1, 0);
        grid.add(summaryBox("Hosts/subred", hostsPerSubnetLabel, "/icons/pc.svg"), 2, 0);
        grid.add(summaryBox("Incremento", incrementLabel, "/icons/info.svg"), 0, 1);
        grid.add(summaryBox("Bits prestados", borrowedBitsLabel, "/icons/check.svg"), 1, 1);
        return grid;
    }

    private void addImmediateFeedback() {
        baseNetworkField.textProperty().addListener((obs, old, value) -> calculateAndRender());
        baseCidrSpinner.valueProperty().addListener((obs, old, value) -> calculateAndRender());
        targetSpinner.valueProperty().addListener((obs, old, value) -> calculateAndRender());
        modeGroup.selectedToggleProperty().addListener((obs, old, value) -> calculateAndRender());
    }

    private void calculateAndRender() {
        try {
            SubnettingMode mode = bySubnets.isSelected() ? SubnettingMode.BY_SUBNETS : SubnettingMode.BY_HOSTS;
            SubnettingResult result = SubnettingCalculator.calculate(
                    baseNetworkField.getText(),
                    baseCidrSpinner.getValue(),
                    mode,
                    targetSpinner.getValue()
            );
            render(result);
            statusLabel.setText("Listo para dividir");
            statusLabel.setStyle(pillStyle("#e7f8ef", "#087f4f"));
            calculateAndRenderVlsm();
        } catch (IllegalArgumentException ex) {
            renderError(ex.getMessage());
            renderVlsmError(ex.getMessage());
        }
    }

    private void render(SubnettingResult result) {
        newMaskLabel.setText(result.newMask() + " (/" + result.newCidr() + ")");
        generatedSubnetsLabel.setText(String.valueOf(result.generatedSubnets()));
        hostsPerSubnetLabel.setText(String.valueOf(result.hostsPerSubnet()));
        incrementLabel.setText(result.increment() + " direcciones");
        borrowedBitsLabel.setText(result.borrowedBits() + " bits");
        subnetBarNodes.clear();
        subnetBars.getChildren().setAll(result.visibleSubnets().stream().map(block -> subnetBar(block, result)).toArray(Node[]::new));
        renderBinary(result);
        subnetRows.getChildren().setAll(result.visibleSubnets().stream().map(block -> subnetRow(block, result.newCidr())).toArray(Node[]::new));
        if (result.visibleSubnets().size() == result.generatedSubnets()) {
            tableNote.setText("Tabla completa: " + result.generatedSubnets() + " subredes calculadas.");
        } else {
            tableNote.setText("Mostrando las primeras " + result.visibleSubnets().size() + " de "
                    + result.generatedSubnets() + " subredes para mantener la pantalla legible.");
        }
    }

    private void renderError(String message) {
        statusLabel.setText("Revisa los datos");
        statusLabel.setStyle(pillStyle("#fff0f0", "#b91c1c"));
        newMaskLabel.setText("-");
        generatedSubnetsLabel.setText("-");
        hostsPerSubnetLabel.setText("-");
        incrementLabel.setText("-");
        borrowedBitsLabel.setText("-");
        subnetBars.getChildren().setAll(emptyHint(message));
        binaryRows.getChildren().setAll(emptyHint("Corrige la IP o la máscara para ver la representación binaria."));
        binaryLegendLabel.setText("");
        subnetRows.getChildren().setAll(emptyHint("No se pueden listar subredes hasta corregir la red base."));
        tableNote.setText("");
    }

    private void renderBinary(SubnettingResult result) {
        long ip = Ipv4SubnetCalculator.parseIpv4(result.baseNetwork());
        long mask = maskFor(result.newCidr());
        long network = ip & mask;
        boolean binary = binaryViewButton.isSelected();
        binaryRows.getChildren().setAll(
                bitLine("IP", ip, result.newCidr(), binary),
                bitLine("Máscara", mask, result.newCidr(), binary),
                bitLine("Red", network, result.newCidr(), binary)
        );
        binaryLegendLabel.setText("IP AND máscara = " + formatIpv4(network) + "/" + result.newCidr());
    }

    private Node bitLine(String labelText, long value, int cidr, boolean binary) {
        Label label = new Label(labelText);
        label.setMinWidth(76);
        label.setStyle("-fx-text-fill: #173452; -fx-font-weight: 900;");
        HBox groups = new HBox(6);
        groups.setAlignment(Pos.CENTER_LEFT);
        if (binary) {
            for (int octet = 0; octet < 4; octet++) {
                HBox octetBox = new HBox(2);
                for (int bit = 0; bit < 8; bit++) {
                    int bitIndex = octet * 8 + bit;
                    long bitValue = (value >>> (31 - bitIndex)) & 1L;
                    octetBox.getChildren().add(bitCell(String.valueOf(bitValue), bitIndex < cidr, 17));
                }
                groups.getChildren().add(octetBox);
            }
        } else {
            for (int octet = 0; octet < 4; octet++) {
                int startBit = octet * 8;
                long octetValue = (value >>> (24 - octet * 8)) & 0xFF;
                groups.getChildren().add(bitCell(String.valueOf(octetValue), startBit < cidr, 94));
            }
        }
        HBox row = new HBox(10, label, groups);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Label bitCell(String text, boolean networkBit, double width) {
        Label cell = new Label(text);
        cell.setAlignment(Pos.CENTER);
        cell.setMinWidth(width);
        cell.setPrefWidth(width);
        cell.setMinHeight(28);
        cell.setStyle("-fx-font-family: 'Monospaced'; -fx-font-weight: 900;"
                + "-fx-text-fill: " + (networkBit ? "#075f3b" : "#1d4ed8") + ";"
                + "-fx-background-color: " + (networkBit ? "#e7f8ef" : "#eaf4ff") + ";"
                + "-fx-border-color: " + (networkBit ? "#7bd8a6" : "#93c5fd") + ";"
                + "-fx-background-radius: 6; -fx-border-radius: 6;");
        return cell;
    }

    private void renderPractice() {
        practiceBlocks.getChildren().setAll(activeScenario.choices().stream().map(this::networkChoiceNode).toArray(Node[]::new));
        practiceTargets.getChildren().setAll(activeScenario.targets().stream().map(this::practiceTargetNode).toArray(Node[]::new));
        updatePracticeFeedback();
    }

    private void calculateAndRenderVlsm() {
        try {
            VlsmResult result = VlsmCalculator.calculate(
                    baseNetworkField.getText(),
                    baseCidrSpinner.getValue(),
                    parseVlsmRequests(vlsmRequestsArea.getText())
            );
            renderVlsm(result);
            vlsmStatusLabel.setText("Asignación óptima");
            vlsmStatusLabel.setStyle(pillStyle("#e7f8ef", "#087f4f"));
        } catch (IllegalArgumentException ex) {
            renderVlsmError(ex.getMessage());
        }
    }

    private List<VlsmNetworkRequest> parseVlsmRequests(String raw) {
        return raw.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .map(this::parseVlsmLine)
                .toList();
    }

    private VlsmNetworkRequest parseVlsmLine(String line) {
        String normalized = line.replace("→", ",").replace("->", ",").replace(":", ",");
        String[] parts = normalized.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Formato VLSM: una línea por red, por ejemplo 'Red A, 100'.");
        }
        try {
            return new VlsmNetworkRequest(parts[0], Integer.parseInt(parts[1].trim()));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Los hosts de VLSM deben ser números.");
        }
    }

    private void renderVlsm(VlsmResult result) {
        vlsmUsageLabel.setText("Base " + result.baseNetwork() + "/" + result.baseCidr()
                + " · usadas " + result.usedAddresses() + " direcciones"
                + " · libres " + result.freeAddresses() + " direcciones");
        vlsmTimeline.getChildren().setAll(result.assignments().stream()
                .map(assignment -> vlsmTimelineBlock(assignment, result.baseBlockSize()))
                .toArray(Node[]::new));
        vlsmRows.getChildren().setAll(result.assignments().stream()
                .map(this::vlsmRow)
                .toArray(Node[]::new));
        vlsmSteps.getChildren().setAll(result.assignments().stream()
                .map(this::vlsmStep)
                .toArray(Node[]::new));
        renderSubnetRouting(result);
    }

    private void renderVlsmError(String message) {
        vlsmStatusLabel.setText("Revisa VLSM");
        vlsmStatusLabel.setStyle(pillStyle("#fff0f0", "#b91c1c"));
        vlsmUsageLabel.setText(message);
        vlsmTimeline.getChildren().setAll(emptyHint(message));
        vlsmRows.getChildren().setAll(emptyHint("No se puede generar la tabla VLSM hasta corregir los datos."));
        vlsmSteps.getChildren().setAll(emptyHint("VLSM asigna primero las redes con más hosts."));
        renderSubnetRoutingError(message);
    }

    private void renderSubnetRouting(VlsmResult result) {
        try {
            SubnetRoutingResult routing = SubnetRoutingSimulator.simulate(result, subnetRoutingTtlSpinner.getValue());
            subnetRoutingStatusLabel.setText(routing.ttlExpired() ? "TTL expirado" : "Ruta completa");
            subnetRoutingStatusLabel.setStyle(pillStyle(routing.ttlExpired() ? "#fff0f0" : "#e7f8ef",
                    routing.ttlExpired() ? "#b91c1c" : "#087f4f"));
            subnetRoutingPathLabel.setText(routing.sourceSubnet() + " → Router R1/R2 → " + routing.destinationSubnet()
                    + " · Host origen " + routing.sourceHost() + " · Host destino " + routing.destinationHost());
            subnetRoutingMap.getChildren().setAll(
                    subnetNode("Subred origen", routing.sourceSubnet(), routing.sourceHost(), "/icons/pc.svg", "#eaf4ff", "#1d4ed8"),
                    mapArrow(),
                    subnetNode("Router R1", "GW " + routing.sourceGateway(), "eth0 / eth1", "/icons/router.svg", "#f6f0ff", "#8a55e6"),
                    mapArrow(),
                    subnetNode("Router R2", "GW " + routing.destinationGateway(), "tránsito 10.10.10.0/30", "/icons/router.svg", "#fff7e8", "#b45309"),
                    mapArrow(),
                    subnetNode("Subred destino", routing.destinationSubnet(), routing.destinationHost(), "/icons/server.svg", "#e7f8ef", "#087f4f")
            );
            subnetRoutingHops.getChildren().setAll(routing.hops().stream().map(this::subnetRoutingHopRow).toArray(Node[]::new));
            subnetRoutingRoutes.getChildren().setAll(subnetRouteTable(routing));
        } catch (IllegalArgumentException ex) {
            renderSubnetRoutingError(ex.getMessage());
        }
    }

    private void renderSubnetRoutingError(String message) {
        subnetRoutingStatusLabel.setText("Sin ruta");
        subnetRoutingStatusLabel.setStyle(pillStyle("#fff0f0", "#b91c1c"));
        subnetRoutingPathLabel.setText(message);
        subnetRoutingMap.getChildren().setAll(emptyHint("Calcula al menos dos subredes VLSM para construir el mapa de routing."));
        subnetRoutingHops.getChildren().setAll(emptyHint("Los saltos aparecerán cuando haya subred origen y destino."));
        subnetRoutingRoutes.getChildren().setAll(emptyHint("La tabla se genera desde las subredes calculadas."));
    }

    private Node vlsmTimelineBlock(VlsmAssignment assignment, long baseBlockSize) {
        Label name = new Label(assignment.name());
        name.setStyle("-fx-font-weight: 900; -fx-text-fill: #075f3b;");
        Label network = new Label(assignment.network() + "/" + assignment.cidr());
        network.setStyle("-fx-font-family: 'Monospaced'; -fx-font-weight: 900; -fx-text-fill: #173452;");
        Label hosts = new Label(assignment.requiredHosts() + " hosts");
        hosts.setStyle("-fx-text-fill: #5f7390; -fx-font-size: 12px; -fx-font-weight: 800;");
        VBox box = new VBox(4, name, network, hosts);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));
        double width = Math.max(118, Math.min(260, 780.0 * assignment.blockSize() / Math.max(1, baseBlockSize)));
        box.setMinWidth(width);
        box.setStyle("-fx-background-color: #e7f8ef; -fx-border-color: #17a765;"
                + "-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-width: 1;");
        return box;
    }

    private Node vlsmRow(VlsmAssignment assignment) {
        HBox row = new HBox(8,
                tableCell(String.valueOf(assignment.order()), 48, false),
                tableCell(assignment.name(), 92, false),
                tableCell(String.valueOf(assignment.requiredHosts()), 74, false),
                tableCell(assignment.network() + "/" + assignment.cidr(), 142, false),
                tableCell(assignment.firstHost(), 124, false),
                tableCell(assignment.lastHost(), 124, false),
                tableCell(assignment.broadcast(), 124, false)
        );
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        row.setStyle(subnetRowStyle(false));
        return row;
    }

    private Node vlsmTableHeader() {
        HBox header = new HBox(8,
                tableCell("Orden", 48, true),
                tableCell("Red", 92, true),
                tableCell("Hosts", 74, true),
                tableCell("Network", 142, true),
                tableCell("First Host", 124, true),
                tableCell("Last Host", 124, true),
                tableCell("Broadcast", 124, true)
        );
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 10, 0, 10));
        return header;
    }

    private Node vlsmStep(VlsmAssignment assignment) {
        Label step = new Label(assignment.order() + ". " + assignment.explanation());
        step.setWrapText(true);
        step.setStyle("-fx-background-color: #f8fbff; -fx-border-color: #d9e6f2;"
                + "-fx-background-radius: 10; -fx-border-radius: 10; -fx-padding: 10;"
                + "-fx-text-fill: #173452; -fx-font-weight: 800;");
        return step;
    }

    private Node subnetNode(String title, String network, String detail, String iconPath, String background, String color) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: 900; -fx-text-fill: " + color + ";");
        Label networkLabel = new Label(network);
        networkLabel.setWrapText(true);
        networkLabel.setStyle("-fx-font-family: 'Monospaced'; -fx-font-weight: 900; -fx-text-fill: #173452;");
        Label detailLabel = new Label(detail);
        detailLabel.setWrapText(true);
        detailLabel.setStyle("-fx-text-fill: #5f7390; -fx-font-size: 12px; -fx-font-weight: 800;");
        VBox text = new VBox(3, titleLabel, networkLabel, detailLabel);
        HBox box = new HBox(8, icon(iconPath, 28), text);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(10));
        box.setMinWidth(150);
        box.setMaxWidth(190);
        box.setStyle("-fx-background-color: " + background + "; -fx-border-color: " + color + ";"
                + "-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-width: 1;");
        return box;
    }

    private Node mapArrow() {
        Label arrow = new Label("→");
        arrow.setStyle("-fx-text-fill: #5f7390; -fx-font-size: 24px; -fx-font-weight: 900;");
        return arrow;
    }

    private Node subnetRoutingHopRow(SubnetRoutingHop hop) {
        Label number = new Label(String.valueOf(hop.hop()));
        number.setAlignment(Pos.CENTER);
        number.setMinSize(30, 30);
        number.setMaxSize(30, 30);
        number.setStyle("-fx-background-color: " + (hop.delivered() ? "#e7f8ef" : "#fff0f0") + ";"
                + "-fx-text-fill: " + (hop.delivered() ? "#087f4f" : "#b91c1c") + ";"
                + "-fx-background-radius: 999; -fx-font-weight: 900;");
        Label title = new Label(hop.device() + " · " + hop.networkInterface());
        title.setStyle("-fx-text-fill: #173452; -fx-font-weight: 900;");
        Label network = new Label(hop.network() + " → " + hop.nextHop());
        network.setWrapText(true);
        network.setStyle("-fx-font-family: 'Monospaced'; -fx-text-fill: #294766; -fx-font-weight: 800;");
        Label ttl = new Label("TTL: " + hop.ttlBefore() + " → " + hop.ttlAfter());
        ttl.setStyle(packetBadgeStyle(hop.delivered() ? "#f8fbff" : "#fff0f0", hop.delivered() ? "#345573" : "#b91c1c"));
        Label detail = new Label(hop.detail());
        detail.setWrapText(true);
        detail.setStyle("-fx-text-fill: #5f7390;");
        VBox text = new VBox(3, title, network, ttl, detail);
        HBox row = new HBox(10, number, text);
        row.setAlignment(Pos.TOP_LEFT);
        row.setPadding(new Insets(10));
        row.setStyle("-fx-background-color: #fbfdff; -fx-border-color: #d9e6f2;"
                + "-fx-background-radius: 10; -fx-border-radius: 10;");
        HBox.setHgrow(text, Priority.ALWAYS);
        return row;
    }

    private Node[] subnetRouteTable(SubnetRoutingResult routing) {
        Node[] nodes = new Node[routing.routes().size() + 1];
        nodes[0] = subnetRouteHeader();
        for (int index = 0; index < routing.routes().size(); index++) {
            nodes[index + 1] = subnetRouteRow(routing.routes().get(index));
        }
        return nodes;
    }

    private Node subnetRouteHeader() {
        HBox row = new HBox(8,
                tableCell("Destino", 128, true),
                tableCell("Gateway", 102, true),
                tableCell("Interfaz", 70, true)
        );
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Node subnetRouteRow(SubnetRoutingRoute route) {
        HBox row = new HBox(8,
                tableCell(route.destinationCidr(), 128, false),
                tableCell(route.gateway(), 102, false),
                tableCell(route.networkInterface(), 70, false)
        );
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8));
        row.setStyle(route.selected()
                ? "-fx-background-color: #e7f8ef; -fx-border-color: #17a765; -fx-background-radius: 10; -fx-border-radius: 10;"
                : "-fx-background-color: #fbfdff; -fx-border-color: #d9e6f2; -fx-background-radius: 10; -fx-border-radius: 10;");
        return row;
    }

    private Node networkChoiceNode(NetworkChoice choice) {
        Label network = new Label(choice.cidr());
        network.setStyle("-fx-font-family: 'Monospaced'; -fx-font-weight: 900; -fx-text-fill: #173452;");
        Label hosts = new Label(choice.usableHosts() + " hosts útiles");
        hosts.setStyle("-fx-text-fill: #5f7390; -fx-font-size: 12px; -fx-font-weight: 800;");
        VBox card = new VBox(3, network, hosts);
        card.setPadding(new Insets(10));
        card.setMinWidth(168);
        card.setStyle("-fx-background-color: #f8fbff; -fx-border-color: #d9e6f2;"
                + "-fx-background-radius: 10; -fx-border-radius: 10; -fx-cursor: hand;");
        card.setOnDragDetected(event -> {
            Dragboard dragboard = card.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            content.putString(choice.id());
            dragboard.setContent(content);
            event.consume();
        });
        return card;
    }

    private Node practiceTargetNode(PracticeTarget target) {
        String assignedId = practiceAssignments.get(target.id());
        NetworkChoice assigned = assignedId == null ? null : choiceById(assignedId);
        Label title = new Label(target.name());
        title.setStyle("-fx-text-fill: #173452; -fx-font-weight: 900;");
        Label need = new Label(target.requiredHosts() + " hosts necesarios");
        need.setStyle("-fx-text-fill: #5f7390; -fx-font-size: 12px; -fx-font-weight: 800;");
        Label assignment = new Label(assigned == null ? "Suelta aquí una subred" : assigned.cidr());
        assignment.setStyle("-fx-font-family: 'Monospaced'; -fx-font-weight: 900;"
                + "-fx-text-fill: " + (assigned == null ? "#5f7390" : "#173452") + ";");
        VBox box = new VBox(3, title, need, assignment);
        box.setPadding(new Insets(10));
        box.setMinHeight(78);
        box.setStyle(practiceTargetStyle(target, assigned));
        box.setOnDragOver(event -> {
            if (event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });
        box.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            if (dragboard.hasString()) {
                practiceAssignments.put(target.id(), dragboard.getString());
                renderPractice();
                event.setDropCompleted(true);
            }
            event.consume();
        });
        box.setOnMouseClicked(event -> {
            if (assigned != null) {
                practiceAssignments.remove(target.id());
                renderPractice();
            }
        });
        return box;
    }

    private void updatePracticeFeedback() {
        if (practiceAssignments.size() < activeScenario.targets().size()) {
            practiceFeedback.setText(activeScenario.description());
            practiceFeedback.setStyle(practiceFeedbackStyle("#f0f6ff", "#1d4ed8"));
            return;
        }
        boolean correct = activeScenario.targets().stream()
                .allMatch(target -> target.expectedChoiceId().equals(practiceAssignments.get(target.id())));
        if (correct) {
            practiceFeedback.setText("Correcto: has asignado cada red con una máscara ajustada a sus hosts. Esto es VLSM básico.");
            practiceFeedback.setStyle(practiceFeedbackStyle("#e7f8ef", "#087f4f"));
        } else {
            practiceFeedback.setText("Incorrecto: revisa la cantidad de hosts y usa primero las redes más grandes.");
            practiceFeedback.setStyle(practiceFeedbackStyle("#fff0f0", "#b91c1c"));
        }
    }

    private NetworkChoice choiceById(String id) {
        return activeScenario.choices().stream()
                .filter(choice -> choice.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    private String practiceTargetStyle(PracticeTarget target, NetworkChoice assigned) {
        if (assigned == null) {
            return "-fx-background-color: #fbfdff; -fx-border-color: #d9e6f2;"
                    + "-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-style: segments(8, 5);";
        }
        if (target.expectedChoiceId().equals(assigned.id())) {
            return "-fx-background-color: #e7f8ef; -fx-border-color: #17a765;"
                    + "-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-width: 2;";
        }
        return "-fx-background-color: #fff0f0; -fx-border-color: #ef4444;"
                + "-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-width: 2;";
    }

    private String practiceFeedbackStyle(String background, String color) {
        return "-fx-background-color: " + background + "; -fx-text-fill: " + color + ";"
                + "-fx-background-radius: 10; -fx-padding: 10; -fx-font-weight: 900;";
    }

    private PracticeScenario officeScenario() {
        return new PracticeScenario(
                "oficina",
                "Oficina: asigna redes para Ventas, Administración y Soporte. Pista: empieza por el departamento con más hosts.",
                List.of(
                        new NetworkChoice("office-sales", "192.168.10.0/26", 62),
                        new NetworkChoice("office-admin", "192.168.10.64/27", 30),
                        new NetworkChoice("office-support", "192.168.10.96/28", 14),
                        new NetworkChoice("office-small", "192.168.10.112/29", 6)
                ),
                List.of(
                        new PracticeTarget("sales", "Ventas", 50, "office-sales"),
                        new PracticeTarget("admin", "Administración", 28, "office-admin"),
                        new PracticeTarget("support", "Soporte", 12, "office-support")
                )
        );
    }

    private PracticeScenario companyScenario() {
        return new PracticeScenario(
                "empresa",
                "Empresa: separa redes para usuarios, servidores e invitados sin desperdiciar máscaras grandes.",
                List.of(
                        new NetworkChoice("company-users", "10.0.0.0/25", 126),
                        new NetworkChoice("company-guests", "10.0.0.192/26", 62),
                        new NetworkChoice("company-servers", "10.0.0.128/27", 30),
                        new NetworkChoice("company-link", "10.0.0.160/30", 2)
                ),
                List.of(
                        new PracticeTarget("users", "Usuarios sede principal", 120, "company-users"),
                        new PracticeTarget("guests", "WiFi invitados", 60, "company-guests"),
                        new PracticeTarget("servers", "Servidores internos", 30, "company-servers")
                )
        );
    }

    private Node subnetBar(SubnetBlock block, SubnettingResult result) {
        Label title = new Label("SUBRED " + block.number());
        title.setStyle("-fx-font-weight: 900; -fx-text-fill: #087f4f;");
        Label network = new Label(block.network() + "/" + result.newCidr());
        network.setStyle("-fx-font-family: 'Monospaced'; -fx-font-weight: 900; -fx-text-fill: #173452;");
        VBox box = new VBox(5, title, network);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(12));
        box.setMinWidth(Math.max(120, 760.0 / Math.min(result.generatedSubnets(), 8)));
        box.setStyle(subnetBarStyle(false));
        subnetBarNodes.put(block.number(), box);
        return box;
    }

    private Node subnetRow(SubnetBlock block, int cidr) {
        HBox row = new HBox(8,
                tableCell(String.valueOf(block.number()), 70, false),
                tableCell(block.network() + "/" + cidr, 150, false),
                tableCell(block.firstHost(), 145, false),
                tableCell(block.lastHost(), 145, false),
                tableCell(block.broadcast(), 145, false)
        );
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        row.setStyle(subnetRowStyle(false));
        row.setOnMouseEntered(event -> {
            row.setStyle(subnetRowStyle(true));
            highlightSubnet(block.number(), true);
        });
        row.setOnMouseExited(event -> {
            row.setStyle(subnetRowStyle(false));
            highlightSubnet(block.number(), false);
        });
        return row;
    }

    private Node tableHeader() {
        HBox header = new HBox(8,
                tableCell("Subred", 70, true),
                tableCell("Network", 150, true),
                tableCell("First Host", 145, true),
                tableCell("Last Host", 145, true),
                tableCell("Broadcast", 145, true)
        );
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 10, 0, 10));
        return header;
    }

    private void highlightSubnet(int number, boolean active) {
        Node bar = subnetBarNodes.get(number);
        if (bar != null) {
            bar.setStyle(subnetBarStyle(active));
        }
    }

    private String subnetBarStyle(boolean active) {
        return active
                ? "-fx-background-color: #dff5ff; -fx-border-color: #0ea5e9; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-width: 2;"
                : "-fx-background-color: #e7f8ef; -fx-border-color: #7bd8a6; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-width: 1;";
    }

    private String subnetRowStyle(boolean active) {
        return active
                ? "-fx-background-color: #eef9ff; -fx-border-color: #0ea5e9; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-width: 2;"
                : "-fx-background-color: #fbfdff; -fx-border-color: #d9e6f2; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-width: 1;";
    }

    private Node inputBlock(String labelText, TextField field, String iconPath) {
        Label label = fieldLabel(labelText);
        field.setMinHeight(42);
        field.setStyle(inputStyle());
        HBox box = new HBox(10, icon(iconPath, 20), field);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(0, 12, 0, 12));
        box.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #d9e6f2;");
        HBox.setHgrow(field, Priority.ALWAYS);
        return new VBox(8, label, box);
    }

    private Node spinnerBlock(String labelText, Spinner<Integer> spinner, String hintText) {
        Label label = fieldLabel(labelText);
        spinner.setMinHeight(42);
        spinner.setMaxWidth(Double.MAX_VALUE);
        spinner.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d9e6f2; -fx-border-radius: 8; -fx-background-radius: 8;");
        Label hint = new Label(hintText);
        hint.setWrapText(true);
        hint.setStyle("-fx-text-fill: #5f7390; -fx-font-size: 12px;");
        return new VBox(8, label, spinner, hint);
    }

    private Node modeBlock() {
        Label label = fieldLabel("Modo de cálculo");
        bySubnets.setStyle("-fx-font-weight: 800; -fx-text-fill: #173452;");
        byHosts.setStyle("-fx-font-weight: 800; -fx-text-fill: #173452;");
        return new VBox(8, label, bySubnets, byHosts);
    }

    private Node primaryButton() {
        Button button = new Button("Dividir red");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMinHeight(46);
        button.setStyle("-fx-background-color: #17a765; -fx-text-fill: white; -fx-font-weight: 900; -fx-background-radius: 8;");
        button.setOnAction(event -> calculateAndRender());
        VBox.setMargin(button, new Insets(10, 0, 0, 0));
        return button;
    }

    private Node summaryBox(String title, Label value, String iconPath) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #5f7390; -fx-font-size: 12px; -fx-font-weight: 800;");
        value.setWrapText(true);
        value.setStyle("-fx-text-fill: #173452; -fx-font-weight: 900;");
        HBox head = new HBox(8, icon(iconPath, 16), titleLabel);
        head.setAlignment(Pos.CENTER_LEFT);
        VBox box = new VBox(4, head, value);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: #f8fbff; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #d9e6f2;");
        return box;
    }

    private Node theoryItem(String iconPath, String title, String body) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: 900; -fx-text-fill: #173452;");
        Label bodyLabel = new Label(body);
        bodyLabel.setWrapText(true);
        bodyLabel.setStyle("-fx-text-fill: #5f7390;");
        HBox row = new HBox(10, iconTile(iconPath), new VBox(3, titleLabel, bodyLabel));
        row.setAlignment(Pos.TOP_LEFT);
        return row;
    }

    private Node iconTile(String path) {
        StackPane pane = new StackPane(icon(path, 22));
        pane.setMinSize(34, 34);
        pane.setMaxSize(34, 34);
        pane.setStyle("-fx-background-color: #f0f6ff; -fx-background-radius: 9;");
        return pane;
    }

    private Label tableCell(String text, double width, boolean header) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMinWidth(width);
        label.setPrefWidth(width);
        label.setStyle(header
                ? "-fx-text-fill: #5f7390; -fx-font-size: 11px; -fx-font-weight: 900;"
                : "-fx-text-fill: #173452; -fx-font-weight: 800;");
        return label;
    }

    private Label legendPill(String text, String background, String color) {
        Label label = new Label(text);
        label.setStyle("-fx-background-color: " + background + "; -fx-text-fill: " + color + ";"
                + "-fx-background-radius: 999; -fx-padding: 5 10 5 10; -fx-font-size: 12px; -fx-font-weight: 900;");
        return label;
    }

    private String toggleStyle(boolean selected) {
        return selected
                ? "-fx-background-color: #173452; -fx-text-fill: white; -fx-font-weight: 900; -fx-background-radius: 8; -fx-padding: 0 14 0 14;"
                : "-fx-background-color: #ffffff; -fx-text-fill: #173452; -fx-font-weight: 900; -fx-border-color: #d9e6f2; -fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 0 14 0 14;";
    }

    private long maskFor(int cidr) {
        if (cidr == 0) {
            return 0;
        }
        return (0xFFFFFFFFL << (32 - cidr)) & 0xFFFFFFFFL;
    }

    private String formatIpv4(long value) {
        return ((value >>> 24) & 0xFF) + "."
                + ((value >>> 16) & 0xFF) + "."
                + ((value >>> 8) & 0xFF) + "."
                + (value & 0xFF);
    }

    private Node emptyHint(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle("-fx-background-color: #fff0f0; -fx-text-fill: #b91c1c; -fx-background-radius: 10; -fx-padding: 12; -fx-font-weight: 800;");
        return label;
    }

    private Label fieldLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #193753; -fx-font-weight: 800;");
        return label;
    }

    private Button ghostButton(String text, String iconPath) {
        Button button = new Button(text);
        button.setGraphic(icon(iconPath, 16));
        button.setGraphicTextGap(8);
        button.setMinHeight(38);
        button.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d9e6f2;"
                + "-fx-background-radius: 8; -fx-border-radius: 8;"
                + "-fx-text-fill: #173452; -fx-font-weight: 800; -fx-padding: 0 14 0 14;");
        return button;
    }

    private Label pill(String text, String background, String color) {
        Label label = new Label(text);
        label.setStyle(pillStyle(background, color));
        return label;
    }

    private String pillStyle(String background, String color) {
        return "-fx-background-color: " + background + "; -fx-text-fill: " + color + ";"
                + "-fx-background-radius: 999; -fx-padding: 7 16 7 16; -fx-font-weight: 900;";
    }

    private String packetBadgeStyle(String background, String color) {
        return "-fx-background-color: " + background + "; -fx-text-fill: " + color + ";"
                + "-fx-background-radius: 999; -fx-padding: 4 10 4 10; -fx-font-weight: 900;";
    }

    private String inputStyle() {
        return "-fx-background-color: transparent; -fx-border-color: transparent; -fx-text-fill: #1f3b57;";
    }

    private ColumnConstraints fixedColumn(double width) {
        ColumnConstraints constraints = new ColumnConstraints(width, width, width);
        constraints.setFillWidth(true);
        return constraints;
    }

    private ColumnConstraints growingColumn(double minWidth) {
        ColumnConstraints constraints = new ColumnConstraints(minWidth, 900, Double.MAX_VALUE);
        constraints.setHgrow(Priority.ALWAYS);
        constraints.setFillWidth(true);
        return constraints;
    }

    private Node icon(String path, double size) {
        Image image = load(path);
        if (image == null) {
            Rectangle fallback = new Rectangle(size, size, Color.web("#17a765"));
            fallback.setArcWidth(6);
            fallback.setArcHeight(6);
            return fallback;
        }
        ImageView view = new ImageView(image);
        view.setFitWidth(size);
        view.setFitHeight(size);
        view.setPreserveRatio(true);
        return view;
    }

    private Image load(String resourcePath) {
        if (resourcePath.endsWith(".svg")) {
            Image png = load(resourcePath.substring(0, resourcePath.length() - 4) + ".png");
            if (png != null) return png;
            return null;
        }
        try (InputStream stream = SubnettingLearningView.class.getResourceAsStream(resourcePath)) {
            return stream == null ? null : new Image(stream);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String cardStyle() {
        return "-fx-background-color: #ffffff; -fx-background-radius: 14;"
                + "-fx-border-radius: 14; -fx-border-color: #d9e6f2;"
                + "-fx-effect: dropshadow(gaussian, rgba(31,80,130,0.035), 22, 0.18, 0, 8);";
    }

    private record PracticeScenario(
            String id,
            String description,
            List<NetworkChoice> choices,
            List<PracticeTarget> targets
    ) {
    }

    private record NetworkChoice(
            String id,
            String cidr,
            int usableHosts
    ) {
    }

    private record PracticeTarget(
            String id,
            String name,
            int requiredHosts,
            String expectedChoiceId
    ) {
    }
}
