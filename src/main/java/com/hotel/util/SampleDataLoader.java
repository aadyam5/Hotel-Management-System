package com.hotel.util;

import com.hotel.model.*;
import com.hotel.service.DataStore;

/**
 * SampleDataLoader — populates DataStore with demo data for testing.
 */
public class SampleDataLoader {

    public static void load(DataStore ds) {
        // ── Rooms ──────────────────────────────────────────────────────────────
        ds.addRoom(new StandardRoom(101, 1500.0));
        ds.addRoom(new StandardRoom(102, 1500.0));
        ds.addRoom(new StandardRoom(103, 1800.0));
        ds.addRoom(new DeluxeRoom  (201, 3000.0));
        ds.addRoom(new DeluxeRoom  (202, 3200.0));
        ds.addRoom(new DeluxeRoom  (203, 3500.0));
        ds.addRoom(new LuxuryRoom  (301, 7000.0));
        ds.addRoom(new LuxuryRoom  (302, 8500.0));
        ds.addRoom(new LuxuryRoom  (303, 10000.0));

        // ── Customers ──────────────────────────────────────────────────────────
        ds.addCustomer(new Customer("C001", "Arjun Sharma",    "9876543210", "arjun@email.com"));
        ds.addCustomer(new Customer("C002", "Priya Menon",     "8765432109", "priya@email.com"));
        ds.addCustomer(new Customer("C003", "Rahul Nair",      "7654321098", "rahul@email.com"));
        ds.addCustomer(new Customer("C004", "Divya Krishnan",  "6543210987", "divya@email.com"));
        ds.addCustomer(new Customer("C005", "Vikram Patel",    "9988776655", "vikram@email.com"));
    }
}
