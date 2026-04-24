package com.example.simulator.ui;

import javafx.geometry.Pos;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

public class LearningLevelToggle extends HBox {
    private final ToggleGroup group = new ToggleGroup();
    private final Map<LearningLevel, ToggleButton> buttons = new EnumMap<>(LearningLevel.class);
    private Consumer<LearningLevel> onLevelChanged;

    public LearningLevelToggle() {
        setSpacing(8);
        setAlignment(Pos.CENTER_LEFT);

        for (LearningLevel level : LearningLevel.values()) {
            ToggleButton button = new ToggleButton(level.getLabel());
            button.setToggleGroup(group);
            button.setMinHeight(34);
            button.setPrefHeight(34);
            button.setStyle(groupStyle(false));
            button.setOnAction(event -> {
                refreshStyles();
                if (onLevelChanged != null) {
                    onLevelChanged.accept(level);
                }
            });
            buttons.put(level, button);
            getChildren().add(button);
        }
        setLevel(LearningLevel.BASIC);
    }

    public void setOnLevelChanged(Consumer<LearningLevel> onLevelChanged) {
        this.onLevelChanged = onLevelChanged;
    }

    public void setLevel(LearningLevel level) {
        ToggleButton button = buttons.get(level);
        if (button != null) {
            group.selectToggle(button);
            refreshStyles();
        }
    }

    private void refreshStyles() {
        buttons.values().forEach(button -> button.setStyle(groupStyle(group.getSelectedToggle() == button)));
    }

    private String groupStyle(boolean selected) {
        if (selected) {
            return "-fx-background-color: #dbeafe;"
                    + "-fx-text-fill: #1d4ed8;"
                    + "-fx-font-weight: bold;"
                    + "-fx-background-radius: 12;"
                    + "-fx-border-radius: 12;"
                    + "-fx-border-color: #93c5fd;";
        }
        return "-fx-background-color: #f8fbfe;"
                + "-fx-text-fill: #5d7287;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 12;"
                + "-fx-border-radius: 12;"
                + "-fx-border-color: #dbe5ef;";
    }
}
