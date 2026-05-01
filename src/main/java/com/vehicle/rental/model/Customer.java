// model/Customer.java
package main.java.com.vehicle.rental.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;
    
    private String name;
    private String username;
    private String password;
    private String phone;
    private String address;
    private Boolean rented = false;
    private Integer loyaltyPoints = 0;
    private String role = "CUSTOMER";
    
    @JsonIgnore
    @OneToMany(mappedBy = "customer")
    private List<Rental> rentals;
    
    public Customer() {}
    
    public Customer(String name, String username, String password, 
                    String phone, String address) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.loyaltyPoints = 0;
        this.rented = false;
        this.role = "CUSTOMER";
    }
    
    // Getters and Setters
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Boolean getRented() { return rented; }
    public void setRented(Boolean rented) { this.rented = rented; }
    public Integer getLoyaltyPoints() { return loyaltyPoints; }
    public void setLoyaltyPoints(Integer loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}