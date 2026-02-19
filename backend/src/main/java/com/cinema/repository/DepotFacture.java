package com.cinema.repository;

import com.cinema.model.Facture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DepotFacture extends JpaRepository<Facture, Long> {

    Optional<Facture> findByNumeroFacture(String numeroFacture);

    Optional<Facture> findByReservationId(Long reservationId);

    List<Facture> findByEmailClient(String emailClient);

    @Query("SELECT f FROM Facture f WHERE f.dateEmission BETWEEN :startDate AND :endDate")
    List<Facture> findFacturesBetweenDates(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(f.montantTtc) FROM Facture f WHERE f.statut = 'PAYE' AND f.dateEmission BETWEEN :startDate AND :endDate")
    java.math.BigDecimal calculateTotalRevenue(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    @Query("SELECT f FROM Facture f " +
           "LEFT JOIN FETCH f.reservation r " +
           "LEFT JOIN FETCH r.seance s " +
           "LEFT JOIN FETCH s.film film " +
           "LEFT JOIN FETCH s.salle salle " +
           "WHERE f.id = :id")
    Optional<Facture> findByIdWithAllData(@Param("id") Long id);
}