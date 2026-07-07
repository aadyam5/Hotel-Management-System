package com.hotel.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Booking — represents a reservation record.
 */
public class Booking implements Serializable {

    private static final long serialVersionUID = 6L;

    private String    bookingId;
    private String    customerId;
    private int       roomNumber;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private double    totalBill;
    private double    additionalServices;
    private String    status;          // ACTIVE, CHECKED_OUT
    private LocalDateTime createdAt;

    public Booking(String bookingId, String customerId, int roomNumber,
                   LocalDate checkIn, LocalDate checkOut, double additionalServices) {
        this.bookingId          = bookingId;
        this.customerId         = customerId;
        this.roomNumber         = roomNumber;
        this.checkIn            = checkIn;
        this.checkOut           = checkOut;
        this.additionalServices = additionalServices;
        this.status             = "ACTIVE";
        this.createdAt          = LocalDateTime.now();
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public String        getBookingId()          { return bookingId; }
    public String        getCustomerId()          { return customerId; }
    public int           getRoomNumber()          { return roomNumber; }
    public LocalDate     getCheckIn()             { return checkIn; }
    public LocalDate     getCheckOut()            { return checkOut; }
    public double        getTotalBill()           { return totalBill; }
    public double        getAdditionalServices()  { return additionalServices; }
    public String        getStatus()              { return status; }
    public LocalDateTime getCreatedAt()           { return createdAt; }

    public int getDays() {
        return (int) java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    // ── Setters ──────────────────────────────────────────────────────────────
    public void setTotalBill(double bill)          { this.totalBill = bill; }
    public void setStatus(String status)           { this.status = status; }
    public void setAdditionalServices(double cost) { this.additionalServices = cost; }

    @Override
    public String toString() {
        return String.format("Booking[%s | Customer:%s | Room:%d | %s→%s | ₹%.2f | %s]",
                bookingId, customerId, roomNumber, checkIn, checkOut, totalBill, status);
    }
}
