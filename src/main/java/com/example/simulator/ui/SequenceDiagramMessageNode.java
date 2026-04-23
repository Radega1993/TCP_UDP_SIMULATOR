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
    private static final double LEFT_X = 110;
    private static final double RIGHT_X = 430;

    public SequenceDiagramMessageNode(SequenceDiagramEventViewModel model, Consumer<String> opener) {
        setPrefWidth(540);
        setMinWidth(540);
        setMaxWidth(540);
        setPrefHeight(42);

        boolean leftToRight = model.getFrom() == Endpoint.CLIENT;
        double startX = leftToRight ? LEFT_X : RIGHT_X;
        double endX = leftToRight ? RIGHT_X : LEFT_X;
        Color color = Color.web(model.getColorHex());

        Line line = new Line(startX, 24, endX, 24);
        line.setStroke(color);
        line.setStrokeWidth(model.isLost() ? 1.8 : 2.4);
        if (model.isLost()) {
            line.getStrokeDashArray().addAll(8.0, 6.0);
        }

        Polygon arrow = leftToRight
                ? new Polygon(endX - 8, 19, endX, 24, endX - 8, 29)
                : new Polygon(endX + 8, 19, endX, 24, endX + 8, 29);
        arrow.setFill(color);

        Rectangle bubble = new Rectangle(190, 24);
        bubble.setArcWidth(14);
        bubble.setArcHeight(14);
        bubble.setFill(Color.web("#ffffff"));
        bubble.setStroke(color);
        bubble.setStrokeWidth(model.isRetransmitted() ? 2.0 : 1.2);
        bubble.setLayoutX((LEFT_X + RIGHT_X - 190) / 2);
        bubble.setLayoutY(4);

        Label label = new Label(model.getLabel());
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        label.setPrefWidth(178);
        label.setMaxWidth(178);
        label.setLayoutX(bubble.getLayoutX() + 6);
        label.setLayoutY(7);
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #203246;");

        getChildren().addAll(line, arrow, bubble, label);
        setOnMouseClicked(event -> opener.accept(model.getDetails()));
    }
}
