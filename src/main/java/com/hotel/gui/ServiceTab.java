package com.hotel.gui;

import com.hotel.model.*;
import com.hotel.service.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

public class ServiceTab extends Tab {

    private final DataStore   ds;
    private final FileService fs;
    private final RoomService roomService;

    private TableView<SvcRow> table;
    private ObservableList<SvcRow> data = FXCollections.observableArrayList();
    private TextArea threadLog;

    public ServiceTab(DataStore ds, FileService fs, RoomService roomService) {
        super("🔧 Services");
        this.ds          = ds;
        this.fs          = fs;
        this.roomService = roomService;
        setClosable(false);
        setContent(buildContent());
    }

    private SplitPane buildContent() {

        Label dispTitle = UIHelper.sectionTitle("Dispatch Room Service");

        ComboBox<Integer> roomCombo = UIHelper.combo();
        roomCombo.setPromptText("Select Room");
        roomCombo.setOnMouseClicked(e -> {
            roomCombo.getItems().clear();
            for (Room<?, ?> r : ds.getRooms())
                roomCombo.getItems().add(r.getRoomNumber());
        });

        ComboBox<ServiceLog.ServiceType> typeCombo = UIHelper.combo();
        typeCombo.getItems().addAll(ServiceLog.ServiceType.values());
        typeCombo.setPromptText("Service Type");

        Label statusLbl = new Label("Thread Status: IDLE");
        statusLbl.setStyle("-fx-text-fill: #a0a0b0; -fx-font-size: 12px;");

        Label progressLbl = new Label();
        progressLbl.setStyle("-fx-text-fill: #00b894; -fx-font-size: 12px;");
        progressLbl.setWrapText(true);

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setStyle("-fx-accent: #e94560;");

        Button dispatchBtn = UIHelper.btn("▶  Dispatch Service", UIHelper.BTN_PRIMARY);
        dispatchBtn.setMaxWidth(Double.MAX_VALUE);
        dispatchBtn.setOnAction(e -> {
            Integer roomNum = roomCombo.getValue();
            ServiceLog.ServiceType svcType = typeCombo.getValue();

            if (roomNum == null || svcType == null) {
                UIHelper.alertError("Error", "Select room and service type."); return;
            }

            statusLbl.setText("Thread Status: DISPATCHING...");
            statusLbl.setStyle("-fx-text-fill: #fdcb6e; -fx-font-size: 12px;");
            progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            dispatchBtn.setDisable(true);

            appendThreadLog(">>> Dispatching " + svcType + " to Room " + roomNum + " ...");

            roomService.dispatchService(roomNum, svcType, msg -> {
                appendThreadLog(msg);
                statusLbl.setText("Thread Status: " + msg);
                statusLbl.setStyle("-fx-text-fill: #00b894; -fx-font-size: 12px;");
                progressBar.setProgress(1.0);
                dispatchBtn.setDisable(false);
                refreshTable();
            });
        });

        Label threadInfoTitle = UIHelper.sectionTitle("Live Thread Log");
        threadLog = new TextArea();
        threadLog.setEditable(false);
        threadLog.setPrefHeight(200);
        threadLog.setStyle("-fx-control-inner-background: #0d0d1a;" +
                           "-fx-text-fill: #a0ffb0; -fx-font-family: 'Courier New';" +
                           "-fx-font-size: 12px; -fx-background-radius: 6;");

        Button clearLogBtn = UIHelper.btn("Clear Log", UIHelper.BTN_NEUTRAL);
        clearLogBtn.setOnAction(e -> threadLog.clear());

        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(12);
        form.add(UIHelper.label("Room:"),    0, 0); form.add(roomCombo,  1, 0);
        form.add(UIHelper.label("Service:"), 0, 1); form.add(typeCombo,  1, 1);

        VBox leftPane = UIHelper.card(
                dispTitle, UIHelper.separator(),
                form, progressBar, statusLbl, dispatchBtn,
                UIHelper.separator(),
                threadInfoTitle,
                threadLog, clearLogBtn);
        leftPane.setSpacing(12);
        leftPane.setPrefWidth(320);

        Label tableTitle = UIHelper.sectionTitle("Service Log");

        ComboBox<String> statusFilter = UIHelper.combo();
        statusFilter.getItems().addAll("All", "PENDING", "IN_PROGRESS", "DONE", "INTERRUPTED");
        statusFilter.setValue("All");
        Button filterBtn = UIHelper.btn("Filter", UIHelper.BTN_NEUTRAL);
        filterBtn.setOnAction(e -> {
            String f = statusFilter.getValue();
            data.clear();
            List<ServiceLog> list = new ArrayList<>(ds.getLogs());
            if (!"All".equals(f)) list.removeIf(l -> !l.getStatus().equals(f));
            for (ServiceLog l : list) data.add(new SvcRow(l));
        });

        Button refreshBtn = UIHelper.btn("⟳ Refresh", UIHelper.BTN_NEUTRAL);
        refreshBtn.setOnAction(e -> refreshTable());

        HBox filterBar = new HBox(10, UIHelper.label("Status:"), statusFilter, filterBtn, refreshBtn);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox rightPane = new VBox(12, tableTitle, filterBar, table);
        rightPane.setPadding(new Insets(20));
        rightPane.setStyle("-fx-background-color: " + UIHelper.PRIMARY + ";");
        VBox.setVgrow(table, Priority.ALWAYS);

        SplitPane split = new SplitPane(leftPane, rightPane);
        split.setDividerPositions(0.32);
        split.setStyle("-fx-background-color: " + UIHelper.PRIMARY + ";");

        appendThreadLog("Service dispatcher ready. Select a room and service type.");
        refreshTable();
        return split;
    }

