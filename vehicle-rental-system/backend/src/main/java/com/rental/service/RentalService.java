package com.rental.service;

import com.rental.model.*;
import com.rental.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class RentalService {

    @Autowired private RentalRepository rentalRepository;
    @Autowired private VehicleRepository vehicleRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private DriverRepository driverRepository;
    @Autowired private TripRequestRepository tripRequestRepository;

    // ===== SELF DRIVE =====
    public Rental rentSelfDrive(Long customerId, Long vehicleId, int days, int pointsToUse, String paymentMode) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        if (!vehicle.getAvailable()) throw new RuntimeException("Vehicle is not available");
        if (customer.getRented()) throw new RuntimeException("Customer already has an active rental");

        double subtotal = vehicle.getRentPerDay() * days;
        double discount = 0;
        int usedPoints = 0;

        if (pointsToUse > 0 && customer.getLoyaltyPoints() >= pointsToUse) {
            usedPoints = pointsToUse;
            discount = usedPoints * 10.0;
            subtotal = Math.max(0, subtotal - discount);
            customer.setLoyaltyPoints(customer.getLoyaltyPoints() - usedPoints);
        }

        double gst = subtotal * 0.05;
        double finalAmount = subtotal + gst;

        Rental rental = new Rental();
        rental.setCustomer(customer);
        rental.setVehicle(vehicle);
        rental.setRentalType("SELF_DRIVE");
        rental.setStatus("ACTIVE");
        rental.setDays(days);
        rental.setRentAmount(finalAmount);
        rental.setOriginalRentAmount(finalAmount);
        rental.setPaymentMode(paymentMode);
        rental.setRentDate(LocalDate.now());
        rental.setBookingTime(LocalDateTime.now());
        rental.setDamage(false);
        rental.setDamageFee(0.0);
        rental.setCustomerRating(0);
        rental.setDistance(0.0);
        rental.setHours(0);

        vehicle.setAvailable(false);
        customer.setRented(true);

        vehicleRepository.save(vehicle);
        customerRepository.save(customer);
        return rentalRepository.save(rental);
    }

    public Rental returnSelfDrive(Long rentalId, String actualReturnDate, boolean damage, String paymentMode) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental not found"));
        if (!rental.getStatus().equals("ACTIVE"))
            throw new RuntimeException("Rental is not active. Current status: " + rental.getStatus());

        LocalDate returnDate = LocalDate.parse(actualReturnDate);
        long daysUsed = ChronoUnit.DAYS.between(rental.getRentDate(), returnDate);
        if (daysUsed == 0) daysUsed = 1;

        double extraPayment = 0;
        if (daysUsed > rental.getDays()) {
            int extraDays = (int)(daysUsed - rental.getDays());
            extraPayment += extraDays * rental.getVehicle().getRentPerDay() * 1.5;
        }
        if (damage) {
            extraPayment += 2000;
            rental.setDamage(true);
            rental.setDamageFee(2000.0);
        }

        if (extraPayment > 0) {
            rental.setRentAmount(rental.getRentAmount() + extraPayment);
            rental.setOriginalRentAmount(rental.getRentAmount());
        }

        rental.setStatus("COMPLETED");
        rental.setReturnDate(returnDate);
        rental.getVehicle().setAvailable(true);
        rental.getCustomer().setRented(false);

        // Add loyalty points on completion
        Customer customer = rental.getCustomer();
        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + 10);

        vehicleRepository.save(rental.getVehicle());
        customerRepository.save(customer);
        return rentalRepository.save(rental);
    }

    // ===== WITH DRIVER =====
    public TripRequest bookWithDriver(Long customerId, Long vehicleId, Long driverId,
                                      String pickupLocation, String dropLocation,
                                      String pickupDate, String pickupTime) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        if (!vehicle.getAvailable()) throw new RuntimeException("Vehicle is not available");
        if (!driver.getAvailable()) throw new RuntimeException("Driver is not available");

        TripRequest request = new TripRequest();
        request.setCustomer(customer);
        request.setVehicle(vehicle);
        request.setDriver(driver);
        request.setPickupLocation(pickupLocation);
        request.setDropLocation(dropLocation);
        request.setPickupDate(LocalDate.parse(pickupDate));
        request.setPickupTime(java.time.LocalTime.parse(pickupTime));
        request.setStatus("PENDING");
        request.setRequestTime(LocalDateTime.now());

        vehicle.setAvailable(false);
        driver.setAvailable(false);
        customer.setRented(true);

        vehicleRepository.save(vehicle);
        driverRepository.save(driver);
        customerRepository.save(customer);
        return tripRequestRepository.save(request);
    }

    public Rental respondToRequest(Long requestId, Long driverId, String action) {
        TripRequest request = tripRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!request.getDriver().getId().equals(driverId))
            throw new RuntimeException("Unauthorized");

        if (action.equalsIgnoreCase("ACCEPT")) {
            request.setStatus("ACCEPTED");
            request.setDriverResponseTime(LocalDateTime.now());
            tripRequestRepository.save(request);

            Rental rental = new Rental();
            rental.setCustomer(request.getCustomer());
            rental.setVehicle(request.getVehicle());
            rental.setDriver(request.getDriver());
            rental.setRentalType("WITH_DRIVER");
            rental.setStatus("ACCEPTED");
            rental.setPickupLocation(request.getPickupLocation());
            rental.setDropLocation(request.getDropLocation());
            rental.setRentDate(request.getPickupDate());
            rental.setPickupTime(request.getPickupTime());
            rental.setBookingTime(LocalDateTime.now());
            rental.setDamage(false);
            rental.setDamageFee(0.0);
            rental.setCustomerRating(0);
            rental.setDistance(0.0);
            rental.setRentAmount(0.0);
            rental.setOriginalRentAmount(0.0);
            rental.setHours(0);
            rental.setDays(0);
            return rentalRepository.save(rental);
        } else {
            request.setStatus("REJECTED");
            request.getVehicle().setAvailable(true);
            request.getDriver().setAvailable(true);
            request.getCustomer().setRented(false);
            vehicleRepository.save(request.getVehicle());
            driverRepository.save(request.getDriver());
            customerRepository.save(request.getCustomer());
            tripRequestRepository.save(request);
            return null;
        }
    }

    public Rental startTrip(Long rentalId, Long driverId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental not found"));
        if (!rental.getDriver().getId().equals(driverId)) throw new RuntimeException("Unauthorized");
        if (!rental.getStatus().equals("ACCEPTED")) throw new RuntimeException("Trip cannot be started. Status: " + rental.getStatus());

        rental.setStartTime(LocalDateTime.now());
        rental.setPickupTime(LocalDateTime.now().toLocalTime());
        rental.setStatus("ONGOING");
        return rentalRepository.save(rental);
    }

    public Rental completeTrip(Long rentalId, Long driverId, double distance, String dropLocation, boolean damage) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental not found"));
        if (!rental.getDriver().getId().equals(driverId)) throw new RuntimeException("Unauthorized");
        if (!rental.getStatus().equals("ONGOING")) throw new RuntimeException("Trip is not ongoing. Status: " + rental.getStatus());

        LocalDateTime now = LocalDateTime.now();
        rental.setEndTime(now);
        rental.setReturnDate(now.toLocalDate());
        rental.setDropTime(now.toLocalTime());
        rental.setDistance(distance);
        rental.setDropLocation(dropLocation);
        rental.setDamage(damage);

        int hours = (int) ChronoUnit.HOURS.between(rental.getStartTime(), now);
        if (hours == 0) hours = 1;
        rental.setHours(hours);

        double amount = (hours * 100.0) + (distance * 15.0);
        if (damage) {
            rental.setDamageFee(2000.0);
            amount += 2000;
        }

        rental.setOriginalRentAmount(amount);
        rental.setRentAmount(amount);
        rental.setStatus("COMPLETED");
        // Note: paymentMode stays null until customer pays — this is how revenue query knows it's unpaid

        return rentalRepository.save(rental);
    }

    public Rental payForTrip(Long rentalId, Long customerId, String paymentMode, int loyaltyPointsToUse) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental not found"));
        if (!rental.getCustomer().getId().equals(customerId)) throw new RuntimeException("Unauthorized");
        if (!rental.getStatus().equals("COMPLETED")) throw new RuntimeException("Trip not ready for payment");
        if (rental.getPaymentMode() != null) throw new RuntimeException("Already paid");

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        double amount = rental.getOriginalRentAmount();
        double discount = 0;

        if (loyaltyPointsToUse > 0 && customer.getLoyaltyPoints() >= loyaltyPointsToUse) {
            discount = loyaltyPointsToUse * 10.0;
            amount = Math.max(0, amount - discount);
            customer.setLoyaltyPoints(customer.getLoyaltyPoints() - loyaltyPointsToUse);
        }

        double gst = amount * 0.05;
        double finalAmount = amount + gst;

        rental.setRentAmount(finalAmount);
        rental.setPaymentMode(paymentMode);
        rental.getVehicle().setAvailable(true);
        rental.getDriver().setAvailable(true);
        customer.setRented(false);
        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + 10); // earn points

        vehicleRepository.save(rental.getVehicle());
        driverRepository.save(rental.getDriver());
        customerRepository.save(customer);
        return rentalRepository.save(rental);
    }

    public Rental rateDriver(Long rentalId, Long customerId, int rating) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental not found"));
        if (!rental.getCustomer().getId().equals(customerId)) throw new RuntimeException("Unauthorized");
        rental.setCustomerRating(rating);
        rental.getDriver().addRating(rating);
        driverRepository.save(rental.getDriver());
        return rentalRepository.save(rental);
    }

    public Rental cancelRental(Long rentalId, Long customerId, String paymentMode) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental not found"));
        if (!rental.getCustomer().getId().equals(customerId)) throw new RuntimeException("Unauthorized");

        String currentStatus = rental.getStatus();
        if (currentStatus.equals("COMPLETED") || currentStatus.equals("CANCELLED"))
            throw new RuntimeException("Cannot cancel: already " + currentStatus);

        long minutesSince = ChronoUnit.MINUTES.between(rental.getBookingTime(), LocalDateTime.now());
        double fee = 0;
        if (currentStatus.equals("ONGOING")) fee = 300;
        else if (minutesSince > 5) fee = 150;
        else if (minutesSince > 2) fee = 50;

        rental.setRentAmount(fee);
        rental.setOriginalRentAmount(fee);
        rental.setPaymentMode(fee > 0 ? paymentMode : "N/A");
        rental.setStatus("CANCELLED");
        rental.getVehicle().setAvailable(true);
        rental.getCustomer().setRented(false);
        if (rental.getDriver() != null) rental.getDriver().setAvailable(true);

        vehicleRepository.save(rental.getVehicle());
        customerRepository.save(rental.getCustomer());
        if (rental.getDriver() != null) driverRepository.save(rental.getDriver());
        return rentalRepository.save(rental);
    }

    public List<Rental> getAllRentals() {
        return rentalRepository.findAll();
    }

    public Rental getRentalById(Long id) {
        return rentalRepository.findById(id).orElse(null);
    }

    public List<Rental> getCustomerRentals(Long customerId) {
        return rentalRepository.findByCustomerId(customerId);
    }

    public List<Rental> getDriverRentals(Long driverId) {
        return rentalRepository.findByDriverId(driverId);
    }

    public List<Rental> getDriverActiveRentals(Long driverId) {
        List<Rental> result = new ArrayList<>();
        result.addAll(rentalRepository.findByDriverIdAndStatus(driverId, "ACCEPTED"));
        result.addAll(rentalRepository.findByDriverIdAndStatus(driverId, "ONGOING"));
        return result;
    }

    public Map<String, Object> getRevenueDashboard(String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        List<Rental> completed = rentalRepository.findCompletedBetweenDates(start, end);
        Double totalRevenue = rentalRepository.getTotalRevenue(start, end);

        long activeRentals = rentalRepository.findByStatus("ACTIVE").size()
                + rentalRepository.findByStatus("ACCEPTED").size()
                + rentalRepository.findByStatus("ONGOING").size();

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
        dashboard.put("totalTrips", completed.size());
        dashboard.put("activeRentals", activeRentals);
        dashboard.put("startDate", startDate);
        dashboard.put("endDate", endDate);
        return dashboard;
    }
}