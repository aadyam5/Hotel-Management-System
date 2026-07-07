package com.hotel.service;

import com.hotel.model.*;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FileService {

    private static final String DATA_DIR     = "hms_data";
    private static final String ROOMS_FILE   = DATA_DIR + "/rooms.ser";
    private static final String CUSTOMERS_FILE = DATA_DIR + "/customers.ser";
    private static final String BOOKINGS_FILE  = DATA_DIR + "/bookings.ser";
    private static final String LOGS_FILE      = DATA_DIR + "/logs.ser";
    private static final String REPORT_FILE    = DATA_DIR + "/report.txt";
    private static final String AUDIT_FILE     = DATA_DIR + "/audit.log";

    public FileService() {
        new File(DATA_DIR).mkdirs();
    }


    public <T extends Serializable> void saveList(List<T> list, String filePath) {
        try (ObjectOutputStream oos =
                 new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(list);
        } catch (IOException e) {
            System.err.println("Save error [" + filePath + "]: " + e.getMessage());
        }
    }

    
    @SuppressWarnings("unchecked")
    public <T> List<T> loadList(String filePath) {
        File f = new File(filePath);
        if (!f.exists()) return new ArrayList<>();
        try (ObjectInputStream ois =
                 new ObjectInputStream(new FileInputStream(f))) {
            return (List<T>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Load error [" + filePath + "]: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void saveAll(DataStore ds) {
        saveList(ds.getRooms(),     ROOMS_FILE);
        saveList(ds.getCustomers(), CUSTOMERS_FILE);
        saveList(ds.getBookings(),  BOOKINGS_FILE);
        saveList(ds.getLogs(),      LOGS_FILE);
        appendAudit("Data saved successfully.");
    }

    @SuppressWarnings("unchecked")
    public void loadAll(DataStore ds) {
        List<Room<?, ?>> rooms     = (List<Room<?, ?>>) (List<?>) loadList(ROOMS_FILE);
        List<Customer>   customers = loadList(CUSTOMERS_FILE);
        List<Booking>    bookings  = loadList(BOOKINGS_FILE);
        List<ServiceLog> logs      = loadList(LOGS_FILE);
        ds.loadAll(rooms, customers, bookings, logs);
        appendAudit("Data loaded successfully.");
    }

    
    public void generateTextReport(DataStore ds) {
        try (FileWriter fw = new FileWriter(REPORT_FILE);
             BufferedWriter bw = new BufferedWriter(fw)) {

            String ts = LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
            bw.write("       HOTEL MANAGEMENT SYSTEM REPORT\n");
            bw.write("       Generated: " + ts + "\n");

            // Rooms
            bw.write("─ ROOMS (" + ds.getRooms().size() + ") ──\n");
            for (Room<?, ?> r : ds.getRooms()) { bw.write("  " + r + "\n"); }

            bw.write("\n─ CUSTOMERS (" + ds.getCustomers().size() + ") ──\n");
            for (Customer c : ds.getCustomers()) { bw.write("  " + c + "\n"); }

            bw.write("\n── BOOKINGS (" + ds.getBookings().size() + ") ──\n");
            for (Booking b : ds.getBookings()) { bw.write("  " + b + "\n"); }

            bw.write("\n── REVENUE ──\n");
            bw.write(String.format("  Total Revenue: ₹%.2f%n", ds.totalRevenue()));

            bw.write("\n════════════════════════════════════════════════\n");
        } catch (IOException e) {
            System.err.println("Report write error: " + e.getMessage());
        }
    }

    public String readReport() {
        StringBuilder sb = new StringBuilder();
        File f = new File(REPORT_FILE);
        if (!f.exists()) return "No report generated yet.";
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            return "Error reading report: " + e.getMessage();
        }
        return sb.toString();
    }

    public void appendAudit(String message) {
        try (RandomAccessFile raf = new RandomAccessFile(AUDIT_FILE, "rw")) {
            raf.seek(raf.length()); // jump to end
            String entry = LocalDateTime.now()
                               .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))
                           + " | " + message + "\n";
            raf.writeBytes(entry);
        } catch (IOException e) {
            System.err.println("Audit write error: " + e.getMessage());
        }
    }

    public String readAuditLog() {
        StringBuilder sb = new StringBuilder();
        File f = new File(AUDIT_FILE);
        if (!f.exists()) return "Audit log is empty.";
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
        } catch (IOException e) {
            return "Error reading audit: " + e.getMessage();
        }
        return sb.toString();
    }

    public String getReportPath() { return new File(REPORT_FILE).getAbsolutePath(); }
}
