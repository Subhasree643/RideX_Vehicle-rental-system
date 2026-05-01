package com.rental.service;

import com.rental.model.Vehicle;
import com.rental.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    public List<Vehicle> getAvailableVehicles() {
        return vehicleRepository.findByAvailableTrue();
    }

    public List<Vehicle> getRentedVehicles() {
        return vehicleRepository.findByAvailableFalse();
    }

    public Optional<Vehicle> getVehicleById(Long id) {
        return vehicleRepository.findById(id);
    }

    public Vehicle addVehicle(Vehicle vehicle) {
        if (vehicleRepository.existsByRegNo(vehicle.getRegNo())) {
            throw new RuntimeException("Vehicle with registration number already exists: " + vehicle.getRegNo());
        }
        vehicle.setAvailable(true);
        return vehicleRepository.save(vehicle);
    }

    public Vehicle updateVehicle(Long id, Vehicle vehicleDetails) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + id));
        vehicle.setType(vehicleDetails.getType());
        vehicle.setCategory(vehicleDetails.getCategory());
        vehicle.setBrand(vehicleDetails.getBrand());
        vehicle.setModel(vehicleDetails.getModel());
        vehicle.setRentPerDay(vehicleDetails.getRentPerDay());
        if (vehicleDetails.getAvailable() != null) {
            vehicle.setAvailable(vehicleDetails.getAvailable());
        }
        return vehicleRepository.save(vehicle);
    }

    public void deleteVehicle(Long id) {
        vehicleRepository.deleteById(id);
    }

    public Vehicle save(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }
}
