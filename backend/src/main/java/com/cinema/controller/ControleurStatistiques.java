package com.cinema.controller;

import com.cinema.model.Reservation;
import com.cinema.model.Film;
import com.cinema.model.Salle;
import com.cinema.model.ReservationPlace;
import com.cinema.repository.DepotChiffreAffaireFilm;
import com.cinema.repository.DepotReservation;
import com.cinema.repository.DepotFilm;
import com.cinema.repository.DepotSalle;
import com.cinema.repository.DepotSeance;
import com.cinema.repository.DepotReservationPlace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/statistiques")
@CrossOrigin(origins = "http://localhost:5173")
public class ControleurStatistiques {

    @Autowired
    private DepotReservation depotReservation;

    @Autowired
    private DepotFilm depotFilm;

    @Autowired
    private DepotSalle depotSalle;

    @Autowired
    private DepotSeance depotSeance;

    @Autowired
    private DepotReservationPlace depotReservationPlace;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats(
            @RequestParam(defaultValue = "MENSUEL") String periode,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateDebut,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFin) {
        
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Statistiques générales
            List<Reservation> reservations = getReservationsPeriode(dateDebut, dateFin);
            List<Film> films = depotFilm.findAll();
            List<Salle> salles = depotSalle.findAll();
            
            // Total réservations
            long totalReservations = reservations.size();
            
            // Total entrées (places) - calcul réel depuis les réservations
            int totalEntrees = 0;
            for (Reservation reservation : reservations) {
                try {
                    // Utiliser le repository pour compter les places directement
                    List<ReservationPlace> places = depotReservationPlace.findByReservationId(reservation.getId());
                    totalEntrees += places.size();
                    System.out.println("Réservation " + reservation.getId() + ": " + places.size() + " places");
                } catch (Exception e) {
                    System.out.println("Erreur lors du chargement des places pour réservation " + reservation.getId() + ": " + e.getMessage());
                    totalEntrees += 1; // Valeur par défaut en cas d'erreur
                }
            }
            
            System.out.println("Calcul entrées: " + reservations.size() + " réservations -> " + totalEntrees + " entrées");
            
            // Chiffre d'affaires total
            BigDecimal totalCA = reservations.stream()
                    .map(Reservation::getMontantTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Films actifs (avec des séances dans la période)
            List<Long> filmIdsActifs = reservations.stream()
                    .map(r -> r.getSeance().getFilm().getId())
                    .distinct()
                    .collect(Collectors.toList());
            long filmsActifs = filmIdsActifs.size();
            
            // Taux d'occupation moyen
            double tauxOccupation = calculerTauxOccupationMoyen(reservations);
            
            // Croissances (comparaison avec période précédente)
            LocalDate debutPrecedent = dateDebut.minusMonths(1);
            LocalDate finPrecedent = dateDebut.minusDays(1);
            List<Reservation> reservationsPrecedent = getReservationsPeriode(debutPrecedent, finPrecedent);
            
            long totalReservationsPrecedent = reservationsPrecedent.size();
            BigDecimal totalCAPrecedent = reservationsPrecedent.stream()
                    .map(Reservation::getMontantTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            int croissanceReservations = calculerCroissance(totalReservations, totalReservationsPrecedent);
            int croissanceCA = calculerCroissance(totalCA, totalCAPrecedent);
            
            // Films et salles nouveaux ce mois
            long nouveauxFilms = films.stream()
                    .filter(f -> f.getCreatedAt() != null && 
                            f.getCreatedAt().toLocalDate().isAfter(dateDebut.minusMonths(1)))
                    .count();
            
            stats.put("totalReservations", totalReservations);
            stats.put("totalCA", totalCA);
            stats.put("totalEntrees", totalEntrees);
            stats.put("filmsActifs", filmIdsActifs.size());
            stats.put("tauxOccupation", Math.round(tauxOccupation));
            stats.put("croissanceReservations", croissanceReservations);
            stats.put("croissanceCA", croissanceCA);
            stats.put("nouveauxFilms", nouveauxFilms);
            stats.put("croissanceOccupation", 8); // Simulé pour l'instant
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            System.err.println("Erreur lors du calcul des statistiques: " + e.getMessage());
            e.printStackTrace();
            
            // Retourner des valeurs par défaut en cas d'erreur
            stats.put("totalReservations", 0);
            stats.put("totalCA", BigDecimal.ZERO);
            stats.put("filmsActifs", 0);
            stats.put("tauxOccupation", 0);
            stats.put("croissanceReservations", 0);
            stats.put("croissanceCA", 0);
            stats.put("nouveauxFilms", 0);
            stats.put("croissanceOccupation", 0);
            
            return ResponseEntity.ok(stats);
        }
    }

    @GetMapping("/top-films")
    public ResponseEntity<List<Map<String, Object>>> getTopFilms(
            @RequestParam(defaultValue = "5") int limite,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateDebut,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFin) {
        
        List<Map<String, Object>> topFilms = new ArrayList<>();
        
        try {
            // Récupérer toutes les réservations de la période
            List<Reservation> reservations = getReservationsPeriode(dateDebut, dateFin);
            
            // Grouper par film
            Map<Film, List<Reservation>> reservationsParFilm = reservations.stream()
                    .collect(Collectors.groupingBy(r -> r.getSeance().getFilm()));
            
            // Calculer les statistiques par film
            List<Map<String, Object>> filmsStats = reservationsParFilm.entrySet().stream()
                    .map(entry -> {
                        Film film = entry.getKey();
                        List<Reservation> filmReservations = entry.getValue();
                        
                        BigDecimal caFilm = filmReservations.stream()
                                .map(Reservation::getMontantTotal)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        
                        Map<String, Object> filmStat = new HashMap<>();
                        filmStat.put("id", film.getId());
                        filmStat.put("titre", film.getTitre());
                        filmStat.put("genre", film.getGenre());
                        filmStat.put("image", film.getImageUrl());
                        filmStat.put("reservations", filmReservations.size());
                        filmStat.put("ca", caFilm);
                        filmStat.put("note", film.getNote() != null ? film.getNote() : 0.0);
                        
                        return filmStat;
                    })
                    .sorted((a, b) -> ((Long) b.get("reservations")).compareTo((Long) a.get("reservations")))
                    .limit(limite)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(filmsStats);
            
        } catch (Exception e) {
            System.err.println("Erreur lors du calcul des top films: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    @GetMapping("/top-salles")
    public ResponseEntity<List<Map<String, Object>>> getTopSalles(
            @RequestParam(defaultValue = "MENSUEL") String periode,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateDebut,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFin) {
        
        List<Map<String, Object>> topSalles = new ArrayList<>();
        
        try {
            List<Salle> salles = depotSalle.findAll();
            List<Reservation> reservations = getReservationsPeriode(dateDebut, dateFin);
            
            // Grouper les réservations par salle
            Map<Salle, List<Reservation>> reservationsParSalle = reservations.stream()
                    .filter(r -> r.getSeance() != null && r.getSeance().getSalle() != null)
                    .collect(Collectors.groupingBy(r -> r.getSeance().getSalle()));
            
            for (Map.Entry<Salle, List<Reservation>> entry : reservationsParSalle.entrySet()) {
                Salle salle = entry.getKey();
                List<Reservation> reservationsSalle = entry.getValue();
                
                if (!reservationsSalle.isEmpty()) {
                    // Calculer les statistiques pour cette salle
                    int placesReservees = reservationsSalle.stream()
                            .mapToInt(r -> r.getReservationPlaces() != null ? r.getReservationPlaces().size() : 1)
                            .sum();
                    
                    // Estimation du taux d'occupation
                    double tauxOccupation = (double) placesReservees / (salle.getCapacite() * reservationsSalle.size()) * 100;
                    
                    BigDecimal caMensuel = reservationsSalle.stream()
                            .map(Reservation::getMontantTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    Map<String, Object> salleStat = new HashMap<>();
                    salleStat.put("id", salle.getId());
                    salleStat.put("nom", salle.getNom());
                    salleStat.put("type", salle.getType());
                    salleStat.put("capacite", salle.getCapacite());
                    salleStat.put("tauxOccupation", Math.round(tauxOccupation));
                    salleStat.put("caMensuel", caMensuel);
                    
                    topSalles.add(salleStat);
                }
            }
            
            // Ajouter les salles sans réservations avec CA = 0
            for (Salle salle : salles) {
                boolean dejaAjoutee = topSalles.stream()
                        .anyMatch(s -> s.get("id").equals(salle.getId()));
                
                if (!dejaAjoutee) {
                    Map<String, Object> salleStat = new HashMap<>();
                    salleStat.put("id", salle.getId());
                    salleStat.put("nom", salle.getNom());
                    salleStat.put("type", salle.getType());
                    salleStat.put("capacite", salle.getCapacite());
                    salleStat.put("tauxOccupation", 0);
                    salleStat.put("caMensuel", BigDecimal.ZERO);
                    
                    topSalles.add(salleStat);
                }
            }
            
            // Trier par CA mensuel décroissant
            topSalles.sort((a, b) -> ((BigDecimal) b.get("caMensuel")).compareTo((BigDecimal) a.get("caMensuel")));
            
            return ResponseEntity.ok(topSalles);
            
        } catch (Exception e) {
            System.err.println("Erreur lors du calcul des top salles: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    @GetMapping("/repartition-genre")
    public ResponseEntity<List<Map<String, Object>>> getRepartitionGenre(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateDebut,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFin) {
        
        List<Map<String, Object>> repartition = new ArrayList<>();
        
        try {
            List<Reservation> reservations = getReservationsPeriode(dateDebut, dateFin);
            
            // Grouper par genre
            Map<String, List<Reservation>> reservationsParGenre = reservations.stream()
                    .filter(r -> r.getSeance().getFilm().getGenre() != null)
                    .collect(Collectors.groupingBy(r -> r.getSeance().getFilm().getGenre()));
            
            // Calculer les pourcentages basés sur le nombre d'entrées (places)
            int totalEntrees = 0;
            for (Reservation reservation : reservations) {
                try {
                    List<ReservationPlace> places = depotReservationPlace.findByReservationId(reservation.getId());
                    totalEntrees += places.size();
                } catch (Exception e) {
                    totalEntrees += 1;
                }
            }
            
            for (Map.Entry<String, List<Reservation>> entry : reservationsParGenre.entrySet()) {
                String genre = entry.getKey();
                List<Reservation> reservationsGenre = entry.getValue();
                
                // Calculer le nombre d'entrées pour ce genre
                int entreesGenre = 0;
                for (Reservation reservation : reservationsGenre) {
                    try {
                        List<ReservationPlace> places = depotReservationPlace.findByReservationId(reservation.getId());
                        entreesGenre += places.size();
                    } catch (Exception e) {
                        entreesGenre += 1;
                    }
                }
                
                BigDecimal pourcentage = totalEntrees > 0 ?
                        BigDecimal.valueOf(entreesGenre)
                                .divide(BigDecimal.valueOf(totalEntrees), 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;
                
                Map<String, Object> genreStat = new HashMap<>();
                genreStat.put("genre", genre);
                genreStat.put("nombre", entreesGenre);
                genreStat.put("nombreEntrees", entreesGenre);
                genreStat.put("pourcentage", pourcentage.doubleValue());
                repartition.add(genreStat);
            }
            
            // Trier par nombre décroissant
            repartition.sort((a, b) -> ((Integer) b.get("nombre")).compareTo((Integer) a.get("nombre")));
            
            return ResponseEntity.ok(repartition);
            
        } catch (Exception e) {
            System.err.println("Erreur lors du calcul de la répartition par genre: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    @GetMapping("/evolution-ca")
    public ResponseEntity<List<Map<String, Object>>> getEvolutionCA(
            @RequestParam(defaultValue = "MENSUEL") String periode,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateDebut,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFin) {
        
        List<Map<String, Object>> evolution = new ArrayList<>();
        
        try {
            // Pour l'instant, simulation de données mensuelles
            List<LocalDate> dates = generateDatesPeriode(periode, dateDebut, dateFin);
            
            for (LocalDate date : dates) {
                LocalDate finPeriode = getFinPeriode(periode, date);
                List<Reservation> reservationsPeriode = getReservationsPeriode(date, finPeriode);
                
                BigDecimal caPeriode = reservationsPeriode.stream()
                        .map(Reservation::getMontantTotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                Map<String, Object> point = new HashMap<>();
                point.put("date", date.toString());
                point.put("ca", caPeriode);
                point.put("reservations", reservationsPeriode.size());
                
                evolution.add(point);
            }
            
            return ResponseEntity.ok(evolution);
            
        } catch (Exception e) {
            System.err.println("Erreur lors du calcul de l'évolution du CA: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // Méthodes utilitaires
    private List<Reservation> getReservationsPeriode(LocalDate dateDebut, LocalDate dateFin) {
        return depotReservation.findAll().stream()
                .filter(r -> r.getDateReservation() != null)
                .filter(r -> !r.getDateReservation().toLocalDate().isBefore(dateDebut) &&
                           !r.getDateReservation().toLocalDate().isAfter(dateFin))
                .collect(Collectors.toList());
    }

    private double calculerTauxOccupationMoyen(List<Reservation> reservations) {
        if (reservations.isEmpty()) return 0.0;
        
        int totalPlacesReservees = reservations.stream()
                .mapToInt(r -> r.getReservationPlaces() != null ? r.getReservationPlaces().size() : 1)
                .sum();
        
        // Estimation : en moyenne 10 places par réservation (à adapter selon votre logique)
        int totalPlacesDisponibles = reservations.size() * 10;
        
        return totalPlacesDisponibles > 0 ? (double) totalPlacesReservees / totalPlacesDisponibles * 100 : 0.0;
    }

    private int calculerCroissance(long actuel, long precedent) {
        if (precedent == 0) return actuel > 0 ? 100 : 0;
        return (int) Math.round(((double) (actuel - precedent) / precedent) * 100);
    }

    private int calculerCroissance(BigDecimal actuel, BigDecimal precedent) {
        if (precedent.compareTo(BigDecimal.ZERO) == 0) {
            return actuel.compareTo(BigDecimal.ZERO) > 0 ? 100 : 0;
        }
        return actuel.subtract(precedent)
                .divide(precedent, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .intValue();
    }

    private List<LocalDate> generateDatesPeriode(String periode, LocalDate dateDebut, LocalDate dateFin) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = dateDebut;
        
        while (!current.isAfter(dateFin)) {
            dates.add(current);
            
            switch (periode) {
                case "JOURNALIER":
                    current = current.plusDays(1);
                    break;
                case "HEBDOMADAIRE":
                    current = current.plusWeeks(1);
                    break;
                case "MENSUEL":
                    current = current.plusMonths(1);
                    break;
                case "ANNUEL":
                    current = current.plusYears(1);
                    break;
            }
        }
        
        return dates;
    }

    private LocalDate getFinPeriode(String periode, LocalDate dateDebut) {
        switch (periode) {
            case "JOURNALIER":
                return dateDebut;
            case "HEBDOMADAIRE":
                return dateDebut.plusDays(6);
            case "MENSUEL":
                return YearMonth.from(dateDebut).atEndOfMonth();
            case "ANNUEL":
                return dateDebut.withDayOfYear(365).isLeapYear() ? 
                        dateDebut.withDayOfYear(366) : dateDebut.withDayOfYear(365);
            default:
                return dateDebut;
        }
    }
}
