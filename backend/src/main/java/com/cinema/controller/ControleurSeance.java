package com.cinema.controller;

import com.cinema.model.Film;
import com.cinema.model.Salle;
import com.cinema.model.Seance;
import com.cinema.repository.DepotFilm;
import com.cinema.repository.DepotSalle;
import com.cinema.repository.DepotSeance;
import com.cinema.repository.DepotReservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/seances")
@CrossOrigin(origins = "*")
public class ControleurSeance {

    @Autowired
    private DepotSeance depotSeance;

    @Autowired
    private DepotFilm depotFilm;

    @Autowired
    private DepotSalle depotSalle;

    @Autowired
    private DepotReservation depotReservation;

    @GetMapping
    public List<Seance> getToutesLesSeances() {
        return depotSeance.findAll();
    }

    @GetMapping("/toutes")
    public List<Map<String, Object>> getToutesLesSeancesAvecDetails() {
        List<Seance> seances = depotSeance.findAll();
        
        return seances.stream()
            .sorted((s1, s2) -> s2.getDateHeure().compareTo(s1.getDateHeure()))
            .map(seance -> {
                Map<String, Object> seanceMap = new HashMap<>();
                seanceMap.put("id", seance.getId());
                seanceMap.put("dateHeure", seance.getDateHeure());
                
                // Récupérer les infos du film et salle
                String filmTitre = "Film inconnu";
                String salleNom = "Salle inconnue";
                
                try {
                    if (seance.getFilm() != null) {
                        filmTitre = seance.getFilm().getTitre();
                    }
                } catch (Exception e) {
                    // Ignorer les erreurs de chargement LAZY
                }
                
                try {
                    if (seance.getSalle() != null) {
                        salleNom = seance.getSalle().getNom();
                    }
                } catch (Exception e) {
                    // Ignorer les erreurs de chargement LAZY
                }
                
                seanceMap.put("filmTitre", filmTitre);
                seanceMap.put("salleNom", salleNom);
                return seanceMap;
            })
            .toList();
    }

    @GetMapping("/toutes-simple")
    public List<Seance> getToutesLesSeancesSimple() {
        List<Seance> seances = depotSeance.findAll();
        seances.sort((s1, s2) -> s2.getDateHeure().compareTo(s1.getDateHeure()));
        return seances;
    }

    @GetMapping("/film/{filmId}")
    public List<Seance> getSeancesParFilm(@PathVariable Long filmId) {
        Optional<Film> film = depotFilm.findById(filmId);
        return film.map(depotSeance::findByFilm).orElse(List.of());
    }

    @GetMapping("/salle/{salleId}")
    public List<Seance> getSeancesParSalle(@PathVariable Long salleId) {
        Optional<Salle> salle = depotSalle.findById(salleId);
        return salle.map(depotSeance::findBySalle).orElse(List.of());
    }

    @GetMapping("/date/{date}")
    public List<Seance> getSeancesParDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return depotSeance.findByDate(date);
    }

    @GetMapping("/film/{filmId}/date/{date}")
    public List<Seance> getSeancesParFilmEtDate(@PathVariable Long filmId,
                                               @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return depotSeance.findByDateAndFilm(date, filmId);
    }

    @GetMapping("/disponibles/film/{filmId}/date/{date}")
    public List<Seance> getSeancesDisponibles(@PathVariable Long filmId,
                                             @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return depotSeance.findSeancesDisponiblesPourFilm(filmId, date);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Seance> getSeanceParId(@PathVariable Long id) {
        Optional<Seance> seance = depotSeance.findById(id);
        if (seance.isPresent()) {
            return ResponseEntity.ok(seance.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/aujourdhui")
    public List<Seance> getSeancesAujourdhui() {
        return depotSeance.findSeancesAujourdhui(LocalDateTime.now());
    }

    @GetMapping("/avenir")
    public List<Seance> getSeancesAVenir() {
        return depotSeance.findSeancesAVenir(LocalDateTime.now());
    }

    @PostMapping
    public ResponseEntity<?> creerSeance(@RequestBody Seance seance) {
        try {
            // Vérifier que le film existe
            if (seance.getFilm() == null || seance.getFilm().getId() == null) {
                return ResponseEntity.badRequest().body("Film requis");
            }
            Optional<Film> film = depotFilm.findById(seance.getFilm().getId());
            if (film.isEmpty()) {
                return ResponseEntity.badRequest().body("Film non trouvé");
            }

            // Vérifier que la salle existe
            if (seance.getSalle() == null || seance.getSalle().getId() == null) {
                return ResponseEntity.badRequest().body("Salle requise");
            }
            Optional<Salle> salle = depotSalle.findById(seance.getSalle().getId());
            if (salle.isEmpty()) {
                return ResponseEntity.badRequest().body("Salle non trouvée");
            }

            // Vérifier que la date/heure est dans le futur
            if (seance.getDateHeure().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body("La séance doit être dans le futur");
            }

            // Initialiser les places disponibles avec la capacité de la salle
            seance.setPlacesDisponibles(salle.get().getCapacite());
            seance.setCreatedAt(LocalDateTime.now());
            seance.setUpdatedAt(LocalDateTime.now());

            Seance seanceCreee = depotSeance.save(seance);
            return ResponseEntity.ok(seanceCreee);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la création de la séance: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modifierSeance(@PathVariable Long id, @RequestBody Seance detailsSeance) {
        Optional<Seance> seanceOptional = depotSeance.findById(id);
        if (seanceOptional.isPresent()) {
            Seance seance = seanceOptional.get();

            // Ne pas permettre la modification si la séance est passée
            if (seance.getDateHeure().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body("Impossible de modifier une séance passée");
            }

            // Mettre à jour les champs modifiables
            if (detailsSeance.getPrix() != null) {
                seance.setPrix(detailsSeance.getPrix());
            }
            if (detailsSeance.getStatut() != null) {
                seance.setStatut(detailsSeance.getStatut());
            }

            seance.setUpdatedAt(LocalDateTime.now());
            return ResponseEntity.ok(depotSeance.save(seance));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> supprimerSeance(@PathVariable Long id) {
        Optional<Seance> seanceOptional = depotSeance.findById(id);
        if (seanceOptional.isPresent()) {
            Seance seance = seanceOptional.get();

            // Vérifier s'il y a des réservations pour cette séance
            Long nombreReservations = depotReservation.countReservationsConfirmeesPourSeance(id);
            if (nombreReservations != null && nombreReservations > 0) {
                return ResponseEntity.badRequest().body("Impossible de supprimer une séance avec des réservations");
            }

            depotSeance.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
