package com.example.simulator.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class MailboxPanel extends DashboardCard {
    private final VBox itemsBox = new VBox(10);
    private final ScrollPane scrollPane = new ScrollPane(itemsBox);

    public MailboxPanel(String title, String subtitle) {
        super(title, subtitle);

        Label helper = new Label("Historial visual de paquetes entregados o perdidos.");
        helper.setStyle(UiTheme.SUBTITLE);

        itemsBox.setPadding(new Insets(6));
        itemsBox.setFillWidth(true);

        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(500);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox inset = new VBox(scrollPane);
        inset.setPadding(new Insets(4));
        inset.setStyle(UiTheme.PANEL_INSET);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getContentBox().getChildren().addAll(helper, inset);
        DashboardCard.grow(inset);
    }

    public VBox getItemsBox() {
        return itemsBox;
    }
}
