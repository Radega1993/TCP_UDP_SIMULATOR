package com.example.simulator.ui;

import javafx.geometry.Insets;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class MessageSummaryPanel extends DashboardCard {
    private final TextArea textArea = new TextArea();

    public MessageSummaryPanel(String title, String subtitle) {
        super("MENSAJE", title, subtitle);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(6);
        textArea.setStyle(UiTheme.MUTED_TEXT_SURFACE);

        VBox wrapper = new VBox(textArea);
        wrapper.setPadding(new Insets(6));
        wrapper.setStyle(UiTheme.PANEL_INSET);
        VBox.setVgrow(textArea, Priority.ALWAYS);

        getContentBox().getChildren().add(wrapper);
        setMaxWidth(Double.MAX_VALUE);
    }

    public TextArea getTextArea() {
        return textArea;
    }
}
