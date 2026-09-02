import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.CardLayout;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

/*
 * Smart Hospital Patient Appointment, Pharmacy Inventory
 * and Alert Notification System
 *
 * AWT GUI + SQLite JDBC | Core Java implementation for CSA09
 */

public class SmartHospitalSystem {

    // ===== Global Output Sink =====
    static Consumer<String> msgSink = System.out::println;

    static void log(String s) {
        msgSink.accept(s);
    }

    // ---------- Custom Exceptions ----------

    static class InvalidPatientException extends Exception {
        InvalidPatientException(String message) {
            super(message);
        }
    }

    static class DuplicateAppointmentException extends Exception {
        DuplicateAppointmentException(String message) {
            super(message);
        }
    }

    static class OutOfStockException extends Exception {
        OutOfStockException(String message) {
            super(message);
        }
    }

    static class InvalidInputException extends Exception {
        InvalidInputException(String message) {
            super(message);
        }
    }

    // ---------- Patient Hierarchy ----------

    static abstract class Patient {
        private String patientId;
        private String name;
        private int age;
        private String phone;

        Patient(String patientId, String name, int age, String phone) {
            this.patientId = patientId;
            this.name = name;
            this.age = age;
            this.phone = phone;
        }

        public String getPatientId() { return patientId; }
        public String getName() { return name; }
        public int getAge() { return age; }
        public String getPhone() { return phone; }

        public void setName(String name) { this.name = name; }
        public void setAge(int age) { this.age = age; }
        public void setPhone(String phone) { this.phone = phone; }

        public abstract double getConsultationFee();
        public abstract String getCategory();

        public void display() {
            log(String.format("%-8s %-20s %-5d %-14s %-12s %.2f",
                    patientId, name, age, phone, getCategory(), getConsultationFee()));
        }
    }

    static class GeneralPatient extends Patient {
        GeneralPatient(String id, String name, int age, String phone) {
            super(id, name, age, phone);
        }

        @Override
        public double getConsultationFee() {
            return 500.0;
        }

        @Override
        public String getCategory() {
            return "General";
        }
    }

    static class SeniorPatient extends Patient {
        SeniorPatient(String id, String name, int age, String phone) {
            super(id, name, age, phone);
        }

        @Override
        public double getConsultationFee() {
            return 250.0;
        }

        @Override
        public String getCategory() {
            return "Senior";
        }
    }

    static class EmergencyPatient extends Patient {
        EmergencyPatient(String id, String name, int age, String phone) {
            super(id, name, age, phone);
        }

        @Override
        public double getConsultationFee() {
            return 750.0;
        }

        @Override
        public String getCategory() {
            return "Emergency";
        }
    }

    // ---------- Doctor ----------

    static class Doctor {
        private String doctorId;
        private String name;
        private String specialization;
        private int maxAppointments;

        Doctor(String doctorId, String name, String specialization, int maxAppointments) {
            this.doctorId = doctorId;
            this.name = name;
            this.specialization = specialization;
            this.maxAppointments = maxAppointments;
        }

        public String getDoctorId() { return doctorId; }
        public String getName() { return name; }
        public String getSpecialization() { return specialization; }
        public int getMaxAppointments() { return maxAppointments; }

        public void display() {
            log(String.format("%-8s %-20s %-20s %-8d",
                    doctorId, name, specialization, maxAppointments));
        }
    }

    // ---------- Appointment ----------

    static class Appointment {
        private String appointmentId;
        private String patientId;
        private String doctorId;
        private String date;
        private String time;
        private String status;

        Appointment(String appointmentId, String patientId, String doctorId,
                    String date, String time) {
            this.appointmentId = appointmentId;
            this.patientId = patientId;
            this.doctorId = doctorId;
            this.date = date;
            this.time = time;
            this.status = "BOOKED";
        }

        public String getAppointmentId() { return appointmentId; }
        public String getPatientId() { return patientId; }
        public String getDoctorId() { return doctorId; }
        public String getDate() { return date; }
        public String getTime() { return time; }
        public String getStatus() { return status; }

        public void setStatus(String status) { this.status = status; }

        public void display() {
            log(String.format("%-8s %-10s %-10s %-12s %-8s %-12s",
                    appointmentId, patientId, doctorId, date, time, status));
        }
    }

    // ---------- Medicine Hierarchy ----------

    static abstract class Medicine {
        private String medicineId;
        private String name;
        private double price;
        private int quantity;
        private int lowStockLevel;

        Medicine(String medicineId, String name, double price,
                 int quantity, int lowStockLevel) {
            this.medicineId = medicineId;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
            this.lowStockLevel = lowStockLevel;
        }

        public String getMedicineId() { return medicineId; }
        public String getName() { return name; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }
        public int getLowStockLevel() { return lowStockLevel; }

        public void setPrice(double price) { this.price = price; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public abstract String getType();

        public void display() {
            log(String.format("%-8s %-20s %-15s %-10.2f %-8d %-10d",
                    medicineId, name, getType(), price, quantity, lowStockLevel));
        }
    }

    static class Tablet extends Medicine {
        Tablet(String id, String name, double price, int quantity, int lowStock) {
            super(id, name, price, quantity, lowStock);
        }

        @Override
        public String getType() {
            return "Tablet";
        }
    }

    static class Syrup extends Medicine {
        Syrup(String id, String name, double price, int quantity, int lowStock) {
            super(id, name, price, quantity, lowStock);
        }

        @Override
        public String getType() {
            return "Syrup";
        }
    }

    static class Injection extends Medicine {
        Injection(String id, String name, double price, int quantity, int lowStock) {
            super(id, name, price, quantity, lowStock);
        }

        @Override
        public String getType() {
            return "Injection";
        }
    }

    // ---------- Inventory ----------

    static class Inventory {
        private final HashMap<String, Medicine> medicines = new HashMap<String, Medicine>();
        private final Hashtable<String, Integer> issuedRecords = new Hashtable<String, Integer>();

        public synchronized void addMedicine(Medicine medicine) {
            medicines.put(medicine.getMedicineId(), medicine);
        }

        public synchronized Medicine getMedicine(String id) {
            return medicines.get(id);
        }

        public synchronized ArrayList<Medicine> getAllMedicines() {
            return new ArrayList<Medicine>(medicines.values());
        }

        public synchronized void loadIssuedRecord(String medicineId, int qty) {
            issuedRecords.put(medicineId, qty);
        }

        public synchronized void displayAll() {
            if (medicines.isEmpty()) {
                log("No medicines available.");
                return;
            }
            log("\n---------------- PHARMACY INVENTORY ----------------");
            log(String.format("%-8s %-20s %-15s %-10s %-8s %-10s",
                    "ID", "Name", "Type", "Price", "Qty", "LowLevel"));
            Iterator<Medicine> iterator = medicines.values().iterator();
            while (iterator.hasNext()) {
                iterator.next().display();
            }
        }

