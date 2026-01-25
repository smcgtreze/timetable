package com.calendarfx.scheduler;

/**
 * Centralized style constants for consistent UI theming across the application
 */
public class StyleConstants {
    
    // Font settings
    public static final String FONT_FAMILY = "'Segoe UI', 'Arial', sans-serif";
    public static final String FONT_SIZE_SMALL = "12px";
    public static final String FONT_SIZE_MEDIUM = "14px";
    public static final String FONT_SIZE_LARGE = "16px";
    public static final String FONT_SIZE_XLARGE = "28px";
    
    // Text styles
    public static final String TEXT_STYLE = String.format("-fx-font-size: %s; -fx-font-family: %s;", FONT_SIZE_MEDIUM, FONT_FAMILY);
    public static final String TEXT_STYLE_SMALL = String.format("-fx-font-size: %s; -fx-font-family: %s;", FONT_SIZE_SMALL, FONT_FAMILY);
    
    // Button styles
    public static final String BUTTON_STYLE_SMALL = String.format("-fx-font-weight: bold; -fx-font-size: %s; -fx-font-family: %s; -fx-padding: 6px 12px;", FONT_SIZE_SMALL, FONT_FAMILY);
    public static final String BUTTON_STYLE_MEDIUM = String.format("-fx-font-weight: bold; -fx-font-size: %s; -fx-font-family: %s; -fx-padding: 8px 15px;", FONT_SIZE_MEDIUM, FONT_FAMILY);
    public static final String BUTTON_STYLE_LARGE = String.format("-fx-font-weight: bold; -fx-font-size: %s; -fx-font-family: %s; -fx-padding: 10px 20px;", FONT_SIZE_LARGE, FONT_FAMILY);
    
    // Color constants
    public static final String COLOR_DARK_BLUE = "#0052B3";
    public static final String COLOR_DARK_ORANGE = "#E67E22";
    public static final String COLOR_GREEN = "green";
    public static final String COLOR_RED = "red";
    public static final String COLOR_WHITE = "white";
    
    // Action button styles with colors
    public static final String ADD_BUTTON_STYLE = buildButtonStyle(COLOR_GREEN, FONT_SIZE_XLARGE);
    public static final String CONFLICT_BUTTON_STYLE = buildButtonStyle(COLOR_DARK_ORANGE, FONT_SIZE_XLARGE);
    public static final String REFRESH_BUTTON_STYLE = buildButtonStyle(COLOR_RED, FONT_SIZE_XLARGE);
    public static final String EDIT_BUTTON_STYLE = buildButtonStyle(COLOR_DARK_BLUE, FONT_SIZE_XLARGE);
    
    // Warning style
    public static final String WARNING_STYLE = "-fx-background-color: rgba(255,0,0,0.3); -fx-border-color: red;";
    
    /**
     * Builds a button style with the given color and font size
     */
    private static String buildButtonStyle(String color, String fontSize) {
        return String.format("-fx-background-color: %s; -fx-text-fill: %s; -fx-font-weight: bold; -fx-font-size: %s; -fx-font-family: %s; -fx-padding: 10px; -fx-cursor: hand;",
                color, COLOR_WHITE, fontSize, FONT_FAMILY);
    }
}
