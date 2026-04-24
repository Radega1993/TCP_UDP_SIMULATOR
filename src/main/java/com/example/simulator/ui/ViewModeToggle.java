package com.example.simulator.ui;

import javafx.geometry.Pos;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

import java.util.function.Consumer;

public class ViewModeToggle extends HBox {
    private final ToggleButton diagramButton = new ToggleButton("Temporal");
    private final ToggleButton sceneButton = new ToggleButton("Paquetes");
    private Consumer<SimulationViewMode> listener;

    public ViewModeToggle() {
        setSpacing(0);
        setAlignment(Pos.CENTER_LEFT);
        setStyle("-fx-background-color: #eef3f8; -fx-background-radius: 999; -fx-padding: 4;");

        ToggleGroup group = new ToggleGroup();
        diagramButton.setToggleGroup(group);
        sceneButton.setToggleGroup(group);
        diagramButton.setSelected(true);

        styleButton(diagramButton, true);
        styleButton(sceneButton, false);
        getChildren().addAll(diagramButton, sceneButton);

        diagramButton.setOnAction(event -> setValue(SimulationViewMode.DIAGRAM));
        sceneButton.setOnAction(event -> setValue(SimulationViewMode.SCENE));
    }

    public void setOnModeChanged(Consumer<SimulationViewMode> listener) {
        this.listener = listener;
    }

    public SimulationViewMode getValue() {
        return diagramButton.isSelected() ? SimulationViewMode.DIAGRAM : SimulationViewMode.SCENE;
    }

    public void setValue(SimulationViewMode mode) {
        boolean scene = mode == SimulationViewMode.SCENE;
        sceneButton.setSelected(scene);
        diagramButton.setSelected(!scene);
        styleButton(sceneButton, scene);
        styleButton(diagramButton, !scene);
        if (listener != null) {
            listener.accept(mode);
        }
    }

    private void styleButton(ToggleButton button, boolean selected) {
        button.setStyle((selected ? UiTheme.PRIMARY_BUTTON : UiTheme.TERTIARY_BUTTON)
                + "-fx-background-radius: 999; -fx-border-radius: 999; -fx-min-height: 34; -fx-pref-height: 34;");
    }
}
