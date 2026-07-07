package com.hotel.gui;

import com.hotel.model.*;
import com.hotel.service.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.Comparator;
import java.util.List;

public class RoomManagementTab extends Tab {

    private final DataStore   ds;
    private final FileService fs;

    private TableView<RoomRow> table;
    private ObservableList<RoomRow> data = FXCollections.observableArrayList();

    private TextField searchField;
    private ComboBox<String> filterCombo;
    private ComboBox<String> sortCombo;

    public RoomManagementTab(DataStore ds, FileService fs) {
        super("🛏 Rooms");
        this.ds = ds;
        this.fs = fs;
        setClosable(false);
        setContent(buildContent());
    }

    private SplitPane buildContent() {
        Label formTitle = UIHelper.sectionTitle("Add New Room");

        TextField roomNumField  = UIHelper.field("Room Number (e.g. 104)");
        ComboBox<String> typeCombo = UIHelper.combo();
        typeCombo.getItems().addAll("Standard", "Deluxe", "Suite");
        typeCombo.setPromptText("Room Type");
        TextField priceField    = UIHelper.field("Price per Day (₹)");
        TextField descField     = UIHelper.field("Description (optional)");

        Button addBtn = UIHelper.btn("+ Add Room", UIHelper.BTN_PRIMARY);
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> {
            try {
                int    num   = Integer.parseInt(roomNumField.getText().trim());
                String type  = typeCombo.getValue();
                double price = Double.parseDouble(priceField.getText().trim());

                if (type == null) { UIHelper.alertError("Error", "Select room type."); return; }
                if (ds.findRoom(num).isPresent()) {
                    UIHelper.alertError("Error", "Room " + num + " already exists."); return;
                }

                Room<?, ?> room = switch (type) {
                    case "Standard" -> new StandardRoom(num, price);
                    case "Deluxe"   -> new DeluxeRoom(num, price);
                    case "Suite"    -> new LuxuryRoom(num, price);
                    default -> throw new IllegalArgumentException("Unknown type");
                };
                if (!descField.getText().isBlank()) room.setDescription(descField.getText().trim());

                ds.addRoom(room);
                fs.saveAll(ds);
                fs.appendAudit("Room " + num + " (" + type + ") added at ₹" + price + "/day");

                roomNumField.clear(); typeCombo.setValue(null);
                priceField.clear(); descField.clear();
                UIHelper.alertInfo("Success", "Room " + num + " added successfully.");
                refreshTable();
            } catch (NumberFormatException ex) {
                UIHelper.alertError("Error", "Invalid number or price.");
            }
        });

        Button removeBtn = UIHelper.btn("Remove Selected", UIHelper.BTN_WARNING);
        removeBtn.setMaxWidth(Double.MAX_VALUE);
        removeBtn.setOnAction(e -> {
            RoomRow sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { UIHelper.alertError("Error", "Select a room first."); return; }
            if (!sel.getAvailability().equals("Available")) {
                UIHelper.alertError("Error", "Cannot remove a booked room."); return;
            }
            ds.removeRoom(sel.getRoomNumber());
            fs.saveAll(ds);
            UIHelper.alertInfo("Removed", "Room " + sel.getRoomNumber() + " removed.");
            refreshTable();
        });

        Button amenitiesBtn = UIHelper.btn("View Amenities", UIHelper.BTN_NEUTRAL);
        amenitiesBtn.setMaxWidth(Double.MAX_VALUE);
        amenitiesBtn.setOnAction(e -> {
            RoomRow sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { UIHelper.alertError("Error", "Select a room first."); return; }
            ds.findRoom(sel.getRoomNumber()).ifPresent(r -> {
                UIHelper.alertInfo("Amenities — Room " + r.getRoomNumber(),
                    "Type: " + r.getRoomType() +
                    "\nWi-Fi: " + (r.provideWifi() ? "✔" : "✘") +
                    "\nBreakfast: " + (r.provideBreakfast() ? "✔" : "✘") +
                    "\nPool: " + (r.providePool() ? "✔" : "✘") +
                    "\nRoom Service: " + (r.provideRoomService() ? "✔" : "✘") +
                    "\n\nAll Amenities: " + r.amenitiesSummary() +
                    "\nDescription: " + r.getDescription());
            });
        });

        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(12);
        form.add(UIHelper.label("Room Number:"), 0, 0); form.add(roomNumField, 1, 0);
        form.add(UIHelper.label("Type:"),        0, 1); form.add(typeCombo,    1, 1);
        form.add(UIHelper.label("Price/Day:"),   0, 2); form.add(priceField,   1, 2);
        form.add(UIHelper.label("Description:"), 0, 3); form.add(descField,    1, 3);

        VBox leftPane = UIHelper.card(formTitle, UIHelper.separator(),
                form, UIHelper.separator(),
                addBtn, removeBtn, amenitiesBtn);
        leftPane.setPrefWidth(300);
        leftPane.setSpacing(12);

