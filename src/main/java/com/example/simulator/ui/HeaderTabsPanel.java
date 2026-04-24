package com.example.simulator.ui;

import com.example.simulator.domain.protocol.ProtocolType;
import com.example.simulator.presentation.layers.EncapsulationSnapshot;
import com.example.simulator.presentation.layers.HeaderFieldViewModel;
import javafx.scene.Node;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HeaderTabsPanel extends VBox {
    private final ToggleGroup tabsGroup = new ToggleGroup();
    private final HBox tabsRow = new HBox(8);
    private final StackPane contentStack = new StackPane();
    private final Map<String, ToggleButton> tabButtons = new LinkedHashMap<>();
    private String activeTab = "TCP";

    public HeaderTabsPanel() {
        setSpacing(12);
    }

    public void update(EncapsulationSnapshot snapshot, LearningLevel level, ProtocolType protocolType) {
        tabsRow.getChildren().clear();
        tabButtons.clear();
        contentStack.getChildren().setAll(
                contentCard("TCP", snapshot.getTransportTitle().contains("TCP") ? snapshot.getTransportHeader() : defaultTcpFields(), level, "Header TCP"),
                contentCard("UDP", snapshot.getTransportTitle().contains("UDP") ? snapshot.getTransportHeader() : defaultUdpFields(), level, "Header UDP"),
                contentCard("IP", snapshot.getIpHeader(), level, "Header IP"),
                contentCard("Enlace", snapshot.getLinkHeader(), level, "Header de enlace")
        );
        for (Node node : contentStack.getChildren()) {
            node.setVisible(false);
            node.setManaged(false);
        }

        addTab("TCP");
        addTab("UDP");
        addTab("IP");
        addTab("Enlace");

        setActiveTab(protocolType == ProtocolType.UDP ? "UDP" : "TCP");

        FlowPane legend = new FlowPane(8, 8,
                legendChip("Aplicación", UiTheme.LAYER_BG_APPLICATION),
                legendChip("Transporte", UiTheme.LAYER_BG_TRANSPORT),
                legendChip("Internet", UiTheme.LAYER_BG_NETWORK),
                legendChip("Acceso red", UiTheme.LAYER_BG_LINK)
        );

        DashboardCard shell = new DashboardCard("CABECERAS", "Cabeceras simplificadas",
                "Consulta los campos esenciales en formato didáctico.");
        shell.setContent(new VBox(12, tabsRow, contentStack, legend));
        getChildren().setAll(shell);
    }

    private void addTab(String name) {
        ToggleButton button = new ToggleButton(name);
        button.setToggleGroup(tabsGroup);
        button.setMinHeight(34);
        button.setPrefHeight(34);
        button.setOnAction(event -> setActiveTab(name));
        tabButtons.put(name, button);
        tabsRow.getChildren().add(button);
    }

    private void setActiveTab(String tab) {
        activeTab = tab;
        tabButtons.forEach((name, button) -> {
            boolean active = name.equals(tab);
            button.setSelected(active);
            button.setStyle(active
                    ? "-fx-background-color: #eaf1ff; -fx-text-fill: #1d4ed8; -fx-font-weight: bold; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #8fb2ff;"
                    : "-fx-background-color: #ffffff; -fx-text-fill: #5d7287; -fx-font-weight: bold; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #dbe5ef;");
        });
        for (Node node : contentStack.getChildren()) {
            boolean active = tab.equals(node.getUserData());
            node.setVisible(active);
            node.setManaged(active);
        }
    }

    private VBox contentCard(String titleText, List<HeaderFieldViewModel> fields, LearningLevel level, String tooltipTitle) {
        VBox box = new VBox(10);
        box.setUserData(titleText);
        box.setPadding(new Insets(12));
        box.setStyle(UiTheme.PANEL_INSET);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(42);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(58);
        grid.getColumnConstraints().setAll(c1, c2);

        int limit = level == LearningLevel.BASIC ? Math.min(4, fields.size()) : fields.size();
        for (int i = 0; i < limit; i++) {
            HeaderFieldViewModel field = fields.get(i);
            Label name = new Label(field.getName());
            name.setWrapText(true);
            name.setStyle(UiTheme.FIELD_LABEL);
            Tooltip help = new Tooltip(field.getHelp());
            help.setWrapText(true);
            help.setMaxWidth(240);
            Tooltip.install(name, help);

            Label value = new Label(field.getValue());
            value.setWrapText(true);
            value.setStyle("-fx-font-size: 13px; -fx-text-fill: #1f3b53;");
            value.setPadding(new Insets(8));
            value.setStyle("-fx-font-size: 13px; -fx-text-fill: #1f3b53;"
                    + "-fx-background-color: #ffffff; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #dbe5ef; -fx-padding: 8;");
            grid.add(name, 0, i);
            grid.add(value, 1, i);
        }

        Label footer = new Label(tooltipText(tooltipTitle));
        footer.setWrapText(true);
        footer.setStyle(UiTheme.SUBTITLE);
        box.getChildren().addAll(grid, footer);
        return box;
    }

    private Label legendChip(String text, String fill) {
        Label chip = new Label(text);
        chip.setStyle("-fx-background-color: " + fill + ";"
                + "-fx-background-radius: 999; -fx-border-radius: 999;"
                + "-fx-border-color: #dbe5ef; -fx-padding: 6 10 6 10;"
                + "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #17324a;");
        return chip;
    }

    private List<HeaderFieldViewModel> defaultTcpFields() {
        return List.of(
                new HeaderFieldViewModel("Puerto origen", "49152", "Identifica la aplicación emisora."),
                new HeaderFieldViewModel("Puerto destino", "8080", "Identifica la aplicación receptora."),
                new HeaderFieldViewModel("Número de secuencia", "101", "Posición de los datos dentro del flujo TCP."),
                new HeaderFieldViewModel("Número de ACK", "201", "Siguiente byte esperado por el receptor."),
                new HeaderFieldViewModel("Flags", "SYN, ACK", "Indican control de conexión y estado del segmento."),
                new HeaderFieldViewModel("Ventana", "4096", "Cantidad de datos que el receptor puede aceptar.")
        );
    }

    private List<HeaderFieldViewModel> defaultUdpFields() {
        return List.of(
                new HeaderFieldViewModel("Puerto origen", "53000", "Identifica el proceso emisor."),
                new HeaderFieldViewModel("Puerto destino", "9000", "Identifica el proceso receptor."),
                new HeaderFieldViewModel("Longitud", "24", "Longitud total del datagrama UDP.")
        );
    }

    private String tooltipText(String titleText) {
        return switch (titleText) {
            case "Header TCP" -> "Contiene campos como puertos, número de secuencia, número de ACK y flags.";
            case "Header UDP" -> "Cabecera más simple, con menos sobrecarga que TCP.";
            case "Header IP" -> "Permite direccionar lógicamente el paquete y llevarlo entre redes.";
            default -> "La cabecera de enlace identifica el origen, destino local y el tipo de protocolo transportado.";
        };
    }
}
