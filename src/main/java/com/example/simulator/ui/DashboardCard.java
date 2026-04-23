package com.example.simulator.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class DashboardCard extends VBox {
    private final Label titleLabel = new Label();
    private final Label subtitleLabel = new Label();
    private final VBox contentBox = new VBox();

    public DashboardCard(String title) {
        this(title, null);
    }

    public DashboardCard(String title, String subtitle) {
        titleLabel.setText(title);
        titleLabel.setStyle(UiTheme.TITLE);

        subtitleLabel.setManaged(subtitle != null && !subtitle.isBlank());
        subtitleLabel.setVisible(subtitle != null && !subtitle.isBlank());
        subtitleLabel.setText(subtitle == null ? "" : subtitle);
        subtitleLabel.setStyle(UiTheme.SUBTITLE);

        contentBox.setSpacing(12);

        setSpacing(10);
        setPadding(new Insets(18));
        setStyle(UiTheme.CARD);
        getChildren().addAll(titleLabel, subtitleLabel, contentBox);
    }

    public void setContent(Node content) {
        contentBox.getChildren().setAll(content);
    }

    public VBox getContentBox() {
        return contentBox;
    }

    public static void grow(Node node) {
        VBox.setVgrow(node, Priority.ALWAYS);
    }
}
