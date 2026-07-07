package com.hotel.model;

public enum RoomType {
    STANDARD("Standard"),
    DELUXE("Deluxe"),
    SUITE("Suite");

    private final String displayName;

    RoomType(String displayName) { this.displayName = displayName; }

    public String getDisplayName() { return displayName; }

    @Override
    public String toString() { return displayName; }
}
