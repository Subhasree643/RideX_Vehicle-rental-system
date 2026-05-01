package com.rental.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "rentals")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Column(name = "rental_type", nullable = false)
    private String rentalType;

    @Column(nullable = false)
    private String status;

    @Column(name = "pickup_location")
    private String pickupLocation;

    @Column(name = "drop_location")
    private String dropLocation;

    private Double distance = 0.0;
    private Integer days = 0;
    private Integer hours = 0;

    @Column(name = "rent_amount")
    private Double rentAmount = 0.0;

    @Column(name = "original_rent_amount")
    private Double originalRentAmount = 0.0;

    @Column(name = "damage_fee")
    private Double damageFee = 0.0;

    private Boolean damage = false;

    @Column(name = "payment_mode")
    private String paymentMode;

    @Column(name = "customer_rating")
    private Integer customerRating = 0;

    @Column(name = "rent_date", nullable = false)
    private LocalDate rentDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "pickup_time")
    private LocalTime pickupTime;

    @Column(name = "drop_time")
    private LocalTime dropTime;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "booking_time")
    private LocalDateTime bookingTime;
}