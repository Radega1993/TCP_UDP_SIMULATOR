package com.example.simulator.ui;

import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.util.Duration;

public class HomeModeCard extends VBox {
    private final boolean featured;
    private final String accentStart;

    public HomeModeCard(String badgeText, String titleText, String subtitleText, String accentText,
                        String resourcePath, String fallbackGlyph, String accentStart, String accentEnd,
                        boolean featured) {
        this.featured = featured;
        this.accentStart = accentStart;

        setSpacing(14);
        setPadding(new Insets(18));
        setCursor(Cursor.HAND);
        setMinHeight(258);
        setPrefHeight(258);
        setMaxWidth(Double.MAX_VALUE);
        setStyle(baseStyle(featured));

        Label badge = new Label(badgeText);
        badge.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;"
                + "-fx-text-fill: " + accentStart + ";"
                + "-fx-background-color: " + softTint(accentStart) + ";"
                + "-fx-background-radius: 999; -fx-padding: 6 10 6 10;");

        HomeIconView icon = new HomeIconView(resourcePath, fallbackGlyph, accentStart, softTint(accentStart));

        VBox titleBox = new VBox(6);
        Label title = new Label(titleText);
        title.setWrapText(true);
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #102a43;");
        Label subtitle = new Label(subtitleText);
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(Double.MAX_VALUE);
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #486581; -fx-line-spacing: 2px;");
        titleBox.getChildren().addAll(title, subtitle);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        HBox topRow = new HBox(14, icon, titleBox);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label accent = new Label(accentText);
        accent.setWrapText(true);
        accent.setMaxWidth(Double.MAX_VALUE);
        accent.setStyle("-fx-font-size: 12px; -fx-text-fill: #334e68; -fx-line-spacing: 2px;");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Region line = new Region();
        line.setPrefHeight(featured ? 7 : 6);
        line.setMaxWidth(Double.MAX_VALUE);
        line.setStyle("-fx-background-color: linear-gradient(to right, " + accentStart + ", " + accentEnd + ");"
                + "-fx-background-radius: 999;");

        Label cta = new Label(featured ? "Abrir comparación  ->" : "Abrir modo  ->");
        cta.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + accentStart + ";"
                + "-fx-background-color: " + softTint(accentStart) + ";"
                + "-fx-background-radius: 999; -fx-padding: 8 12 8 12;");

        HBox footer = new HBox(12, line, cta);
        footer.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(line, Priority.ALWAYS);

        getChildren().addAll(badge, topRow, accent, spacer, footer);

        setOnMouseEntered(event -> {
            setStyle(hoverStyle());
            animateScale(1.012);
        });
        setOnMouseExited(event -> {
            setStyle(baseStyle(featured));
            animateScale(1.0);
        });
    }

    private String baseStyle(boolean featured) {
        String border = featured
                ? "linear-gradient(to right, #4F8EF7, #31A891, #8A63D2)"
                : "#d9e2ec";
        return "-fx-background-color: linear-gradient(to bottom, #ffffff, #fbfdff);"
                + "-fx-background-radius: 18;"
                + "-fx-border-radius: 18;"
                + "-fx-border-color: " + border + ";"
                + "-fx-border-width: " + (featured ? "2" : "1") + ";"
                + "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.08), 24, 0.18, 0, 8);";
    }

    private String hoverStyle() {
        return "-fx-background-color: linear-gradient(to bottom, #ffffff, #f8fbff);"
                + "-fx-background-radius: 18;"
                + "-fx-border-radius: 18;"
                + "-fx-border-color: " + accentStart + ";"
                + "-fx-border-width: " + (featured ? "2" : "1.4") + ";"
                + "-fx-effect: dropshadow(gaussian, rgba(37,99,235,0.15), 30, 0.25, 0, 12);";
    }

    private void animateScale(double target) {
        ScaleTransition transition = new ScaleTransition(Duration.millis(140), this);
        transition.setToX(target);
        transition.setToY(target);
        transition.play();
    }

    private String softTint(String color) {
        return switch (color.toUpperCase()) {
            case "#4F8EF7" -> "#edf5ff";
            case "#8A63D2" -> "#f2edff";
            case "#F08A24" -> "#fff4e8";
            case "#5BAA4A" -> "#eef9ee";
            default -> "#eef4fb";
        };
    }
}
