// model/Rental.java
package main.java.com.vehicle.rental.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "rentals")
public class Rental {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rentalId;
    
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
    
    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;
    
    @ManyToOne
    @JoinColumn(name = "driver_id")
    private Driver driver;
    
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDate rentDate;
    private LocalDate returnDate;
    private String pickupLocation;
    private String dropLocation;
    private Double distance = 0.0;
    private Double rentAmount = 0.0;
    private Integer hours = 0;
    private Integer days = 0;
    private Boolean active = true;
    private String rentalType;  // SELF_DRIVE, WITH_DRIVER
    private String status;      // ACTIVE, ACCEPTED, ONGOING, COMPLETED, CANCELLED
    private String paymentMode;
    private Boolean damage = false;
    private Double damageFee = 0.0;
    private Integer customerRating = 0;
    private LocalDateTime bookingTime;
    
    public Rental() {}
    
    public Rental(Customer customer, Vehicle vehicle, Driver driver,
                  String pickupLocation, String dropLocation, String rentalType) {
        this.customer = customer;
        this.vehicle = vehicle;
        this.driver = driver;
        this.pickupLocation = pickupLocation;
        this.dropLocation = dropLocation;
        this.rentalType = rentalType;
        this.rentDate = LocalDate.now();
        this.bookingTime = LocalDateTime.now();
        this.active = true;
        this.status = rentalType.equals("SELF_DRIVE") ? "ACTIVE" : "ACCEPTED";
    }
    
    // Getters and Setters (generate all)
    public Long getRentalId() { return rentalId; }
    public void setRentalId(Long rentalId) { this.rentalId = rentalId; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
    public Driver getDriver() { return driver; }
    public void setDriver(Driver driver) { this.driver = driver; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public LocalDate getRentDate() { return rentDate; }
    public void setRentDate(LocalDate rentDate) { this.rentDate = rentDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }
    public String getDropLocation() { return dropLocation; }
    public void setDropLocation(String dropLocation) { this.dropLocation = dropLocation; }
    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }
    public Double getRentAmount() { return rentAmount; }
    public void setRentAmount(Double rentAmount) { this.rentAmount = rentAmount; }
    public Integer getHours() { return hours; }
    public void setHours(Integer hours) { this.hours = hours; }
    public Integer getDays() { return days; }
    public void setDays(Integer days) { this.days = days; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public String getRentalType() { return rentalType; }
    public void setRentalType(String rentalType) { this.rentalType = rentalType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }
    public Boolean getDamage() { return damage; }
    public void setDamage(Boolean damage) { this.damage = damage; }
    public Double getDamageFee() { return damageFee; }
    public void setDamageFee(Double damageFee) { this.damageFee = damageFee; }
    public Integer getCustomerRating() { return customerRating; }
    public void setCustomerRating(Integer customerRating) { this.customerRating = customerRating; }
    public LocalDateTime getBookingTime() { return bookingTime; }
    public void setBookingTime(LocalDateTime bookingTime) { this.bookingTime = bookingTime; }
}