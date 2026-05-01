// model/Driver.java
package main.java.com.vehicle.rental.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Entity
@Table(name = "drivers")
public class Driver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long driverId;
    
    private String name;
    private String username;
    private String password;
    private String licenseNumber;
    private String phone;
    private Boolean available = true;
    private Double rating = 0.0;
    private Integer totalRatings = 0;
    private String role = "DRIVER";
    
    @JsonIgnore
    @OneToMany(mappedBy = "driver")
    private List<Rental> rentals;
    
    public Driver() {}
    
    public Driver(String name, String username, String password, 
                  String licenseNumber, String phone) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.licenseNumber = licenseNumber;
        this.phone = phone;
        this.available = true;
        this.rating = 0.0;
        this.role = "DRIVER";
    }
    
    public void addRating(Double newRating) {
        if (totalRatings == 0) {
            this.rating = newRating;
        } else {
            this.rating = (rating * totalRatings + newRating) / (totalRatings + 1);
        }
        totalRatings++;
    }
    
    // Getters and Setters
    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public Integer getTotalRatings() { return totalRatings; }
    public void setTotalRatings(Integer totalRatings) { this.totalRatings = totalRatings; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}