package com.example.simulator.ui;

import com.example.simulator.domain.protocol.ProtocolType;
import javafx.geometry.Pos;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

public class ProtocolFocusToggle extends HBox {
    private final ToggleGroup group = new ToggleGroup();
    private final Map<ProtocolType, ToggleButton> buttons = new EnumMap<>(ProtocolType.class);
    private Consumer<ProtocolType> onProtocolChanged;

    public ProtocolFocusToggle() {
        setSpacing(8);
        setAlignment(Pos.CENTER_LEFT);

        createButton(ProtocolType.TCP, "TCP");
        createButton(ProtocolType.UDP, "UDP");
        setProtocol(ProtocolType.TCP);
    }

    public void setOnProtocolChanged(Consumer<ProtocolType> onProtocolChanged) {
        this.onProtocolChanged = onProtocolChanged;
    }

    public void setProtocol(ProtocolType protocolType) {
        ToggleButton button = buttons.get(protocolType);
        if (button != null) {
            group.selectToggle(button);
            refreshStyles();
        }
    }

    private void createButton(ProtocolType protocolType, String text) {
        ToggleButton button = new ToggleButton(text);
        button.setToggleGroup(group);
        button.setMinHeight(36);
        button.setPrefHeight(36);
        button.setOnAction(event -> {
            refreshStyles();
            if (onProtocolChanged != null) {
                onProtocolChanged.accept(protocolType);
            }
        });
        buttons.put(protocolType, button);
        getChildren().add(button);
    }

    private void refreshStyles() {
        buttons.forEach((type, button) -> button.setStyle(group.getSelectedToggle() == button
                ? selectedStyle(type)
                : unselectedStyle()));
    }

    private String selectedStyle(ProtocolType protocolType) {
        String fill = protocolType == ProtocolType.TCP ? "#e8f5e4" : "#efe9ff";
        String border = protocolType == ProtocolType.TCP ? UiTheme.LAYER_BORDER_TRANSPORT : "#8A63D2";
        return "-fx-background-color: " + fill + ";"
                + "-fx-text-fill: #17324a;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 12;"
                + "-fx-border-radius: 12;"
                + "-fx-border-color: " + border + ";";
    }

    private String unselectedStyle() {
        return "-fx-background-color: #ffffff;"
                + "-fx-text-fill: #5d7287;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 12;"
                + "-fx-border-radius: 12;"
                + "-fx-border-color: #dbe5ef;";
    }
}
