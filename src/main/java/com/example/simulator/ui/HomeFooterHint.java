package com.example.simulator.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class HomeFooterHint extends VBox {
    public HomeFooterHint(String titleText, String bodyText, String sideText) {
        setSpacing(8);
        setPadding(new Insets(14, 16, 14, 16));
        setStyle("-fx-background-color: linear-gradient(to right, #ffffff, #f8fbff);"
                + "-fx-background-radius: 16;"
                + "-fx-border-radius: 16;"
                + "-fx-border-color: #d9e2ec;"
                + "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 16, 0.16, 0, 5);");

        Label title = new Label(titleText);
        title.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #102a43;");

        Label body = new Label(bodyText);
        body.setWrapText(true);
        body.setStyle("-fx-font-size: 12px; -fx-text-fill: #486581;");

        Label side = new Label(sideText);
        side.setWrapText(true);
        side.setStyle("-fx-font-size: 11px; -fx-text-fill: #627d98;");

        HBox row = new HBox(18, body, side);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(body, Priority.ALWAYS);
        HBox.setHgrow(side, Priority.ALWAYS);

        getChildren().addAll(title, row);
    }
}
