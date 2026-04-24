package com.example.simulator.ui;

import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.InputStream;

public class QuickAccessChip extends HBox {
    public QuickAccessChip(String iconText, String titleText, String tintColor) {
        this(iconText, titleText, tintColor, null);
    }

    public QuickAccessChip(String iconText, String titleText, String tintColor, String resourcePath) {
        setSpacing(8);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(9, 12, 9, 10));
        setCursor(Cursor.HAND);
        setStyle(baseStyle());

        Node icon = iconNode(iconText, tintColor, resourcePath);
        Label title = new Label(titleText);
        title.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #334e68;");
        getChildren().addAll(icon, title);

        setOnMouseEntered(event -> {
            setStyle(hoverStyle(tintColor));
            animateScale(1.018);
        });
        setOnMouseExited(event -> {
            setStyle(baseStyle());
            animateScale(1.0);
        });
    }

    private String baseStyle() {
        return "-fx-background-color: #ffffff;"
                + "-fx-background-radius: 14;"
                + "-fx-border-radius: 14;"
                + "-fx-border-color: #d9e2ec;"
                + "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.04), 10, 0.14, 0, 3);";
    }

    private String hoverStyle(String tint) {
        return "-fx-background-color: #f8fbff;"
                + "-fx-background-radius: 14;"
                + "-fx-border-radius: 14;"
                + "-fx-border-color: " + tint + ";"
                + "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.08), 14, 0.20, 0, 5);";
    }

    private Node iconNode(String iconText, String tintColor, String resourcePath) {
        StackPane shell = new StackPane();
        shell.setMinSize(24, 24);
        shell.setPrefSize(24, 24);
        shell.setMaxSize(24, 24);
        shell.setStyle("-fx-background-color: " + softTint(tintColor) + "; -fx-background-radius: 8;");

        Image image = load(resourcePath);
        if (image != null) {
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(16);
            imageView.setFitHeight(16);
            imageView.setPreserveRatio(true);
            shell.getChildren().add(imageView);
            return shell;
        }

        Label fallback = new Label(iconText);
        fallback.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: " + tintColor + ";");
        shell.getChildren().add(fallback);
        return shell;
    }

    private Image load(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            return null;
        }
        try (InputStream stream = QuickAccessChip.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                return null;
            }
            return new Image(stream);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void animateScale(double target) {
        ScaleTransition transition = new ScaleTransition(Duration.millis(120), this);
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
