package com.example.simulator.ui;

import com.example.simulator.domain.simulation.CwndHistoryPoint;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;

import java.util.ArrayList;
import java.util.List;

public class CwndChartPanel extends VBox {
    private static final double PLOT_WIDTH = 240;
    private static final double PLOT_HEIGHT = 150;
    private final Pane plotPane = new Pane();
    private List<CwndHistoryPoint> history = List.of();

    public CwndChartPanel() {
        setSpacing(8);
        setPadding(new Insets(8));
        setStyle(UiTheme.PANEL_INSET);

        Label title = new Label("Evolución de cwnd");
        title.setStyle(UiTheme.FIELD_LABEL);

        Label caption = new Label("Tiempo / step");
        caption.setStyle("-fx-font-size: 11px; -fx-text-fill: #617487;");

        plotPane.setPrefSize(PLOT_WIDTH, PLOT_HEIGHT);
        plotPane.setMinSize(PLOT_WIDTH, PLOT_HEIGHT);
        plotPane.setMaxSize(PLOT_WIDTH, PLOT_HEIGHT);
        plotPane.setStyle("-fx-background-color: #fbfdff; -fx-background-radius: 12; -fx-border-color: #dfe7ef; -fx-border-radius: 12;");

        getChildren().addAll(title, plotPane, caption);
        render();
    }

    public void setHistory(List<CwndHistoryPoint> history) {
        this.history = history == null ? List.of() : new ArrayList<>(history);
        render();
    }

    public List<CwndHistoryPoint> getHistory() {
        return List.copyOf(history);
    }

    private void render() {
        plotPane.getChildren().clear();
        drawGrid();
        if (history.isEmpty()) {
            return;
        }

        double left = 28;
        double right = PLOT_WIDTH - 12;
        double bottom = PLOT_HEIGHT - 24;
        double top = 12;
        int maxCwnd = history.stream().mapToInt(CwndHistoryPoint::getCwndBytes).max().orElse(1);
        maxCwnd = Math.max(4, maxCwnd);

        Polyline line = new Polyline();
        line.setStroke(Color.web("#2563eb"));
        line.setStrokeWidth(2.2);

        for (int i = 0; i < history.size(); i++) {
            CwndHistoryPoint point = history.get(i);
            double x = history.size() == 1 ? (left + right) / 2 : left + ((right - left) * i / (history.size() - 1.0));
            double y = bottom - ((bottom - top) * point.getCwndBytes() / maxCwnd);
            line.getPoints().addAll(x, y);

            Circle circle = new Circle(x, y, point.isLossEvent() ? 4.6 : 3.6);
            circle.setFill(point.isLossEvent() ? Color.web(UiTheme.LOST) : Color.web("#2563eb"));
            circle.setStroke(point.isLossEvent() ? Color.web("#b91c1c") : Color.WHITE);
            Tooltip tooltip = new Tooltip(point.getLabel() + "\ncwnd=" + point.getCwndBytes() + " B");
            tooltip.setWrapText(true);
            tooltip.setMaxWidth(220);
            attachHoverTooltip(circle, tooltip);
            plotPane.getChildren().add(circle);
        }

        plotPane.getChildren().add(0, line);
    }

    private void drawGrid() {
        double left = 28;
        double right = PLOT_WIDTH - 12;
        double bottom = PLOT_HEIGHT - 24;
        double top = 12;

        for (int i = 0; i < 4; i++) {
            double y = top + ((bottom - top) * i / 3.0);
            Line grid = new Line(left, y, right, y);
            grid.setStroke(Color.web("#e7edf4"));
            plotPane.getChildren().add(grid);
        }

        Line axisY = new Line(left, top, left, bottom);
        axisY.setStroke(Color.web("#9eb1c4"));
        Line axisX = new Line(left, bottom, right, bottom);
        axisX.setStroke(Color.web("#9eb1c4"));
        plotPane.getChildren().addAll(axisY, axisX);
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
