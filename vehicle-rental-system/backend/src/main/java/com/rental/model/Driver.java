package com.rental.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "drivers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "license_number", nullable = false)
    private String licenseNumber;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private Boolean available = true;

    @Column(nullable = false)
    private Double rating = 0.0;

    @Column(name = "total_ratings", nullable = false)
    private Integer totalRatings = 0;

    public void addRating(double newRating) {
        if (totalRatings == 0) {
            rating = newRating;
        } else {
            rating = (rating * totalRatings + newRating) / (totalRatings + 1);
        }
        totalRatings++;
    }
}