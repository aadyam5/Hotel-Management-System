package com.hotel.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ServiceLog implements Serializable {

    private static final long serialVersionUID = 7L;

    public enum ServiceType { CLEANING, FOOD_DELIVERY, LAUNDRY, MAINTENANCE }

    private String      logId;
    private int         roomNumber;
    private ServiceType serviceType;
    private String      status;   
    private LocalDateTime timestamp;
    private String      notes;

    public ServiceLog(String logId, int roomNumber, ServiceType serviceType) {
        this.logId       = logId;
        this.roomNumber  = roomNumber;
        this.serviceType = serviceType;
        this.status      = "PENDING";
        this.timestamp   = LocalDateTime.now();
    }

    public String      getLogId()       { return logId; }
    public int         getRoomNumber()  { return roomNumber; }
    public ServiceType getServiceType() { return serviceType; }
    public String      getStatus()      { return status; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String      getNotes()       { return notes; }

    public void setStatus(String status)     { this.status    = status; }
    public void setNotes(String notes)       { this.notes     = notes; }
    public void setTimestamp(LocalDateTime t){ this.timestamp = t; }

    @Override
    public String toString() {
        return String.format("[%s] Room %d — %s (%s)", timestamp.toLocalTime(),
                roomNumber, serviceType, status);
    }
}
