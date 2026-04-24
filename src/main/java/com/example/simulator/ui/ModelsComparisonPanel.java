package com.example.simulator.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class ModelsComparisonPanel extends VBox {
    public ModelsComparisonPanel() {
        setSpacing(16);
    }

    public void setLearningLevel(LearningLevel level) {
        DashboardCard shell = new DashboardCard("COMPARATIVA", "OSI vs TCP/IP",
                "Esquema visual inspirado en diagramas docentes clásicos para entender equivalencias de un vistazo.");

        HBox board = new HBox(18,
                osiColumn(level),
                bridgeColumn(),
                tcpIpColumn(level)
        );
        HBox.setHgrow(board.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(board.getChildren().get(2), Priority.ALWAYS);

        shell.setContent(board);
        shell.setMaxWidth(Double.MAX_VALUE);
        getChildren().setAll(shell);
    }

    private VBox osiColumn(LearningLevel level) {
        VBox column = new VBox(8);
        column.getChildren().add(header("Modelo OSI"));
        column.getChildren().addAll(
                osiLayer(7, "Aplicación", UiTheme.LAYER_BG_APPLICATION, UiTheme.LAYER_BORDER_APPLICATION),
                osiLayer(6, "Presentación", UiTheme.LAYER_BG_PRESENTATION, UiTheme.LAYER_BORDER_PRESENTATION),
                osiLayer(5, "Sesión", UiTheme.LAYER_BG_SESSION, UiTheme.LAYER_BORDER_SESSION),
                osiLayer(4, "Transporte", UiTheme.LAYER_BG_TRANSPORT, UiTheme.LAYER_BORDER_TRANSPORT),
                osiLayer(3, "Red", UiTheme.LAYER_BG_NETWORK, UiTheme.LAYER_BORDER_NETWORK),
                osiLayer(2, "Enlace de datos", UiTheme.LAYER_BG_LINK, UiTheme.LAYER_BORDER_LINK),
                osiLayer(1, "Física", UiTheme.LAYER_BG_PHYSICAL, UiTheme.LAYER_BORDER_PHYSICAL)
        );
        return column;
    }

    private VBox tcpIpColumn(LearningLevel level) {
        VBox column = new VBox(8);
        column.getChildren().add(header("Modelo TCP/IP"));
        column.getChildren().addAll(
                tcpLayer("Aplicación", "Agrupa Aplicación + Presentación + Sesión",
                        UiTheme.LAYER_BG_APPLICATION, UiTheme.LAYER_BORDER_APPLICATION, 124),
                tcpLayer("Transporte", "Equivale a la capa Transporte de OSI",
                        UiTheme.LAYER_BG_TRANSPORT, UiTheme.LAYER_BORDER_TRANSPORT, 76),
                tcpLayer("Internet", "Equivale a la capa Red de OSI",
                        UiTheme.LAYER_BG_NETWORK, UiTheme.LAYER_BORDER_NETWORK, 76),
                tcpLayer("Acceso a la red", "Agrupa Enlace de datos + Física",
                        UiTheme.LAYER_BG_LINK, UiTheme.LAYER_BORDER_LINK, 110)
        );
        return column;
    }

    private VBox bridgeColumn() {
        VBox column = new VBox(22);
        column.setPadding(new Insets(38, 0, 0, 0));
        column.getChildren().addAll(
                bridge("Aplicación / Presentación / Sesión", UiTheme.LAYER_BORDER_APPLICATION),
                bridge("Transporte", UiTheme.LAYER_BORDER_TRANSPORT),
                bridge("Red", UiTheme.LAYER_BORDER_NETWORK),
                bridge("Enlace + Física", UiTheme.LAYER_BORDER_LINK)
        );
        return column;
    }

    private Label header(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #102033;");
        return label;
    }

    private VBox osiLayer(int number, String title, String fill, String border) {
        VBox card = new VBox();
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: " + fill + ";"
                + "-fx-background-radius: 14; -fx-border-radius: 14;"
                + "-fx-border-color: " + border + "; -fx-border-width: 1.4;");
        Label label = new Label(number + "   " + title);
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #102033;");
        card.getChildren().add(label);
        return card;
    }

    private VBox tcpLayer(String title, String subtitle, String fill, String border, double prefHeight) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(14));
        card.setPrefHeight(prefHeight);
        card.setStyle("-fx-background-color: " + fill + ";"
                + "-fx-background-radius: 16; -fx-border-radius: 16;"
                + "-fx-border-color: " + border + "; -fx-border-width: 1.6;");
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #102033;");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setWrapText(true);
        subtitleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #475b71;");
        card.getChildren().addAll(titleLabel, subtitleLabel);
        return card;
    }

    private VBox bridge(String text, String border) {
        VBox box = new VBox(6);
        box.setPadding(new Insets(10));
        box.setMinWidth(160);
        box.setStyle("-fx-background-color: #ffffff;"
                + "-fx-background-radius: 14; -fx-border-radius: 14;"
                + "-fx-border-color: " + border + "; -fx-border-width: 1.2;");
        Label arrow = new Label("------>");
        arrow.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + border + ";");
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #405467;");
        box.getChildren().addAll(arrow, label);
        return box;
    }
}
