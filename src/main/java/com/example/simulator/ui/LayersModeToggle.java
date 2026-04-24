package com.example.simulator.ui;

import javafx.geometry.Pos;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

public class LayersModeToggle extends HBox {
    private final ToggleGroup group = new ToggleGroup();
    private final Map<LayersMode, ToggleButton> buttons = new EnumMap<>(LayersMode.class);
    private Consumer<LayersMode> onModeChanged;

    public LayersModeToggle() {
        setSpacing(8);
        setAlignment(Pos.CENTER_LEFT);
        setStyle("-fx-background-color: #eef4fa; -fx-background-radius: 999; -fx-padding: 6;");

        for (LayersMode mode : LayersMode.values()) {
            ToggleButton button = new ToggleButton(mode.getLabel());
            button.setToggleGroup(group);
            button.setStyle(unselectedStyle());
            button.setMinHeight(38);
            button.setPrefHeight(38);
            button.setOnAction(event -> {
                refreshStyles();
                if (onModeChanged != null) {
                    onModeChanged.accept(mode);
                }
            });
            buttons.put(mode, button);
            getChildren().add(button);
        }

        setMode(LayersMode.TCP_IP);
    }

    public void setOnModeChanged(Consumer<LayersMode> onModeChanged) {
        this.onModeChanged = onModeChanged;
    }

    public void setMode(LayersMode mode) {
        ToggleButton button = buttons.get(mode);
        if (button != null) {
            group.selectToggle(button);
            refreshStyles();
        }
    }

    private void refreshStyles() {
        buttons.forEach((mode, button) -> button.setStyle(group.getSelectedToggle() == button
                ? selectedStyle()
                : unselectedStyle()));
    }

    private String selectedStyle() {
        return "-fx-background-color: linear-gradient(to bottom, #2e6ef7, #215bdf);"
                + "-fx-text-fill: white;"
                + "-fx-font-size: 12px;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 999;"
                + "-fx-padding: 0 16 0 16;";
    }

    private String unselectedStyle() {
        return "-fx-background-color: transparent;"
                + "-fx-text-fill: #33485d;"
                + "-fx-font-size: 12px;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 999;"
                + "-fx-padding: 0 16 0 16;";
    }
}
