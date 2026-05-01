package com.rental.repository;

import com.rental.model.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
    List<Rental> findByCustomerId(Long customerId);
    List<Rental> findByDriverId(Long driverId);
    List<Rental> findByStatus(String status);
    List<Rental> findByCustomerIdAndStatus(Long customerId, String status);
    List<Rental> findByDriverIdAndStatus(Long driverId, String status);
    List<Rental> findByRentalTypeAndStatus(String rentalType, String status);

    // FIX: only count rentals that are COMPLETED AND have a paymentMode (fully paid)
    @Query("SELECT r FROM Rental r WHERE r.status = 'COMPLETED' AND r.paymentMode IS NOT NULL AND r.rentDate >= :start AND r.rentDate <= :end")
    List<Rental> findCompletedBetweenDates(@Param("start") LocalDate start, @Param("end") LocalDate end);

    // FIX: SUM only paid completed rentals
    @Query("SELECT COALESCE(SUM(r.rentAmount), 0) FROM Rental r WHERE r.status = 'COMPLETED' AND r.paymentMode IS NOT NULL AND r.rentDate >= :start AND r.rentDate <= :end")
    Double getTotalRevenue(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT r FROM Rental r WHERE r.customer.id = :customerId AND r.status IN :statuses")
    List<Rental> findByCustomerIdAndStatusIn(@Param("customerId") Long customerId, @Param("statuses") List<String> statuses);

    @Query("SELECT r FROM Rental r WHERE r.driver.id = :driverId AND r.status IN :statuses")
    List<Rental> findByDriverIdAndStatusIn(@Param("driverId") Long driverId, @Param("statuses") List<String> statuses);
}