package com.example.simulator.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class DashboardCard extends VBox {
    private final VBox contentBox = new VBox(12);
    private SectionHeader header;

    public DashboardCard(String title) {
        this(title, null);
    }

    public DashboardCard(String title, String subtitle) {
        this(null, title, subtitle);
    }

    public DashboardCard(String eyebrow, String title, String subtitle) {
        header = new SectionHeader(eyebrow, title, subtitle);
        setSpacing(14);
        setPadding(new Insets(16));
        setStyle(UiTheme.CARD);
        getChildren().addAll(header, contentBox);
    }

    public void setContent(Node content) {
        contentBox.getChildren().setAll(content);
    }

    public void setTitle(String title, String subtitle) {
        int headerIndex = getChildren().indexOf(header);
        header = new SectionHeader(null, title, subtitle);
        getChildren().set(headerIndex, header);
    }

    public VBox getContentBox() {
        return contentBox;
    }

    public static void grow(Node node) {
        VBox.setVgrow(node, Priority.ALWAYS);
    }
}
