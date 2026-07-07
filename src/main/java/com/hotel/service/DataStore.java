package com.hotel.service;

import com.hotel.model.*;

import java.util.*;


public class DataStore {

    private static DataStore instance;

    private final ArrayList<Room<?, ?>>     rooms     = new ArrayList<>();
    private final ArrayList<Customer>       customers = new ArrayList<>();
    private final ArrayList<Booking>        bookings  = new ArrayList<>();
    private final ArrayList<ServiceLog>     logs      = new ArrayList<>();

    private final HashMap<Integer, Customer> roomCustomerMap = new HashMap<>();

    private DataStore() {}

    public static synchronized DataStore getInstance() {
        if (instance == null) instance = new DataStore();
        return instance;
    }

    public ArrayList<Room<?, ?>>    getRooms()          { return rooms; }
    public HashMap<Integer, Customer> getRoomCustomerMap() { return roomCustomerMap; }

    public void addRoom(Room<?, ?> room) {
        rooms.add(room);
    }

    public Optional<Room<?, ?>> findRoom(int number) {
        Iterator<Room<?, ?>> it = rooms.iterator();
        while (it.hasNext()) {
            Room<?, ?> r = it.next();
            if (r.getRoomNumber() == number) return Optional.of(r);
        }
        return Optional.empty();
    }

    public boolean removeRoom(int number) {
        Iterator<Room<?, ?>> it = rooms.iterator();
        while (it.hasNext()) {
            if (it.next().getRoomNumber() == number) { it.remove(); return true; }
        }
        return false;
    }

    public List<Room<?, ?>> availableRooms() {
        List<Room<?, ?>> list = new ArrayList<>();
        for (Room<?, ?> r : rooms) { if (r.isAvailable()) list.add(r); }
        return list;
    }

    public ArrayList<Customer> getCustomers() { return customers; }

    public void addCustomer(Customer c) { customers.add(c); }

    public Optional<Customer> findCustomer(String id) {
        for (Customer c : customers) {
            if (c.getCustomerId().equalsIgnoreCase(id)) return Optional.of(c);
        }
        return Optional.empty();
    }

    public boolean removeCustomer(String id) {
        Iterator<Customer> it = customers.iterator();
        while (it.hasNext()) {
            if (it.next().getCustomerId().equalsIgnoreCase(id)) { it.remove(); return true; }
        }
        return false;
    }

    public ArrayList<Booking>  getBookings() { return bookings; }

    public void addBooking(Booking b) { bookings.add(b); }

    public Optional<Booking> findActiveBookingByRoom(int roomNumber) {
        for (Booking b : bookings) {
            if (b.getRoomNumber() == roomNumber && "ACTIVE".equals(b.getStatus()))
                return Optional.of(b);
        }
        return Optional.empty();
    }

    public Optional<Booking> findActiveBookingByCustomer(String customerId) {
        for (Booking b : bookings) {
            if (b.getCustomerId().equals(customerId) && "ACTIVE".equals(b.getStatus()))
                return Optional.of(b);
        }
        return Optional.empty();
    }

    public double totalRevenue() {
        double sum = 0;
        for (Booking b : bookings) {
            if ("CHECKED_OUT".equals(b.getStatus())) sum += b.getTotalBill();
        }
        return sum;
    }

    public ArrayList<ServiceLog> getLogs() { return logs; }
    public void addLog(ServiceLog log)     { logs.add(log); }

    public void sortRoomsByPrice() {
        rooms.sort(Comparator.comparingDouble(r -> r.getPricePerDay().doubleValue()));
    }

    public void sortRoomsByNumber() {
        rooms.sort(Comparator.comparingInt(Room::getRoomNumber));
    }

    public String nextCustomerId() {
        return "C" + String.format("%03d", customers.size() + 1);
    }

    public String nextBookingId() {
        return "B" + String.format("%04d", bookings.size() + 1);
    }

    public String nextLogId() {
        return "S" + String.format("%04d", logs.size() + 1);
    }

    public void loadAll(List<Room<?, ?>> r, List<Customer> c,
                        List<Booking> b, List<ServiceLog> l) {
        rooms.clear();     rooms.addAll(r);
        customers.clear(); customers.addAll(c);
        bookings.clear();  bookings.addAll(b);
        logs.clear();      logs.addAll(l);
        rebuildMap();
    }

    public void rebuildMap() {
        roomCustomerMap.clear();
        for (Booking bk : bookings) {
            if ("ACTIVE".equals(bk.getStatus())) {
                findCustomer(bk.getCustomerId()).ifPresent(
                    c -> roomCustomerMap.put(bk.getRoomNumber(), c));
            }
        }
    }
}
