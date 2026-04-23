package com.example.simulator.ui;

import com.example.simulator.domain.protocol.tcp.CongestionPhase;
import com.example.simulator.domain.simulation.CongestionSnapshot;
import com.example.simulator.domain.simulation.CwndHistoryPoint;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class CongestionPanel extends DashboardCard {
    private final Label phaseValue = chip("Slow start");
    private final Label cwndValue = chip("0 B");
    private final Label ssthreshValue = chip("0 B");
    private final Label duplicateAckValue = chip("0");
    private final Label bytesInFlightValue = chip("0 B");
    private final Label effectiveWindowValue = chip("0 B");
    private final Label reasonLabel = new Label("Sin datos de congestión todavía.");
    private final CwndChartPanel chartPanel = new CwndChartPanel();
    private final List<CwndHistoryPoint> history = new ArrayList<>();

    public CongestionPanel() {
        super("TCP", "Congestión", "Estado de cwnd, ssthresh, duplicate ACK y crecimiento de la ventana.");

        GridPane stats = new GridPane();
        stats.setHgap(10);
        stats.setVgap(10);
        stats.add(statRow("Fase", phaseValue, null), 0, 0);
        stats.add(statRow("cwnd", cwndValue, "Ventana de congestión. Limita la cantidad de datos en vuelo para evitar saturar la red."), 1, 0);
        stats.add(statRow("ssthresh", ssthreshValue, "Umbral que separa la fase de slow start de la fase de congestion avoidance."), 0, 1);
        stats.add(statRow("Duplicate ACK", duplicateAckValue, "ACK repetido que indica que el receptor sigue esperando un segmento anterior."), 1, 1);
        stats.add(statRow("Bytes en vuelo", bytesInFlightValue, null), 0, 2);
        stats.add(statRow("Ventana efectiva", effectiveWindowValue, null), 1, 2);

        reasonLabel.setWrapText(true);
        reasonLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #42586d;");

        VBox root = new VBox(12, stats, chartPanel, reasonLabel);
        root.setPadding(new Insets(10));
        root.setStyle(UiTheme.PANEL_INSET_TINT);
        getContentBox().getChildren().add(root);
        reset();
    }

    public void update(CongestionSnapshot snapshot) {
        if (snapshot == null) {
            reset();
            return;
        }
        phaseValue.setText(formatPhase(snapshot.getPhase()));
        cwndValue.setText(snapshot.getCwndBytes() + " B");
        ssthreshValue.setText(snapshot.getSlowStartThresholdBytes() + " B");
        duplicateAckValue.setText(String.valueOf(snapshot.getDuplicateAckCount()));
        bytesInFlightValue.setText(snapshot.getBytesInFlight() + " B");
        effectiveWindowValue.setText(snapshot.getEffectiveWindowBytes() + " B");
        reasonLabel.setText(snapshot.getReason());

        if (snapshot.getHistoryPoint() != null) {
            if (history.isEmpty() || history.get(history.size() - 1).getStep() != snapshot.getHistoryPoint().getStep()) {
                history.add(snapshot.getHistoryPoint());
                chartPanel.setHistory(history);
            }
        }
    }

    public void restore(CongestionSnapshot snapshot, List<CwndHistoryPoint> historyPoints) {
        history.clear();
        if (historyPoints != null) {
            history.addAll(historyPoints);
        }
        chartPanel.setHistory(history);
        if (snapshot == null) {
            reset();
            return;
        }
        phaseValue.setText(formatPhase(snapshot.getPhase()));
        cwndValue.setText(snapshot.getCwndBytes() + " B");
        ssthreshValue.setText(snapshot.getSlowStartThresholdBytes() + " B");
        duplicateAckValue.setText(String.valueOf(snapshot.getDuplicateAckCount()));
        bytesInFlightValue.setText(snapshot.getBytesInFlight() + " B");
        effectiveWindowValue.setText(snapshot.getEffectiveWindowBytes() + " B");
        reasonLabel.setText(snapshot.getReason());
    }

    public void reset() {
        history.clear();
        chartPanel.setHistory(history);
        phaseValue.setText("Slow start");
        cwndValue.setText("0 B");
        ssthreshValue.setText("0 B");
        duplicateAckValue.setText("0");
        bytesInFlightValue.setText("0 B");
        effectiveWindowValue.setText("0 B");
        reasonLabel.setText("La congestión TCP todavía no ha empezado a evolucionar.");
    }

    public void showUnavailable() {
        reset();
        reasonLabel.setText("Este panel solo se activa en simulaciones TCP.");
    }

    public List<CwndHistoryPoint> getHistory() {
        return List.copyOf(history);
    }

    private HBox statRow(String title, Label value, String tooltipText) {
        Label label = new Label(title);
        label.setStyle(UiTheme.FIELD_LABEL);
        label.setWrapText(true);
        HBox.setHgrow(label, Priority.ALWAYS);

        HBox row = new HBox(6, label);
        row.setAlignment(Pos.CENTER_LEFT);
        if (tooltipText != null) {
            Label hint = new Label("?");
            hint.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #2d6df6;"
                    + "-fx-background-color: #e8f0ff; -fx-background-radius: 999; -fx-padding: 1 5 1 5;");
            Tooltip tooltip = new Tooltip(tooltipText);
            tooltip.setWrapText(true);
            tooltip.setMaxWidth(260);
            attachHoverTooltip(hint, tooltip);
            row.getChildren().add(hint);
        }
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        row.getChildren().addAll(spacer, value);
        return row;
    }

    private Label chip(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #274057;"
                + "-fx-background-color: #eef4fb; -fx-background-radius: 999; -fx-padding: 5 8 5 8;");
        return label;
    }

    private String formatPhase(CongestionPhase phase) {
        if (phase == null) {
            return "-";
        }
        return switch (phase) {
            case SLOW_START -> "Slow start";
            case CONGESTION_AVOIDANCE -> "Congestion avoidance";
            case FAST_RETRANSMIT -> "Fast retransmit";
        };
    }

    private void attachHoverTooltip(Node target, Tooltip tooltip) {
        target.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> tooltip.show(target, event.getScreenX() + 12, event.getScreenY() + 12));
        target.addEventHandler(MouseEvent.MOUSE_MOVED, event -> {
            if (tooltip.isShowing()) {
                tooltip.setAnchorX(event.getScreenX() + 12);
                tooltip.setAnchorY(event.getScreenY() + 12);
            }
        });
        target.addEventHandler(MouseEvent.MOUSE_EXITED, event -> tooltip.hide());
    }
}
