package com.hotel.model;

public class StandardRoom extends Room<RoomType, Double> {

    private static final long serialVersionUID = 2L;

    public StandardRoom(int roomNumber, double pricePerDay) {
        super(roomNumber, RoomType.STANDARD, pricePerDay,
              "Comfortable standard room with essential amenities.");
    }

    @Override
    public double calculateTariff(int days, double additionalCost) {
        
        Double price = getPricePerDay();          
        double base  = price * days;              
        return base + additionalCost;
    }

    
    @Override public boolean provideWifi()        { return true;  }
    @Override public boolean provideBreakfast()   { return false; }
    @Override public boolean providePool()        { return false; }
    @Override public boolean provideRoomService() { return false; }
}
