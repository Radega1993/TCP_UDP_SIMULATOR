package com.example.simulator.ui;

import com.example.simulator.domain.simulation.FlowControlSnapshot;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class SlidingWindowPanel extends DashboardCard {
    private static final int WINDOW_TRACK_CELLS = 12;
    private static final double WINDOW_CELL_SIZE = 16;
    private static final double WINDOW_CELL_GAP = 4;
    private static final double WINDOW_TRACK_PADDING = 6;
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
    private final HBox windowCells = new HBox(WINDOW_CELL_GAP);
    private final Pane windowTrack = new Pane();
    private final Rectangle windowFrame = new Rectangle();
    private final Label windowRangeLabel = new Label("Ventana activa: bytes 0-24");

    public SlidingWindowPanel() {
        super(null, "Ventana deslizante", null);

        GridPane stats = new GridPane();
        stats.setHgap(8);
        stats.setVgap(5);
        stats.add(statRow("Tamaño ventana", windowSizeValue), 0, 0);
        stats.add(statRow("Bytes enviados", bytesSentValue), 1, 0);
        stats.add(statRow("Bytes confirmados", bytesAckedValue), 0, 1);
        stats.add(statRow("Bytes en vuelo", bytesInFlightValue), 1, 1);

        guidanceLabel.setWrapText(true);
        guidanceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #42586d;");

        VBox root = new VBox(8, stats, buildWindowGraphic(), guidanceLabel);
        root.setPadding(new Insets(0));
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
        updateWindowGraphic(snapshot);

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

        VBox box = new VBox(4, header, bar);
        box.setPadding(new Insets(5, 6, 5, 6));
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
                + "-fx-background-color: #eef4fb; -fx-background-radius: 8; -fx-padding: 4 6 4 6;");
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

    private Node buildWindowGraphic() {
        windowCells.setAlignment(Pos.CENTER_LEFT);
        windowCells.setLayoutX(WINDOW_TRACK_PADDING);
        windowCells.setLayoutY(6);
        for (int i = 0; i < WINDOW_TRACK_CELLS; i++) {
            windowCells.getChildren().add(byteCell("#edf2f7", "#d9e5f2"));
        }

        double trackWidth = (WINDOW_TRACK_CELLS * WINDOW_CELL_SIZE) + ((WINDOW_TRACK_CELLS - 1) * WINDOW_CELL_GAP);
        windowFrame.setHeight(WINDOW_CELL_SIZE + 8);
        windowFrame.setWidth(trackWidth + 8);
        windowFrame.setLayoutX(WINDOW_TRACK_PADDING - 4);
        windowFrame.setLayoutY(2);
        windowFrame.setArcWidth(8);
        windowFrame.setArcHeight(8);
        windowFrame.setFill(javafx.scene.paint.Color.TRANSPARENT);
        windowFrame.setStroke(javafx.scene.paint.Color.web("#2f80ed"));
        windowFrame.setStrokeWidth(2);
        windowFrame.setMouseTransparent(true);

        windowTrack.getChildren().setAll(windowCells, windowFrame);
        windowTrack.setMinWidth(trackWidth + (WINDOW_TRACK_PADDING * 2));
        windowTrack.setPrefWidth(trackWidth + (WINDOW_TRACK_PADDING * 2));
        windowTrack.setMaxWidth(trackWidth + (WINDOW_TRACK_PADDING * 2));
        windowTrack.setMinHeight(WINDOW_CELL_SIZE + 12);
        windowTrack.setPrefHeight(WINDOW_CELL_SIZE + 12);
        windowTrack.setStyle("-fx-background-color: #fbfdff; -fx-background-radius: 10; -fx-border-color: #dfe7ef; -fx-border-radius: 10;");

        HBox legendDots = new HBox(8,
                legendDot("#6fcf97", "Confirmado"),
                legendDot("#56b4f0", "En vuelo"),
                legendDot("#edf2f7", "Pendiente")
        );
        legendDots.setAlignment(Pos.CENTER_LEFT);
        windowRangeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #486581; -fx-font-weight: 700;");
        return new VBox(4, windowTrack, new HBox(10, legendDots, windowRangeLabel));
    }

    private Region byteCell(String fill, String stroke) {
        Region cell = new Region();
        cell.setMinSize(WINDOW_CELL_SIZE, WINDOW_CELL_SIZE);
        cell.setPrefSize(WINDOW_CELL_SIZE, WINDOW_CELL_SIZE);
        cell.setMaxSize(WINDOW_CELL_SIZE, WINDOW_CELL_SIZE);
        cell.setStyle("-fx-background-color: " + fill + "; -fx-border-color: " + stroke + ";"
                + "-fx-background-radius: 4; -fx-border-radius: 4;");
        return cell;
    }

    private void updateWindowGraphic(FlowControlSnapshot snapshot) {
        int messageBytes = Math.max(0, snapshot.getBytesAcknowledged() + snapshot.getBytesPending());
        int total = Math.max(1, Math.max(messageBytes, snapshot.getWindowSizeBytes()));
        double bytesPerCell = Math.max(1.0, total / (double) WINDOW_TRACK_CELLS);

        for (int i = 0; i < windowCells.getChildren().size(); i++) {
            double cellStart = i * bytesPerCell;
            double cellEnd = cellStart + bytesPerCell;
            String fill = "#edf2f7";
            String stroke = "#d9e5f2";
            if (cellEnd <= snapshot.getBytesAcknowledged()) {
                fill = "#6fcf97";
                stroke = "#68c38f";
            } else if (cellStart < snapshot.getBytesSent() && cellEnd > snapshot.getBytesAcknowledged()) {
                fill = "#56b4f0";
                stroke = "#4ca7e0";
            }
            windowCells.getChildren().get(i).setStyle("-fx-background-color: " + fill + "; -fx-border-color: " + stroke + ";"
                    + "-fx-background-radius: 4; -fx-border-radius: 4;");
        }

        double cellPitch = WINDOW_CELL_SIZE + WINDOW_CELL_GAP;
        double frameStartCell = Math.min(WINDOW_TRACK_CELLS - 1, snapshot.getBytesAcknowledged() / bytesPerCell);
        double frameCells = Math.max(1.0, Math.min(WINDOW_TRACK_CELLS - frameStartCell, snapshot.getWindowSizeBytes() / bytesPerCell));
        double targetX = WINDOW_TRACK_PADDING - 4 + (frameStartCell * cellPitch);
        double targetWidth = Math.max(WINDOW_CELL_SIZE + 8, (frameCells * cellPitch) - WINDOW_CELL_GAP + 8);

        Timeline animation = new Timeline(
                new KeyFrame(Duration.millis(170),
                        new KeyValue(windowFrame.layoutXProperty(), targetX),
                        new KeyValue(windowFrame.widthProperty(), targetWidth)
                )
        );
        animation.play();
        int windowStart = snapshot.getBytesAcknowledged();
        int windowEnd = Math.min(total, windowStart + snapshot.getWindowSizeBytes());
        windowRangeLabel.setText("Ventana activa: bytes " + windowStart + "-" + windowEnd);
    }

    private Node legendDot(String color, String text) {
        Region dot = new Region();
        dot.setMinSize(9, 9);
        dot.setPrefSize(9, 9);
        dot.setMaxSize(9, 9);
        dot.setStyle("-fx-background-color: " + color + "; -fx-border-color: #d9e5f2; -fx-background-radius: 3; -fx-border-radius: 3;");
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 10px; -fx-text-fill: #627d98;");
        HBox box = new HBox(4, dot, label);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }
}
