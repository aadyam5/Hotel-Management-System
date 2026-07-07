package com.hotel.gui;

import com.hotel.model.*;
import com.hotel.service.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.Optional;

public class BillingTab extends Tab {

    private final DataStore   ds;
    private final FileService fs;

    private ComboBox<String> bookingCombo;
    private TextArea invoiceArea;

    // Live calculator fields
    private ComboBox<Integer> calcRoomCombo;
    private TextField calcDaysField;
    private TextField calcExtraField;
    private Label calcResultLbl;

    public BillingTab(DataStore ds, FileService fs) {
        super("💰 Billing");
        this.ds = ds;
        this.fs = fs;
        setClosable(false);
        setContent(buildContent());
    }

    private ScrollPane buildContent() {

        Label invTitle = UIHelper.sectionTitle("Invoice Viewer");

        bookingCombo = UIHelper.combo();
        bookingCombo.setPromptText("Select Booking ID");
        bookingCombo.setPrefWidth(220);
        bookingCombo.setOnMouseClicked(e -> refreshBookingCombo());

        Button viewInvoiceBtn = UIHelper.btn("Generate Invoice", UIHelper.BTN_PRIMARY);
        viewInvoiceBtn.setOnAction(e -> generateInvoice());

        HBox invRow = new HBox(12, UIHelper.label("Booking:"), bookingCombo, viewInvoiceBtn);
        invRow.setAlignment(Pos.CENTER_LEFT);

        invoiceArea = new TextArea();
        invoiceArea.setEditable(false);
        invoiceArea.setPrefHeight(280);
        invoiceArea.setStyle("-fx-control-inner-background: #0a0a18;" +
                             "-fx-text-fill: #00e676; -fx-font-family: 'Courier New';" +
                             "-fx-font-size: 13px; -fx-background-radius: 8;");
        invoiceArea.setPromptText("Invoice will appear here...");

        VBox invoiceCard = UIHelper.card(invTitle, UIHelper.separator(), invRow, invoiceArea);

        Label calcTitle = UIHelper.sectionTitle("Tariff Calculator");

        calcRoomCombo = UIHelper.combo();
        calcRoomCombo.setPromptText("Select Room");
        calcRoomCombo.setOnMouseClicked(e -> {
            calcRoomCombo.getItems().clear();
            for (Room<?, ?> r : ds.getRooms())
                calcRoomCombo.getItems().add(r.getRoomNumber());
        });

        calcDaysField  = UIHelper.field("Number of Days");
        calcExtraField = UIHelper.field("Extra Services (₹)");
        calcExtraField.setText("0");

        calcResultLbl = new Label("Estimated Total: —");
        calcResultLbl.setStyle("-fx-text-fill: #fdcb6e; -fx-font-size: 20px; -fx-font-weight: bold;");

        Label breakdownLbl = new Label();
        breakdownLbl.setStyle("-fx-text-fill: #a0a0b0; -fx-font-size: 12px;");
        breakdownLbl.setWrapText(true);

        Button calcBtn = UIHelper.btn("Calculate", UIHelper.BTN_SUCCESS);
        calcBtn.setOnAction(e -> {
            Integer roomNum = calcRoomCombo.getValue();
            if (roomNum == null) { UIHelper.alertError("Error", "Select a room."); return; }
            try {
                int days      = Integer.parseInt(calcDaysField.getText().trim());
                double extra  = Double.parseDouble(calcExtraField.getText().trim().isEmpty()
                                                   ? "0" : calcExtraField.getText().trim());
                if (days <= 0) { UIHelper.alertError("Error", "Days must be positive."); return; }

                Optional<Room<?, ?>> roomOpt = ds.findRoom(roomNum);
                if (roomOpt.isEmpty()) return;
                Room<?, ?> room = roomOpt.get();

                double baseRate  = room.getPricePerDay().doubleValue();
                double baseTotal = baseRate * days;
                double total     = room.calculateTariff(days, extra);
                double surcharge = total - baseTotal - extra;

                calcResultLbl.setText(String.format("₹ %.2f", total));

                String breakdown =
                    "Room: " + roomNum + " (" + room.getRoomType() + ")\n" +
                    "Base Rate:   ₹" + String.format("%.2f", baseRate) + " × " + days + " days"
                    + " = ₹" + String.format("%.2f", baseTotal) + "\n" +
                    (surcharge > 0 ? "Surcharge:   ₹" + String.format("%.2f", surcharge) + "\n" : "") +
                    "Extra Svcs:  ₹" + String.format("%.2f", extra) + "\n" +
                    "─────────────────────────────\n" +
                    "TOTAL:       ₹" + String.format("%.2f", total);
                breakdownLbl.setText(breakdown);

            } catch (NumberFormatException ex) {
                UIHelper.alertError("Error", "Enter valid number of days.");
            }
        });

        GridPane calcForm = new GridPane();
        calcForm.setHgap(10); calcForm.setVgap(12);
        calcForm.add(UIHelper.label("Room:"),          0, 0); calcForm.add(calcRoomCombo,  1, 0);
        calcForm.add(UIHelper.label("Days:"),          0, 1); calcForm.add(calcDaysField,  1, 1);
        calcForm.add(UIHelper.label("Extra Cost:"),    0, 2); calcForm.add(calcExtraField, 1, 2);

        VBox calcCard = UIHelper.card(calcTitle, UIHelper.separator(),
                calcForm, calcBtn, calcResultLbl, breakdownLbl);
        calcCard.setSpacing(12);

        Label revTitle = UIHelper.sectionTitle("Revenue Summary");
        Label revLbl = new Label();
        revLbl.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #00b894;");

        Label activeRevLbl = new Label();
        activeRevLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #74b9ff;");
        Label totalBkLbl = new Label();
        totalBkLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #fdcb6e;");

        Button refreshRevBtn = UIHelper.btn("Refresh Revenue", UIHelper.BTN_NEUTRAL);
        refreshRevBtn.setOnAction(e -> {
            double rev = ds.totalRevenue();
            revLbl.setText("₹ " + String.format("%.2f", rev));

            double activeRev = ds.getBookings().stream()
                .filter(b -> "ACTIVE".equals(b.getStatus()))
                .mapToDouble(Booking::getTotalBill).sum();
            activeRevLbl.setText("Pending (Active Bookings): ₹" + String.format("%.2f", activeRev));
            totalBkLbl.setText("Total Bookings: " + ds.getBookings().size() +
                               "  |  Checked Out: " +
                               ds.getBookings().stream().filter(b -> "CHECKED_OUT".equals(b.getStatus())).count());
        });
        refreshRevBtn.fire(); 

        VBox revCard = UIHelper.card(revTitle, UIHelper.separator(),
                revLbl, activeRevLbl, totalBkLbl, refreshRevBtn);
        revCard.setSpacing(10);
        revCard.setMinWidth(300);

        
        HBox topRow = new HBox(15, invoiceCard, calcCard);
        HBox.setHgrow(invoiceCard, Priority.ALWAYS);
        topRow.setAlignment(Pos.TOP_LEFT);

        VBox root = new VBox(18, topRow, revCard);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: " + UIHelper.PRIMARY + ";");

        ScrollPane sp = new ScrollPane(root);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: " + UIHelper.PRIMARY + "; -fx-background-color: " + UIHelper.PRIMARY + ";");

        refreshBookingCombo();
        return sp;
    }

