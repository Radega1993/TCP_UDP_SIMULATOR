package com.example.simulator.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class HomeHeroHeader extends VBox {
    public HomeHeroHeader(Node actions) {
        setSpacing(14);
        setPadding(new Insets(20, 22, 18, 22));
        setStyle("-fx-background-color: linear-gradient(to bottom, #ffffff 0%, #fbfdff 100%);"
                + "-fx-background-radius: 20;"
                + "-fx-border-radius: 20;"
                + "-fx-border-color: #d7e1ec;"
                + "-fx-border-width: 1;"
                + "-fx-effect: dropshadow(gaussian, rgba(37,99,235,0.10), 30, 0.22, 0, 10);");

        Label eyebrow = new Label("SIMULADOR EDUCATIVO");
        eyebrow.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #4F8EF7;");

        Label title = new Label("Simulador visual de TCP y UDP");
        title.setWrapText(true);
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #102A43;");

        Label subtitle = new Label("Explora handshake, entrega, pérdidas y retransmisiones con una interfaz pensada para explicar redes en clase.");
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(760);
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #486581; -fx-line-spacing: 2px;");

        VBox copy = new VBox(6, eyebrow, title, subtitle);
        HBox.setHgrow(copy, Priority.ALWAYS);

        HBox top = new HBox(18, copy, actions);
        top.setAlignment(Pos.CENTER_LEFT);

        Region accentLine = new Region();
        accentLine.setPrefHeight(5);
        accentLine.setMaxWidth(Double.MAX_VALUE);
        accentLine.setStyle("-fx-background-color: linear-gradient(to right, #4F8EF7, #31A891, #8A63D2);"
                + "-fx-background-radius: 999;");

        HBox highlights = new HBox(10,
                metric("4 modos", "#4F8EF7"),
                metric("Escenarios guiados", "#5BAA4A"),
                metric("Teoría de capas", "#F08A24")
        );
        highlights.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(top, accentLine, highlights);
    }

    private Label metric(String text, String accent) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + accent + ";"
                + "-fx-background-color: #f6f9fd;"
                + "-fx-background-radius: 999;"
                + "-fx-border-radius: 999;"
                + "-fx-border-color: #dce6ef;"
                + "-fx-padding: 7 10 7 10;");
        return label;
    }
}
