package com.hotel;

import com.hotel.gui.*;
import com.hotel.service.*;
import com.hotel.util.SampleDataLoader;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

public class MainApp extends Application {

    private DataStore      ds;
    private FileService    fs;
    private BookingService bookingService;
    private RoomService    roomService;

    private DashboardTab        dashboardTab;
    private RoomManagementTab   roomTab;
    private CustomerManagementTab custTab;
    private BookingTab          bookingTab;
    private BillingTab          billingTab;
    private ServiceTab          serviceTab;
    private ReportsTab          reportsTab;

    @Override
    public void start(Stage primaryStage) {
        ds             = DataStore.getInstance();
        fs             = new FileService();
        bookingService = new BookingService(ds, fs);
        roomService    = new RoomService(ds, fs);

        fs.loadAll(ds);
        if (ds.getRooms().isEmpty()) {
            SampleDataLoader.load(ds);
            fs.saveAll(ds);
            fs.appendAudit("Sample data loaded (first run).");
        }

        TabPane tabPane = buildTabPane();

        Label hotelName = new Label("Aadya Mukherjee's Project");
        hotelName.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;" +
                           "-fx-text-fill: #e94560;");
        Label systemName = new Label("Hotel Management System");
        systemName.setStyle("-fx-font-size: 11px; -fx-text-fill: #a0a0b0; -fx-padding: 0 0 0 4;");
        VBox brand = new VBox(1, hotelName, systemName);


        Label versionLbl = new Label("Java 17 + JavaFX  |  v1.0");
        versionLbl.setStyle("-fx-text-fill: #606070; -fx-font-size: 11px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox headerBar = new HBox(20, brand, spacer, versionLbl);
        headerBar.setPadding(new Insets(12, 20, 12, 20));
        headerBar.setStyle("-fx-background-color: #0d0d1e;" +
                           "-fx-border-color: #e94560; -fx-border-width: 0 0 2 0;");

        VBox root = new VBox(headerBar, tabPane);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        root.setStyle("-fx-background-color: #1a1a2e;");

        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, old, now) -> {
            if      (now == dashboardTab) dashboardTab.refresh();
            else if (now == roomTab)      roomTab.refreshTable();
            else if (now == custTab)      custTab.refreshTable();
            else if (now == bookingTab)   bookingTab.refreshTable();
            else if (now == billingTab)   billingTab.refresh();
            else if (now == serviceTab)   serviceTab.refreshTable();
            else if (now == reportsTab)   reportsTab.refresh();
        });

        Scene scene = new Scene(root, 1280, 780);
        scene.setFill(Color.web("#1a1a2e"));
        applyGlobalStyles(scene);

        primaryStage.setTitle("Hotel Management System");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> {
            fs.saveAll(ds);
            fs.appendAudit("Application closed. Data saved.");
        });
    }

    private TabPane buildTabPane() {
        dashboardTab = new DashboardTab(ds, fs);
        roomTab      = new RoomManagementTab(ds, fs);
        custTab      = new CustomerManagementTab(ds, fs);
        bookingTab   = new BookingTab(ds, fs, bookingService);
        billingTab   = new BillingTab(ds, fs);
        serviceTab   = new ServiceTab(ds, fs, roomService);
        reportsTab   = new ReportsTab(ds, fs);

        TabPane tp = new TabPane(dashboardTab, roomTab, custTab,
                                 bookingTab, billingTab, serviceTab, reportsTab);
        tp.setTabMinWidth(100);
        tp.setStyle(
            "-fx-background-color: #1a1a2e;" +
            "-fx-tab-min-height: 38px;"
        );
        return tp;
    }

    private void applyGlobalStyles(Scene scene) {
        scene.getStylesheets().add(
            "data:text/css," +
            ".tab-pane > .tab-header-area > .tab-header-background {" +
            "  -fx-background-color: #0d0d1e;" +
            "}" +
            ".tab {" +
            "  -fx-background-color: #16213e;" +
            "  -fx-text-base-color: #a0a0b0;" +
            "  -fx-font-size: 13px;" +
            "  -fx-font-weight: bold;" +
            "  -fx-padding: 8 16;" +
            "}" +
            ".tab:selected {" +
            "  -fx-background-color: #1a1a2e;" +
            "  -fx-text-base-color: #e94560;" +
            "}" +
            ".tab:hover {" +
            "  -fx-background-color: #1e2140;" +
            "}" +
            ".tab-pane > .tab-header-area {" +
            "  -fx-background-color: #0d0d1e;" +
            "}" +
            ".table-view .column-header {" +
            "  -fx-background-color: #0f3460;" +
            "  -fx-text-fill: #eaeaea;" +
            "  -fx-font-weight: bold;" +
            "  -fx-font-size: 12px;" +
            "}" +
            ".table-view .column-header-background {" +
            "  -fx-background-color: #0f3460;" +
            "}" +
            ".table-row-cell {" +
            "  -fx-text-fill: #eaeaea;" +
            "  -fx-border-color: transparent transparent #252845 transparent;" +
            "}" +
            ".table-row-cell:selected {" +
            "  -fx-background-color: #3a3d80;" +
            "}" +
            ".table-view .table-cell {" +
            "  -fx-text-fill: #eaeaea;" +
            "  -fx-font-size: 12px;" +
            "  -fx-alignment: center;" +
            "}" +
            ".scroll-bar:vertical .track," +
            ".scroll-bar:horizontal .track {" +
            "  -fx-background-color: #16213e;" +
            "}" +
            ".scroll-bar:vertical .thumb," +
            ".scroll-bar:horizontal .thumb {" +
            "  -fx-background-color: #3a3d60;" +
            "  -fx-background-radius: 4;" +
            "}" +
            ".date-picker .arrow-button {" +
            "  -fx-background-color: #0f3460;" +
            "}" +
            ".combo-box-popup .list-view {" +
            "  -fx-background-color: #1e2140;" +
            "}" +
            ".combo-box-popup .list-cell {" +
            "  -fx-text-fill: #eaeaea;" +
            "  -fx-background-color: #1e2140;" +
            "}" +
            ".combo-box-popup .list-cell:hover {" +
            "  -fx-background-color: #3a3d80;" +
            "}" +
            ".split-pane-divider {" +
            "  -fx-background-color: #252845;" +
            "  -fx-padding: 0 1;" +
            "}"
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}