        searchField = UIHelper.field("Search room number...");
        filterCombo = UIHelper.combo();
        filterCombo.getItems().addAll("All", "Available", "Booked",
                                      "Standard", "Deluxe", "Suite");
        filterCombo.setValue("All");

        sortCombo = UIHelper.combo();
        sortCombo.getItems().addAll("Room Number ↑", "Price ↑", "Price ↓");
        sortCombo.setValue("Room Number ↑");

        Button applyBtn = UIHelper.btn("Apply", UIHelper.BTN_NEUTRAL);
        applyBtn.setOnAction(e -> refreshTable());
        Button clearBtn = UIHelper.btn("Clear", UIHelper.BTN_WARNING);
        clearBtn.setOnAction(e -> {
            searchField.clear(); filterCombo.setValue("All");
            sortCombo.setValue("Room Number ↑"); refreshTable();
        });

        HBox filterBar = new HBox(10, UIHelper.label("Search:"), searchField,
                UIHelper.label("Filter:"), filterCombo,
                UIHelper.label("Sort:"), sortCombo,
                applyBtn, clearBtn);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(5));

        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        Label tableTitle = UIHelper.sectionTitle("Room List");
        VBox rightPane = new VBox(12, tableTitle, filterBar, table);
        rightPane.setPadding(new Insets(20));
        rightPane.setStyle("-fx-background-color: " + UIHelper.PRIMARY + ";");
        VBox.setVgrow(table, Priority.ALWAYS);

        SplitPane split = new SplitPane(leftPane, rightPane);
        split.setDividerPositions(0.28);
        split.setStyle("-fx-background-color: " + UIHelper.PRIMARY + ";");
        refreshTable();
        return split;
    }

    private TableView<RoomRow> buildTable() {
        TableView<RoomRow> tv = new TableView<>(data);
        tv.setStyle(UIHelper.TABLE_STYLE);
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        tv.getColumns().addAll(
            col("Room #",       "roomNumber",   80),
            col("Type",         "roomType",     100),
            col("Price/Day",    "pricePerDay",  110),
            col("Availability", "availability", 110),
            col("Description",  "description",  260),
            col("Amenities",    "amenities",    230)
        );

        tv.setRowFactory(t -> new TableRow<>() {
            @Override
            protected void updateItem(RoomRow item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) { setStyle(""); return; }
                setStyle("Available".equals(item.getAvailability())
                    ? "-fx-background-color: #1a2e20;"
                    : "-fx-background-color: #2e1a20;");
            }
        });

        return tv;
    }

    private <T> TableColumn<RoomRow, T> col(String name, String prop, int w) {
        TableColumn<RoomRow, T> c = new TableColumn<>(name);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setMinWidth(w);
        c.setStyle("-fx-text-fill: " + UIHelper.TEXT_LIGHT + "; -fx-alignment: CENTER;");
        return c;
    }

    public void refreshTable() {
        data.clear();
        String search = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        String filter = filterCombo != null ? filterCombo.getValue() : "All";
        String sort   = sortCombo   != null ? sortCombo.getValue()   : "Room Number ↑";

        List<Room<?, ?>> list = new java.util.ArrayList<>(ds.getRooms());

        list.removeIf(r -> {
            if (!search.isEmpty() && !String.valueOf(r.getRoomNumber()).contains(search)) return true;
            return switch (filter) {
                case "Available" -> !r.isAvailable();
                case "Booked"    ->  r.isAvailable();
                case "Standard"  -> !r.getRoomType().toString().equals("Standard");
                case "Deluxe"    -> !r.getRoomType().toString().equals("Deluxe");
                case "Suite"     -> !r.getRoomType().toString().equals("Suite");
                default -> false;
            };
        });

        Comparator<Room<?, ?>> cmp = switch (sort) {
            case "Price ↑"        -> Comparator.comparingDouble(r -> r.getPricePerDay().doubleValue());
            case "Price ↓"        -> Comparator.<Room<?,?>,Double>comparing(
                                        r -> r.getPricePerDay().doubleValue()).reversed();
            default               -> Comparator.comparingInt(Room::getRoomNumber);
        };
        list.sort(cmp);

        for (Room<?, ?> r : list) {
            data.add(new RoomRow(r));
        }
    }

    public static class RoomRow {
        private final int    roomNumber;
        private final String roomType;
        private final String pricePerDay;
        private final String availability;
        private final String description;
        private final String amenities;

        public RoomRow(Room<?, ?> r) {
            this.roomNumber   = r.getRoomNumber();
            this.roomType     = r.getRoomType().toString();
            this.pricePerDay  = "₹" + String.format("%.0f", r.getPricePerDay().doubleValue());
            this.availability = r.isAvailable() ? "Available" : "Booked";
            this.description  = r.getDescription();
            this.amenities    = r.amenitiesSummary();
        }

        public int    getRoomNumber()   { return roomNumber; }
        public String getRoomType()     { return roomType; }
        public String getPricePerDay()  { return pricePerDay; }
        public String getAvailability() { return availability; }
        public String getDescription()  { return description; }
        public String getAmenities()    { return amenities; }
    }
}
