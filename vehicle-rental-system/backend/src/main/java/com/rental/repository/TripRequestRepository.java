package com.rental.repository;

import com.rental.model.TripRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TripRequestRepository extends JpaRepository<TripRequest, Long> {
    List<TripRequest> findByCustomerId(Long customerId);
    List<TripRequest> findByDriverId(Long driverId);
    List<TripRequest> findByDriverIdAndStatus(Long driverId, String status);
    List<TripRequest> findByStatus(String status);
}
