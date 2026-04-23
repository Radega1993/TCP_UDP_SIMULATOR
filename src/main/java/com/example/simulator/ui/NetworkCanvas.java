package com.example.simulator.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

public class NetworkCanvas extends Pane {
    public NetworkCanvas() {
        setPrefSize(920, 540);
        setMinHeight(540);
        setStyle(UiTheme.CARD + "-fx-background-color: linear-gradient(to bottom, #ffffff, #f8fbff);");

        Circle focusGlow = new Circle(460, 270, 120, Color.web("#dbeafe"));
        focusGlow.setOpacity(0.38);

        Line line = new Line(240, 250, 680, 250);
        line.setStroke(Color.web("#a9b8c8"));
        line.setStrokeWidth(5);

        Line lineOverlay = new Line(260, 250, 660, 250);
        lineOverlay.setStroke(Color.web("#dbe7f3"));
        lineOverlay.setStrokeWidth(2);

        StackPane clientNode = endpointNode("Cliente", "Origen del tráfico", "#eff6ff", "#3b82f6");
        clientNode.setLayoutX(72);
        clientNode.setLayoutY(186);

        StackPane serverNode = endpointNode("Servidor", "Destino del tráfico", "#f8fafc", "#0f766e");
        serverNode.setLayoutX(678);
        serverNode.setLayoutY(186);

        Label networkLabel = new Label("Red intermedia");
        networkLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #4f6274;");
        networkLabel.setLayoutX(402);
        networkLabel.setLayoutY(178);

        Label networkHint = new Label("Espacio reservado para la animación de paquetes");
        networkHint.setStyle("-fx-font-size: 12px; -fx-text-fill: #7a8a9b;");
        networkHint.setLayoutX(350);
        networkHint.setLayoutY(312);

        Rectangle laneGuide = new Rectangle(290, 208, 340, 88);
        laneGuide.setArcWidth(28);
        laneGuide.setArcHeight(28);
        laneGuide.setFill(Color.web("#f8fbfe"));
        laneGuide.setStroke(Color.web("#dfe7ef"));
        laneGuide.getStrokeDashArray().addAll(12.0, 8.0);

        getChildren().addAll(focusGlow, laneGuide, line, lineOverlay, clientNode, serverNode, networkLabel, networkHint);
    }

    private StackPane endpointNode(String title, String subtitle, String fill, String stroke) {
        Rectangle shell = new Rectangle(170, 92);
        shell.setArcWidth(28);
        shell.setArcHeight(28);
        shell.setFill(Color.web(fill));
        shell.setStroke(Color.web(stroke));
        shell.setStrokeWidth(2.2);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 19px; -fx-font-weight: bold; -fx-text-fill: #102033;");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #5f7080;");

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(4, titleLabel, subtitleLabel);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(0, 0, 0, 16));

        StackPane wrapper = new StackPane(shell, content);
        StackPane.setAlignment(content, Pos.CENTER_LEFT);
        return wrapper;
    }
}
