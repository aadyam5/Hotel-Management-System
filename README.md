# Grand Horizon — Hotel Management System (JavaFX)

A complete, standalone Hotel Management System desktop application built with
**Java 17 + JavaFX 21**, demonstrating all core Java concepts in a real-world system.

---

## Project Structure

```
HMS/
├── pom.xml                                 ← Maven build file
└── src/main/java/com/hotel/
    ├── MainApp.java                        ← JavaFX entry point (Stage, Scene)
    ├── model/
    │   ├── Amenities.java                  ← Interface (provideWifi, provideBreakfast, ...)
    │   ├── Room.java                       ← Abstract generic class Room<T, U>
    │   ├── RoomType.java                   ← Enum (STANDARD, DELUXE, SUITE)
    │   ├── StandardRoom.java               ← Concrete: extends Room, overrides calculateTariff()
    │   ├── DeluxeRoom.java                 ← Concrete: +10% service charge
    │   ├── LuxuryRoom.java                 ← Concrete: +20% luxury tax
    │   ├── Customer.java                   ← Encapsulated customer data
    │   ├── Booking.java                    ← Booking record with status
    │   └── ServiceLog.java                 ← Room service log entry
    ├── service/
    │   ├── DataStore.java                  ← Singleton: ArrayList, HashMap, Iterator
    │   ├── FileService.java                ← Serialization + FileWriter + RandomAccessFile
    │   ├── BookingService.java             ← Synchronized booking/checkout logic
    │   └── RoomService.java               ← Thread-based service simulation
    ├── gui/
    │   ├── UIHelper.java                   ← Shared styles, factory methods
    │   ├── DashboardTab.java               ← Overview stats + activity log
    │   ├── RoomManagementTab.java          ← Add/view/filter/sort rooms
    │   ├── CustomerManagementTab.java      ← Add/view/search customers
    │   ├── BookingTab.java                 ← Book room / checkout
    │   ├── BillingTab.java                 ← Invoice + tariff calculator
    │   ├── ServiceTab.java                 ← Thread-based room services
    │   └── ReportsTab.java                 ← Reports, audit log, persistence controls
    └── util/
        └── SampleDataLoader.java           ← Pre-loads 9 rooms + 5 customers
```

---

## Java Concepts Demonstrated

| Concept              | Where Used |
|----------------------|-----------|
| **Abstraction**      | `Room<T,U>` abstract class with `calculateTariff()` |
| **Encapsulation**    | Private fields + getters/setters in all model classes |
| **Inheritance**      | `StandardRoom`, `DeluxeRoom`, `LuxuryRoom` extend `Room` |
| **Polymorphism**     | Each room type overrides `calculateTariff()` differently |
| **Interface**        | `Amenities` interface with default `amenitiesSummary()` |
| **Generics**         | `Room<T extends RoomType, U extends Number>` + generic `display<E>()` |
| **Enum**             | `RoomType`, `ServiceLog.ServiceType` |
| **ArrayList**        | Rooms, customers, bookings, logs in `DataStore` |
| **HashMap**          | `roomCustomerMap` for fast booking lookups |
| **Iterator**         | Used in `DataStore.findRoom()`, `findCustomer()`, `removeRoom()` |
| **Sorting**          | `Comparator` for room price/number sorting |
| **Serialization**    | `ObjectOutputStream/InputStream` for `.ser` files |
| **FileWriter**       | Text report written via `BufferedWriter` |
| **RandomAccessFile** | Audit log appended with `raf.seek(raf.length())` |
| **Threads**          | `RoomService` dispatches `ServiceTask implements Runnable` |
| **sleep()**          | Simulates service duration in `ServiceTask.run()` |
| **synchronized**     | `BOOKING_LOCK` prevents double-booking; `SERVICE_LOCK` for services |
| **Platform.runLater**| Thread-safe JavaFX UI update from background thread |
| **Wrapper classes**  | `Double` autoboxing/unboxing in `calculateTariff()` |
| **Optional**         | `findRoom()`, `findCustomer()` return `Optional<T>` |
| **Switch expression**| Java 17 `switch` in `ServiceTask`, `RoomManagementTab` |
| **JavaFX**           | `Stage`, `Scene`, `TabPane`, `TableView`, `GridPane`, `SplitPane` |

