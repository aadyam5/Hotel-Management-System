package com.hotel.service;

import com.hotel.model.*;

import java.time.LocalDate;
import java.util.Optional;

public class BookingService {

    private final DataStore   ds;
    private final FileService fs;

    private static final Object BOOKING_LOCK = new Object();

    public BookingService(DataStore ds, FileService fs) {
        this.ds = ds;
        this.fs = fs;
    }

    public Optional<Booking> bookRoom(String customerId, int roomNumber,
                                      LocalDate checkIn, LocalDate checkOut,
                                      double additionalServices) {
        synchronized (BOOKING_LOCK) {
            Optional<Customer> custOpt = ds.findCustomer(customerId);
            if (custOpt.isEmpty()) return Optional.empty();
            Customer customer = custOpt.get();

            if (customer.hasRoom()) return Optional.empty(); 

            Optional<Room<?, ?>> roomOpt = ds.findRoom(roomNumber);
            if (roomOpt.isEmpty()) return Optional.empty();
            Room<?, ?> room = roomOpt.get();

            if (!room.isAvailable()) return Optional.empty(); 

            
            int days = (int) java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
            if (days <= 0) return Optional.empty();

            double totalBill = room.calculateTariff(days, additionalServices);

            
            String bookingId = ds.nextBookingId();
            Booking booking  = new Booking(bookingId, customerId, roomNumber,
                                           checkIn, checkOut, additionalServices);
            booking.setTotalBill(totalBill);

            room.setAvailable(false);
            customer.setAllocatedRoomNumber(roomNumber);
            customer.setCheckInDate(checkIn);
            customer.setCheckOutDate(checkOut);

            ds.addBooking(booking);
            ds.getRoomCustomerMap().put(roomNumber, customer);

            fs.saveAll(ds);
            fs.appendAudit("Booking " + bookingId + " created for customer " + customerId
                           + " | Room " + roomNumber + " | ₹" + totalBill);

            try { Thread.sleep(200); } 
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }

            return Optional.of(booking);
        }
    }

    public Optional<Booking> checkout(String customerId) {
        synchronized (BOOKING_LOCK) {
            Optional<Customer> custOpt = ds.findCustomer(customerId);
            if (custOpt.isEmpty()) return Optional.empty();
            Customer customer = custOpt.get();

            if (!customer.hasRoom()) return Optional.empty();

            int roomNumber = customer.getAllocatedRoomNumber();

            Optional<Booking> bkOpt = ds.findActiveBookingByCustomer(customerId);
            if (bkOpt.isEmpty()) return Optional.empty();
            Booking booking = bkOpt.get();

            ds.findRoom(roomNumber).ifPresent(r -> r.setAvailable(true));

            booking.setStatus("CHECKED_OUT");
            customer.setAllocatedRoomNumber(-1);
            customer.setCheckInDate(null);
            customer.setCheckOutDate(null);
            ds.getRoomCustomerMap().remove(roomNumber);

            fs.saveAll(ds);
            fs.appendAudit("Checkout: Booking " + booking.getBookingId()
                           + " | Customer " + customerId + " | Room " + roomNumber);

            return Optional.of(booking);
        }
    }
}
