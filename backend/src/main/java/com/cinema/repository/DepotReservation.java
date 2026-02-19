package com.cinema.repository;

import com.cinema.model.Reservation;
import com.cinema.model.Seance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DepotReservation extends JpaRepository<Reservation, Long> {

    List<Reservation> findBySeance(Seance seance);

    List<Reservation> findByNomClient(String nomClient);

    List<Reservation> findByEmailClient(String emailClient);

    Reservation findByReferenceReservation(String referenceReservation);

    @Query("SELECT r FROM Reservation r WHERE r.statut = :statut")
    List<Reservation> findByStatut(@Param("statut") String statut);

    @Query("SELECT r FROM Reservation r WHERE r.seance.film.id = :filmId")
    List<Reservation> findByFilm(@Param("filmId") Long filmId);

    @Query("SELECT r FROM Reservation r WHERE r.seance.salle.id = :salleId")
    List<Reservation> findBySalle(@Param("salleId") Long salleId);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.seance.id = :seanceId AND r.statut = 'CONFIRMEE'")
    Long countReservationsConfirmeesPourSeance(@Param("seanceId") Long seanceId);

    @Query("SELECT COUNT(rp) FROM ReservationPlace rp WHERE rp.reservation.seance.id = :seanceId AND rp.reservation.statut = 'CONFIRMEE'")
    Long countPlacesReserveesPourSeance(@Param("seanceId") Long seanceId);

    @Query("SELECT r FROM Reservation r WHERE r.dateReservation >= :debut AND r.dateReservation <= :fin")
    List<Reservation> findReservationsPeriode(@Param("debut") LocalDateTime debut,
                                             @Param("fin") LocalDateTime fin);

    // Réservations récentes
    @Query("SELECT r FROM Reservation r ORDER BY r.dateReservation DESC")
    List<Reservation> findReservationsRecentes();

    // Chiffre d'affaires total
    @Query("SELECT SUM(r.montantTotal) FROM Reservation r WHERE r.statut = 'CONFIRMEE'")
    Double getChiffreAffairesTotal();

    // Chiffre d'affaires pour une période
    @Query("SELECT SUM(r.montantTotal) FROM Reservation r WHERE r.statut = 'CONFIRMEE' " +
           "AND r.dateReservation >= :debut AND r.dateReservation <= :fin")
    Double getChiffreAffairesPeriode(@Param("debut") LocalDateTime debut,
                                    @Param("fin") LocalDateTime fin);
}