---

## How to Run

### Prerequisites
- **Java 17+** (JDK, not just JRE)
- **Maven 3.8+**
- Internet connection (first run downloads JavaFX via Maven)

### Step 1 — Build
```bash
cd HMS
mvn clean package -q
```

### Step 2 — Run (recommended)
```bash
mvn javafx:run
```

### Alternative: Run directly with JavaFX modules
If you have JavaFX SDK downloaded separately:
```bash
java --module-path /path/to/javafx-sdk/lib \
     --add-modules javafx.controls,javafx.fxml \
     -cp target/hotel-management-system-1.0.jar \
     com.hotel.MainApp
```

### IntelliJ IDEA
1. `File → Open` → select the `HMS` folder
2. Maven will auto-import
3. Right-click `MainApp.java` → `Run`
4. (If needed: Add VM option `--module-path <javafx-lib> --add-modules javafx.controls`)

### Eclipse
1. Import as Maven project
2. Add JavaFX user library or use Maven plugin
3. Run `MainApp.java`

---

## Sample Data (Auto-Loaded on First Run)

### Rooms
| Room # | Type     | Price/Day |
|--------|----------|-----------|
| 101–103| Standard | ₹1500–1800|
| 201–203| Deluxe   | ₹3000–3500|
| 301–303| Suite    | ₹7000–10000|

### Customers (pre-loaded)
| ID   | Name           | Phone      |
|------|----------------|------------|
| C001 | Arjun Sharma   | 9876543210 |
| C002 | Priya Menon    | 8765432109 |
| C003 | Rahul Nair     | 7654321098 |
| C004 | Divya Krishnan | 6543210987 |
| C005 | Vikram Patel   | 9988776655 |

---

## Quick Test Workflow

1. **Launch** the app → Dashboard shows 9 rooms, 5 customers
2. **Room Management** → Add a new room (e.g., Room 401, Suite, ₹12000)
3. **Customer Management** → Add a new customer
4. **Booking** → Select customer C001, Room 101, date range → Confirm Booking
5. **Billing** → Select the new booking → Generate Invoice to see full bill
6. **Services** → Select Room 101 → CLEANING → Dispatch (watch thread log animate)
7. **Booking** → Checkout C001 → Room 101 becomes available again
8. **Reports** → Generate Report → view saved text file + audit log

---

## Data Files

All data is auto-saved to the `hms_data/` folder in the working directory:

| File              | Format             | Description |
|-------------------|--------------------|-------------|
| `rooms.ser`       | Java Serialization | All room objects |
| `customers.ser`   | Java Serialization | All customer objects |
| `bookings.ser`    | Java Serialization | All booking records |
| `logs.ser`        | Java Serialization | All service logs |
| `report.txt`      | Plain text         | Generated hotel report |
| `audit.log`       | Plain text         | RandomAccessFile audit trail |

---

## GUI Color Scheme

| Color     | Hex       | Used For |
|-----------|-----------|----------|
| Deep navy | `#1a1a2e` | Background |
| Dark blue | `#16213e` | Secondary panels |
| Mid blue  | `#0f3460` | Accents, headers |
| Red accent| `#e94560` | Primary actions, brand |
| Green     | `#00b894` | Success, availability |
| Yellow    | `#fdcb6e` | Warnings |

---

## Troubleshooting

**"Error: JavaFX runtime components are missing"**
→ Use `mvn javafx:run` instead of `java -jar`

**Tables appear empty after first run**
→ Click the tab again or press Refresh — data loads from sample set

**Service thread hangs**
→ Each service has a simulated duration (2–5 seconds). Wait for completion.

**Data not persisting**
→ Check that `hms_data/` folder is writable in your working directory

---

*Built with ☕ Java 17 + JavaFX 21 | No database — file-only persistence*
