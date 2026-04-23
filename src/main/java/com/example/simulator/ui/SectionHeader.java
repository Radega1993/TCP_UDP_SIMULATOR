package com.example.simulator.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class SectionHeader extends VBox {
    private final Label eyebrow = new Label();
    private final Label title = new Label();
    private final Label subtitle = new Label();

    public SectionHeader(String eyebrowText, String titleText, String subtitleText) {
        eyebrow.setText(eyebrowText == null ? "" : eyebrowText);
        eyebrow.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #5f7c9c; -fx-letter-spacing: 0.8px;");
        eyebrow.setManaged(eyebrowText != null && !eyebrowText.isBlank());
        eyebrow.setVisible(eyebrow.isManaged());

        title.setText(titleText == null ? "" : titleText);
        title.setWrapText(true);
        title.setMaxWidth(Double.MAX_VALUE);
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #122033;");

        subtitle.setText(subtitleText == null ? "" : subtitleText);
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(Double.MAX_VALUE);
        subtitle.setStyle(UiTheme.SUBTITLE);
        subtitle.setManaged(subtitleText != null && !subtitleText.isBlank());
        subtitle.setVisible(subtitle.isManaged());

        setSpacing(4);
        setPadding(new Insets(0));
        getChildren().addAll(eyebrow, title, subtitle);
    }
}
