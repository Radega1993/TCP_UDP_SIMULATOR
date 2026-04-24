package com.example.simulator.ui;

public final class UiTheme {
    public static final String APP_BACKGROUND = "-fx-background-color: linear-gradient(to bottom, #f5f7fb 0%, #edf2f8 100%);";
    public static final String SURFACE = "#ffffff";
    public static final String SURFACE_MUTED = "#f7f9fc";
    public static final String STROKE = "#d8e0ea";
    public static final String STROKE_SOFT = "#e6edf5";
    public static final String INK = "#122033";
    public static final String INK_MUTED = "#627487";
    public static final String BRAND = "#2563eb";
    public static final String BRAND_DARK = "#1d4ed8";
    public static final String BRAND_SOFT = "#e7efff";
    public static final String TEAL = "#0f766e";
    public static final String TCP_SYN = "#93c5fd";
    public static final String TCP_SYN_ACK = "#60a5fa";
    public static final String TCP_ACK = "#86efac";
    public static final String TCP_DATA = "#7dd3fc";
    public static final String UDP = "#c4b5fd";
    public static final String LOST = "#fecaca";
    public static final String RETRY = "#fed7aa";
    public static final String LAYER_BG_APPLICATION = "#DCCEF6";
    public static final String LAYER_BORDER_APPLICATION = "#8A63D2";
    public static final String LAYER_BG_PRESENTATION = "#D9E3F4";
    public static final String LAYER_BORDER_PRESENTATION = "#6D87C6";
    public static final String LAYER_BG_SESSION = "#D2E1F2";
    public static final String LAYER_BORDER_SESSION = "#5D89B3";
    public static final String LAYER_BG_TRANSPORT = "#D6EBCF";
    public static final String LAYER_BORDER_TRANSPORT = "#5BAA4A";
    public static final String LAYER_BG_NETWORK = "#F6E7A8";
    public static final String LAYER_BORDER_NETWORK = "#E2B400";
    public static final String LAYER_BG_LINK = "#F7D2B8";
    public static final String LAYER_BORDER_LINK = "#F08A24";
    public static final String LAYER_BG_PHYSICAL = "#F6C8BE";
    public static final String LAYER_BORDER_PHYSICAL = "#E96A4A";

    public static final String CARD = "-fx-background-color: rgba(255,255,255,0.98);"
            + "-fx-background-radius: 16;"
            + "-fx-border-radius: 16;"
            + "-fx-border-color: #d8e0ea;"
            + "-fx-border-width: 1;"
            + "-fx-effect: dropshadow(gaussian, rgba(15, 23, 42, 0.08), 24, 0.20, 0, 8);";
    public static final String HERO_CARD = "-fx-background-color: linear-gradient(to bottom, #ffffff 0%, #fbfdff 100%);"
            + "-fx-background-radius: 18;"
            + "-fx-border-radius: 18;"
            + "-fx-border-color: #d7e1ec;"
            + "-fx-border-width: 1;"
            + "-fx-effect: dropshadow(gaussian, rgba(37, 99, 235, 0.08), 28, 0.22, 0, 10);";
    public static final String PANEL_INSET = "-fx-background-color: #f8fbfe;"
            + "-fx-background-radius: 14;"
            + "-fx-border-radius: 14;"
            + "-fx-border-color: #e2eaf2;"
            + "-fx-border-width: 1;";
    public static final String PANEL_INSET_TINT = "-fx-background-color: linear-gradient(to bottom, #fbfdff, #f4f8fc);"
            + "-fx-background-radius: 14;"
            + "-fx-border-radius: 14;"
            + "-fx-border-color: #e2e9f1;"
            + "-fx-border-width: 1;";
    public static final String PANEL_DARK_SOFT = "-fx-background-color: linear-gradient(to right, #16263c, #20314b);"
            + "-fx-background-radius: 16;"
            + "-fx-border-radius: 16;";

    public static final String TITLE = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #122033;";
    public static final String SUBTITLE = "-fx-font-size: 13px; -fx-text-fill: #627487;";
    public static final String BODY = "-fx-font-size: 12px; -fx-text-fill: #223244;";
    public static final String MONO = "-fx-font-family: 'Monospaced'; -fx-font-size: 12px; -fx-text-fill: #203246;";
    public static final String SECTION_LABEL = "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #708194; -fx-letter-spacing: 0.6px;";
    public static final String FIELD_LABEL = "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #203246;";
    public static final String TEXT_SURFACE = BODY
            + "-fx-control-inner-background: #f8fbfe; -fx-background-insets: 0; -fx-background-radius: 14;"
            + "-fx-border-color: #dbe5ef; -fx-border-radius: 14;";
    public static final String MUTED_TEXT_SURFACE = MONO
            + "-fx-control-inner-background: #f8fbfe; -fx-background-insets: 0; -fx-background-radius: 14;"
            + "-fx-border-color: #dbe5ef; -fx-border-radius: 14;";
    public static final String PRIMARY_BUTTON = "-fx-background-color: linear-gradient(to bottom, #2e6ef7, #215bdf);"
            + "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 14; -fx-padding: 0 18 0 18;";
    public static final String EMPHASIS_BUTTON = "-fx-background-color: linear-gradient(to bottom, #0f8f88, #0b6f69);"
            + "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 14; -fx-padding: 0 16 0 16;";
    public static final String SOFT_BUTTON = "-fx-background-color: #eef3f8;"
            + "-fx-text-fill: #203246; -fx-font-weight: bold; -fx-background-radius: 14;"
            + "-fx-border-radius: 14; -fx-border-color: #d7e2ec; -fx-padding: 0 16 0 16;";
    public static final String TERTIARY_BUTTON = "-fx-background-color: #f8fbfe;"
            + "-fx-text-fill: #2f4256; -fx-font-weight: bold; -fx-background-radius: 14;"
            + "-fx-border-radius: 14; -fx-border-color: #dce5ee; -fx-padding: 0 14 0 14;";
    public static final String CHIP = "-fx-background-color: #edf3f9;"
            + "-fx-background-radius: 999; -fx-border-radius: 999; -fx-border-color: #d6e0ea;"
            + "-fx-padding: 8 12 8 12; -fx-font-weight: bold; -fx-text-fill: #17324a;";
    public static final String REVIEW_BAR = "-fx-background-color: linear-gradient(to bottom, #f6f9fc, #eff4f8);"
            + "-fx-background-radius: 14; -fx-border-radius: 14; -fx-border-color: #dce6ef; -fx-padding: 10 12 10 12;";
    public static final String STATUS_POSITIVE = "-fx-background-color: #ecfdf5; -fx-text-fill: #047857; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 7 10 7 10;";
    public static final String STATUS_NEGATIVE = "-fx-background-color: #fef2f2; -fx-text-fill: #b91c1c; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 7 10 7 10;";
    public static final String STATUS_NEUTRAL = "-fx-background-color: #eef3f8; -fx-text-fill: #334155; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 7 10 7 10;";

    private UiTheme() {
    }
}
