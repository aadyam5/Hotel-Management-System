package com.hotel.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

public class UIHelper {

    public static final String PRIMARY      = "#1a1a2e";
    public static final String SECONDARY    = "#16213e";
    public static final String ACCENT       = "#0f3460";
    public static final String HIGHLIGHT    = "#e94560";
    public static final String SUCCESS      = "#00b894";
    public static final String WARNING      = "#fdcb6e";
    public static final String TEXT_LIGHT   = "#eaeaea";
    public static final String TEXT_MUTED   = "#a0a0b0";
    public static final String CARD_BG      = "#1e2140";

    public static final String BTN_PRIMARY =
        "-fx-background-color: " + HIGHLIGHT + "; -fx-text-fill: white;" +
        "-fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6;" +
        "-fx-cursor: hand; -fx-font-size: 13px;";

    public static final String BTN_SUCCESS =
        "-fx-background-color: " + SUCCESS + "; -fx-text-fill: white;" +
        "-fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6;" +
        "-fx-cursor: hand; -fx-font-size: 13px;";

    public static final String BTN_WARNING =
        "-fx-background-color: " + WARNING + "; -fx-text-fill: #1a1a2e;" +
        "-fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6;" +
        "-fx-cursor: hand; -fx-font-size: 13px;";

    public static final String BTN_NEUTRAL =
        "-fx-background-color: " + ACCENT + "; -fx-text-fill: white;" +
        "-fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6;" +
        "-fx-cursor: hand; -fx-font-size: 13px;";

    public static final String FIELD_STYLE =
        "-fx-background-color: #252845; -fx-text-fill: " + TEXT_LIGHT + ";" +
        "-fx-border-color: #3a3d60; -fx-border-radius: 5; -fx-background-radius: 5;" +
        "-fx-padding: 7 10; -fx-font-size: 13px; -fx-prompt-text-fill: " + TEXT_MUTED + ";";

    public static final String LABEL_STYLE =
        "-fx-text-fill: " + TEXT_LIGHT + "; -fx-font-size: 13px;";

    public static final String SECTION_TITLE =
        "-fx-text-fill: " + TEXT_LIGHT + "; -fx-font-size: 15px; -fx-font-weight: bold;";

    public static final String CARD_STYLE =
        "-fx-background-color: " + CARD_BG + "; -fx-background-radius: 10;" +
        "-fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 3);";

    public static final String TABLE_STYLE =
        "-fx-background-color: " + CARD_BG + "; -fx-text-fill: " + TEXT_LIGHT + ";" +
        "-fx-border-color: transparent;";

    
    public static Label sectionTitle(String text) {
        Label l = new Label(text);
        l.setStyle(SECTION_TITLE);
        return l;
    }

    public static Label label(String text) {
        Label l = new Label(text);
        l.setStyle(LABEL_STYLE);
        return l;
    }

    public static TextField field(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(FIELD_STYLE);
        tf.setPrefWidth(220);
        return tf;
    }

    public static <T> ComboBox<T> combo() {
        ComboBox<T> cb = new ComboBox<>();
        cb.setStyle(FIELD_STYLE + "-fx-pref-width: 220px;");
        cb.setPrefWidth(220);
        return cb;
    }

    public static Button btn(String text, String style) {
        Button b = new Button(text);
        b.setStyle(style);
        return b;
    }

    public static VBox card(javafx.scene.Node... children) {
        VBox v = new VBox(12, children);
        v.setStyle(CARD_STYLE);
        return v;
    }

    public static Separator separator() {
        Separator s = new Separator();
        s.setStyle("-fx-background-color: #3a3d60;");
        return s;
    }

    public static void alertInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        styleAlert(a);
        a.showAndWait();
    }

    public static void alertError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        styleAlert(a);
        a.showAndWait();
    }

    private static void styleAlert(Alert a) {
        DialogPane dp = a.getDialogPane();
        dp.setStyle("-fx-background-color: " + SECONDARY + "; -fx-text-fill: " + TEXT_LIGHT + ";");
        dp.lookup(".content.label").setStyle("-fx-text-fill: " + TEXT_LIGHT + ";");
    }

    public static VBox statCard(String value, String title, String color) {
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label lbl = new Label(title);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + TEXT_MUTED + "; -fx-font-weight: bold;");
        VBox box = new VBox(4, val, lbl);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 30, 20, 30));
        box.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 12;" +
                     "-fx-border-color: " + color + "; -fx-border-width: 0 0 3 0; -fx-border-radius: 12;" +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 10, 0, 0, 4);");
        box.setMinWidth(160);
        return box;
    }
}
