package com.rental.controller;

import com.rental.model.*;
import com.rental.repository.*;
import com.rental.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    @Autowired private CustomerService customerService;
    @Autowired private DriverService driverService;
    @Autowired private VehicleService vehicleService;
    @Autowired private RentalService rentalService;
    @Autowired private RentalRepository rentalRepository;
    @Autowired private TripRequestRepository tripRequestRepository;

    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> creds) {
        if (ADMIN_USERNAME.equals(creds.get("username")) && ADMIN_PASSWORD.equals(creds.get("password"))) {
            return ResponseEntity.ok(Map.of("message", "Login successful", "role", "ADMIN"));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid admin credentials"));
    }

    @GetMapping("/customers")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/drivers")
    public ResponseEntity<List<Driver>> getAllDrivers() {
        return ResponseEntity.ok(driverService.getAllDrivers());
    }

    @GetMapping("/vehicles")
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

    @GetMapping("/rentals")
    public ResponseEntity<?> getAllRentals() {
        return ResponseEntity.ok(rentalService.getAllRentals());
    }

    @GetMapping("/requests")
    public ResponseEntity<?> getAllRequests() {
        return ResponseEntity.ok(tripRequestRepository.findAll());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(
            @RequestParam(defaultValue = "2024-01-01") String startDate,
            @RequestParam(defaultValue = "2099-12-31") String endDate) {
        try {
            Map<String, Object> dashboard = rentalService.getRevenueDashboard(startDate, endDate);
            dashboard.put("totalCustomers", customerService.getAllCustomers().size());
            dashboard.put("totalDrivers", driverService.getAllDrivers().size());
            dashboard.put("totalVehicles", vehicleService.getAllVehicles().size());
            dashboard.put("availableVehicles", vehicleService.getAvailableVehicles().size());
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/seed")
    public ResponseEntity<?> seedData() {
        try {
            if (!vehicleService.getAllVehicles().isEmpty()) {
                return ResponseEntity.ok(Map.of("message", "Data already seeded"));
            }
            vehicleService.addVehicle(new Vehicle(null, "Car", "SUV", "Toyota", "Fortuner", "KA01AB1234", 5000.0, true));
            vehicleService.addVehicle(new Vehicle(null, "Car", "Sedan", "Honda", "City", "KA02CD5678", 3000.0, true));
            vehicleService.addVehicle(new Vehicle(null, "Bike", "Sports", "Yamaha", "R15", "KA03EF9012", 1500.0, true));
            vehicleService.addVehicle(new Vehicle(null, "Bike", "Cruiser", "Royal Enfield", "Classic 350", "KA04GH3456", 2000.0, true));
            vehicleService.addVehicle(new Vehicle(null, "Car", "Mini", "Maruti", "Swift", "KA05IJ7890", 2500.0, true));

            driverService.registerDriver(new Driver(null, "John Driver", "john123", "pass123", "DL123456", "9876543210", true, 4.5, 10));
            driverService.registerDriver(new Driver(null, "Mike Driver", "mike123", "pass123", "DL789012", "9876543211", true, 4.8, 8));
            driverService.registerDriver(new Driver(null, "Sarah Driver", "sarah123", "pass123", "DL345678", "9876543212", true, 4.2, 5));

            Customer c1 = new Customer(null, "Alice Customer", "alice", "pass123", "9876543213", "Bangalore", false, 100);
            Customer c2 = new Customer(null, "Bob Customer", "bob", "pass123", "9876543214", "Mumbai", false, 50);
            customerService.registerCustomer(c1);
            customerService.registerCustomer(c2);

            return ResponseEntity.ok(Map.of("message", "Sample data seeded successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/customers/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long id) {
        try {
            List<Rental> active = rentalRepository.findByCustomerIdAndStatusIn(
                id, Arrays.asList("ACTIVE", "ACCEPTED", "ONGOING"));
            if (!active.isEmpty())
                return ResponseEntity.badRequest().body(Map.of("error", "Cannot delete customer with active rentals"));
            customerService.deleteCustomer(id);
            return ResponseEntity.ok(Map.of("message", "Customer deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/drivers/{id}")
    public ResponseEntity<?> deleteDriver(@PathVariable Long id) {
        try {
            List<Rental> active = rentalRepository.findByDriverIdAndStatusIn(
                id, Arrays.asList("ACCEPTED", "ONGOING"));
            if (!active.isEmpty())
                return ResponseEntity.badRequest().body(Map.of("error", "Cannot delete driver with active trips"));
            driverService.deleteDriver(id);
            return ResponseEntity.ok(Map.of("message", "Driver deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}