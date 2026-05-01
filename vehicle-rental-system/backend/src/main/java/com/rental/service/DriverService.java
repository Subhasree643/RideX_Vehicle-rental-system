package com.rental.service;

import com.rental.model.Driver;
import com.rental.repository.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class DriverService {

    @Autowired
    private DriverRepository driverRepository;

    public List<Driver> getAllDrivers() {
        return driverRepository.findAll();
    }

    public List<Driver> getAvailableDrivers() {
        return driverRepository.findByAvailableTrue();
    }

    public Optional<Driver> getDriverById(Long id) {
        return driverRepository.findById(id);
    }

    public Driver registerDriver(Driver driver) {
        if (driverRepository.existsByUsername(driver.getUsername())) {
            throw new RuntimeException("Username already exists: " + driver.getUsername());
        }
        driver.setAvailable(true);
        driver.setRating(0.0);
        driver.setTotalRatings(0);
        return driverRepository.save(driver);
    }

    public Driver login(String username, String password) {
        Driver driver = driverRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        if (!driver.getPassword().equals(password)) {
            throw new RuntimeException("Invalid password");
        }
        return driver;
    }

    public Driver save(Driver driver) {
        return driverRepository.save(driver);
    }
    public void deleteDriver(Long id) {
    Driver driver = driverRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Driver not found"));
    driverRepository.delete(driver);
}
}
