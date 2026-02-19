package com.cinema.controller;

import com.cinema.dto.ChiffreAffaireDTO;
import com.cinema.service.ChiffreAffaireService;
import com.cinema.service.ReinitialisationSallesService;
import com.cinema.repository.DepotChiffreAffaireFilm;
import com.cinema.repository.DepotReservation;
import com.cinema.repository.DepotFilm;
import com.cinema.repository.DepotReservationPlace;
import com.cinema.model.Reservation;
import com.cinema.model.Film;
import com.cinema.model.ReservationPlace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@RestController
@RequestMapping("/api/chiffre-affaires")
@CrossOrigin(origins = "http://localhost:5173")
public class ControleurChiffreAffaires {

    @Autowired
    private ChiffreAffaireService chiffreAffaireService;

    @Autowired
    private ReinitialisationSallesService reinitialisationSallesService;

    @Autowired
    private DepotChiffreAffaireFilm depotChiffreAffaireFilm;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private DepotReservation depotReservation;

    @Autowired
    private DepotFilm depotFilm;

    @Autowired
    private DepotReservationPlace depotReservationPlace;

    @GetMapping("/films/{filmId}")
    public ResponseEntity<List<ChiffreAffaireDTO>> getChiffreAffaireParFilm(
            @PathVariable Long filmId,
            @RequestParam String periode,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateDebut,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFin) {
        
        List<ChiffreAffaireDTO> ca = chiffreAffaireService.getChiffreAffaireParFilm(filmId, periode, dateDebut, dateFin);
        return ResponseEntity.ok(ca);
    }

    @GetMapping("/tous-les-films")
    public ResponseEntity<List<ChiffreAffaireDTO>> getChiffreAffaireTousFilms(
            @RequestParam String periode,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateDebut,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFin) {
        
        List<ChiffreAffaireDTO> ca = chiffreAffaireService.getChiffreAffaireTousFilms(periode, dateDebut, dateFin);
        return ResponseEntity.ok(ca);
    }

