package com.cinema.repository;

import com.cinema.model.Film;
import com.cinema.model.Salle;
import com.cinema.model.Seance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DepotSeance extends JpaRepository<Seance, Long> {

    List<Seance> findByFilm(Film film);

    List<Seance> findBySalle(Salle salle);

    List<Seance> findByFilmAndSalle(Film film, Salle salle);

    @Query("SELECT s FROM Seance s WHERE DATE(s.dateHeure) = :date")
    List<Seance> findByDate(@Param("date") LocalDate date);

    @Query("SELECT s FROM Seance s WHERE DATE(s.dateHeure) = :date AND s.film.id = :filmId")
    List<Seance> findByDateAndFilm(@Param("date") LocalDate date, @Param("filmId") Long filmId);

    @Query("SELECT s FROM Seance s WHERE DATE(s.dateHeure) = :date AND s.salle.id = :salleId")
    List<Seance> findByDateAndSalle(@Param("date") LocalDate date, @Param("salleId") Long salleId);

    @Query("SELECT s FROM Seance s WHERE s.statut = :statut")
    List<Seance> findByStatut(@Param("statut") String statut);

    @Query("SELECT s FROM Seance s WHERE s.placesDisponibles >= :nombrePlaces")
    List<Seance> findSeancesAvecPlacesDisponibles(@Param("nombrePlaces") Integer nombrePlaces);

    @Query("SELECT s FROM Seance s WHERE s.film.id = :filmId AND DATE(s.dateHeure) = :date " +
           "AND s.placesDisponibles > 0 AND s.statut = 'ACTIVE' " +
           "ORDER BY s.dateHeure")
    List<Seance> findSeancesDisponiblesPourFilm(@Param("filmId") Long filmId,
                                               @Param("date") LocalDate date);

    @Query(value = "SELECT * FROM seances s WHERE s.film_id = :filmId AND s.salle_id = :salleId " +
           "AND CAST(s.date_heure AS DATE) = :date AND to_char(s.date_heure, 'HH24:MI') = :heure " +
           "AND s.statut = 'ACTIVE' LIMIT 1", nativeQuery = true)
    Seance findSeanceSpecifique(@Param("filmId") Long filmId,
                               @Param("salleId") Long salleId,
                               @Param("date") LocalDate date,
                               @Param("heure") String heure);

    // Séances à venir
    @Query("SELECT s FROM Seance s WHERE s.dateHeure > :maintenant ORDER BY s.dateHeure")
    List<Seance> findSeancesAVenir(@Param("maintenant") LocalDateTime maintenant);

    // Séances d'aujourd'hui
    @Query("SELECT s FROM Seance s WHERE DATE(s.dateHeure) = DATE(:aujourdhui) AND s.statut = 'ACTIVE' " +
           "ORDER BY s.dateHeure")
    List<Seance> findSeancesAujourdhui(@Param("aujourdhui") LocalDateTime aujourdhui);
}




