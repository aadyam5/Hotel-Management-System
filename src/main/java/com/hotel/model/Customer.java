package com.hotel.model;

import java.io.Serializable;
import java.time.LocalDate;

public class Customer implements Serializable {

    private static final long serialVersionUID = 5L;

    private String customerId;
    private String name;
    private String contactNumber;
    private String email;
    private int    allocatedRoomNumber; 
    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    public Customer(String customerId, String name, String contactNumber, String email) {
        this.customerId          = customerId;
        this.name                = name;
        this.contactNumber       = contactNumber;
        this.email               = email;
        this.allocatedRoomNumber = -1;
    }

    public String    getCustomerId()          { return customerId; }
    public String    getName()                { return name; }
    public String    getContactNumber()       { return contactNumber; }
    public String    getEmail()               { return email; }
    public int       getAllocatedRoomNumber()  { return allocatedRoomNumber; }
    public LocalDate getCheckInDate()         { return checkInDate; }
    public LocalDate getCheckOutDate()        { return checkOutDate; }

    public void setName(String name)                       { this.name = name; }
    public void setContactNumber(String c)                 { this.contactNumber = c; }
    public void setEmail(String email)                     { this.email = email; }
    public void setAllocatedRoomNumber(int room)           { this.allocatedRoomNumber = room; }
    public void setCheckInDate(LocalDate d)                { this.checkInDate = d; }
    public void setCheckOutDate(LocalDate d)               { this.checkOutDate = d; }

    public boolean hasRoom() { return allocatedRoomNumber != -1; }

    public int getDaysOfStay() {
        if (checkInDate == null || checkOutDate == null) return 0;
        return (int) java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    @Override
    public String toString() {
        return String.format("Customer[%s | %s | Room:%s]",
                customerId, name,
                allocatedRoomNumber == -1 ? "None" : String.valueOf(allocatedRoomNumber));
    }
}
