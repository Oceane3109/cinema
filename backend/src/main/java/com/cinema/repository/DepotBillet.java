package com.cinema.repository;

import com.cinema.model.Billet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepotBillet extends JpaRepository<Billet, Long> {

    List<Billet> findByReservationId(Long reservationId);

    Optional<Billet> findByReferenceBillet(String referenceBillet);

    Optional<Billet> findByCodeQr(String codeQr);

    @Query("SELECT b FROM Billet b WHERE b.reservation.seance.id = :seanceId AND b.statut = 'ACTIF'")
    List<Billet> findActiveBilletsBySeance(@Param("seanceId") Long seanceId);

    @Query("SELECT COUNT(b) FROM Billet b WHERE b.reservation.seance.id = :seanceId AND b.statut = 'UTILISE'")
    Long countUsedBilletsBySeance(@Param("seanceId") Long seanceId);

    @Query("SELECT b FROM Billet b " +
           "LEFT JOIN FETCH b.reservation r " +
           "LEFT JOIN FETCH r.seance s " +
           "LEFT JOIN FETCH s.film f " +
           "LEFT JOIN FETCH s.salle salle " +
           "LEFT JOIN FETCH b.place p " +
           "WHERE b.id = :id")
    Optional<Billet> findByIdWithAllData(@Param("id") Long id);
}