package com.example.simulator.ui;

import com.example.simulator.domain.simulation.FlowControlSnapshot;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;

public class SlidingWindowPanel extends DashboardCard {
    private final Label windowSizeValue = statValue("24 B");
    private final Label bytesSentValue = statValue("0 B");
    private final Label bytesAckedValue = statValue("0 B");
    private final Label bytesInFlightValue = statValue("0 B");
    private final Label bytesPendingValue = statValue("0 B");
    private final Label bufferValue = statValue("0 / 24 B");
    private final Label advertisedWindowValue = statValue("24 B");
    private final Label guidanceLabel = new Label("La ventana deslizante todavía no ha empezado a moverse.");
    private final ProgressBar inFlightBar = new ProgressBar(0);
    private final ProgressBar bufferBar = new ProgressBar(0);

    public SlidingWindowPanel() {
        super("TCP", "Ventana deslizante", "Bytes enviados, confirmados, en vuelo y buffer del receptor.");

        GridPane stats = new GridPane();
        stats.setHgap(10);
        stats.setVgap(10);
        stats.add(statRow("Tamaño ventana", windowSizeValue), 0, 0);
        stats.add(statRow("Bytes enviados", bytesSentValue), 1, 0);
        stats.add(statRow("Bytes confirmados", bytesAckedValue), 0, 1);
        stats.add(statRow("Bytes en vuelo", bytesInFlightValue), 1, 1);
        stats.add(statRow("Bytes pendientes", bytesPendingValue), 0, 2);
        stats.add(statRow("Ventana anunciada", advertisedWindowValue), 1, 2);

        VBox flightBox = progressBox("Bytes en vuelo", inFlightBar,
                "Datos enviados por el emisor que aún no han sido confirmados.");
        VBox bufferBox = progressBox("Buffer de recepción", bufferBar,
                "Espacio disponible en el receptor para aceptar nuevos datos.");
        bufferBox.getChildren().add(bufferValue);

        guidanceLabel.setWrapText(true);
        guidanceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #42586d;");

        VBox root = new VBox(12, stats, flightBox, bufferBox, guidanceLabel);
        root.setPadding(new Insets(10));
        root.setStyle(UiTheme.PANEL_INSET_TINT);
        getContentBox().getChildren().add(root);

        reset();
    }

    public void update(FlowControlSnapshot snapshot) {
        if (snapshot == null) {
            reset();
            return;
        }
        windowSizeValue.setText(snapshot.getWindowSizeBytes() + " B");
        bytesSentValue.setText(snapshot.getBytesSent() + " B");
        bytesAckedValue.setText(snapshot.getBytesAcknowledged() + " B");
        bytesInFlightValue.setText(snapshot.getBytesInFlight() + " B");
        bytesPendingValue.setText(snapshot.getBytesPending() + " B");
        bufferValue.setText(snapshot.getReceiverBufferUsed() + " / " + snapshot.getReceiverBufferCapacity() + " B");
        advertisedWindowValue.setText(snapshot.getReceiverAdvertisedWindow() + " B");

        inFlightBar.setProgress(ratio(snapshot.getBytesInFlight(), snapshot.getWindowSizeBytes()));
        bufferBar.setProgress(ratio(snapshot.getReceiverBufferUsed(), snapshot.getReceiverBufferCapacity()));

        guidanceLabel.setText(buildGuidance(snapshot));
    }

    public void reset() {
        update(new FlowControlSnapshot(24, 0, 0, 0, 0, 24, 0, 24));
        guidanceLabel.setText("La ventana deslizante todavía no ha empezado a moverse.");
    }

    public void showUnavailable() {
        update(new FlowControlSnapshot(24, 0, 0, 0, 0, 24, 0, 24));
        guidanceLabel.setText("Este panel solo se activa en simulaciones TCP.");
    }

    private HBox statRow(String labelText, Label valueLabel) {
        Label label = new Label(labelText);
        label.setStyle(UiTheme.FIELD_LABEL);
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(label, Priority.ALWAYS);
        valueLabel.setMinWidth(Region.USE_PREF_SIZE);
        HBox row = new HBox(8, label, valueLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setFillHeight(true);
        row.setMaxWidth(Double.MAX_VALUE);
        row.setFillHeight(true);
        return row;
    }

    private VBox progressBox(String title, ProgressBar bar, String tooltipText) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle(UiTheme.FIELD_LABEL);
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        Label hint = new Label("?");
        hint.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #2d6df6;"
                + "-fx-background-color: #e8f0ff; -fx-background-radius: 999; -fx-padding: 1 5 1 5;");
        hint.setMinWidth(Region.USE_PREF_SIZE);
        Tooltip tooltip = buildTooltip(tooltipText);
        attachHoverTooltip(hint, tooltip);
        HBox header = new HBox(6, titleLabel, hint);
        header.setAlignment(Pos.CENTER_LEFT);

        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(10);
        HBox.setHgrow(bar, Priority.ALWAYS);
        bar.setStyle("-fx-accent: #2563eb;");

        VBox box = new VBox(8, header, bar);
        box.setPadding(new Insets(8));
        box.setStyle(UiTheme.PANEL_INSET);
        return box;
    }

    private Tooltip buildTooltip(String help) {
        Tooltip tooltip = new Tooltip(help);
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(280);
        return tooltip;
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

    private Label statValue(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #274057;"
                + "-fx-background-color: #eef4fb; -fx-background-radius: 999; -fx-padding: 5 8 5 8;");
        return label;
    }

    private double ratio(int current, int max) {
        if (max <= 0) {
            return 0;
        }
        return Math.min(1.0, Math.max(0.0, current / (double) max));
    }

    private String buildGuidance(FlowControlSnapshot snapshot) {
        if (snapshot.getBytesInFlight() > 0 && snapshot.getReceiverAdvertisedWindow() == 0) {
            return "El emisor está esperando porque el receptor ha anunciado ventana cero.";
        }
        if (snapshot.getBytesInFlight() > 0) {
            return "Hay varios segmentos en vuelo dentro de la ventana de envío.";
        }
        if (snapshot.getBytesPending() > 0) {
            return "La ventana ha liberado espacio y aún quedan bytes pendientes por enviar.";
        }
        return "Todos los bytes actualmente enviados ya han sido confirmados.";
    }
}