        public synchronized void issueMedicine(String id, int amount)
                throws OutOfStockException, InvalidInputException {

            if (amount <= 0) {
                throw new InvalidInputException("Medicine quantity must be positive.");
            }

            Medicine medicine = medicines.get(id);

            if (medicine == null) {
                throw new InvalidInputException("Medicine ID not found.");
            }

            if (medicine.getQuantity() < amount) {
                throw new OutOfStockException(
                        "Insufficient stock for " + medicine.getName() +
                                ". Available: " + medicine.getQuantity());
            }

            medicine.setQuantity(medicine.getQuantity() - amount);

            int old = issuedRecords.containsKey(id) ? issuedRecords.get(id) : 0;
            issuedRecords.put(id, old + amount);
        }

        public synchronized void restockMedicine(String id, int amount)
                throws InvalidInputException {

            if (amount <= 0) {
                throw new InvalidInputException("Restock quantity must be positive.");
            }

            Medicine medicine = medicines.get(id);

            if (medicine == null) {
                throw new InvalidInputException("Medicine ID not found.");
            }

            medicine.setQuantity(medicine.getQuantity() + amount);
        }

        public synchronized ArrayList<Medicine> getLowStockMedicines() {
            ArrayList<Medicine> lowStock = new ArrayList<Medicine>();

            for (Medicine medicine : medicines.values()) {
                if (medicine.getQuantity() <= medicine.getLowStockLevel()) {
                    lowStock.add(medicine);
                }
            }

            return lowStock;
        }

        public synchronized void updateMedicine(String id, double price, int quantity)
                throws InvalidInputException {

            Medicine medicine = medicines.get(id);

            if (medicine == null) {
                throw new InvalidInputException("Medicine ID not found.");
            }

            if (price < 0 || quantity < 0) {
                throw new InvalidInputException("Price and quantity cannot be negative.");
            }

            medicine.setPrice(price);
            medicine.setQuantity(quantity);
        }

        public synchronized String getInventoryReportText() {
            StringBuilder sb = new StringBuilder();
            sb.append("\n================ INVENTORY REPORT ================\n");

            int totalStock = 0;
            double stockValue = 0;

            for (Medicine medicine : medicines.values()) {
                totalStock += medicine.getQuantity();
                stockValue += medicine.getQuantity() * medicine.getPrice();
            }

            sb.append("Different medicines : ").append(medicines.size()).append("\n");
            sb.append("Total stock units   : ").append(totalStock).append("\n");
            sb.append(String.format("Current stock value  : Rs. %.2f%n", stockValue));

            sb.append("\nIssued Medicine Records:\n");
            if (issuedRecords.isEmpty()) {
                sb.append("No medicines issued.\n");
            } else {
                for (Map.Entry<String, Integer> entry : issuedRecords.entrySet()) {
                    Medicine medicine = medicines.get(entry.getKey());
                    String name = medicine == null ? entry.getKey() : medicine.getName();
                    sb.append(name).append(" -> ").append(entry.getValue()).append(" unit(s)\n");
                }
            }

            ArrayList<Medicine> lowStock = getLowStockMedicines();

            sb.append("\nLow Stock Medicines:\n");
            if (lowStock.isEmpty()) {
                sb.append("No low-stock medicines.\n");
            } else {
                for (Medicine medicine : lowStock) {
                    sb.append("- ").append(medicine.getName())
                            .append(" (remaining: ").append(medicine.getQuantity()).append(")\n");
                }
            }
            return sb.toString();
        }

        public synchronized void inventoryReport() {
            log(getInventoryReportText());
        }
    }

    // ---------- Notification with Inter-thread Communication ----------

    static class Notification {
        private String message;
        private String type;

        Notification(String type, String message) {
            this.type = type;
            this.message = message;
        }

        public String getType() { return type; }
        public String getMessage() { return message; }

        public void display() {
            log("[NOTIFICATION - " + type + "] " + message);
        }
    }

    static class NotificationQueue {
        private final Queue<Notification> queue = new LinkedList<Notification>();

        public synchronized void add(Notification notification) {
            queue.offer(notification);
            notifyAll();
        }

        public synchronized Notification take() throws InterruptedException {
            while (queue.isEmpty()) {
                wait();
            }
            return queue.poll();
        }
    }

    static class NotificationDispatcher extends Thread {
        private final NotificationQueue queue;
        private volatile boolean running = true;
        private volatile Consumer<String> sink = System.out::println;

        NotificationDispatcher(NotificationQueue queue) {
            this.queue = queue;
            setName("Notification-Dispatcher");
            setPriority(Thread.NORM_PRIORITY + 1);
        }

        public void setSink(Consumer<String> sink) {
            this.sink = sink;
        }

        public void stopDispatcher() {
            running = false;
            interrupt();
        }

        @Override
        public void run() {
            while (running) {
                try {
                    Notification notification = queue.take();
                    sink.accept("[NOTIFICATION - " + notification.getType() + "] " + notification.getMessage());
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    // Thread is being stopped or waiting for a notification.
                }
            }
        }
    }

    // ---------- JDBC Database Manager (SQLite) ----------

    static class DatabaseManager {
        static final String URL = "jdbc:sqlite:smart_hospital.db";

        static {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                log("SQLite JDBC driver not found in classpath: " + e.getMessage());
            }
        }

        static Connection getConnection() throws SQLException {
            return DriverManager.getConnection(URL);
        }

        static void createTables() {
            String patientsDDL = """
                CREATE TABLE IF NOT EXISTS patients (
                    patient_id  VARCHAR(20) PRIMARY KEY,
                    name        VARCHAR(80),
                    age         INT,
                    phone       VARCHAR(20),
                    category    VARCHAR(20),
                    fee         DOUBLE
                )
                """;
            String doctorsDDL = """
                CREATE TABLE IF NOT EXISTS doctors (
                    doctor_id       VARCHAR(20) PRIMARY KEY,
                    name            VARCHAR(80),
                    specialization  VARCHAR(80),
                    max_appointments INT
                )
                """;
            String appointmentsDDL = """
                CREATE TABLE IF NOT EXISTS appointments (
                    appointment_id  VARCHAR(20) PRIMARY KEY,
                    patient_id      VARCHAR(20),
                    doctor_id       VARCHAR(20),
                    date_val        VARCHAR(20),
                    time_val        VARCHAR(20),
                    status          VARCHAR(20)
                )
                """;
            String medicinesDDL = """
                CREATE TABLE IF NOT EXISTS medicines (
                    medicine_id     VARCHAR(20) PRIMARY KEY,
                    name            VARCHAR(80),
                    type            VARCHAR(20),
                    price           DOUBLE,
                    quantity        INT,
                    low_stock_level INT
                )
                """;
            String issuedDDL = """
                CREATE TABLE IF NOT EXISTS issued_records (
                    medicine_id VARCHAR(20),
                    issued_qty  INT
                )
                """;
            try (Connection c = getConnection(); Statement s = c.createStatement()) {
                s.executeUpdate(patientsDDL);
                s.executeUpdate(doctorsDDL);
                s.executeUpdate(appointmentsDDL);
                s.executeUpdate(medicinesDDL);
                s.executeUpdate(issuedDDL);
            } catch (SQLException e) {
                log("Database error: " + e.getMessage());
            }
        }

