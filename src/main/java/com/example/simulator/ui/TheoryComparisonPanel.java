package com.example.simulator.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class TheoryComparisonPanel extends DashboardCard {
    public TheoryComparisonPanel() {
        super("TEORÍA", "Comparación TCP vs UDP", "Tabla de apoyo docente para acompañar la demo.");

        Label intro = new Label("TCP prioriza fiabilidad, orden y control. UDP prioriza simplicidad, baja sobrecarga y rapidez.");
        intro.setWrapText(true);
        intro.setStyle(UiTheme.BODY);

        TheoryTable table = new TheoryTable();
        table.addHeader("Característica", "TCP", "UDP");
        table.addRow(1, "Conexión previa", "Sí", "No");
        table.addRow(2, "ACK", "Sí", "No");
        table.addRow(3, "Retransmisión", "Sí", "No");
        table.addRow(4, "Orden garantizado", "Sí", "No");
        table.addRow(5, "Sobrecarga", "Mayor", "Menor");
        table.addRow(6, "Casos de uso", "web, email, archivos", "streaming, VoIP, juegos");

        VBox wrapper = new VBox(12, intro, table);
        wrapper.setPadding(new Insets(8));
        wrapper.setStyle(UiTheme.PANEL_INSET_TINT);
        getContentBox().getChildren().add(wrapper);
    }
}