    private TableView<SvcRow> buildTable() {
        TableView<SvcRow> tv = new TableView<>(data);
        tv.setStyle(UIHelper.TABLE_STYLE);
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        tv.getColumns().addAll(
            col("Log ID",      "logId",       90),
            col("Room #",      "roomNumber",   80),
            col("Service",     "serviceType", 130),
            col("Status",      "status",      110),
            col("Time",        "timestamp",   160)
        );

        tv.setRowFactory(t -> new TableRow<>() {
            @Override
            protected void updateItem(SvcRow item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) { setStyle(""); return; }
                setStyle(switch (item.getStatus()) {
                    case "DONE"        -> "-fx-background-color: #1a2e1a;";
                    case "IN_PROGRESS" -> "-fx-background-color: #2a2a1a;";
                    case "INTERRUPTED" -> "-fx-background-color: #2e1a1a;";
                    default            -> "-fx-background-color: #1e1e2e;";
                });
            }
        });
        return tv;
    }

    private <T> TableColumn<SvcRow, T> col(String name, String prop, int w) {
        TableColumn<SvcRow, T> c = new TableColumn<>(name);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setMinWidth(w);
        return c;
    }

    private void appendThreadLog(String msg) {
        threadLog.appendText(msg + "\n");
    }

    public void refreshTable() {
        data.clear();
        for (ServiceLog l : ds.getLogs()) data.add(new SvcRow(l));
    }

    public static class SvcRow {
        private final String logId;
        private final int    roomNumber;
        private final String serviceType;
        private final String status;
        private final String timestamp;

        public SvcRow(ServiceLog l) {
            this.logId       = l.getLogId();
            this.roomNumber  = l.getRoomNumber();
            this.serviceType = l.getServiceType().name();
            this.status      = l.getStatus();
            this.timestamp   = l.getTimestamp().toString().replace("T", " ");
        }

        public String getLogId()       { return logId; }
        public int    getRoomNumber()  { return roomNumber; }
        public String getServiceType() { return serviceType; }
        public String getStatus()      { return status; }
        public String getTimestamp()   { return timestamp; }
    }
}