        // ---------- Patient CRUD ----------

        static void insertPatient(Patient p) {
            String sql = "INSERT OR REPLACE INTO patients (patient_id, name, age, phone, category, fee) VALUES (?,?,?,?,?,?)";
            try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, p.getPatientId());
                ps.setString(2, p.getName());
                ps.setInt(3, p.getAge());
                ps.setString(4, p.getPhone());
                ps.setString(5, p.getCategory());
                ps.setDouble(6, p.getConsultationFee());
                ps.executeUpdate();
            } catch (SQLException e) {
                log("DB insert patient error: " + e.getMessage());
            }
        }

        static void updatePatient(Patient p) {
            String sql = "UPDATE patients SET name=?, age=?, phone=?, category=?, fee=? WHERE patient_id=?";
            try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, p.getName());
                ps.setInt(2, p.getAge());
                ps.setString(3, p.getPhone());
                ps.setString(4, p.getCategory());
                ps.setDouble(5, p.getConsultationFee());
                ps.setString(6, p.getPatientId());
                ps.executeUpdate();
            } catch (SQLException e) {
                log("DB update patient error: " + e.getMessage());
            }
        }

        static ArrayList<Patient> getAllPatients() {
            ArrayList<Patient> list = new ArrayList<Patient>();
            String sql = "SELECT * FROM patients";
            try (Connection c = getConnection();
                 Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    String id = rs.getString("patient_id");
                    String name = rs.getString("name");
                    int age = rs.getInt("age");
                    String phone = rs.getString("phone");
                    String category = rs.getString("category");
                    if ("General".equals(category)) {
                        list.add(new GeneralPatient(id, name, age, phone));
                    } else if ("Senior".equals(category)) {
                        list.add(new SeniorPatient(id, name, age, phone));
                    } else if ("Emergency".equals(category)) {
                        list.add(new EmergencyPatient(id, name, age, phone));
                    }
                }
            } catch (SQLException e) {
                log("DB load patients error: " + e.getMessage());
            }
            return list;
        }

        // ---------- Doctor CRUD ----------

        static void insertDoctor(Doctor d) {
            String sql = "INSERT OR REPLACE INTO doctors (doctor_id, name, specialization, max_appointments) VALUES (?,?,?,?)";
            try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, d.getDoctorId());
                ps.setString(2, d.getName());
                ps.setString(3, d.getSpecialization());
                ps.setInt(4, d.getMaxAppointments());
                ps.executeUpdate();
            } catch (SQLException e) {
                log("DB insert doctor error: " + e.getMessage());
            }
        }

        static ArrayList<Doctor> getAllDoctors() {
            ArrayList<Doctor> list = new ArrayList<Doctor>();
            String sql = "SELECT * FROM doctors";
            try (Connection c = getConnection();
                 Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(new Doctor(
                            rs.getString("doctor_id"),
                            rs.getString("name"),
                            rs.getString("specialization"),
                            rs.getInt("max_appointments")
                    ));
                }
            } catch (SQLException e) {
                log("DB load doctors error: " + e.getMessage());
            }
            return list;
        }

        // ---------- Appointment CRUD ----------

        static void insertAppointment(Appointment a) {
            String sql = "INSERT INTO appointments (appointment_id, patient_id, doctor_id, date_val, time_val, status) VALUES (?,?,?,?,?,?)";
            try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, a.getAppointmentId());
                ps.setString(2, a.getPatientId());
                ps.setString(3, a.getDoctorId());
                ps.setString(4, a.getDate());
                ps.setString(5, a.getTime());
                ps.setString(6, a.getStatus());
                ps.executeUpdate();
            } catch (SQLException e) {
                log("DB insert appointment error: " + e.getMessage());
            }
        }

        static void updateAppointmentStatus(String appointmentId, String status) {
            String sql = "UPDATE appointments SET status=? WHERE appointment_id=?";
            try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, status);
                ps.setString(2, appointmentId);
                ps.executeUpdate();
            } catch (SQLException e) {
                log("DB update appointment error: " + e.getMessage());
            }
        }

        static ArrayList<Appointment> getAllAppointments() {
            ArrayList<Appointment> list = new ArrayList<Appointment>();
            String sql = "SELECT * FROM appointments";
            try (Connection c = getConnection();
                 Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    Appointment a = new Appointment(
                            rs.getString("appointment_id"),
                            rs.getString("patient_id"),
                            rs.getString("doctor_id"),
                            rs.getString("date_val"),
                            rs.getString("time_val")
                    );
                    a.setStatus(rs.getString("status"));
                    list.add(a);
                }
            } catch (SQLException e) {
                log("DB load appointments error: " + e.getMessage());
            }
            return list;
        }

        // ---------- Medicine CRUD ----------

        static void insertMedicine(Medicine m) {
            String sql = "INSERT OR REPLACE INTO medicines (medicine_id, name, type, price, quantity, low_stock_level) VALUES (?,?,?,?,?,?)";
            try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, m.getMedicineId());
                ps.setString(2, m.getName());
                ps.setString(3, m.getType());
                ps.setDouble(4, m.getPrice());
                ps.setInt(5, m.getQuantity());
                ps.setInt(6, m.getLowStockLevel());
                ps.executeUpdate();
            } catch (SQLException e) {
                log("DB insert medicine error: " + e.getMessage());
            }
        }

        static void updateMedicine(Medicine m) {
            String sql = "UPDATE medicines SET name=?, type=?, price=?, quantity=?, low_stock_level=? WHERE medicine_id=?";
            try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, m.getName());
                ps.setString(2, m.getType());
                ps.setDouble(3, m.getPrice());
                ps.setInt(4, m.getQuantity());
                ps.setInt(5, m.getLowStockLevel());
                ps.setString(6, m.getMedicineId());
                ps.executeUpdate();
            } catch (SQLException e) {
                log("DB update medicine error: " + e.getMessage());
            }
        }

        static ArrayList<Medicine> getAllMedicines() {
            ArrayList<Medicine> list = new ArrayList<Medicine>();
            String sql = "SELECT * FROM medicines";
            try (Connection c = getConnection();
                 Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    String id = rs.getString("medicine_id");
                    String name = rs.getString("name");
                    String type = rs.getString("type");
                    double price = rs.getDouble("price");
                    int qty = rs.getInt("quantity");
                    int low = rs.getInt("low_stock_level");
                    if ("Tablet".equals(type)) {
                        list.add(new Tablet(id, name, price, qty, low));
                    } else if ("Syrup".equals(type)) {
                        list.add(new Syrup(id, name, price, qty, low));
                    } else if ("Injection".equals(type)) {
                        list.add(new Injection(id, name, price, qty, low));
                    }
                }
            } catch (SQLException e) {
                log("DB load medicines error: " + e.getMessage());
            }
            return list;
        }

        // ---------- Issued Records ----------

        static void insertIssuedRecord(String medicineId, int qty) {
            String sql = "INSERT INTO issued_records (medicine_id, issued_qty) VALUES (?,?)";
            try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, medicineId);
                ps.setInt(2, qty);
                ps.executeUpdate();
            } catch (SQLException e) {
                log("DB insert issued record error: " + e.getMessage());
            }
        }

        static Hashtable<String, Integer> getIssuedRecords() {
            Hashtable<String, Integer> records = new Hashtable<String, Integer>();
            String sql = "SELECT medicine_id, SUM(issued_qty) AS total FROM issued_records GROUP BY medicine_id";
            try (Connection c = getConnection();
                 Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    records.put(rs.getString("medicine_id"), rs.getInt("total"));
                }
            } catch (SQLException e) {
                log("DB load issued records error: " + e.getMessage());
            }
            return records;
        }
    }

    // ---------- Hospital Manager ----------

    static class HospitalManager {
        private final ArrayList<Patient> patients = new ArrayList<Patient>();
        private final ArrayList<Doctor> doctors = new ArrayList<Doctor>();
        private final ArrayList<Appointment> appointments = new ArrayList<Appointment>();
        private final Set<String> appointmentKeys = new HashSet<String>();
        private final HashMap<String, Patient> patientMap = new HashMap<String, Patient>();
        private final Hashtable<String, Doctor> doctorTable = new Hashtable<String, Doctor>();
        private final HashMap<String, Queue<String>> waitlists = new HashMap<String, Queue<String>>();
        private final Inventory inventory = new Inventory();
        private final NotificationQueue notificationQueue = new NotificationQueue();
        private final NotificationDispatcher dispatcher = new NotificationDispatcher(notificationQueue);

        public HospitalManager() {
            DatabaseManager.createTables();
            loadFromDatabase();
            dispatcher.start();
        }

        public void shutdown() {
            dispatcher.stopDispatcher();
        }

        public void setNotificationSink(Consumer<String> sink) {
            dispatcher.setSink(sink);
        }

        // ---------- Getters for GUI ----------

        public synchronized ArrayList<Patient> getPatientsList() {
            return new ArrayList<Patient>(patients);
        }

        public synchronized ArrayList<Doctor> getDoctorsList() {
            return new ArrayList<Doctor>(doctors);
        }

        public synchronized ArrayList<Appointment> getAppointmentsList() {
            return new ArrayList<Appointment>(appointments);
        }

        public ArrayList<Medicine> getMedicinesList() {
            return inventory.getAllMedicines();
        }

        public ArrayList<Medicine> getLowStockMedicines() {
            return inventory.getLowStockMedicines();
        }

        // ---------- Database load ----------

        private void loadFromDatabase() {
            for (Patient p : DatabaseManager.getAllPatients()) {
                patients.add(p);
                patientMap.put(p.getPatientId(), p);
            }

            for (Doctor d : DatabaseManager.getAllDoctors()) {
                doctors.add(d);
                doctorTable.put(d.getDoctorId(), d);
                waitlists.put(d.getDoctorId(), new LinkedList<String>());
            }

            for (Appointment a : DatabaseManager.getAllAppointments()) {
                appointments.add(a);
                if ("BOOKED".equals(a.getStatus())) {
                    appointmentKeys.add(appointmentKey(a.getPatientId(), a.getDoctorId(), a.getDate(), a.getTime()));
                }
            }

            for (Medicine m : DatabaseManager.getAllMedicines()) {
                inventory.addMedicine(m);
            }

            Hashtable<String, Integer> records = DatabaseManager.getIssuedRecords();
            for (Map.Entry<String, Integer> entry : records.entrySet()) {
                inventory.loadIssuedRecord(entry.getKey(), entry.getValue());
            }
        }

        // ----- Patient operations -----

        public synchronized void registerPatient(Patient patient)
                throws InvalidInputException {

            if (patient.getPatientId() == null || patient.getPatientId().trim().isEmpty()) {
                throw new InvalidInputException("Patient ID cannot be empty.");
            }

            if (patient.getAge() <= 0 || patient.getAge() > 120) {
                throw new InvalidInputException("Invalid patient age.");
            }

            if (patientMap.containsKey(patient.getPatientId())) {
                throw new InvalidInputException("Duplicate Patient ID.");
            }

            patients.add(patient);
            patientMap.put(patient.getPatientId(), patient);
            DatabaseManager.insertPatient(patient);
            log("Patient registered successfully.");
        }

        public Patient findPatient(String id) throws InvalidPatientException {
            Patient patient = patientMap.get(id);

            if (patient == null) {
                throw new InvalidPatientException("Invalid Patient ID: " + id);
            }

            return patient;
        }

        public synchronized void displayPatients() {
            log("\n---------------- PATIENTS ----------------");
            log(String.format("%-8s %-20s %-5s %-14s %-12s %s",
                    "ID", "Name", "Age", "Phone", "Category", "Fee"));

            Iterator<Patient> iterator = patients.iterator();
            while (iterator.hasNext()) {
                iterator.next().display();
            }
        }

        public synchronized void updatePatient(String id, String name, int age, String phone)
                throws InvalidPatientException, InvalidInputException {

            Patient patient = findPatient(id);

            if (age <= 0 || age > 120) {
                throw new InvalidInputException("Invalid patient age.");
            }

            patient.setName(name);
            patient.setAge(age);
            patient.setPhone(phone);
            DatabaseManager.updatePatient(patient);
            log("Patient record updated successfully.");
        }

        // ----- Doctor operations -----

        public synchronized void registerDoctor(Doctor doctor)
                throws InvalidInputException {

            if (doctorTable.containsKey(doctor.getDoctorId())) {
                throw new InvalidInputException("Duplicate Doctor ID.");
            }

            if (doctor.getMaxAppointments() <= 0) {
                throw new InvalidInputException("Maximum appointments must be positive.");
            }

            doctors.add(doctor);
            doctorTable.put(doctor.getDoctorId(), doctor);
            waitlists.put(doctor.getDoctorId(), new LinkedList<String>());
            DatabaseManager.insertDoctor(doctor);
            log("Doctor registered successfully.");
        }

        public synchronized Doctor findDoctor(String id)
                throws InvalidInputException {

            Doctor doctor = doctorTable.get(id);

            if (doctor == null) {
                throw new InvalidInputException("Doctor ID not found.");
            }

            return doctor;
        }

        public synchronized void displayDoctors() {
            log("\n---------------- DOCTORS ----------------");
            log(String.format("%-8s %-20s %-20s %-8s",
                    "ID", "Name", "Specialization", "MaxSlots"));

            for (Doctor doctor : doctors) {
                doctor.display();
            }
        }

        // ----- Appointment operations -----

        private String appointmentKey(String patientId, String doctorId,
                                      String date, String time) {
            return patientId + "|" + doctorId + "|" + date + "|" + time;
        }

        private int bookedCount(String doctorId, String date) {
            int count = 0;

            for (Appointment appointment : appointments) {
                if (appointment.getDoctorId().equals(doctorId) &&
                        appointment.getDate().equals(date) &&
                        appointment.getStatus().equals("BOOKED")) {
                    count++;
                }
            }

            return count;
        }

        public synchronized void bookAppointment(String appointmentId,
                                                 String patientId,
                                                 String doctorId,
                                                 String date,
                                                 String time)
                throws InvalidPatientException, InvalidInputException,
                DuplicateAppointmentException {

            findPatient(patientId);
            findDoctor(doctorId);

            String key = appointmentKey(patientId, doctorId, date, time);

            if (appointmentKeys.contains(key)) {
                throw new DuplicateAppointmentException(
                        "Duplicate appointment for this patient and time.");
            }

            Doctor doctor = doctorTable.get(doctorId);
            int booked = bookedCount(doctorId, date);

            if (booked >= doctor.getMaxAppointments()) {
                Queue<String> waitlist = waitlists.get(doctorId);
                waitlist.offer(patientId);

                notificationQueue.add(new Notification(
                        "WAITLIST",
                        "Patient " + patientId +
                                " added to Dr. " + doctor.getName() + "'s waitlist."));

                log("Doctor is fully booked.");
                log("Patient added to waitlist.");
                return;
            }

            Appointment appointment = new Appointment(
                    appointmentId, patientId, doctorId, date, time);

            appointments.add(appointment);
            appointmentKeys.add(key);
            DatabaseManager.insertAppointment(appointment);

            notificationQueue.add(new Notification(
                    "APPOINTMENT",
                    "Appointment " + appointmentId +
                            " booked for patient " + patientId +
                            " with Dr. " + doctor.getName() +
                            " on " + date + " at " + time + "."));

            log("Appointment booked successfully.");
        }

        public synchronized void cancelAppointment(String appointmentId)
                throws InvalidInputException {

            Iterator<Appointment> iterator = appointments.iterator();

            while (iterator.hasNext()) {
                Appointment appointment = iterator.next();

                if (appointment.getAppointmentId().equals(appointmentId) &&
                        appointment.getStatus().equals("BOOKED")) {

                    appointment.setStatus("CANCELLED");
                    DatabaseManager.updateAppointmentStatus(appointmentId, "CANCELLED");

                    String key = appointmentKey(
                            appointment.getPatientId(),
                            appointment.getDoctorId(),
                            appointment.getDate(),
                            appointment.getTime());

                    appointmentKeys.remove(key);

                    promoteFromWaitlist(appointment.getDoctorId(),
                            appointment.getDate(),
                            appointment.getTime());

                    notificationQueue.add(new Notification(
                            "CANCELLATION",
                            "Appointment " + appointmentId + " cancelled."));

                    log("Appointment cancelled successfully.");
                    return;
                }
            }

            throw new InvalidInputException("Active appointment ID not found.");
        }

        private void promoteFromWaitlist(String doctorId, String date, String time) {
            Queue<String> waitlist = waitlists.get(doctorId);

            if (waitlist == null || waitlist.isEmpty()) {
                return;
            }

            String patientId = waitlist.poll();
            String newId = "AUTO-" + System.currentTimeMillis() % 100000;

            Appointment appointment = new Appointment(
                    newId, patientId, doctorId, date, time);

            appointments.add(appointment);
            appointmentKeys.add(appointmentKey(patientId, doctorId, date, time));
            DatabaseManager.insertAppointment(appointment);

            Doctor doctor = doctorTable.get(doctorId);

            notificationQueue.add(new Notification(
                    "WAITLIST",
                    "Waitlisted patient " + patientId +
                            " promoted to appointment " + newId +
                            " with Dr. " + doctor.getName() + "."));
        }

        public synchronized void displayAppointments() {
            log("\n---------------- APPOINTMENTS ----------------");
            log(String.format("%-8s %-10s %-10s %-12s %-8s %-12s",
                    "ApptID", "Patient", "Doctor", "Date", "Time", "Status"));

            if (appointments.isEmpty()) {
                log("No appointments.");
                return;
            }

            for (Appointment appointment : appointments) {
                appointment.display();
            }
        }

        public synchronized String getPatientVisitReportText() {
            StringBuilder sb = new StringBuilder();
            sb.append("\n================ PATIENT VISIT REPORT ================\n");

            HashMap<String, Integer> visitCount = new HashMap<String, Integer>();

            for (Appointment appointment : appointments) {
                if (appointment.getStatus().equals("BOOKED")) {
                    String patientId = appointment.getPatientId();
                    int count = visitCount.containsKey(patientId)
                            ? visitCount.get(patientId) : 0;
                    visitCount.put(patientId, count + 1);
                }
            }

            if (visitCount.isEmpty()) {
                sb.append("No active visits.\n");
                return sb.toString();
            }

            for (Map.Entry<String, Integer> entry : visitCount.entrySet()) {
                Patient patient = patientMap.get(entry.getKey());
                String name = patient == null ? entry.getKey() : patient.getName();
                sb.append(entry.getKey()).append(" - ").append(name)
                        .append(" : ").append(entry.getValue()).append(" active visit(s)\n");
            }
            return sb.toString();
        }

        public synchronized void patientVisitReport() {
            log(getPatientVisitReportText());
        }

        // ----- Pharmacy operations -----

        public void addMedicine(Medicine medicine) {
            inventory.addMedicine(medicine);
            DatabaseManager.insertMedicine(medicine);
            log("Medicine added successfully.");
        }

        public void displayInventory() {
            inventory.displayAll();
        }

        public void issueMedicine(String id, int quantity)
                throws OutOfStockException, InvalidInputException {

            inventory.issueMedicine(id, quantity);
            DatabaseManager.insertIssuedRecord(id, quantity);

            Medicine medicine = inventory.getMedicine(id);

            notificationQueue.add(new Notification(
                    "PHARMACY",
                    quantity + " unit(s) of " + medicine.getName() +
                            " issued successfully."));

            if (medicine.getQuantity() <= medicine.getLowStockLevel()) {
                notificationQueue.add(new Notification(
                        "LOW STOCK",
                        medicine.getName() +
                                " is low on stock. Remaining: " +
                                medicine.getQuantity()));
            }
        }

        public void restockMedicine(String id, int quantity)
                throws InvalidInputException {

            inventory.restockMedicine(id, quantity);

            Medicine medicine = inventory.getMedicine(id);
            DatabaseManager.updateMedicine(medicine);

            notificationQueue.add(new Notification(
                    "PHARMACY",
                    medicine.getName() +
                            " restocked. Current quantity: " +
                            medicine.getQuantity()));
        }

        public void updateMedicine(String id, double price, int quantity)
                throws InvalidInputException {

            inventory.updateMedicine(id, price, quantity);

            Medicine medicine = inventory.getMedicine(id);
            DatabaseManager.updateMedicine(medicine);

            notificationQueue.add(new Notification(
                    "PHARMACY",
                    "Medicine " + id + " updated successfully."));
        }

        public void inventoryReport() {
            log(getInventoryReportText());
        }

        public String getInventoryReportText() {
            return inventory.getInventoryReportText();
        }

        public void checkLowStock() {
            ArrayList<Medicine> lowStock = inventory.getLowStockMedicines();

            log("\n---------------- LOW STOCK ALERT ----------------");

            if (lowStock.isEmpty()) {
                log("No low-stock medicines.");
                return;
            }

            for (Medicine medicine : lowStock) {
                log(medicine.getMedicineId() + " - " +
                        medicine.getName() + " : " +
                        medicine.getQuantity() + " remaining");

                notificationQueue.add(new Notification(
                        "LOW STOCK",
                        medicine.getName() + " requires restocking."));
            }
        }

        // ----- Concurrent booking simulation -----

        public void simulateConcurrentBooking(String patientId,
                                              String doctorId,
                                              String date) {

            Thread bookingThread1 = new Thread(() -> {
                try {
                    bookAppointment(
                            "TH-" + System.currentTimeMillis() % 10000,
                            patientId, doctorId, date, "10:00");
                } catch (Exception e) {
                    log("Booking Thread 1: " + e.getMessage());
                }
            }, "Booking-Thread-1");

            Thread bookingThread2 = new Thread(() -> {
                try {
                    bookAppointment(
                            "TH-" + (System.currentTimeMillis() + 1) % 10000,
                            patientId, doctorId, date, "11:00");
                } catch (Exception e) {
                    log("Booking Thread 2: " + e.getMessage());
                }
            }, "Booking-Thread-2");

            bookingThread1.setPriority(Thread.MAX_PRIORITY);
            bookingThread2.setPriority(Thread.NORM_PRIORITY);

            bookingThread1.start();
            bookingThread2.start();

            try {
                bookingThread1.join();
                bookingThread2.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            log("Concurrent booking simulation completed.");
        }
    }

    // ---------- GUI Helpers ----------

    static String[] showInputDialog(Frame parent, String title, String[] labels) {
        Dialog d = new Dialog(parent, title, true);
        d.setLayout(new GridLayout(labels.length + 1, 2, 5, 5));

        TextField[] fields = new TextField[labels.length];
        for (int i = 0; i < labels.length; i++) {
            d.add(new Label(labels[i]));
            fields[i] = new TextField(20);
            d.add(fields[i]);
        }

        Button ok = new Button("OK");
        Button cancel = new Button("Cancel");
        boolean[] confirmed = {false};

        ok.addActionListener(e -> {
            confirmed[0] = true;
            d.dispose();
        });
        cancel.addActionListener(e -> d.dispose());

        d.add(ok);
        d.add(cancel);

        d.setSize(400, 60 + labels.length * 40);
        d.setLocationRelativeTo(parent);
        d.setVisible(true); // blocks until dialog closes

        if (!confirmed[0]) return null;

        String[] result = new String[labels.length];
        for (int i = 0; i < labels.length; i++) {
            result[i] = fields[i].getText();
        }
        return result;
    }

    static void showMessageDialog(Frame parent, String title, String message) {
        Dialog d = new Dialog(parent, title, true);
        d.setLayout(new BorderLayout(5, 5));
        TextArea ta = new TextArea(message, 10, 50);
        ta.setEditable(false);
        d.add(ta, BorderLayout.CENTER);
        Button ok = new Button("OK");
        ok.addActionListener(e -> d.dispose());
        Panel p = new Panel();
        p.add(ok);
        d.add(p, BorderLayout.SOUTH);
        d.setSize(450, 300);
        d.setLocationRelativeTo(parent);
        d.setVisible(true);
    }

    // ---------- Hospital GUI (AWT) ----------

    static class HospitalGUI extends Frame {
        private final HospitalManager manager;
        private final CardLayout cardLayout = new CardLayout();
        private final Panel cardPanel = new Panel(cardLayout);
        private final List navList = new List();
        private final TextArea outputArea = new TextArea(6, 80);
        private final TextArea notificationArea = new TextArea();
        private final TextArea reportsArea = new TextArea();
        private final List patientsList = new List();
        private final List doctorsList = new List();
        private final List appointmentsList = new List();
        private final List medicinesList = new List();

        public HospitalGUI() {
            super("Smart Hospital Management System");

            manager = new HospitalManager();

            SmartHospitalSystem.msgSink = msg -> EventQueue.invokeLater(() -> outputArea.append(msg + "\n"));
            manager.setNotificationSink(msg -> EventQueue.invokeLater(() -> notificationArea.append(msg + "\n")));

            if (manager.getPatientsList().isEmpty()) {
                seedSampleData();
            }

            initUI();
            refreshAllLists();

            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    manager.shutdown();
                    dispose();
                    System.exit(0);
                }
            });

            setSize(950, 700);
            setLocationRelativeTo(null);
        }

        private void seedSampleData() {
            try {
                if (manager.getPatientsList().isEmpty()) {
                    manager.registerPatient(new GeneralPatient("P101", "Anirudh", 21, "9876543210"));
                    manager.registerPatient(new SeniorPatient("P102", "Ravi", 68, "9876501234"));
                }
                if (manager.getDoctorsList().isEmpty()) {
                    manager.registerDoctor(new Doctor("D101", "Dr. Priya", "Cardiology", 2));
                    manager.registerDoctor(new Doctor("D102", "Dr. Kumar", "General Medicine", 3));
                }
                if (manager.getMedicinesList().isEmpty()) {
                    manager.addMedicine(new Tablet("M101", "Paracetamol", 25, 50, 10));
                    manager.addMedicine(new Syrup("M102", "Cough Syrup", 90, 8, 5));
                    manager.addMedicine(new Injection("M103", "Insulin", 250, 12, 4));
                }
            } catch (Exception e) {
                log("Seed data error: " + e.getMessage());
            }
        }

        private void initUI() {
            setLayout(new BorderLayout(5, 5));

            Label title = new Label("SMART HOSPITAL MANAGEMENT SYSTEM", Label.CENTER);
            title.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 18));
            add(title, BorderLayout.NORTH);

            // Navigation list
            navList.add("Patients");
            navList.add("Doctors");
            navList.add("Appointments");
            navList.add("Pharmacy");
            navList.add("Reports");
            navList.add("Notifications");
            navList.select(0);
            navList.addItemListener(e -> cardLayout.show(cardPanel, navList.getSelectedItem()));

            Panel westPanel = new Panel(new BorderLayout());
            westPanel.add(new Label("  Navigation"), BorderLayout.NORTH);
            westPanel.add(navList, BorderLayout.CENTER);
            add(westPanel, BorderLayout.WEST);

            // Cards
            cardPanel.add(buildPatientsCard(), "Patients");
            cardPanel.add(buildDoctorsCard(), "Doctors");
            cardPanel.add(buildAppointmentsCard(), "Appointments");
            cardPanel.add(buildPharmacyCard(), "Pharmacy");
            cardPanel.add(buildReportsCard(), "Reports");
            cardPanel.add(buildNotificationsCard(), "Notifications");
            add(cardPanel, BorderLayout.CENTER);

            // Bottom log
            outputArea.setEditable(false);
            Panel southPanel = new Panel(new BorderLayout());
            southPanel.add(new Label(" System Log"), BorderLayout.NORTH);
            southPanel.add(outputArea, BorderLayout.CENTER);
            add(southPanel, BorderLayout.SOUTH);
        }

        // ----- Card builders -----

        private Panel buildPatientsCard() {
            Panel panel = new Panel(new BorderLayout(5, 5));
            panel.add(patientsList, BorderLayout.CENTER);

            Panel buttonPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 5));
            Button registerBtn = new Button("Register Patient");
            registerBtn.addActionListener(e -> registerPatientDialog());
            Button updateBtn = new Button("Update Patient");
            updateBtn.addActionListener(e -> updatePatientDialog());
            Button refreshBtn = new Button("Refresh");
            refreshBtn.addActionListener(e -> refreshPatientsList());
            buttonPanel.add(registerBtn);
            buttonPanel.add(updateBtn);
            buttonPanel.add(refreshBtn);
            panel.add(buttonPanel, BorderLayout.SOUTH);
            return panel;
        }

        private Panel buildDoctorsCard() {
            Panel panel = new Panel(new BorderLayout(5, 5));
            panel.add(doctorsList, BorderLayout.CENTER);

            Panel buttonPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 5));
            Button registerBtn = new Button("Register Doctor");
            registerBtn.addActionListener(e -> registerDoctorDialog());
            Button refreshBtn = new Button("Refresh");
            refreshBtn.addActionListener(e -> refreshDoctorsList());
            buttonPanel.add(registerBtn);
            buttonPanel.add(refreshBtn);
            panel.add(buttonPanel, BorderLayout.SOUTH);
            return panel;
        }

        private Panel buildAppointmentsCard() {
            Panel panel = new Panel(new BorderLayout(5, 5));
            panel.add(appointmentsList, BorderLayout.CENTER);

            Panel buttonPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 5));
            Button bookBtn = new Button("Book Appointment");
            bookBtn.addActionListener(e -> bookAppointmentDialog());
            Button cancelBtn = new Button("Cancel Appointment");
            cancelBtn.addActionListener(e -> cancelAppointmentDialog());
            Button demoBtn = new Button("Concurrent Booking Demo");
            demoBtn.addActionListener(e -> concurrentBookingDialog());
            Button refreshBtn = new Button("Refresh");
            refreshBtn.addActionListener(e -> refreshAppointmentsList());
            buttonPanel.add(bookBtn);
            buttonPanel.add(cancelBtn);
            buttonPanel.add(demoBtn);
            buttonPanel.add(refreshBtn);
            panel.add(buttonPanel, BorderLayout.SOUTH);
            return panel;
        }

        private Panel buildPharmacyCard() {
            Panel panel = new Panel(new BorderLayout(5, 5));
            panel.add(medicinesList, BorderLayout.CENTER);

            Panel buttonPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 5));
            Button addBtn = new Button("Add Medicine");
            addBtn.addActionListener(e -> addMedicineDialog());
            Button issueBtn = new Button("Issue Medicine");
            issueBtn.addActionListener(e -> issueMedicineDialog());
            Button restockBtn = new Button("Restock Medicine");
            restockBtn.addActionListener(e -> restockMedicineDialog());
            Button updateBtn = new Button("Update Medicine");
            updateBtn.addActionListener(e -> updateMedicineDialog());
            Button lowBtn = new Button("Low Stock");
            lowBtn.addActionListener(e -> lowStockDialog());
            Button refreshBtn = new Button("Refresh");
            refreshBtn.addActionListener(e -> refreshMedicinesList());
            buttonPanel.add(addBtn);
            buttonPanel.add(issueBtn);
            buttonPanel.add(restockBtn);
            buttonPanel.add(updateBtn);
            buttonPanel.add(lowBtn);
            buttonPanel.add(refreshBtn);
            panel.add(buttonPanel, BorderLayout.SOUTH);
            return panel;
        }

        private Panel buildReportsCard() {
            Panel panel = new Panel(new BorderLayout(5, 5));
            reportsArea.setEditable(false);
            panel.add(reportsArea, BorderLayout.CENTER);

            Panel buttonPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 5));
            Button visitBtn = new Button("Patient Visit Report");
            visitBtn.addActionListener(e -> reportsArea.setText(manager.getPatientVisitReportText()));
            Button invBtn = new Button("Inventory Report");
            invBtn.addActionListener(e -> reportsArea.setText(manager.getInventoryReportText()));
            buttonPanel.add(visitBtn);
            buttonPanel.add(invBtn);
            panel.add(buttonPanel, BorderLayout.SOUTH);
            return panel;
        }

        private Panel buildNotificationsCard() {
            Panel panel = new Panel(new BorderLayout(5, 5));
            notificationArea.setEditable(false);
            panel.add(notificationArea, BorderLayout.CENTER);

            Panel buttonPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 5));
            Button clearBtn = new Button("Clear");
            clearBtn.addActionListener(e -> notificationArea.setText(""));
            buttonPanel.add(clearBtn);
            panel.add(buttonPanel, BorderLayout.SOUTH);
            return panel;
        }

        // ----- Dialog actions -----

        private void registerPatientDialog() {
            String[] values = showInputDialog(this, "Register Patient",
                    new String[]{"Patient ID:", "Name:", "Age:", "Phone:", "Category (General/Senior/Emergency):"});
            if (values == null) return;
            try {
                String id = values[0].trim();
                String name = values[1].trim();
                int age = Integer.parseInt(values[2].trim());
                String phone = values[3].trim();
                String cat = values[4].trim();
                Patient p;
                if (cat.equalsIgnoreCase("General") || cat.equals("1")) {
                    p = new GeneralPatient(id, name, age, phone);
                } else if (cat.equalsIgnoreCase("Senior") || cat.equals("2")) {
                    p = new SeniorPatient(id, name, age, phone);
                } else if (cat.equalsIgnoreCase("Emergency") || cat.equals("3")) {
                    p = new EmergencyPatient(id, name, age, phone);
                } else {
                    throw new InvalidInputException("Invalid category.");
                }
                manager.registerPatient(p);
                refreshPatientsList();
            } catch (Exception ex) {
                outputArea.append("ERROR: " + ex.getMessage() + "\n");
            }
        }

        private void updatePatientDialog() {
            String[] values = showInputDialog(this, "Update Patient",
                    new String[]{"Patient ID:", "New Name:", "New Age:", "New Phone:"});
            if (values == null) return;
            try {
                String id = values[0].trim();
                String name = values[1].trim();
                int age = Integer.parseInt(values[2].trim());
                String phone = values[3].trim();
                manager.updatePatient(id, name, age, phone);
                refreshPatientsList();
            } catch (Exception ex) {
                outputArea.append("ERROR: " + ex.getMessage() + "\n");
            }
        }

        private void registerDoctorDialog() {
            String[] values = showInputDialog(this, "Register Doctor",
                    new String[]{"Doctor ID:", "Name:", "Specialization:", "Max Appointments:"});
            if (values == null) return;
            try {
                String id = values[0].trim();
                String name = values[1].trim();
                String spec = values[2].trim();
                int max = Integer.parseInt(values[3].trim());
                manager.registerDoctor(new Doctor(id, name, spec, max));
                refreshDoctorsList();
            } catch (Exception ex) {
                outputArea.append("ERROR: " + ex.getMessage() + "\n");
            }
        }

        private void bookAppointmentDialog() {
            String[] values = showInputDialog(this, "Book Appointment",
                    new String[]{"Appointment ID:", "Patient ID:", "Doctor ID:", "Date (DD-MM-YYYY):", "Time (HH:MM):"});
            if (values == null) return;
            try {
                manager.bookAppointment(values[0].trim(), values[1].trim(), values[2].trim(),
                        values[3].trim(), values[4].trim());
                refreshAppointmentsList();
            } catch (Exception ex) {
                outputArea.append("ERROR: " + ex.getMessage() + "\n");
            }
        }

        private void cancelAppointmentDialog() {
            String[] values = showInputDialog(this, "Cancel Appointment",
                    new String[]{"Appointment ID:"});
            if (values == null) return;
            try {
                manager.cancelAppointment(values[0].trim());
                refreshAppointmentsList();
            } catch (Exception ex) {
                outputArea.append("ERROR: " + ex.getMessage() + "\n");
            }
        }

        private void concurrentBookingDialog() {
            String[] values = showInputDialog(this, "Concurrent Booking Demo",
                    new String[]{"Patient ID:", "Doctor ID:", "Date (DD-MM-YYYY):"});
            if (values == null) return;
            try {
                manager.simulateConcurrentBooking(values[0].trim(), values[1].trim(), values[2].trim());
                refreshAppointmentsList();
            } catch (Exception ex) {
                outputArea.append("ERROR: " + ex.getMessage() + "\n");
            }
        }

        private void addMedicineDialog() {
            String[] values = showInputDialog(this, "Add Medicine",
                    new String[]{"Medicine ID:", "Name:", "Price:", "Quantity:", "Low Stock Level:", "Type (Tablet/Syrup/Injection):"});
            if (values == null) return;
            try {
                String id = values[0].trim();
                String name = values[1].trim();
                double price = Double.parseDouble(values[2].trim());
                int qty = Integer.parseInt(values[3].trim());
                int low = Integer.parseInt(values[4].trim());
                String type = values[5].trim();
                Medicine m;
                if (type.equalsIgnoreCase("Tablet") || type.equals("1")) {
                    m = new Tablet(id, name, price, qty, low);
                } else if (type.equalsIgnoreCase("Syrup") || type.equals("2")) {
                    m = new Syrup(id, name, price, qty, low);
                } else if (type.equalsIgnoreCase("Injection") || type.equals("3")) {
                    m = new Injection(id, name, price, qty, low);
                } else {
                    throw new InvalidInputException("Invalid type.");
                }
                manager.addMedicine(m);
                refreshMedicinesList();
            } catch (Exception ex) {
                outputArea.append("ERROR: " + ex.getMessage() + "\n");
            }
        }

        private void issueMedicineDialog() {
            String[] values = showInputDialog(this, "Issue Medicine",
                    new String[]{"Medicine ID:", "Quantity:"});
            if (values == null) return;
            try {
                manager.issueMedicine(values[0].trim(), Integer.parseInt(values[1].trim()));
                refreshMedicinesList();
            } catch (Exception ex) {
                outputArea.append("ERROR: " + ex.getMessage() + "\n");
            }
        }

        private void restockMedicineDialog() {
            String[] values = showInputDialog(this, "Restock Medicine",
                    new String[]{"Medicine ID:", "Quantity:"});
            if (values == null) return;
            try {
                manager.restockMedicine(values[0].trim(), Integer.parseInt(values[1].trim()));
                refreshMedicinesList();
            } catch (Exception ex) {
                outputArea.append("ERROR: " + ex.getMessage() + "\n");
            }
        }

        private void updateMedicineDialog() {
            String[] values = showInputDialog(this, "Update Medicine",
                    new String[]{"Medicine ID:", "New Price:", "New Quantity:"});
            if (values == null) return;
            try {
                manager.updateMedicine(values[0].trim(), Double.parseDouble(values[1].trim()),
                        Integer.parseInt(values[2].trim()));
                refreshMedicinesList();
            } catch (Exception ex) {
                outputArea.append("ERROR: " + ex.getMessage() + "\n");
            }
        }

        private void lowStockDialog() {
            ArrayList<Medicine> lowStock = manager.getLowStockMedicines();
            StringBuilder sb = new StringBuilder("LOW STOCK MEDICINES:\n");
            if (lowStock.isEmpty()) {
                sb.append("No low-stock medicines.\n");
            } else {
                for (Medicine m : lowStock) {
                    sb.append(m.getMedicineId()).append(" - ").append(m.getName())
                            .append(" : ").append(m.getQuantity()).append(" remaining\n");
                }
            }
            showMessageDialog(this, "Low Stock", sb.toString());
        }

        // ----- Refresh methods -----

        private void refreshAllLists() {
            refreshPatientsList();
            refreshDoctorsList();
            refreshAppointmentsList();
            refreshMedicinesList();
        }

        private void refreshPatientsList() {
            patientsList.removeAll();
            for (Patient p : manager.getPatientsList()) {
                patientsList.add(String.format("%-8s %-20s %-5d %-14s %-10s %.2f",
                        p.getPatientId(), p.getName(), p.getAge(), p.getPhone(), p.getCategory(), p.getConsultationFee()));
            }
        }

        private void refreshDoctorsList() {
            doctorsList.removeAll();
            for (Doctor d : manager.getDoctorsList()) {
                doctorsList.add(String.format("%-8s %-20s %-20s %d",
                        d.getDoctorId(), d.getName(), d.getSpecialization(), d.getMaxAppointments()));
            }
        }

        private void refreshAppointmentsList() {
            appointmentsList.removeAll();
            for (Appointment a : manager.getAppointmentsList()) {
                appointmentsList.add(String.format("%-8s %-10s %-10s %-12s %-8s %-10s",
                        a.getAppointmentId(), a.getPatientId(), a.getDoctorId(), a.getDate(), a.getTime(), a.getStatus()));
            }
        }

        private void refreshMedicinesList() {
            medicinesList.removeAll();
            for (Medicine m : manager.getMedicinesList()) {
                medicinesList.add(String.format("%-8s %-20s %-10s %.2f  Qty:%d  Low:%d",
                        m.getMedicineId(), m.getName(), m.getType(), m.getPrice(), m.getQuantity(), m.getLowStockLevel()));
            }
        }
    }

    // ---------- Main ----------

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new HospitalGUI().setVisible(true));
    }
}
