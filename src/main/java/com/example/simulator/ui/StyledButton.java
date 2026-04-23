package com.example.simulator.ui;

import javafx.scene.control.Button;

public class StyledButton extends Button {
    public enum Kind {
        PRIMARY,
        EMPHASIS,
        SOFT,
        TERTIARY
    }

    public StyledButton(String text, Kind kind) {
        super(text);
        setMinHeight(40);
        setPrefHeight(40);
        setStyle(styleFor(kind));
    }

    private String styleFor(Kind kind) {
        return switch (kind) {
            case PRIMARY -> UiTheme.PRIMARY_BUTTON;
            case EMPHASIS -> UiTheme.EMPHASIS_BUTTON;
            case SOFT -> UiTheme.SOFT_BUTTON;
            case TERTIARY -> UiTheme.TERTIARY_BUTTON;
        };
    }
}
