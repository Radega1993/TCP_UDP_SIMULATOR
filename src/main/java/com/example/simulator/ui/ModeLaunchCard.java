package com.example.simulator.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class ModeLaunchCard extends DashboardCard {
    public ModeLaunchCard(String eyebrow, String title, String subtitle, String accentText) {
        super(eyebrow, title, subtitle);
        Label accent = new Label(accentText);
        accent.setWrapText(true);
        accent.setStyle(UiTheme.BODY + "-fx-text-fill: #415569;");

        Region strip = new Region();
        strip.setPrefHeight(5);
        strip.setMaxWidth(Double.MAX_VALUE);
        strip.setStyle("-fx-background-color: linear-gradient(to right, #2e6ef7, #60a5fa); -fx-background-radius: 999;");

        VBox content = new VBox(12, accent, strip);
        content.setAlignment(Pos.TOP_LEFT);
        content.setPadding(new Insets(6, 0, 2, 0));
        setContent(content);
        setCursor(Cursor.HAND);
        setPrefWidth(340);
        setPrefHeight(240);
        setStyle(UiTheme.HERO_CARD);
    }
}
