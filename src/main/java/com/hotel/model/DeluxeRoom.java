package com.hotel.model;

public class DeluxeRoom extends Room<RoomType, Double> {

    private static final long serialVersionUID = 3L;
    private static final double SERVICE_CHARGE = 0.10; 

    public DeluxeRoom(int roomNumber, double pricePerDay) {
        super(roomNumber, RoomType.DELUXE, pricePerDay,
              "Spacious deluxe room with breakfast and premium bedding.");
    }

    @Override
    public double calculateTariff(int days, double additionalCost) {
        Double price = getPricePerDay();
        double base  = price * days;
        double withService = base * (1 + SERVICE_CHARGE);
        return withService + additionalCost;
    }

    @Override public boolean provideWifi()        { return true; }
    @Override public boolean provideBreakfast()   { return true; }
    @Override public boolean providePool()        { return false;}
    @Override public boolean provideRoomService() { return true; }
}