    private void generateInvoice() {
        String val = bookingCombo.getValue();
        if (val == null) { UIHelper.alertError("Error", "Select a booking."); return; }
        String bookingId = val.contains(" ") ? val.split(" ")[0] : val;

        Optional<Booking> bkOpt = ds.getBookings().stream()
            .filter(b -> b.getBookingId().equals(bookingId)).findFirst();

        if (bkOpt.isEmpty()) { UIHelper.alertError("Error", "Booking not found."); return; }
        Booking bk = bkOpt.get();

        Optional<Customer> custOpt = ds.findCustomer(bk.getCustomerId());
        Optional<Room<?, ?>> roomOpt = ds.findRoom(bk.getRoomNumber());

        String custName  = custOpt.map(Customer::getName).orElse("Unknown");
        String custPhone = custOpt.map(Customer::getContactNumber).orElse("—");
        String custEmail = custOpt.map(Customer::getEmail).orElse("—");
        String roomType  = roomOpt.map(r -> r.getRoomType().toString()).orElse("—");
        double pricePerDay = roomOpt.map(r -> r.getPricePerDay().doubleValue()).orElse(0.0);
        String amenities = roomOpt.map(Amenities::amenitiesSummary).orElse("—");

        double baseTotal = pricePerDay * bk.getDays();
        double surcharge = bk.getTotalBill() - baseTotal - bk.getAdditionalServices();

        String line = "═══════════════════════════════════════════════\n";
        String invoice =
            line +
            "        HOTEL MANAGEMENT SYSTEM\n" +
            "             INVOICE\n" +
            line +
            "Booking ID  : " + bk.getBookingId() + "\n" +
            "Status      : " + bk.getStatus() + "\n" +
            "─────────────────────────────────────────────\n" +
            "GUEST DETAILS\n" +
            "Name        : " + custName + "\n" +
            "Customer ID : " + bk.getCustomerId() + "\n" +
            "Phone       : " + custPhone + "\n" +
            "Email       : " + custEmail + "\n" +
            "─────────────────────────────────────────────\n" +
            "ROOM DETAILS\n" +
            "Room No.    : " + bk.getRoomNumber() + "\n" +
            "Room Type   : " + roomType + "\n" +
            "Amenities   : " + amenities + "\n" +
            "Rate/Day    : ₹" + String.format("%.2f", pricePerDay) + "\n" +
            "─────────────────────────────────────────────\n" +
            "BILLING\n" +
            "Check-In    : " + bk.getCheckIn() + "\n" +
            "Check-Out   : " + bk.getCheckOut() + "\n" +
            "Days        : " + bk.getDays() + "\n" +
            "Base Total  : ₹" + String.format("%.2f", baseTotal) + "\n" +
            (surcharge > 0.01 ?
            "Surcharge   : ₹" + String.format("%.2f", surcharge) + "\n" : "") +
            "Extra Svcs  : ₹" + String.format("%.2f", bk.getAdditionalServices()) + "\n" +
            line +
            "TOTAL BILL  : ₹" + String.format("%.2f", bk.getTotalBill()) + "\n" +
            line +
            "  Thank you for staying!\n" +
            line;

        invoiceArea.setText(invoice);
        fs.appendAudit("Invoice generated for booking " + bookingId);
    }

    private void refreshBookingCombo() {
        bookingCombo.getItems().clear();
        for (Booking b : ds.getBookings()) {
            String custName = ds.findCustomer(b.getCustomerId())
                               .map(Customer::getName).orElse("?");
            bookingCombo.getItems().add(b.getBookingId() + " — " + custName
                                       + " (Room " + b.getRoomNumber() + ") [" + b.getStatus() + "]");
        }
    }

    public void refresh() { refreshBookingCombo(); }
}
