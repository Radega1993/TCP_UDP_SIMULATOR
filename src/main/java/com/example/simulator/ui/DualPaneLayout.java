package com.example.simulator.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class DualPaneLayout extends FlowPane {
    public DualPaneLayout(Node left, Node right, double prefWrapLength) {
        super(18, 18, left, right);
        setAlignment(Pos.TOP_LEFT);
        setPrefWrapLength(prefWrapLength);
        if (left instanceof VBox leftBox) {
            VBox.setVgrow(leftBox, Priority.ALWAYS);
        }
        if (right instanceof VBox rightBox) {
            VBox.setVgrow(rightBox, Priority.ALWAYS);
        }
    }
}
