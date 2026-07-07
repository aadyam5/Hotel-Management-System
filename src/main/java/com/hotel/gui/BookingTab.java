package com.hotel.gui;

import com.hotel.model.*;
import com.hotel.service.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingTab extends Tab {

    private final DataStore      ds;
    private final FileService    fs;
    private final BookingService bookingService;

    private TableView<BkRow> table;
    private ObservableList<BkRow> data = FXCollections.observableArrayList();

    private ComboBox<String> checkoutCustomerCombo;

    public BookingTab(DataStore ds, FileService fs, BookingService bookingService) {
        super("📋 Booking");
        this.ds             = ds;
        this.fs             = fs;
        this.bookingService = bookingService;
        setClosable(false);
        setContent(buildContent());
    }

    private SplitPane buildContent() {

        Label bookTitle = UIHelper.sectionTitle("New Booking");

        ComboBox<String> custCombo = UIHelper.combo();
        custCombo.setPromptText("Select Customer");

        ComboBox<Integer> roomCombo = UIHelper.combo();
        roomCombo.setPromptText("Select Available Room");

        DatePicker checkInPicker  = new DatePicker(LocalDate.now());
        DatePicker checkOutPicker = new DatePicker(LocalDate.now().plusDays(1));
        styleDatePicker(checkInPicker);
        styleDatePicker(checkOutPicker);

        TextField addServicesField = UIHelper.field("Additional Services Cost (₹)");
        addServicesField.setText("0");

        Label tariffPreviewLbl = new Label("Estimated Bill: —");
        tariffPreviewLbl.setStyle("-fx-text-fill: #74b9ff; -fx-font-size: 13px; -fx-font-weight: bold;");

        
        custCombo.setOnMouseClicked(e -> refreshCustCombo(custCombo));
        roomCombo.setOnMouseClicked(e -> refreshRoomCombo(roomCombo));

        
        Runnable calcPreview = () -> {
            try {
                Integer roomNum = roomCombo.getValue();
                if (roomNum == null) { tariffPreviewLbl.setText("Estimated Bill: —"); return; }
                LocalDate ci  = checkInPicker.getValue();
                LocalDate co  = checkOutPicker.getValue();
                double extra  = Double.parseDouble(addServicesField.getText().trim().isEmpty()
                                                   ? "0" : addServicesField.getText().trim());
                int days = (int) java.time.temporal.ChronoUnit.DAYS.between(ci, co);
                if (days <= 0) { tariffPreviewLbl.setText("Check-out must be after check-in"); return; }
                ds.findRoom(roomNum).ifPresent(r -> {
                    double bill = r.calculateTariff(days, extra);
                    tariffPreviewLbl.setText(String.format("Estimated Bill: ₹%.2f  (%d day%s)",
                                            bill, days, days == 1 ? "" : "s"));
                });
            } catch (NumberFormatException ex) {
                tariffPreviewLbl.setText("Invalid extra cost");
            }
        };

        roomCombo.setOnAction(e -> calcPreview.run());
        checkInPicker.setOnAction(e  -> calcPreview.run());
        checkOutPicker.setOnAction(e -> calcPreview.run());
        addServicesField.textProperty().addListener((o, ov, nv) -> calcPreview.run());

        Button bookBtn = UIHelper.btn("✔  Confirm Booking", UIHelper.BTN_SUCCESS);
        bookBtn.setMaxWidth(Double.MAX_VALUE);
        bookBtn.setOnAction(e -> {
            String custId  = custCombo.getValue();
            Integer roomNo = roomCombo.getValue();
            if (custId == null || roomNo == null) {
                UIHelper.alertError("Error", "Select customer and room."); return;
            }
            LocalDate ci = checkInPicker.getValue();
            LocalDate co = checkOutPicker.getValue();
            if (ci == null || co == null || !co.isAfter(ci)) {
                UIHelper.alertError("Error", "Check-out must be after check-in."); return;
            }
            double extra;
            try { extra = Double.parseDouble(addServicesField.getText().trim().isEmpty()
                                             ? "0" : addServicesField.getText().trim()); }
            catch (NumberFormatException ex) { UIHelper.alertError("Error", "Invalid extra cost."); return; }

            
            String id = custId.contains(" ") ? custId.split(" ")[0] : custId;

            bookingService.bookRoom(id, roomNo, ci, co, extra).ifPresentOrElse(
                bk -> {
                    UIHelper.alertInfo("Booked!",
                        "Booking ID: " + bk.getBookingId() +
                        "\nRoom: " + bk.getRoomNumber() +
                        "\nDays: " + bk.getDays() +
                        "\nTotal Bill: ₹" + String.format("%.2f", bk.getTotalBill()));
                    refreshTable();
                    refreshCheckoutCombo();
                    custCombo.setValue(null);
                    roomCombo.setValue(null);
                    addServicesField.setText("0");
                    tariffPreviewLbl.setText("Estimated Bill: —");
                },
                () -> UIHelper.alertError("Failed",
                        "Booking failed. Customer may already be booked, or room is unavailable.")
            );
        });

        
        Label checkoutTitle = UIHelper.sectionTitle("Checkout");
        checkoutCustomerCombo = UIHelper.combo();
        checkoutCustomerCombo.setPromptText("Select Customer to Checkout");
        checkoutCustomerCombo.setOnMouseClicked(e -> refreshCheckoutCombo());
        checkoutCustomerCombo.setPrefWidth(240);

        Button checkoutBtn = UIHelper.btn("✖  Checkout", UIHelper.BTN_WARNING);
        checkoutBtn.setMaxWidth(Double.MAX_VALUE);
        checkoutBtn.setOnAction(e -> {
            String val = checkoutCustomerCombo.getValue();
            if (val == null) { UIHelper.alertError("Error", "Select a customer to checkout."); return; }
            String id = val.contains(" ") ? val.split(" ")[0] : val;

            bookingService.checkout(id).ifPresentOrElse(
                bk -> {
                    UIHelper.alertInfo("Checked Out",
                        "Customer checked out.\nRoom " + bk.getRoomNumber() + " is now available." +
                        "\nFinal Bill: ₹" + String.format("%.2f", bk.getTotalBill()));
                    refreshTable();
                    refreshCheckoutCombo();
                },
                () -> UIHelper.alertError("Failed", "No active booking found for this customer.")
            );
        });

        GridPane bookForm = new GridPane();
        bookForm.setHgap(10); bookForm.setVgap(12);
        bookForm.add(UIHelper.label("Customer:"),       0, 0); bookForm.add(custCombo,         1, 0);
        bookForm.add(UIHelper.label("Available Room:"), 0, 1); bookForm.add(roomCombo,         1, 1);
        bookForm.add(UIHelper.label("Check-In:"),       0, 2); bookForm.add(checkInPicker,     1, 2);
        bookForm.add(UIHelper.label("Check-Out:"),      0, 3); bookForm.add(checkOutPicker,    1, 3);
        bookForm.add(UIHelper.label("Extra Services:"), 0, 4); bookForm.add(addServicesField,  1, 4);
        bookForm.add(tariffPreviewLbl,                  0, 5, 2, 1);

        VBox leftPane = UIHelper.card(
                bookTitle, UIHelper.separator(),
                bookForm, bookBtn,
                UIHelper.separator(),
                checkoutTitle,
                checkoutCustomerCombo, checkoutBtn);
        leftPane.setSpacing(12);
        leftPane.setPrefWidth(310);

        Label tableTitle = UIHelper.sectionTitle("All Bookings");

        ComboBox<String> statusFilter = UIHelper.combo();
        statusFilter.getItems().addAll("All", "ACTIVE", "CHECKED_OUT");
        statusFilter.setValue("All");
        Button filterBtn = UIHelper.btn("Filter", UIHelper.BTN_NEUTRAL);

        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        filterBtn.setOnAction(e -> {
            String f = statusFilter.getValue();
            data.clear();
            List<Booking> list = new ArrayList<>(ds.getBookings());
            if (!"All".equals(f)) list.removeIf(b -> !b.getStatus().equals(f));
            for (Booking b : list) data.add(new BkRow(b, ds));
        });

        HBox filterBar = new HBox(10, UIHelper.label("Status:"), statusFilter, filterBtn);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        VBox rightPane = new VBox(12, tableTitle, filterBar, table);
        rightPane.setPadding(new Insets(20));
        rightPane.setStyle("-fx-background-color: " + UIHelper.PRIMARY + ";");
        VBox.setVgrow(table, Priority.ALWAYS);

        SplitPane split = new SplitPane(leftPane, rightPane);
        split.setDividerPositions(0.30);
        split.setStyle("-fx-background-color: " + UIHelper.PRIMARY + ";");

        refreshTable();
        refreshCheckoutCombo();
        refreshCustCombo(custCombo);
        refreshRoomCombo(roomCombo);
        return split;
    }

    private TableView<BkRow> buildTable() {
        TableView<BkRow> tv = new TableView<>(data);
        tv.setStyle(UIHelper.TABLE_STYLE);
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.getColumns().addAll(
            col("Booking ID",   "bookingId",  110),
            col("Customer ID",  "customerId",  100),
            col("Customer Name","custName",    140),
            col("Room #",       "roomNumber",   70),
            col("Check-In",     "checkIn",     105),
            col("Check-Out",    "checkOut",    105),
            col("Days",         "days",         50),
            col("Bill (₹)",     "bill",        100),
            col("Status",       "status",       100)
        );
        tv.setRowFactory(t -> new TableRow<>() {
            @Override
            protected void updateItem(BkRow item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) { setStyle(""); return; }
                setStyle("ACTIVE".equals(item.getStatus())
                    ? "-fx-background-color: #1a2e20;"
                    : "-fx-background-color: #2a2a35;");
            }
        });
        return tv;
    }

    private <T> TableColumn<BkRow, T> col(String name, String prop, int w) {
        TableColumn<BkRow, T> c = new TableColumn<>(name);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setMinWidth(w);
        return c;
    }

    private void refreshCustCombo(ComboBox<String> combo) {
        combo.getItems().clear();
        for (Customer c : ds.getCustomers()) {
            if (!c.hasRoom()) combo.getItems().add(c.getCustomerId() + " " + c.getName());
        }
    }

    private void refreshRoomCombo(ComboBox<Integer> combo) {
        combo.getItems().clear();
        for (Room<?, ?> r : ds.availableRooms()) combo.getItems().add(r.getRoomNumber());
    }

    private void refreshCheckoutCombo() {
        checkoutCustomerCombo.getItems().clear();
        for (Customer c : ds.getCustomers()) {
            if (c.hasRoom()) checkoutCustomerCombo.getItems().add(c.getCustomerId() + " " + c.getName());
        }
    }

    public void refreshTable() {
        data.clear();
        for (Booking b : ds.getBookings()) data.add(new BkRow(b, ds));
    }

    private void styleDatePicker(DatePicker dp) {
        dp.setStyle("-fx-background-color: #252845; -fx-text-fill: #eaeaea;" +
                    "-fx-pref-width: 220px;");
        dp.getEditor().setStyle("-fx-background-color: #252845; -fx-text-fill: #eaeaea;" +
                                "-fx-padding: 6 10;");
    }

    
    public static class BkRow {
        private final String bookingId;
        private final String customerId;
        private final String custName;
        private final int    roomNumber;
        private final String checkIn;
        private final String checkOut;
        private final int    days;
        private final String bill;
        private final String status;

        public BkRow(Booking b, DataStore ds) {
            this.bookingId  = b.getBookingId();
            this.customerId = b.getCustomerId();
            this.custName   = ds.findCustomer(b.getCustomerId())
                               .map(Customer::getName).orElse("—");
            this.roomNumber = b.getRoomNumber();
            this.checkIn    = b.getCheckIn().toString();
            this.checkOut   = b.getCheckOut().toString();
            this.days       = b.getDays();
            this.bill       = "₹" + String.format("%.2f", b.getTotalBill());
            this.status     = b.getStatus();
        }

        public String getBookingId()  { return bookingId; }
        public String getCustomerId() { return customerId; }
        public String getCustName()   { return custName; }
        public int    getRoomNumber() { return roomNumber; }
        public String getCheckIn()    { return checkIn; }
        public String getCheckOut()   { return checkOut; }
        public int    getDays()       { return days; }
        public String getBill()       { return bill; }
        public String getStatus()     { return status; }
    }
}
