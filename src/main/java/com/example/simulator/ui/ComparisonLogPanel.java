package com.example.simulator.ui;

import javafx.geometry.Insets;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ComparisonLogPanel extends DashboardCard {
    private final EventLogPanel tcpLogPanel = new EventLogPanel();
    private final EventLogPanel udpLogPanel = new EventLogPanel();

    public ComparisonLogPanel() {
        super("LOGS", "Eventos en paralelo", "Registro simultáneo de ambos protocolos bajo la misma red.");
        tcpLogPanel.setTitle("Log TCP", "Evolución del lado fiable");
        udpLogPanel.setTitle("Log UDP", "Evolución del lado ligero");
        tcpLogPanel.setPreferredHeight(220);
        udpLogPanel.setPreferredHeight(220);

        HBox logs = new HBox(16, tcpLogPanel, udpLogPanel);
        HBox.setHgrow(tcpLogPanel, Priority.ALWAYS);
        HBox.setHgrow(udpLogPanel, Priority.ALWAYS);
        tcpLogPanel.setMaxWidth(Double.MAX_VALUE);
        udpLogPanel.setMaxWidth(Double.MAX_VALUE);

        VBox wrapper = new VBox(logs);
        wrapper.setPadding(new Insets(2));
        getContentBox().getChildren().add(wrapper);
    }

    public TextArea getTcpLogArea() {
        return tcpLogPanel.getLogArea();
    }

    public TextArea getUdpLogArea() {
        return udpLogPanel.getLogArea();
    }
}
