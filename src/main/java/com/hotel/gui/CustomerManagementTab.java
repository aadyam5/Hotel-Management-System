package com.hotel.gui;

import com.hotel.model.Customer;
import com.hotel.service.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

public class CustomerManagementTab extends Tab {

    private final DataStore   ds;
    private final FileService fs;

    private TableView<CustRow> table;
    private ObservableList<CustRow> data = FXCollections.observableArrayList();
    private TextField searchField;

    public CustomerManagementTab(DataStore ds, FileService fs) {
        super("👤 Customers");
        this.ds = ds;
        this.fs = fs;
        setClosable(false);
        setContent(buildContent());
    }

    private SplitPane buildContent() {
        Label formTitle = UIHelper.sectionTitle("Add New Customer");

        TextField nameField    = UIHelper.field("Full Name");
        TextField phoneField   = UIHelper.field("Contact Number");
        TextField emailField   = UIHelper.field("Email Address");

        Button addBtn = UIHelper.btn("+ Add Customer", UIHelper.BTN_PRIMARY);
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> {
            String name  = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                UIHelper.alertError("Error", "Name and phone are required."); return;
            }
            if (phone.length() < 10) {
                UIHelper.alertError("Error", "Enter a valid 10-digit phone."); return;
            }

            String id = ds.nextCustomerId();
            Customer c = new Customer(id, name, phone, email);
            ds.addCustomer(c);
            fs.saveAll(ds);
            fs.appendAudit("Customer added: " + id + " — " + name);

            nameField.clear(); phoneField.clear(); emailField.clear();
            UIHelper.alertInfo("Success", "Customer " + name + " added.\nID: " + id);
            refreshTable();
        });

        Button removeBtn = UIHelper.btn("Remove Selected", UIHelper.BTN_WARNING);
        removeBtn.setMaxWidth(Double.MAX_VALUE);
        removeBtn.setOnAction(e -> {
            CustRow sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { UIHelper.alertError("Error", "Select a customer first."); return; }
            if (!sel.getRoomAllocated().equals("None")) {
                UIHelper.alertError("Error", "Cannot remove a customer with an active booking."); return;
            }
            ds.removeCustomer(sel.getCustomerId());
            fs.saveAll(ds);
            UIHelper.alertInfo("Removed", "Customer " + sel.getName() + " removed.");
            refreshTable();
        });

        Button detailBtn = UIHelper.btn("View Details", UIHelper.BTN_NEUTRAL);
        detailBtn.setMaxWidth(Double.MAX_VALUE);
        detailBtn.setOnAction(e -> {
            CustRow sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { UIHelper.alertError("Error", "Select a customer first."); return; }
            ds.findCustomer(sel.getCustomerId()).ifPresent(c -> {
                String checkIn  = c.getCheckInDate()  != null ? c.getCheckInDate().toString()  : "—";
                String checkOut = c.getCheckOutDate() != null ? c.getCheckOutDate().toString() : "—";
                UIHelper.alertInfo("Customer Details — " + c.getCustomerId(),
                    "Name:       " + c.getName() +
                    "\nPhone:      " + c.getContactNumber() +
                    "\nEmail:      " + c.getEmail() +
                    "\nRoom:       " + (c.hasRoom() ? c.getAllocatedRoomNumber() : "None") +
                    "\nCheck-In:   " + checkIn +
                    "\nCheck-Out:  " + checkOut +
                    "\nDays Stay:  " + c.getDaysOfStay());
            });
        });

        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(12);
        form.add(UIHelper.label("Name:"),    0, 0); form.add(nameField,  1, 0);
        form.add(UIHelper.label("Phone:"),   0, 1); form.add(phoneField, 1, 1);
        form.add(UIHelper.label("Email:"),   0, 2); form.add(emailField, 1, 2);

        VBox leftPane = UIHelper.card(formTitle, UIHelper.separator(),
                form, UIHelper.separator(),
                addBtn, removeBtn, detailBtn);
        leftPane.setPrefWidth(300);
        leftPane.setSpacing(12);

        searchField = UIHelper.field("Search by name or ID...");
        Button searchBtn = UIHelper.btn("Search", UIHelper.BTN_NEUTRAL);
        searchBtn.setOnAction(e -> refreshTable());
        Button clearBtn = UIHelper.btn("Clear", UIHelper.BTN_WARNING);
        clearBtn.setOnAction(e -> { searchField.clear(); refreshTable(); });

        HBox filterBar = new HBox(10, UIHelper.label("Search:"), searchField, searchBtn, clearBtn);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        Label tableTitle = UIHelper.sectionTitle("Customer List");
        VBox rightPane = new VBox(12, tableTitle, filterBar, table);
        rightPane.setPadding(new Insets(20));
        rightPane.setStyle("-fx-background-color: " + UIHelper.PRIMARY + ";");

        SplitPane split = new SplitPane(leftPane, rightPane);
        split.setDividerPositions(0.28);
        split.setStyle("-fx-background-color: " + UIHelper.PRIMARY + ";");
        refreshTable();
        return split;
    }

    private TableView<CustRow> buildTable() {
        TableView<CustRow> tv = new TableView<>(data);
        tv.setStyle(UIHelper.TABLE_STYLE);
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        tv.getColumns().addAll(
            col("ID",          "customerId",     80),
            col("Name",        "name",           160),
            col("Phone",       "phone",          130),
            col("Email",       "email",          200),
            col("Room",        "roomAllocated",   80),
            col("Check-In",    "checkIn",        110),
            col("Check-Out",   "checkOut",       110),
            col("Status",      "status",          90)
        );

        tv.setRowFactory(t -> new TableRow<>() {
            @Override
            protected void updateItem(CustRow item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) { setStyle(""); return; }
                setStyle("Checked In".equals(item.getStatus())
                    ? "-fx-background-color: #1e2e1e;"
                    : "-fx-background-color: #1e1e2e;");
            }
        });

        return tv;
    }

    private <T> TableColumn<CustRow, T> col(String name, String prop, int w) {
        TableColumn<CustRow, T> c = new TableColumn<>(name);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setMinWidth(w);
        return c;
    }

    public void refreshTable() {
        data.clear();
        String search = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        List<Customer> list = new ArrayList<>(ds.getCustomers());
        if (!search.isEmpty()) {
            list.removeIf(c -> !c.getName().toLowerCase().contains(search) &&
                               !c.getCustomerId().toLowerCase().contains(search));
        }
        for (Customer c : list) data.add(new CustRow(c));
    }

    
    public static class CustRow {
        private final String customerId;
        private final String name;
        private final String phone;
        private final String email;
        private final String roomAllocated;
        private final String checkIn;
        private final String checkOut;
        private final String status;

        public CustRow(Customer c) {
            this.customerId    = c.getCustomerId();
            this.name          = c.getName();
            this.phone         = c.getContactNumber();
            this.email         = c.getEmail();
            this.roomAllocated = c.hasRoom() ? String.valueOf(c.getAllocatedRoomNumber()) : "None";
            this.checkIn       = c.getCheckInDate()  != null ? c.getCheckInDate().toString()  : "—";
            this.checkOut      = c.getCheckOutDate() != null ? c.getCheckOutDate().toString() : "—";
            this.status        = c.hasRoom() ? "Checked In" : "Available";
        }

        public String getCustomerId()    { return customerId; }
        public String getName()          { return name; }
        public String getPhone()         { return phone; }
        public String getEmail()         { return email; }
        public String getRoomAllocated() { return roomAllocated; }
        public String getCheckIn()       { return checkIn; }
        public String getCheckOut()      { return checkOut; }
        public String getStatus()        { return status; }
    }
}
