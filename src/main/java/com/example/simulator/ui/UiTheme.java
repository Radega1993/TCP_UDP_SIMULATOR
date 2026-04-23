package com.example.simulator.ui;

public final class UiTheme {
    public static final String APP_BACKGROUND = "-fx-background-color: linear-gradient(to bottom, #f6f9fc, #e8eef5);";
    public static final String CARD = "-fx-background-color: rgba(255,255,255,0.96);"
            + "-fx-background-radius: 22;"
            + "-fx-border-radius: 22;"
            + "-fx-border-color: #d6e0ea;"
            + "-fx-border-width: 1;"
            + "-fx-effect: dropshadow(gaussian, rgba(15, 23, 42, 0.08), 18, 0.18, 0, 6);";
    public static final String PANEL_INSET = "-fx-background-color: #f8fbfe;"
            + "-fx-background-radius: 16;"
            + "-fx-border-radius: 16;"
            + "-fx-border-color: #e2eaf2;"
            + "-fx-border-width: 1;";
    public static final String PANEL_INSET_TINT = "-fx-background-color: linear-gradient(to bottom, #fbfdff, #f3f8fc);"
            + "-fx-background-radius: 16;"
            + "-fx-border-radius: 16;"
            + "-fx-border-color: #dde7f0;"
            + "-fx-border-width: 1;";
    public static final String TITLE = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #102033;";
    public static final String SUBTITLE = "-fx-font-size: 13px; -fx-text-fill: #5c6b7a;";
    public static final String BODY = "-fx-font-size: 12px; -fx-text-fill: #1f2d3d;";
    public static final String MONO = "-fx-font-family: 'Monospaced'; -fx-font-size: 12px; -fx-text-fill: #1f2d3d;";
    public static final String SECTION_LABEL = "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #607283; -fx-letter-spacing: 0.3px;";
    public static final String FIELD_LABEL = "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #274057;";
    public static final String TEXT_SURFACE = BODY + "-fx-control-inner-background: #f8fbfe; -fx-background-insets: 0; -fx-background-radius: 16;";
    public static final String MUTED_TEXT_SURFACE = MONO + "-fx-control-inner-background: #f8fbfe; -fx-background-insets: 0; -fx-background-radius: 16;";
    public static final String PRIMARY_BUTTON = "-fx-background-color: linear-gradient(to bottom, #2d6df6, #1f56d6);"
            + "-fx-text-fill: white;"
            + "-fx-font-weight: bold;"
            + "-fx-background-radius: 14;"
            + "-fx-padding: 0 18 0 18;";
    public static final String EMPHASIS_BUTTON = "-fx-background-color: linear-gradient(to bottom, #0f8f88, #0b6e69);"
            + "-fx-text-fill: white;"
            + "-fx-font-weight: bold;"
            + "-fx-background-radius: 14;"
            + "-fx-padding: 0 16 0 16;";
    public static final String SOFT_BUTTON = "-fx-background-color: #eef3f8;"
            + "-fx-text-fill: #203246;"
            + "-fx-font-weight: bold;"
            + "-fx-background-radius: 14;"
            + "-fx-border-radius: 14;"
            + "-fx-border-color: #d7e2ec;"
            + "-fx-padding: 0 16 0 16;";
    public static final String CHIP = "-fx-background-color: #edf3f9;"
            + "-fx-background-radius: 999;"
            + "-fx-border-radius: 999;"
            + "-fx-border-color: #d7e1eb;"
            + "-fx-padding: 10 14 10 14;"
            + "-fx-font-weight: bold;"
            + "-fx-text-fill: #17324a;";
    public static final String REVIEW_BAR = "-fx-background-color: #f3f7fb;"
            + "-fx-background-radius: 16;"
            + "-fx-border-radius: 16;"
            + "-fx-border-color: #dce6ef;"
            + "-fx-padding: 10 12 10 12;";

    private UiTheme() {
    }
}
