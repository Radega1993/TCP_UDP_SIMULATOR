package com.example.simulator.ui;

import com.example.simulator.application.dto.ComparisonSummary;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ComparisonSummaryPanel extends DashboardCard {
    private final Label tcpDelivery = valueBadge();
    private final Label udpDelivery = valueBadge();
    private final Label tcpRetry = valueBadge();
    private final Label udpRetry = valueBadge();
    private final Label tcpOrder = valueBadge();
    private final Label udpOrder = valueBadge();
    private final Label tcpOverhead = valueBadge();
    private final Label udpOverhead = valueBadge();
    private final Label observations = new Label("Lanza una comparación para ver el resumen final.");

    public ComparisonSummaryPanel() {
        super("RESUMEN", "Resumen comparado", "Lectura visual rápida de las diferencias observadas.");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        ColumnConstraints concept = new ColumnConstraints();
        concept.setPercentWidth(36);
        ColumnConstraints left = new ColumnConstraints();
        left.setPercentWidth(32);
        left.setHgrow(Priority.ALWAYS);
        ColumnConstraints right = new ColumnConstraints();
        right.setPercentWidth(32);
        right.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(concept, left, right);

        grid.add(header("Criterio"), 0, 0);
        grid.add(header("TCP"), 1, 0);
        grid.add(header("UDP"), 2, 0);
        addRow(grid, 1, "Entrega", tcpDelivery, udpDelivery);
        addRow(grid, 2, "Retransmisión", tcpRetry, udpRetry);
        addRow(grid, 3, "Orden", tcpOrder, udpOrder);
        addRow(grid, 4, "Sobrecarga", tcpOverhead, udpOverhead);

        observations.setWrapText(true);
        observations.setStyle(UiTheme.BODY);

        VBox wrapper = new VBox(12, grid, observations);
        wrapper.setPadding(new Insets(8));
        wrapper.setStyle(UiTheme.PANEL_INSET_TINT);
        getContentBox().getChildren().add(wrapper);
    }

    public void update(ComparisonSummary summary) {
        setBadge(tcpDelivery, summary.isTcpDeliveredComplete(), "✔ Completo", "✖ Incompleto");
        setBadge(udpDelivery, summary.isUdpDeliveredComplete(), "✔ Completo", "✖ Incompleto");
        setBadge(tcpRetry, summary.isTcpRetransmitted(), "✔ Sí", "✖ No");
        setBadge(udpRetry, summary.isUdpRetransmitted(), "✔ Sí", "✖ No");
        setBadge(tcpOrder, summary.isTcpOrdered(), "✔ Garantizado", "✖ Alterado");
        setBadge(udpOrder, summary.isUdpOrdered(), "✔ Estable", "✖ No garantizado");
        tcpOverhead.setText(summary.getTcpPacketCount() + " eventos");
        udpOverhead.setText(summary.getUdpPacketCount() + " eventos");
        tcpOverhead.setStyle(UiTheme.STATUS_NEUTRAL);
        udpOverhead.setStyle(UiTheme.STATUS_NEUTRAL);
        observations.setText(buildObservations(summary));
    }

    private void addRow(GridPane grid, int row, String concept, Label tcp, Label udp) {
        Label conceptLabel = new Label(concept);
        conceptLabel.setStyle(UiTheme.FIELD_LABEL);
        grid.add(conceptLabel, 0, row);
        grid.add(tcp, 1, row);
        grid.add(udp, 2, row);
    }

    private Label header(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #122033;");
        return label;
    }

    private Label valueBadge() {
        Label label = new Label("-");
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle(UiTheme.STATUS_NEUTRAL);
        return label;
    }

    private void setBadge(Label label, boolean positive, String positiveText, String negativeText) {
        label.setText(positive ? positiveText : negativeText);
        label.setStyle(positive ? UiTheme.STATUS_POSITIVE : UiTheme.STATUS_NEGATIVE);
    }

    private String buildObservations(ComparisonSummary summary) {
        return (summary.isTcpDeliveredComplete() ? "TCP entregó todo el mensaje. " : "TCP no completó la entrega. ")
                + (summary.isUdpDeliveredComplete() ? "UDP también llegó completo. " : "UDP no necesariamente entregó todo. ")
                + (summary.isTcpRetransmitted() ? "TCP mostró recuperación mediante retransmisión. " : "TCP no necesitó retransmisión en esta ejecución. ")
                + "Esto hace visible la diferencia entre fiabilidad y simplicidad.";
    }
}
