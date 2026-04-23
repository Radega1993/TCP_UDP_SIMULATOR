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
        setPrefSize(700, 430);
        setMinHeight(410);
        setMinWidth(460);
        setStyle(UiTheme.HERO_CARD);

        Circle glow = new Circle(350, 220, 118, Color.web("#dbeafe"));
        glow.setOpacity(0.34);

        Rectangle coreZone = new Rectangle(220, 172, 260, 92);
        coreZone.setArcWidth(34);
        coreZone.setArcHeight(34);
        coreZone.setFill(Color.web("#fbfdff"));
        coreZone.setStroke(Color.web("#dce6f0"));
        coreZone.getStrokeDashArray().addAll(12.0, 9.0);

        Line line = new Line(174, 216, 526, 216);
        line.setStroke(Color.web("#a6b6c8"));
        line.setStrokeWidth(5);

        Line overlay = new Line(196, 216, 504, 216);
        overlay.setStroke(Color.web("#e7edf5"));
        overlay.setStrokeWidth(2.2);

        StackPane clientNode = endpointNode("Cliente", "Origen del tráfico", "#eff6ff", "#3b82f6");
        clientNode.setLayoutX(28);
        clientNode.setLayoutY(146);

        StackPane serverNode = endpointNode("Servidor", "Destino del tráfico", "#f8fafc", "#0f766e");
        serverNode.setLayoutX(502);
        serverNode.setLayoutY(146);

        Label networkLabel = new Label("Red intermedia");
        networkLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #4f6274;");
        networkLabel.setLayoutX(284);
        networkLabel.setLayoutY(138);

        Label networkHint = new Label("Zona prioritaria de observación de paquetes");
        networkHint.setStyle("-fx-font-size: 12px; -fx-text-fill: #728295;");
        networkHint.setLayoutX(224);
        networkHint.setLayoutY(288);

        getChildren().addAll(glow, coreZone, line, overlay, clientNode, serverNode, networkLabel, networkHint);
    }

    private StackPane endpointNode(String title, String subtitle, String fill, String stroke) {
        Rectangle shell = new Rectangle(148, 84);
        shell.setArcWidth(28);
        shell.setArcHeight(28);
        shell.setFill(Color.web(fill));
        shell.setStroke(Color.web(stroke));
        shell.setStrokeWidth(2.2);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #102033;");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #607283;");

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(5, titleLabel, subtitleLabel);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(0, 0, 0, 18));

        StackPane wrapper = new StackPane(shell, content);
        StackPane.setAlignment(content, Pos.CENTER_LEFT);
        return wrapper;
    }
}
