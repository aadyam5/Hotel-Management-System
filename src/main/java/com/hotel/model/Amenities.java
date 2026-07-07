package com.hotel.model;

public interface Amenities {

    boolean provideWifi();

    boolean provideBreakfast();

    boolean providePool();

    boolean provideRoomService();

    default String amenitiesSummary() {
        StringBuilder sb = new StringBuilder();
        if (provideWifi())        sb.append("WiFi  ");
        if (provideBreakfast())   sb.append("Breakfast  ");
        if (providePool())        sb.append("Pool  ");
        if (provideRoomService()) sb.append("Room Service");
        return sb.toString().trim().isEmpty() ? "None" : sb.toString().trim();
    }
}
