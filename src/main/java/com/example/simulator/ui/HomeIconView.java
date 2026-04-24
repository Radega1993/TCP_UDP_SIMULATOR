package com.example.simulator.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.io.InputStream;

public class HomeIconView extends StackPane {
    public HomeIconView(String resourcePath, String fallbackGlyph, String tintColor, String backgroundColor) {
        setPrefSize(58, 58);
        setMinSize(58, 58);
        setMaxSize(58, 58);
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: " + backgroundColor + ";"
                + "-fx-background-radius: 16;"
                + "-fx-border-radius: 16;"
                + "-fx-border-color: rgba(79,142,247,0.14);"
                + "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.07), 12, 0.18, 0, 3);");

        Image image = load(resourcePath);
        if (image != null) {
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(36);
            imageView.setFitHeight(36);
            imageView.setPreserveRatio(true);
            getChildren().add(imageView);
            return;
        }

        Label glyph = new Label(fallbackGlyph);
        glyph.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + tintColor + ";");
        getChildren().add(glyph);
    }

    private Image load(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            return null;
        }
        try (InputStream stream = HomeIconView.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                return null;
            }
            return new Image(stream);
        } catch (Exception ignored) {
            return null;
        }
    }
}
