package com.cinema.controller;

import com.cinema.model.Film;
import com.cinema.model.Reservation;
import com.cinema.model.Salle;
import com.cinema.model.Seance;
import com.cinema.repository.DepotFilm;
import com.cinema.repository.DepotReservation;
import com.cinema.repository.DepotSalle;
import com.cinema.repository.DepotSeance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Contrôleur principal pour les fonctionnalités de cinéma
 * Point d'entrée principal pour la gestion des réservations
 */
@RestController
@RequestMapping("/api/cinema")
@CrossOrigin(origins = "*")
public class ControleurCinema {

    @Autowired
    private DepotFilm depotFilm;

    @Autowired
    private DepotSalle depotSalle;

    @Autowired
    private DepotSeance depotSeance;

    @Autowired
    private DepotReservation depotReservation;

    /**
     * Recherche de séances disponibles
     * GET /api/cinema/rechercher?filmId=1&date=2024-01-10
     */
    @GetMapping("/rechercher")
    public ResponseEntity<?> rechercherSeances(
            @RequestParam(required = false) Long filmId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "1") Integer nombrePlaces) {

        try {
            List<Seance> seances;

            if (filmId != null && date != null) {
                // Recherche par film et date
                seances = depotSeance.findSeancesDisponiblesPourFilm(filmId, date);
            } else if (filmId != null) {
                // Recherche par film uniquement
                seances = depotSeance.findByFilm(depotFilm.findById(filmId).orElse(null));
                seances = seances.stream()
                    .filter(s -> s.getPlacesDisponibles() > 0 && "ACTIVE".equals(s.getStatut()) && s.getDateHeure().isAfter(java.time.LocalDateTime.now()))
                    .toList();
            } else if (date != null) {
                // Recherche par date uniquement
                seances = depotSeance.findByDate(date);
                seances = seances.stream()
                    .filter(s -> s.getPlacesDisponibles() > 0 && "ACTIVE".equals(s.getStatut()))
                    .toList();
            } else {
                // Toutes les séances disponibles
                seances = depotSeance.findAll();
                seances = seances.stream()
                    .filter(s -> s.getPlacesDisponibles() > 0 && "ACTIVE".equals(s.getStatut()) && s.getDateHeure().isAfter(java.time.LocalDateTime.now()))
                    .toList();
            }

            return ResponseEntity.ok(seances);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la recherche: " + e.getMessage());
        }
    }

    /**
     * Réserver des places pour une séance spécifique
     * POST /api/cinema/reserver
     */
    /**
     * Méthode de réservation obsolète - déplacée vers ControleurReservation
     * Utilisez maintenant POST /api/reservations avec sélection de places spécifiques
     */

    /**
     * Trouver une séance spécifique par film, date, heure et salle
     * GET /api/cinema/seance?filmId=1&date=2024-01-10&heure=10:00&salleId=1
     */
    @GetMapping("/seance")
    public ResponseEntity<?> trouverSeance(
            @RequestParam Long filmId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String heure,
            @RequestParam Long salleId) {

        try {
            Seance seance = depotSeance.findSeanceSpecifique(filmId, salleId, date, heure);

            if (seance != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("seance", seance);
                response.put("film", seance.getFilm());
                response.put("salle", seance.getSalle());
                response.put("placesDisponibles", seance.getPlacesDisponibles());

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la recherche de séance: " + e.getMessage());
        }
    }

    /**
     * Obtenir les films disponibles
     */
    @GetMapping("/films")
    public List<Film> getFilmsDisponibles() {
        return depotFilm.findAll();
    }

    /**
     * Obtenir les salles disponibles
     */
    @GetMapping("/salles")
    public List<Salle> getSallesDisponibles() {
        return depotSalle.findAll();
    }

    /**
     * Informations générales sur le cinéma
     */
    @GetMapping("/info")
    public ResponseEntity<?> getInfoCinema() {
        try {
            Map<String, Object> info = new HashMap<>();
            info.put("totalFilms", depotFilm.count());
            info.put("totalSalles", depotSalle.count());
            info.put("totalSeances", depotSeance.count());
            info.put("totalReservations", depotReservation.count());
            info.put("chiffreAffaires", depotReservation.getChiffreAffairesTotal());

            return ResponseEntity.ok(info);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la récupération des informations");
        }
    }

    /**
     * Génère une référence de réservation unique
     */
    private String genererReferenceReservation() {
        // Format: RES + YYMMDD + numéro séquentiel
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
        String datePart = LocalDate.now().format(formatter);

        // Générer un numéro unique (simple pour l'instant)
        long timestamp = System.currentTimeMillis();
        String sequence = String.format("%06d", (int)(timestamp % 1000000));

        return "RES" + datePart + sequence;
    }

}