    @GetMapping("/statistiques-generales")
    public ResponseEntity<Map<String, Object>> getStatistiquesGenerales(
            @RequestParam String periode,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateDebut,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFin) {
        
        Map<String, Object> stats = chiffreAffaireService.getStatistiquesGenerales(periode, dateDebut, dateFin);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/top-films")
    public ResponseEntity<List<Map<String, Object>>> getTopFilms(
            @RequestParam String periode,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateDebut,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFin,
            @RequestParam(defaultValue = "10") int limite) {
        
        List<Map<String, Object>> topFilms = chiffreAffaireService.getTopFilms(periode, dateDebut, dateFin, limite);
        return ResponseEntity.ok(topFilms);
    }

    @GetMapping("/par-categorie")
    public ResponseEntity<List<Map<String, Object>>> getChiffreAffaireParCategorie(
            @RequestParam String periode,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateDebut,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFin) {
        
        List<Map<String, Object>> caParCategorie = chiffreAffaireService.getChiffreAffaireParCategorie(periode, dateDebut, dateFin);
        return ResponseEntity.ok(caParCategorie);
    }

    @GetMapping("/par-genre")
    public ResponseEntity<List<Map<String, Object>>> getChiffreAffaireParGenre(
            @RequestParam String periode,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateDebut,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFin) {
        
        List<Map<String, Object>> caParGenre = chiffreAffaireService.getChiffreAffaireParGenre(periode, dateDebut, dateFin);
        return ResponseEntity.ok(caParGenre);
    }

    @GetMapping("/evolution-temporelle")
    public ResponseEntity<List<Map<String, Object>>> getEvolutionTemporelle(
            @PathVariable Long filmId,
            @RequestParam String periode,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateDebut,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFin) {
        
        List<Map<String, Object>> evolution = chiffreAffaireService.getEvolutionTemporelle(filmId, periode, dateDebut, dateFin);
        return ResponseEntity.ok(evolution);
    }

    @GetMapping("/check-data")
    public ResponseEntity<Map<String, Object>> checkData() {
        Map<String, Object> result = new HashMap<>();
        
        // Vérifier s'il y a des réservations
        long totalReservations = depotReservation.count();
        result.put("totalReservations", totalReservations);
        
        // Vérifier s'il y a des chiffres d'affaires
        long totalCA = depotChiffreAffaireFilm.count();
        result.put("totalChiffreAffaires", totalCA);
        
        // Vérifier s'il y a des films
        long totalFilms = depotFilm.count();
        result.put("totalFilms", totalFilms);
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/test-data")
    public ResponseEntity<List<Map<String, Object>>> getTestData() {
        List<Map<String, Object>> testData = new ArrayList<>();
        
        try {
            // Récupérer les réservations CONFIRMÉES réelles
            List<Reservation> reservations = depotReservation.findAll().stream()
                    .filter(r -> "CONFIRMEE".equals(r.getStatut()))  // Uniquement les confirmées
                    .collect(Collectors.toList());
            System.out.println("Nombre de réservations confirmées trouvées pour test-data: " + reservations.size());
            
            if (!reservations.isEmpty()) {
                // Grouper par film pour calculer les vraies statistiques
                Map<Long, List<Reservation>> reservationsParFilm = reservations.stream()
                        .filter(r -> r.getSeance() != null && r.getSeance().getFilm() != null)
                        .collect(Collectors.groupingBy(r -> r.getSeance().getFilm().getId()));
                
                for (Map.Entry<Long, List<Reservation>> entry : reservationsParFilm.entrySet()) {
                    Long filmId = entry.getKey();
                    List<Reservation> filmReservations = entry.getValue();
                    
                    // Calculer le CA réel pour ce film
                    BigDecimal caFilm = filmReservations.stream()
                            .map(Reservation::getMontantTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    // Calculer le nombre réel d'entrées (places) pour ce film
                    int totalEntrees = 0;
                    for (Reservation reservation : filmReservations) {
                        try {
                            List<ReservationPlace> places = depotReservationPlace.findByReservationId(reservation.getId());
                            totalEntrees += places.size();
                        } catch (Exception e) {
                            System.out.println("Erreur lors du chargement des places pour réservation " + reservation.getId() + ": " + e.getMessage());
                            totalEntrees += 1; // Valeur par défaut
                        }
                    }
                    
                    // Récupérer les infos du film
                    Optional<Film> filmOpt = depotFilm.findById(filmId);
                    if (filmOpt.isPresent()) {
                        Film film = filmOpt.get();
                        
                        Map<String, Object> ca = new HashMap<>();
                        ca.put("id", filmId);
                        ca.put("filmId", filmId);
                        ca.put("filmTitre", film.getTitre());
                        ca.put("dateCalcul", LocalDate.now());
                        ca.put("typePeriode", "MENSUEL");
                        ca.put("chiffreAffaire", caFilm);
                        ca.put("nombreEntrees", totalEntrees); // Utiliser le vrai calcul
                        testData.add(ca);
                        
                        System.out.println("CA réel pour film '" + film.getTitre() + "': " + caFilm + "€ (" + totalEntrees + " entrées)");
                    }
                }
            } else {
                // Données de test par défaut si aucune réservation
                Map<String, Object> ca1 = new HashMap<>();
                ca1.put("id", 1L);
                ca1.put("filmId", 1L);
                ca1.put("filmTitre", "Aucune réservation");
                ca1.put("dateCalcul", LocalDate.now());
                ca1.put("typePeriode", "MENSUEL");
                ca1.put("chiffreAffaire", BigDecimal.ZERO);
                ca1.put("nombreEntrees", 0);
                testData.add(ca1);
            }
            
            System.out.println("Données de test générées: " + testData.size() + " enregistrements");
            return ResponseEntity.ok(testData);
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la génération des données de test: " + e.getMessage());
            e.printStackTrace();
            
            // Retourner une donnée par défaut en cas d'erreur
            Map<String, Object> defaultCa = new HashMap<>();
            defaultCa.put("id", 0L);
            defaultCa.put("filmId", 0L);
            defaultCa.put("filmTitre", "Erreur de chargement");
            defaultCa.put("dateCalcul", LocalDate.now());
            defaultCa.put("typePeriode", "MENSUEL");
            defaultCa.put("chiffreAffaire", BigDecimal.ZERO);
            defaultCa.put("nombreEntrees", 0);
            
            testData.add(defaultCa);
            return ResponseEntity.ok(testData);
        }
    }

    @PostMapping("/recalculer")
    public ResponseEntity<Map<String, Object>> recalculerChiffreAffaire(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateDebut,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFin) {
        
        Map<String, Object> result = chiffreAffaireService.recalculerChiffreAffaire(dateDebut, dateFin);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exporterChiffreAffairePDF(
            @RequestParam String periode,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateDebut,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFin,
            @RequestParam(required = false) Long filmId) {
        
        byte[] pdfData = chiffreAffaireService.exporterChiffreAffairePDF(periode, dateDebut, dateFin, filmId);
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=\"chiffre-affaire.pdf\"")
                .body(pdfData);
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exporterChiffreAffaireExcel(
            @RequestParam String periode,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateDebut,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFin,
            @RequestParam(required = false) Long filmId) {
        
        byte[] excelData = chiffreAffaireService.exporterChiffreAffaireExcel(periode, dateDebut, dateFin, filmId);
        return ResponseEntity.ok()
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header("Content-Disposition", "attachment; filename=\"chiffre-affaire.xlsx\"")
                .body(excelData);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData(
            @RequestParam(defaultValue = "MOIS") String periode,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateDebut,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFin) {
        
        Map<String, Object> dashboard = chiffreAffaireService.getDashboardData(periode, dateDebut, dateFin);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/fix-sequence")
    public ResponseEntity<String> fixSequence() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Récupérer le max ID actuel
            ResultSet rs = stmt.executeQuery("SELECT MAX(id) FROM seances");
            long maxId = 0;
            if (rs.next()) {
                maxId = rs.getLong(1);
            }
            
            // Mettre à jour la séquence
            stmt.executeUpdate("ALTER SEQUENCE seances_id_seq RESTART WITH " + (maxId + 1));
            
            return ResponseEntity.ok("Séquence corrigée. Prochain ID: " + (maxId + 1));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur: " + e.getMessage());
        }
    }

    @GetMapping("/revenu-reel-par-seance")
    public ResponseEntity<List<Map<String, Object>>> getRevenuReelParSeance() {
        List<Map<String, Object>> revenus = chiffreAffaireService.getRevenuReelParSeance();
        
        // Ajouter le CA publicité pour chaque séance
        for (Map<String, Object> seance : revenus) {
            try {
                Long seanceId = ((Number) seance.get("seanceId")).longValue();
                
                // Calculer le CA publicité pour cette séance
                String sql = "SELECT COALESCE(SUM(prix_unitaire), 0) as ca_pub FROM diffusion_pub WHERE id_sceance = ?";
                
                try (Connection conn = dataSource.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, seanceId);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        seance.put("caPublicite", rs.getDouble("ca_pub"));
                    }
                }
            } catch (Exception e) {
                seance.put("caPublicite", 0.0);
            }
        }
        
        return ResponseEntity.ok(revenus);
    }

    @GetMapping("/revenu-max-par-seance")
    public ResponseEntity<List<Map<String, Object>>> getRevenuMaxParSeance() {
        List<Map<String, Object>> revenus = chiffreAffaireService.getRevenuMaxParSeance();
        return ResponseEntity.ok(revenus);
    }

    @GetMapping("/revenu-max-par-salle")
    public ResponseEntity<List<Map<String, Object>>> getRevenuMaxParSalle() {
        List<Map<String, Object>> revenus = chiffreAffaireService.getRevenuMaxParSalle();
        return ResponseEntity.ok(revenus);
    }

    @PostMapping("/reinitialiser-salles")
    public ResponseEntity<Map<String, Object>> reinitialiserSalles() {
        Map<String, Object> result = reinitialisationSallesService.reinitialiserToutesLesSalles();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/par-film")
    public ResponseEntity<List<Map<String, Object>>> getChiffreAffairesParFilm(
            @RequestParam(required = false) String dateDebut,
            @RequestParam(required = false) String dateFin,
            @RequestParam(required = false) String periode,
            @RequestParam(required = false) Long filmId) {
        
        List<Map<String, Object>> ca = chiffreAffaireService.getChiffreAffairesParFilm(dateDebut, dateFin, periode, filmId);
        return ResponseEntity.ok(ca);
    }

    @GetMapping("/film/{filmId}/seances")
    public ResponseEntity<List<ChiffreAffaireDTO>> getChiffreAffaireParFilmEtSeance(
            @PathVariable Long filmId,
            @RequestParam("mois") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate mois) {
        try {
            List<ChiffreAffaireDTO> result = chiffreAffaireService.calculerChiffreAffaireParFilmEtSeance(filmId, mois);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
