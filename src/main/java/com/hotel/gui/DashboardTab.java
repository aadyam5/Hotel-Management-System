package com.hotel.gui;

import com.hotel.model.*;
import com.hotel.service.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DashboardTab extends Tab {

    private final DataStore   ds;
    private final FileService fs;

    
    private Label totalRoomsLbl   = new Label();
    private Label availRoomsLbl   = new Label();
    private Label bookedRoomsLbl  = new Label();
    private Label totalCustLbl    = new Label();
    private Label revenueLbl      = new Label();
    private Label activeBkLbl     = new Label();

    private TextArea activityLog  = new TextArea();

    public DashboardTab(DataStore ds, FileService fs) {
        super("🏨 Dashboard");
        this.ds = ds;
        this.fs = fs;
        setClosable(false);
        setContent(buildContent());
    }

    private ScrollPane buildContent() {
        Label title = new Label("Grand Horizon Hotel");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; " +
                       "-fx-text-fill: #e94560;");
        Label subtitle = new Label("Hotel Management System  |  Dashboard");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #a0a0b0;");

        Button refreshBtn = UIHelper.btn("⟳  Refresh", UIHelper.BTN_NEUTRAL);
        refreshBtn.setOnAction(e -> refresh());

        HBox header = new HBox(20, new VBox(2, title, subtitle));
        HBox.setHgrow(header.getChildren().get(0), Priority.ALWAYS);
        header.getChildren().add(refreshBtn);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));

        VBox statTotalRooms  = statCard(totalRoomsLbl,  "TOTAL ROOMS",    UIHelper.HIGHLIGHT);
        VBox statAvailRooms  = statCard(availRoomsLbl,  "AVAILABLE",      UIHelper.SUCCESS);
        VBox statBookedRooms = statCard(bookedRoomsLbl, "BOOKED",         "#e17055");
        VBox statCustomers   = statCard(totalCustLbl,   "CUSTOMERS",      UIHelper.WARNING);
        VBox statRevenue     = statCard(revenueLbl,     "REVENUE (₹)",    "#74b9ff");
        VBox statActive      = statCard(activeBkLbl,    "ACTIVE BOOKINGS","#a29bfe");

        HBox statsRow = new HBox(15,
                statTotalRooms, statAvailRooms, statBookedRooms,
                statCustomers, statRevenue, statActive);
        statsRow.setAlignment(Pos.CENTER_LEFT);
        statsRow.setPadding(new Insets(5, 0, 5, 0));

        Label rtTitle = UIHelper.sectionTitle("Room Type Overview");
        GridPane rtGrid = new GridPane();
        rtGrid.setHgap(30);
        rtGrid.setVgap(10);
        String[] types = {"Standard", "Deluxe", "Suite"};
        String[] colors = {UIHelper.TEXT_LIGHT, UIHelper.WARNING, UIHelper.HIGHLIGHT};
        for (int i = 0; i < 3; i++) {
            Label t = new Label(types[i]);
            t.setStyle("-fx-text-fill: " + colors[i] + "; -fx-font-weight: bold;");
            rtGrid.add(t, i * 3, 0);
        }

        VBox rtCard = UIHelper.card(rtTitle, UIHelper.separator(), rtGrid);

        Label logTitle = UIHelper.sectionTitle("Recent Activity");
        activityLog.setEditable(false);
        activityLog.setPrefHeight(220);
        activityLog.setStyle("-fx-control-inner-background: #252845;" +
                             "-fx-text-fill: #a0ffb0; -fx-font-family: 'Courier New';" +
                             "-fx-font-size: 12px; -fx-background-radius: 6;");
        activityLog.setWrapText(true);

        VBox logCard = UIHelper.card(logTitle, UIHelper.separator(), activityLog);

        Label qiTitle = UIHelper.sectionTitle("Quick Info");
        String info =
            "• Room Types: Standard | Deluxe | Suite\n" +
            "• Tariffs include service charge (Deluxe: 10%, Suite: 20%)\n" +
            "• Data is auto-saved to hms_data/ folder\n" +
            "• Room services run in background threads\n" +
            "• Use Booking tab to check-in / check-out guests";
        Label infoLbl = new Label(info);
        infoLbl.setStyle("-fx-text-fill: #a0a0b0; -fx-font-size: 12px;");
        infoLbl.setWrapText(true);
        VBox qiCard = UIHelper.card(qiTitle, UIHelper.separator(), infoLbl);
        qiCard.setPrefWidth(380);

        HBox bottomRow = new HBox(15, logCard, qiCard);
        HBox.setHgrow(logCard, Priority.ALWAYS);
        bottomRow.setAlignment(Pos.TOP_LEFT);

        VBox root = new VBox(18, header, statsRow, rtCard, bottomRow);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: " + UIHelper.PRIMARY + ";");

        ScrollPane sp = new ScrollPane(root);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: " + UIHelper.PRIMARY + "; -fx-background-color: " + UIHelper.PRIMARY + ";");

        refresh(); 
        return sp;
    }

    private VBox statCard(Label valueLbl, String title, String color) {
        valueLbl.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label lbl = new Label(title);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + UIHelper.TEXT_MUTED + "; -fx-font-weight: bold;");
        VBox box = new VBox(4, valueLbl, lbl);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(18, 25, 18, 25));
        box.setStyle("-fx-background-color: " + UIHelper.CARD_BG + "; -fx-background-radius: 12;" +
                     "-fx-border-color: " + color + "; -fx-border-width: 0 0 3 0; -fx-border-radius: 12;" +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 10, 0, 0, 4);");
        box.setMinWidth(140);
        return box;
    }

    public void refresh() {
        int total    = ds.getRooms().size();
        int avail    = (int) ds.getRooms().stream().filter(Room::isAvailable).count();
        int booked   = total - avail;
        int custs    = ds.getCustomers().size();
        long activeBk = ds.getBookings().stream().filter(b -> "ACTIVE".equals(b.getStatus())).count();
        double rev   = ds.totalRevenue();

        totalRoomsLbl.setText(String.valueOf(total));
        availRoomsLbl.setText(String.valueOf(avail));
        bookedRoomsLbl.setText(String.valueOf(booked));
        totalCustLbl.setText(String.valueOf(custs));
        revenueLbl.setText(String.format("%.0f", rev));
        activeBkLbl.setText(String.valueOf(activeBk));

        String audit = fs.readAuditLog();
        String[] lines = audit.split("\n");
        StringBuilder sb = new StringBuilder();
        int start = Math.max(0, lines.length - 20);
        for (int i = lines.length - 1; i >= start; i--) {
            sb.append(lines[i]).append("\n");
        }
        activityLog.setText(sb.toString().trim().isEmpty()
                            ? "No activity yet." : sb.toString().trim());
    }
}
