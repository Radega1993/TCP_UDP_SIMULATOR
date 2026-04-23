package com.example.simulator.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class BottomControlBar extends HBox {
    private final Button homeButton = new StyledButton("Volver al inicio", StyledButton.Kind.TERTIARY);
    private final Button theoryButton = new StyledButton("Teoría", StyledButton.Kind.TERTIARY);
    private final Button runButton = new StyledButton("Iniciar", StyledButton.Kind.PRIMARY);
    private final Button playPauseButton = new StyledButton("Pausar", StyledButton.Kind.EMPHASIS);
    private final Button stepButton = new StyledButton("Paso", StyledButton.Kind.EMPHASIS);
    private final Button resetButton = new StyledButton("Reiniciar", StyledButton.Kind.SOFT);
    private final Button reviewFirstButton = new StyledButton("|<", StyledButton.Kind.TERTIARY);
    private final Button reviewStepBackButton = new StyledButton("Paso <-", StyledButton.Kind.TERTIARY);
    private final Button reviewStepForwardButton = new StyledButton("Paso ->", StyledButton.Kind.TERTIARY);
    private final Button reviewLastButton = new StyledButton(">|", StyledButton.Kind.TERTIARY);
    private final Label stepIndicatorLabel = new Label("Paso: 0/0");
    private final HBox reviewBox;

    public BottomControlBar() {
        setSpacing(12);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(14, 18, 14, 18));
        setStyle(UiTheme.CARD);

        Label navLabel = new Label("Acciones");
        navLabel.setStyle(UiTheme.SECTION_LABEL);

        stepIndicatorLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #334155;");

        reviewBox = new HBox(8, reviewFirstButton, reviewStepBackButton, reviewStepForwardButton, reviewLastButton, stepIndicatorLabel);
        reviewBox.setAlignment(Pos.CENTER_LEFT);
        reviewBox.setVisible(false);
        reviewBox.setManaged(false);
        reviewBox.setPadding(new Insets(6, 10, 6, 10));
        reviewBox.setStyle(UiTheme.REVIEW_BAR);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(
                navLabel,
                homeButton,
                theoryButton,
                spacer,
                runButton,
                playPauseButton,
                stepButton,
                resetButton,
                reviewBox
        );
    }

    public Button getHomeButton() {
        return homeButton;
    }

    public Button getTheoryButton() {
        return theoryButton;
    }

    public Button getRunButton() {
        return runButton;
    }

    public Button getPlayPauseButton() {
        return playPauseButton;
    }

    public Button getStepButton() {
        return stepButton;
    }

    public Button getResetButton() {
        return resetButton;
    }

    public Button getReviewFirstButton() {
        return reviewFirstButton;
    }

    public Button getReviewStepBackButton() {
        return reviewStepBackButton;
    }

    public Button getReviewStepForwardButton() {
        return reviewStepForwardButton;
    }

    public Button getReviewLastButton() {
        return reviewLastButton;
    }

    public Label getStepIndicatorLabel() {
        return stepIndicatorLabel;
    }

    public HBox getReviewBox() {
        return reviewBox;
    }
}
