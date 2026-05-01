package com.rental.controller;

import com.rental.model.*;
import com.rental.repository.TripRequestRepository;
import com.rental.service.RentalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/rentals")
@CrossOrigin(origins = "*")
public class RentalController {

    @Autowired private RentalService rentalService;
    @Autowired private TripRequestRepository tripRequestRepository;

    // ===== SELF DRIVE =====

    @PostMapping("/self-drive")
    public ResponseEntity<?> rentSelfDrive(@RequestBody Map<String, Object> body) {
        try {
            Long customerId = Long.valueOf(body.get("customerId").toString());
            Long vehicleId  = Long.valueOf(body.get("vehicleId").toString());
            int days        = Integer.parseInt(body.get("days").toString());
            int points      = body.containsKey("loyaltyPointsToUse") ? Integer.parseInt(body.get("loyaltyPointsToUse").toString()) : 0;
            String payment  = body.getOrDefault("paymentMode", "Cash").toString();
            Rental rental = rentalService.rentSelfDrive(customerId, vehicleId, days, points, payment);
            return ResponseEntity.status(HttpStatus.CREATED).body(rental);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<?> returnSelfDrive(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            String returnDate = body.get("returnDate").toString();
            boolean damage    = Boolean.parseBoolean(body.getOrDefault("damage", "false").toString());
            String payment    = body.getOrDefault("paymentMode", "Cash").toString();
            return ResponseEntity.ok(rentalService.returnSelfDrive(id, returnDate, damage, payment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ===== WITH DRIVER =====

    @PostMapping("/with-driver/request")
    public ResponseEntity<?> bookWithDriver(@RequestBody Map<String, Object> body) {
        try {
            Long customerId = Long.valueOf(body.get("customerId").toString());
            Long vehicleId  = Long.valueOf(body.get("vehicleId").toString());
            Long driverId   = Long.valueOf(body.get("driverId").toString());
            String pickup   = body.get("pickupLocation").toString();
            String drop     = body.get("dropLocation").toString();
            String date     = body.get("pickupDate").toString();
            String time     = body.get("pickupTime").toString();
            TripRequest request = rentalService.bookWithDriver(customerId, vehicleId, driverId, pickup, drop, date, time);
            return ResponseEntity.status(HttpStatus.CREATED).body(request);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/requests/{requestId}/respond")
    public ResponseEntity<?> respondToRequest(@PathVariable Long requestId, @RequestBody Map<String, Object> body) {
        try {
            Long driverId = Long.valueOf(body.get("driverId").toString());
            String action = body.get("action").toString();
            Rental rental = rentalService.respondToRequest(requestId, driverId, action);
            if (rental != null) return ResponseEntity.ok(rental);
            return ResponseEntity.ok(Map.of("message", "Request rejected"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<?> startTrip(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Long driverId = Long.valueOf(body.get("driverId").toString());
            return ResponseEntity.ok(rentalService.startTrip(id, driverId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeTrip(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Long driverId    = Long.valueOf(body.get("driverId").toString());
            double distance  = Double.parseDouble(body.get("distance").toString());
            String dropLoc   = body.get("dropLocation").toString();
            boolean damage   = Boolean.parseBoolean(body.getOrDefault("damage", "false").toString());
            return ResponseEntity.ok(rentalService.completeTrip(id, driverId, distance, dropLoc, damage));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<?> payForTrip(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Long customerId = Long.valueOf(body.get("customerId").toString());
            String payment  = body.get("paymentMode").toString();
            int points      = body.containsKey("loyaltyPointsToUse") ? Integer.parseInt(body.get("loyaltyPointsToUse").toString()) : 0;
            return ResponseEntity.ok(rentalService.payForTrip(id, customerId, payment, points));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/rate")
    public ResponseEntity<?> rateDriver(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Long customerId = Long.valueOf(body.get("customerId").toString());
            int rating      = Integer.parseInt(body.get("rating").toString());
            return ResponseEntity.ok(rentalService.rateDriver(id, customerId, rating));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelRental(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Long customerId = Long.valueOf(body.get("customerId").toString());
            String payment  = body.getOrDefault("paymentMode", "Cash").toString();
            return ResponseEntity.ok(rentalService.cancelRental(id, customerId, payment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ===== QUERIES =====

    @GetMapping
    public ResponseEntity<?> getAllRentals() {
        try {
            return ResponseEntity.ok(rentalService.getAllRentals());
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getCustomerRentals(@PathVariable Long customerId) {
        return ResponseEntity.ok(rentalService.getCustomerRentals(customerId));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<?> getDriverRentals(@PathVariable Long driverId) {
        return ResponseEntity.ok(rentalService.getDriverRentals(driverId));
    }

    @GetMapping("/driver/{driverId}/active")
    public ResponseEntity<?> getDriverActiveRentals(@PathVariable Long driverId) {
        return ResponseEntity.ok(rentalService.getDriverActiveRentals(driverId));
    }

    @GetMapping("/requests/driver/{driverId}")
    public ResponseEntity<?> getDriverPendingRequests(@PathVariable Long driverId) {
        return ResponseEntity.ok(tripRequestRepository.findByDriverIdAndStatus(driverId, "PENDING"));
    }

    @GetMapping("/requests/customer/{customerId}")
    public ResponseEntity<?> getCustomerRequests(@PathVariable Long customerId) {
        return ResponseEntity.ok(tripRequestRepository.findByCustomerId(customerId));
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
            return ResponseEntity.ok(rentalService.getRevenueDashboard(startDate, endDate));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    // Add this method to RentalController.java

@GetMapping("/{id}")
public ResponseEntity<?> getRentalById(@PathVariable Long id) {
    try {
        Rental rental = rentalService.getRentalById(id);
        if (rental != null) {
            return ResponseEntity.ok(rental);
        } else {
            return ResponseEntity.notFound().build();
        }
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}
}