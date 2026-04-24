package com.example.simulator.ui;

import com.example.simulator.domain.network.Endpoint;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

import java.util.function.Consumer;

public class SequenceDiagramMessageNode extends Pane {
    private static final double WIDTH = 560;
    private static final double LEFT_X = 110;
    private static final double RIGHT_X = 430;

    public SequenceDiagramMessageNode(SequenceDiagramEventViewModel model, Consumer<String> opener) {
        this(model, opener, WIDTH, LEFT_X, RIGHT_X);
    }

    public SequenceDiagramMessageNode(SequenceDiagramEventViewModel model, Consumer<String> opener,
                                      double width, double leftX, double rightX) {
        setPrefWidth(width);
        setMinWidth(width);
        setMaxWidth(width);
        setPrefHeight(30);

        boolean leftToRight = model.getFrom() == Endpoint.CLIENT;
        double startX = leftToRight ? leftX : rightX;
        double endX = leftToRight ? rightX : leftX;
        Color color = Color.web(model.getColorHex());

        Line line = new Line(startX, 17, endX, 17);
        line.setStroke(color);
        line.setStrokeWidth(model.isLost() ? 1.8 : 2.4);
        if (model.isLost()) {
            line.getStrokeDashArray().addAll(8.0, 6.0);
        }

        Polygon arrow = leftToRight
                ? new Polygon(endX - 8, 12, endX, 17, endX - 8, 22)
                : new Polygon(endX + 8, 12, endX, 17, endX + 8, 22);
        arrow.setFill(color);

        double bubbleWidth = Math.max(140, Math.min(190, Math.abs(rightX - leftX) - 24));
        Rectangle bubble = new Rectangle(bubbleWidth, 22);
        bubble.setArcWidth(14);
        bubble.setArcHeight(14);
        bubble.setFill(Color.web("#ffffff"));
        bubble.setStroke(color);
        bubble.setStrokeWidth(model.isRetransmitted() ? 2.0 : 1.2);
        bubble.setLayoutX(((leftX + rightX) - bubbleWidth) / 2);
        bubble.setLayoutY(2);

        Label label = new Label(model.getLabel());
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        label.setPrefWidth(bubbleWidth - 12);
        label.setMaxWidth(bubbleWidth - 12);
        label.setLayoutX(bubble.getLayoutX() + 6);
        label.setLayoutY(5);
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #203246;");

        getChildren().addAll(line, arrow, bubble, label);
        setOnMouseClicked(event -> opener.accept(model.getDetails()));
    }
}
