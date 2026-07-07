package com.hotel.model;

public class LuxuryRoom extends Room<RoomType, Double> {

    private static final long serialVersionUID = 4L;
    private static final double LUXURY_TAX = 0.20; // 20%

    public LuxuryRoom(int roomNumber, double pricePerDay) {
        super(roomNumber, RoomType.SUITE, pricePerDay,
              "Opulent luxury suite with all amenities, pool access, and butler service.");
    }

    @Override
    public double calculateTariff(int days, double additionalCost) {
        Double price = getPricePerDay();
        double base  = price * days;
        double taxed = base * (1 + LUXURY_TAX);
        return taxed + additionalCost;
    }

    @Override public boolean provideWifi()        { return true; }
    @Override public boolean provideBreakfast()   { return true; }
    @Override public boolean providePool()        { return true; }
    @Override public boolean provideRoomService() { return true; }
}
