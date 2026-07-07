package com.hotel.service;

import com.hotel.model.ServiceLog;
import com.hotel.model.ServiceLog.ServiceType;
import javafx.application.Platform;

import java.util.function.Consumer;

public class RoomService {

    private final DataStore   ds;
    private final FileService fs;

    private static final Object SERVICE_LOCK = new Object();

    public RoomService(DataStore ds, FileService fs) {
        this.ds = ds;
        this.fs = fs;
    }

    public void dispatchService(int roomNumber, ServiceType type,
                                Consumer<String> uiCallback) {
        ServiceLog log = new ServiceLog(ds.nextLogId(), roomNumber, type);
        ds.addLog(log);

        Runnable task = new ServiceTask(log, uiCallback);
        Thread t = new Thread(task);
        t.setDaemon(true);      // won't prevent JVM exit
        t.setName("Service-" + log.getLogId());
        t.start();
    }

    private class ServiceTask implements Runnable {

        private final ServiceLog       log;
        private final Consumer<String> callback;

        ServiceTask(ServiceLog log, Consumer<String> callback) {
            this.log      = log;
            this.callback = callback;
        }

        @Override
        public void run() {
            synchronized (SERVICE_LOCK) {
                try {
                    log.setStatus("IN_PROGRESS");
                    notify(log.getServiceType() + " started for Room " + log.getRoomNumber());

                    int duration = serviceDuration(log.getServiceType());
                    Thread.sleep(duration);

                    log.setStatus("DONE");
                    fs.appendAudit("Service complete: " + log);
                    fs.saveAll(ds);

                    String msg = "✔ " + log.getServiceType()
                                 + " completed for Room " + log.getRoomNumber();
                    notify(msg);
                    Platform.runLater(() -> callback.accept(msg));

                } catch (InterruptedException e) {
                    log.setStatus("INTERRUPTED");
                    Thread.currentThread().interrupt();
                    Platform.runLater(() ->
                        callback.accept("⚠ Service interrupted for Room " + log.getRoomNumber()));
                }
            }
        }

        private void notify(String msg) {
            System.out.println("[Thread:" + Thread.currentThread().getName() + "] " + msg);
        }

        private int serviceDuration(ServiceType type) {
            return switch (type) {
                case CLEANING       -> 3000;  // 3 sec
                case FOOD_DELIVERY  -> 2000;
                case LAUNDRY        -> 4000;
                case MAINTENANCE    -> 5000;
            };
        }
    }
}
