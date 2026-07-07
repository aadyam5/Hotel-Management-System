package com.hotel.model;

import java.io.Serializable;

public abstract class Room<T, U extends Number> implements Serializable, Amenities {

    private static final long serialVersionUID = 1L;

    
    private int roomNumber;
    private T roomType;
    private U pricePerDay;
    private boolean available;
    private String description;

    public Room(int roomNumber, T roomType, U pricePerDay, String description) {
        this.roomNumber  = roomNumber;
        this.roomType    = roomType;
        this.pricePerDay = pricePerDay;
        this.available   = true;
        this.description = description;
    }

    
    public abstract double calculateTariff(int days, double additionalCost);

    public int    getRoomNumber()  { return roomNumber; }
    public T      getRoomType()    { return roomType; }
    public U      getPricePerDay() { return pricePerDay; }
    public boolean isAvailable()  { return available; }
    public String  getDescription(){ return description; }

    public void setAvailable(boolean available) { this.available = available; }
    public void setPricePerDay(U price)          { this.pricePerDay = price; }
    public void setDescription(String desc)      { this.description = desc; }

    @Override
    public String toString() {
        return String.format("Room[%d | %s | ₹%.2f/day | %s]",
                roomNumber, roomType,
                pricePerDay.doubleValue(),
                available ? "Available" : "Booked");
    }

    public static <E extends Room<?, ?>> void display(E room) {
        System.out.println(room);
    }
}
