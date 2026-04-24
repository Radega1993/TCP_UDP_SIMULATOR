package com.example.simulator.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;

public class TheorySidebarCard extends VBox {
    public TheorySidebarCard(String titleText, String bodyText, String tooltipText) {
        setSpacing(8);
        setPadding(new Insets(14));
        setStyle(UiTheme.PANEL_INSET_TINT);

        HBox header = new HBox(6);
        Label title = new Label(titleText);
        title.setStyle(UiTheme.FIELD_LABEL);
        title.setWrapText(true);

        Label hint = new Label("?");
        hint.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #2d6df6;"
                + "-fx-background-color: #e8f0ff; -fx-background-radius: 999; -fx-padding: 1 5 1 5;");
        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(260);
        attachHoverTooltip(hint, tooltip);
        hint.setPickOnBounds(true);
        header.getChildren().addAll(title, hint);

        Label body = new Label(bodyText);
        body.setWrapText(true);
        body.setStyle(UiTheme.SUBTITLE);

        getChildren().addAll(header, body);
    }

    private void attachHoverTooltip(Node target, Tooltip tooltip) {
        target.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> tooltip.show(target, event.getScreenX() + 12, event.getScreenY() + 12));
        target.addEventHandler(MouseEvent.MOUSE_MOVED, event -> {
            if (tooltip.isShowing()) {
                tooltip.setAnchorX(event.getScreenX() + 12);
                tooltip.setAnchorY(event.getScreenY() + 12);
            }
        });
        target.addEventHandler(MouseEvent.MOUSE_EXITED, event -> tooltip.hide());
    }
}
