package com.hotel.gui;

import com.hotel.model.*;
import com.hotel.service.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ReportsTab extends Tab {

    private final DataStore   ds;
    private final FileService fs;

    private TextArea reportArea;
    private TextArea auditArea;

    public ReportsTab(DataStore ds, FileService fs) {
        super("📊 Reports");
        this.ds = ds;
        this.fs = fs;
        setClosable(false);
        setContent(buildContent());
    }

    private ScrollPane buildContent() {

        
        Label repTitle = UIHelper.sectionTitle("System Report");

        Button genReportBtn = UIHelper.btn("Generate Report", UIHelper.BTN_PRIMARY);
        Button loadReportBtn = UIHelper.btn("Load Saved Report", UIHelper.BTN_NEUTRAL);

        HBox repBtns = new HBox(12, genReportBtn, loadReportBtn);
        repBtns.setAlignment(Pos.CENTER_LEFT);

        reportArea = new TextArea();
        reportArea.setEditable(false);
        reportArea.setPrefHeight(300);
        reportArea.setStyle("-fx-control-inner-background: #0a0a18;" +
                            "-fx-text-fill: #00e676; -fx-font-family: 'Courier New';" +
                            "-fx-font-size: 12px; -fx-background-radius: 8;");

        genReportBtn.setOnAction(e -> {
            fs.generateTextReport(ds);
            reportArea.setText(fs.readReport());
            fs.appendAudit("Report generated.");
            UIHelper.alertInfo("Report Saved", "Report saved to:\n" + fs.getReportPath());
        });

        loadReportBtn.setOnAction(e -> reportArea.setText(fs.readReport()));

        VBox reportCard = UIHelper.card(repTitle, UIHelper.separator(), repBtns, reportArea);

        
        Label auditTitle = UIHelper.sectionTitle("Audit Log (RandomAccessFile)");
        Button refreshAuditBtn = UIHelper.btn("⟳ Refresh", UIHelper.BTN_NEUTRAL);
        Button clearBtn = UIHelper.btn("Clear View", UIHelper.BTN_WARNING);

        auditArea = new TextArea();
        auditArea.setEditable(false);
        auditArea.setPrefHeight(260);
        auditArea.setStyle("-fx-control-inner-background: #0d0d1a;" +
                           "-fx-text-fill: #a0c4ff; -fx-font-family: 'Courier New';" +
                           "-fx-font-size: 12px; -fx-background-radius: 8;");

        refreshAuditBtn.setOnAction(e -> auditArea.setText(fs.readAuditLog()));
        clearBtn.setOnAction(e -> auditArea.clear());

        HBox auditBtns = new HBox(12, refreshAuditBtn, clearBtn);
        VBox auditCard = UIHelper.card(auditTitle, UIHelper.separator(), auditBtns, auditArea);

        
        Label dataTitle = UIHelper.sectionTitle("Data Persistence (Serialization)");

        Button saveBtn = UIHelper.btn("💾  Save All Data", UIHelper.BTN_SUCCESS);
        saveBtn.setOnAction(e -> {
            fs.saveAll(ds);
            UIHelper.alertInfo("Saved", "All data serialized to hms_data/ folder.");
        });

        Button loadBtn = UIHelper.btn("📂  Load Saved Data", UIHelper.BTN_PRIMARY);
        loadBtn.setOnAction(e -> {
            fs.loadAll(ds);
            UIHelper.alertInfo("Loaded", "Data loaded from hms_data/ folder.\n" +
                "Rooms: " + ds.getRooms().size() +
                " | Customers: " + ds.getCustomers().size() +
                " | Bookings: " + ds.getBookings().size());
        });

        Label dataInfoLbl = new Label(
            "Data is automatically saved after every booking, checkout, or room change.\n" +
            "Files are stored in: hms_data/  (rooms.ser, customers.ser, bookings.ser, logs.ser)\n" +
            "The audit log uses RandomAccessFile for direct append access.\n" +
            "Report is a plain-text file written via FileWriter / BufferedWriter.");
        dataInfoLbl.setStyle("-fx-text-fill: #a0a0b0; -fx-font-size: 12px;");
        dataInfoLbl.setWrapText(true);

        HBox dataBtns = new HBox(15, saveBtn, loadBtn);
        VBox dataCard = UIHelper.card(dataTitle, UIHelper.separator(), dataBtns, dataInfoLbl);

        
        Label statsTitle = UIHelper.sectionTitle("Quick Statistics");
        Button refreshStatsBtn = UIHelper.btn("Refresh Stats", UIHelper.BTN_NEUTRAL);

        TextArea statsArea = new TextArea();
        statsArea.setEditable(false);
        statsArea.setPrefHeight(160);
        statsArea.setStyle("-fx-control-inner-background: #1a1a2e;" +
                           "-fx-text-fill: #fdcb6e; -fx-font-family: 'Courier New';" +
                           "-fx-font-size: 13px;");

        refreshStatsBtn.setOnAction(e -> {
            long avail   = ds.getRooms().stream().filter(Room::isAvailable).count();
            long booked  = ds.getRooms().size() - avail;
            long active  = ds.getBookings().stream().filter(b -> "ACTIVE".equals(b.getStatus())).count();
            long done    = ds.getBookings().stream().filter(b -> "CHECKED_OUT".equals(b.getStatus())).count();
            double rev   = ds.totalRevenue();
            long svcDone = ds.getLogs().stream().filter(l -> "DONE".equals(l.getStatus())).count();

            statsArea.setText(
                "Total Rooms       : " + ds.getRooms().size() + "\n" +
                "  Available       : " + avail + "\n" +
                "  Booked          : " + booked + "\n" +
                "Total Customers   : " + ds.getCustomers().size() + "\n" +
                "Total Bookings    : " + ds.getBookings().size() + "\n" +
                "  Active          : " + active + "\n" +
                "  Checked Out     : " + done + "\n" +
                "Total Revenue     : ₹" + String.format("%.2f", rev) + "\n" +
                "Services Completed: " + svcDone
            );
        });
        refreshStatsBtn.fire();

        VBox statsCard = UIHelper.card(statsTitle, UIHelper.separator(), refreshStatsBtn, statsArea);

        HBox topRow = new HBox(15, reportCard, auditCard);
        HBox.setHgrow(reportCard, Priority.ALWAYS);
        HBox.setHgrow(auditCard, Priority.ALWAYS);

        HBox bottomRow = new HBox(15, dataCard, statsCard);
        HBox.setHgrow(dataCard, Priority.ALWAYS);

        VBox root = new VBox(18, topRow, bottomRow);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: " + UIHelper.PRIMARY + ";");

        auditArea.setText(fs.readAuditLog()); 

        ScrollPane sp = new ScrollPane(root);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: " + UIHelper.PRIMARY + "; -fx-background-color: " + UIHelper.PRIMARY + ";");
        return sp;
    }

    public void refresh() { auditArea.setText(fs.readAuditLog()); }
}
