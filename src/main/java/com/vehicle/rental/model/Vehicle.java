// model/Vehicle.java
package main.java.com.vehicle.rental.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Entity
@Table(name = "vehicles")
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vehicleId;
    
    private String type;      // Car, Bike
    private String category;  // SUV, Sedan, Sports, etc.
    private String brand;
    private String model;
    private String regNo;
    private Double rentPerDay;
    private Boolean available = true;
    
    @JsonIgnore
    @OneToMany(mappedBy = "vehicle")
    private List<Rental> rentals;
    
    // Constructors
    public Vehicle() {}
    
    public Vehicle(String type, String category, String brand, String model, 
                   String regNo, Double rentPerDay) {
        this.type = type;
        this.category = category;
        this.brand = brand;
        this.model = model;
        this.regNo = regNo;
        this.rentPerDay = rentPerDay;
        this.available = true;
    }
    
    // Getters and Setters
    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getRegNo() { return regNo; }
    public void setRegNo(String regNo) { this.regNo = regNo; }
    public Double getRentPerDay() { return rentPerDay; }
    public void setRentPerDay(Double rentPerDay) { this.rentPerDay = rentPerDay; }
    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
    
    public Double calculateRent(int days) {
        return rentPerDay * days;
    }
}