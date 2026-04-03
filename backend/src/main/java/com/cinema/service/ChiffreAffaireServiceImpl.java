package com.cinema.service;

import com.cinema.dto.ChiffreAffaireDTO;
import com.cinema.model.*;
import com.cinema.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.sql.DataSource;

@Service
@Transactional
public class ChiffreAffaireServiceImpl implements ChiffreAffaireService {

    @Autowired
    private DepotChiffreAffaireFilm depotChiffreAffaireFilm;

    @Autowired
    private DepotFilm depotFilm;
    
    @Autowired
    private DataSource dataSource;

    @Autowired
    private DepotReservation depotReservation;

    @Autowired
    private DepotReservationPlace depotReservationPlace;

    @Autowired
    private DepotSeance depotSeance;

    @Autowired
    private DepotPlace depotPlace;

    @Autowired
    private DepotSalle depotSalle;

    @Autowired
    private DepotTarif depotTarif;

    @Override
    public List<ChiffreAffaireDTO> getChiffreAffaireParFilm(Long filmId, String periode, LocalDate dateDebut, LocalDate dateFin) {
        List<ChiffreAffaireFilm> caFilms = depotChiffreAffaireFilm.findByFilmIdAndDateCalculBetweenAndTypePeriode(
                filmId, dateDebut, dateFin, periode);
        
        return caFilms.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChiffreAffaireDTO> getChiffreAffaireTousFilms(String periode, LocalDate dateDebut, LocalDate dateFin) {
        List<ChiffreAffaireFilm> caFilms = depotChiffreAffaireFilm.findByDateCalculBetweenAndTypePeriode(
                dateDebut, dateFin, periode);
        
        return caFilms.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getStatistiquesGenerales(String periode, LocalDate dateDebut, LocalDate dateFin) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Récupérer les réservations CONFIRMÉES réelles pour la période
            List<Reservation> reservations = depotReservation.findAll().stream()
                    .filter(r -> r.getDateReservation() != null)
                    .filter(r -> "CONFIRMEE".equals(r.getStatut()))  // Uniquement les confirmées
                    .filter(r -> !r.getDateReservation().toLocalDate().isBefore(dateDebut) &&
                               !r.getDateReservation().toLocalDate().isAfter(dateFin))
                    .collect(Collectors.toList());
            
            System.out.println("Statistiques CA - Réservations trouvées: " + reservations.size());
            
            // Calculer le CA réel depuis les réservations
            BigDecimal totalCA = reservations.stream()
                    .map(Reservation::getMontantTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Calculer les entrées réelles depuis les réservations
            int totalEntrees = 0;
            for (Reservation reservation : reservations) {
                try {
                    List<ReservationPlace> places = depotReservationPlace.findByReservationId(reservation.getId());
                    totalEntrees += places.size();
                } catch (Exception e) {
                    System.out.println("Erreur lors du chargement des places pour réservation " + reservation.getId() + ": " + e.getMessage());
                    totalEntrees += 1;
                }
            }
            
            // Calculer le tarif moyen
            BigDecimal tarifMoyen = totalEntrees > 0 ? 
                    totalCA.divide(BigDecimal.valueOf(totalEntrees), 2, RoundingMode.HALF_UP) : 
                    BigDecimal.ZERO;
            
            // Compter les films uniques
            Set<Long> filmIds = reservations.stream()
                    .filter(r -> r.getSeance() != null && r.getSeance().getFilm() != null)
                    .map(r -> r.getSeance().getFilm().getId())
                    .collect(Collectors.toSet());
            
            stats.put("totalChiffreAffaire", totalCA);
            stats.put("totalEntrees", totalEntrees);
            stats.put("tarifMoyen", tarifMoyen);
            stats.put("nombreFilms", filmIds.size());
            
            System.out.println("Statistiques CA - CA: " + totalCA + ", Entrées: " + totalEntrees + ", Films: " + filmIds.size());
            
        } catch (Exception e) {
            System.err.println("Erreur lors du calcul des statistiques générales: " + e.getMessage());
            e.printStackTrace();
            
            // Valeurs par défaut en cas d'erreur
            stats.put("totalChiffreAffaire", BigDecimal.ZERO);
            stats.put("totalEntrees", 0);
            stats.put("tarifMoyen", BigDecimal.ZERO);
            stats.put("nombreFilms", 0);
        }
        
        return stats;
    }

    @Override
    public List<Map<String, Object>> getTopFilms(String periode, LocalDate dateDebut, LocalDate dateFin, int limite) {
        try {
            // Récupérer les réservations réelles pour la période
            List<Reservation> reservations = depotReservation.findAll().stream()
                    .filter(r -> r.getDateReservation() != null)
                    .filter(r -> !r.getDateReservation().toLocalDate().isBefore(dateDebut) &&
                               !r.getDateReservation().toLocalDate().isAfter(dateFin))
                    .collect(Collectors.toList());
            
            // Grouper par film pour calculer les vraies statistiques
            Map<Long, List<Reservation>> reservationsParFilm = reservations.stream()
                    .filter(r -> r.getSeance() != null && r.getSeance().getFilm() != null)
                    .collect(Collectors.groupingBy(r -> r.getSeance().getFilm().getId()));
            
            // Calculer le CA par film
            Map<Long, BigDecimal> caParFilm = new HashMap<>();
            for (Map.Entry<Long, List<Reservation>> entry : reservationsParFilm.entrySet()) {
                Long filmId = entry.getKey();
                List<Reservation> filmReservations = entry.getValue();
                
                BigDecimal caFilm = filmReservations.stream()
                        .map(Reservation::getMontantTotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                caParFilm.put(filmId, caFilm);
            }
            
            return caParFilm.entrySet().stream()
                    .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                    .limit(limite)
                    .map(entry -> {
                        Map<String, Object> filmInfo = new HashMap<>();
                        Optional<Film> film = depotFilm.findById(entry.getKey());
                        filmInfo.put("filmId", entry.getKey());
                        filmInfo.put("filmTitre", film.map(Film::getTitre).orElse("Film inconnu"));
                        filmInfo.put("chiffreAffaire", entry.getValue());
                        return filmInfo;
                    })
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            System.err.println("Erreur lors du calcul des top films: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getChiffreAffaireParCategorie(String periode, LocalDate dateDebut, LocalDate dateFin) {
        // Simulation de données par catégorie (à adapter selon votre logique métier)
        List<Map<String, Object>> result = new ArrayList<>();
        
        // Données exemple - à remplacer avec votre logique réelle
        Map<String, Object> standard = new HashMap<>();
        standard.put("categorie", "STANDARD");
        standard.put("chiffreAffaire", new BigDecimal("15000.00"));
        standard.put("nombreEntrees", 1250);
        result.add(standard);
        
        Map<String, Object> etudiant = new HashMap<>();
        etudiant.put("categorie", "ETUDIANT");
        etudiant.put("chiffreAffaire", new BigDecimal("4500.00"));
        etudiant.put("nombreEntrees", 500);
        result.add(etudiant);
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getChiffreAffaireParGenre(String periode, LocalDate dateDebut, LocalDate dateFin) {
        try {
            // Récupérer les réservations réelles pour la période
            List<Reservation> reservations = depotReservation.findAll().stream()
                    .filter(r -> r.getDateReservation() != null)
                    .filter(r -> !r.getDateReservation().toLocalDate().isBefore(dateDebut) &&
                               !r.getDateReservation().toLocalDate().isAfter(dateFin))
                    .collect(Collectors.toList());
            
            // Grouper par genre de film pour calculer les vraies statistiques
            Map<String, List<Reservation>> reservationsParGenre = reservations.stream()
                    .filter(r -> r.getSeance() != null && r.getSeance().getFilm() != null)
                    .collect(Collectors.groupingBy(r -> r.getSeance().getFilm().getGenre()));
            
            // Calculer le CA et les entrées par genre
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map.Entry<String, List<Reservation>> entry : reservationsParGenre.entrySet()) {
                String genre = entry.getKey();
                List<Reservation> genreReservations = entry.getValue();
                
                BigDecimal caGenre = genreReservations.stream()
                        .map(Reservation::getMontantTotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                int totalEntrees = 0;
                for (Reservation reservation : genreReservations) {
                    try {
                        List<ReservationPlace> places = depotReservationPlace.findByReservationId(reservation.getId());
                        totalEntrees += places.size();
                    } catch (Exception e) {
                        totalEntrees += 1;
                    }
                }
                
                Map<String, Object> genreData = new HashMap<>();
                genreData.put("genre", genre);
                genreData.put("chiffreAffaire", caGenre);
                genreData.put("nombreEntrees", totalEntrees);
                genreData.put("nombre", totalEntrees); // Pour compatibilité avec l'ancien format
                genreData.put("pourcentage", 100); // Sera calculé dans le frontend
                result.add(genreData);
            }
            
            // Si aucune réservation, retourner des données par défaut
            if (result.isEmpty()) {
                Map<String, Object> defaultGenre = new HashMap<>();
                defaultGenre.put("genre", "Aucune réservation");
                defaultGenre.put("chiffreAffaire", BigDecimal.ZERO);
                defaultGenre.put("nombreEntrees", 0);
                defaultGenre.put("nombre", 0); // Pour compatibilité
                defaultGenre.put("pourcentage", 0);
                result.add(defaultGenre);
            }
            
            return result;
            
        } catch (Exception e) {
            System.err.println("Erreur lors du calcul du chiffre d'affaires par genre: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getEvolutionTemporelle(Long filmId, String periode, LocalDate dateDebut, LocalDate dateFin) {
        try {
            // Récupérer les réservations réelles pour la période
            List<Reservation> reservations = depotReservation.findAll().stream()
                    .filter(r -> r.getDateReservation() != null)
                    .filter(r -> !r.getDateReservation().toLocalDate().isBefore(dateDebut) &&
                               !r.getDateReservation().toLocalDate().isAfter(dateFin))
                    .filter(r -> filmId == null || 
                               (r.getSeance() != null && r.getSeance().getFilm() != null && 
                                r.getSeance().getFilm().getId().equals(filmId)))
                    .collect(Collectors.toList());
            
            // Grouper par jour pour l'évolution
            Map<LocalDate, List<Reservation>> reservationsParJour = reservations.stream()
                    .collect(Collectors.groupingBy(r -> r.getDateReservation().toLocalDate()));
            
            // Calculer l'évolution jour par jour
            List<Map<String, Object>> evolution = new ArrayList<>();
            List<LocalDate> dates = reservationsParJour.keySet().stream()
                    .sorted()
                    .collect(Collectors.toList());
            
            for (LocalDate date : dates) {
                List<Reservation> jourReservations = reservationsParJour.get(date);
                
                BigDecimal caJour = jourReservations.stream()
                        .map(Reservation::getMontantTotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                int totalEntrees = 0;
                for (Reservation reservation : jourReservations) {
                    try {
                        List<ReservationPlace> places = depotReservationPlace.findByReservationId(reservation.getId());
                        totalEntrees += places.size();
                    } catch (Exception e) {
                        totalEntrees += 1;
                    }
                }
                
                Map<String, Object> jourData = new HashMap<>();
                jourData.put("date", date);
                jourData.put("ca", caJour);
                jourData.put("entrees", totalEntrees);
                evolution.add(jourData);
            }
            
            // Si aucune réservation, retourner des données par défaut
            if (evolution.isEmpty()) {
                Map<String, Object> defaultData = new HashMap<>();
                defaultData.put("date", LocalDate.now());
                defaultData.put("ca", BigDecimal.ZERO);
                defaultData.put("entrees", 0);
                evolution.add(defaultData);
            }
            
            return evolution;
            
        } catch (Exception e) {
            System.err.println("Erreur lors du calcul de l'évolution temporelle: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> recalculerChiffreAffaire(LocalDate dateDebut, LocalDate dateFin) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("=== DÉBUT RECALCUL CA SIMPLIFIÉ ===");
            
            // Récupérer toutes les réservations
            List<Reservation> reservations = depotReservation.findAll();
            System.out.println("Nombre total de réservations: " + reservations.size());
            
            if (reservations.isEmpty()) {
                result.put("success", true);
                result.put("message", "Aucune réservation trouvée");
                result.put("totalCalculs", 0);
                return result;
            }
            
            // Calcul simple sans sauvegarde en base
            double totalCA = reservations.stream()
                .filter(r -> r.getMontantTotal() != null)
                .mapToDouble(r -> r.getMontantTotal().doubleValue())
                .sum();
            
            result.put("success", true);
            result.put("message", "Recalcul terminé avec succès");
            result.put("totalCalculs", reservations.size());
            result.put("totalCA", totalCA);
            result.put("dateDebut", dateDebut.toString());
            result.put("dateFin", dateFin.toString());
            
            System.out.println("=== FIN RECALCUL CA ===");
            System.out.println("Total CA calculé: " + totalCA);
            
        } catch (Exception e) {
            System.out.println("ERREUR RECALCUL CA: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Erreur: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }
        
        return result;
    }

    @Override
    public byte[] exporterChiffreAffairePDF(String periode, LocalDate dateDebut, LocalDate dateFin, Long filmId) {
        // Implémentation PDF à faire plus tard
        String csvContent = "Film,Date,Période,Entrées,Chiffre d'affaires\n";
        csvContent += "Export PDF - à implémenter";
        return csvContent.getBytes();
    }

    @Override
    public byte[] exporterChiffreAffaireExcel(String periode, LocalDate dateDebut, LocalDate dateFin, Long filmId) {
        // Implémentation Excel à faire plus tard
        String csvContent = "Film,Date,Période,Entrées,Chiffre d'affaires\n";
        csvContent += "Export Excel - à implémenter";
        return csvContent.getBytes();
    }

    @Override
    public Map<String, Object> getDashboardData(String periode, LocalDate dateDebut, LocalDate dateFin) {
        Map<String, Object> dashboard = new HashMap<>();
        
        // Statistiques générales
        dashboard.put("statistiques", getStatistiquesGenerales(periode, dateDebut, dateFin));
        
        // Top films
        dashboard.put("topFilms", getTopFilms(periode, dateDebut, dateFin, 5));
        
        // Répartition par catégorie
        dashboard.put("repartitionCategorie", getChiffreAffaireParCategorie(periode, dateDebut, dateFin));
        
        // Répartition par genre
        dashboard.put("repartitionGenre", getChiffreAffaireParGenre(periode, dateDebut, dateFin));
        
        return dashboard;
    }

    private ChiffreAffaireDTO convertToDTO(ChiffreAffaireFilm ca) {
        ChiffreAffaireDTO dto = new ChiffreAffaireDTO();
        dto.setId(ca.getId());
        dto.setFilmId(ca.getFilmId());
        
        // Récupérer le titre du film
        Optional<Film> film = depotFilm.findById(ca.getFilmId());
        dto.setFilmTitre(film.map(Film::getTitre).orElse("Film inconnu"));
        
        dto.setDateCalcul(ca.getDateCalcul());
        dto.setTypePeriode(ca.getTypePeriode());
        dto.setNombreEntrees(ca.getNombreEntrees());
        dto.setChiffreAffaire(ca.getChiffreAffaire());
        
        // Calculer le tarif moyen
        BigDecimal tarifMoyen = ca.getNombreEntrees() > 0 ? 
                ca.getChiffreAffaire().divide(BigDecimal.valueOf(ca.getNombreEntrees()), 2, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
        dto.setTarifMoyen(tarifMoyen);
        
        return dto;
    }
    @Override
    public List<Map<String, Object>> getRevenuReelParSeance() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection()) {
            // Requête SQL directe pour obtenir les réservations CONFIRMEE avec leurs séances
            String sql = """
                SELECT r.id as reservation_id, r.seance_id, r.montant_total, r.statut,
                       s.id as seance_id, s.date_heure as seance_date, s.prix_base,
                       f.id as film_id, f.titre as film_titre,
                       sal.id as salle_id, sal.nom as salle_nom,
                       COUNT(rp.id) as total_places
                FROM reservations r
                JOIN seances s ON r.seance_id = s.id
                JOIN films f ON s.film_id = f.id
                JOIN salles sal ON s.salle_id = sal.id
                LEFT JOIN reservation_places rp ON r.id = rp.reservation_id
                WHERE r.statut = 'CONFIRMEE'
                GROUP BY r.id, s.id, f.id, sal.id
                ORDER BY s.date_heure
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                
                // Grouper par séance
                Map<Long, Map<String, Object>> seancesMap = new HashMap<>();
                
                while (rs.next()) {
                    Long seanceId = rs.getLong("seance_id");
                    
                    if (!seancesMap.containsKey(seanceId)) {
                        Map<String, Object> seanceData = new HashMap<>();
                        seanceData.put("seanceId", seanceId);
                        seanceData.put("dateHeure", rs.getTimestamp("seance_date").toLocalDateTime());
                        seanceData.put("filmId", rs.getLong("film_id"));
                        seanceData.put("filmTitre", rs.getString("film_titre"));
                        seanceData.put("salleId", rs.getLong("salle_id"));
                        seanceData.put("salleNom", rs.getString("salle_nom"));
                        seanceData.put("revenuReel", BigDecimal.ZERO);
                        seanceData.put("totalPlacesReservees", 0);
                        seanceData.put("placesParCategorie", new HashMap<String, Long>());
                        seancesMap.put(seanceId, seanceData);
                    }
                    
                    Map<String, Object> seanceData = seancesMap.get(seanceId);
                    
                    // Ajouter le montant total
                    BigDecimal montantTotal = rs.getBigDecimal("montant_total");
                    if (montantTotal != null) {
                        seanceData.put("revenuReel", 
                            ((BigDecimal) seanceData.get("revenuReel")).add(montantTotal));
                    }
                    
                    // Ajouter les places
                    int totalPlaces = rs.getInt("total_places");
                    seanceData.put("totalPlacesReservees", 
                        (Integer) seanceData.get("totalPlacesReservees") + totalPlaces);
                }
                
                // Ajouter le CA publicité et les informations de paiement pour chaque séance
                for (Map<String, Object> seance : seancesMap.values()) {
                    Long seanceId = (Long) seance.get("seanceId");
                    
                    // Calculer le montant total généré par la publicité pour cette séance
                    String pubSql = "SELECT COALESCE(SUM(dp.prix_unitaire), 0) as ca_pub FROM diffusion_pub dp WHERE dp.id_sceance = ?";
                    try (PreparedStatement pubStmt = conn.prepareStatement(pubSql)) {
                        pubStmt.setLong(1, seanceId);
                        ResultSet pubRs = pubStmt.executeQuery();
                        if (pubRs.next()) {
                            double caPublicite = pubRs.getDouble("ca_pub");
                            seance.put("caPublicite", caPublicite);
                            
                            // Calculer le montant déjà payé pour cette séance avec répartition proportionnelle
                            String payeSql = """
                                    SELECT COALESCE(SUM(p.montant), 0) as montant_paye_total,
                                           COALESCE(SUM(dp.prix_unitaire), 0) as ca_total_film
                                    FROM paiement p
                                    JOIN societe s ON p.id_societe = s.id
                                    JOIN diffusion_pub dp ON dp.id_societe = s.id
                                    JOIN seances s2 ON dp.id_sceance = s2.id
                                    WHERE s2.film_id = (SELECT film_id FROM seances WHERE id = ?)
                                    """;
                            try (PreparedStatement payeStmt = conn.prepareStatement(payeSql)) {
                                payeStmt.setLong(1, seanceId);
                                ResultSet payeRs = payeStmt.executeQuery();
                                if (payeRs.next()) {
                                    double montantPayeTotal = payeRs.getDouble("montant_paye_total");
                                    double caTotalFilm = payeRs.getDouble("ca_total_film");
                                    
                                    // Calculer le pourcentage payé pour ce film
                                    double pourcentagePaye = caTotalFilm > 0 ? (montantPayeTotal / caTotalFilm) : 0.0;
                                    
                                    // Appliquer ce pourcentage à la séance actuelle
                                    double montantPayeSeance = caPublicite * pourcentagePaye;
                                    double montantResteSeance = caPublicite - montantPayeSeance;
                                    
                                    seance.put("montantPublicitePaye", montantPayeSeance);
                                    seance.put("montantPubliciteReste", montantResteSeance);
                                } else {
                                    seance.put("montantPublicitePaye", 0.0);
                                    seance.put("montantPubliciteReste", caPublicite);
                                }
                            }
                        } else {
                            seance.put("caPublicite", 0.0);
                            seance.put("montantPublicitePaye", 0.0);
                            seance.put("montantPubliciteReste", 0.0);
                        }
                    }
                    
                    // Convert BigDecimal `revenuReel` to a primitive double for JSON/JS usage
                    Object revenuObj = seance.get("revenuReel");
                    if (revenuObj instanceof BigDecimal) {
                        seance.put("revenuReel", ((BigDecimal) revenuObj).doubleValue());
                    } else if (revenuObj == null) {
                        seance.put("revenuReel", 0.0);
                    }

                    // Ensure totalPlacesReservees is a primitive number (Integer)
                    Object placesObj = seance.get("totalPlacesReservees");
                    if (placesObj == null) {
                        seance.put("totalPlacesReservees", 0);
                    }
                    // Debug log the seance map before adding to result
                    System.out.println("[DEBUG] getRevenuReelParSeance - seance: " + seance);

                    // Recompute totalPlacesReservees accurately from reservation_places
                    try {
                        String countSql = "SELECT COUNT(rp.id) as total_places FROM reservation_places rp JOIN reservations r ON rp.reservation_id = r.id WHERE r.seance_id = ? AND r.statut = 'CONFIRMEE'";
                        try (PreparedStatement countStmt = conn.prepareStatement(countSql)) {
                            countStmt.setLong(1, seanceId);
                            ResultSet countRs = countStmt.executeQuery();
                            if (countRs.next()) {
                                int totalPlacesCount = countRs.getInt("total_places");
                                seance.put("totalPlacesReservees", totalPlacesCount);
                            }
                        }
                    } catch (Exception ex) {
                        // ignore and keep existing value
                    }

                    result.add(seance);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            // En cas d'erreur, retourner une liste vide
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getRevenuMaxParSeance() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            // Récupérer toutes les séances
            List<Seance> seances = depotSeance.findAll();
            
            for (Seance seance : seances) {
                if (seance.getFilm() == null || seance.getSalle() == null) {
                    continue;
                }
                
                // Récupérer toutes les places de la salle
                List<Place> places = depotPlace.findBySalleId(seance.getSalle().getId());
                
                // Grouper les places par catégorie
                Map<String, Long> placesParCategorie = places.stream()
                        .collect(Collectors.groupingBy(
                                Place::getCategorie, 
                                Collectors.counting()
                        ));
                
                // Calculer le revenu maximum par catégorie
                BigDecimal revenuMax = BigDecimal.ZERO;
                
                for (Map.Entry<String, Long> entry : placesParCategorie.entrySet()) {
                    String categorie = entry.getKey();
                    Long nombrePlaces = entry.getValue();
                    
                    // Récupérer le tarif pour cette catégorie
                    List<Tarif> tarifs = depotTarif.findByTypeAndActifTrue(categorie);
                    
                    if (!tarifs.isEmpty()) {
                        // Utiliser le tarif le plus élevé pour cette catégorie
                        BigDecimal prix = tarifs.stream()
                                .map(Tarif::getPrix)
                                .max(BigDecimal::compareTo)
                                .orElse(BigDecimal.ZERO);
                        
                        BigDecimal revenuCategorie = prix.multiply(BigDecimal.valueOf(nombrePlaces));
                        revenuMax = revenuMax.add(revenuCategorie);
                    }
                }
                
                // Créer l'entrée pour cette séance
                Map<String, Object> seanceData = new HashMap<>();
                seanceData.put("salleId", seance.getSalle().getId());
                seanceData.put("salleNom", seance.getSalle().getNom());
                seanceData.put("filmId", seance.getFilm().getId());
                seanceData.put("filmTitre", seance.getFilm().getTitre());
                seanceData.put("seanceId", seance.getId());
                seanceData.put("dateHeure", seance.getDateHeure());
                seanceData.put("revenuMaximum", revenuMax);
                seanceData.put("totalPlaces", places.size());
                seanceData.put("placesParCategorie", placesParCategorie);
                
                result.add(seanceData);
                
                System.out.println("Séance " + seance.getId() + " - Film: " + seance.getFilm().getTitre() + 
                        " - Salle: " + seance.getSalle().getNom() + " - Revenu max: " + revenuMax + "€");
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors du calcul du revenu max par séance: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getRevenuMaxParSalle() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            // Récupérer toutes les salles
            List<Salle> salles = depotSalle.findAll();
            
            for (Salle salle : salles) {
                // Utiliser directement la configuration de la salle (pas besoin de la table places)
                Map<String, Integer> repartition = salle.getRepartitionPlacesMap();
                
                // Calculer le revenu maximum par catégorie
                BigDecimal revenuMax = BigDecimal.ZERO;
                Map<String, Long> placesParCategorie = new HashMap<>();
                
                for (Map.Entry<String, Integer> entry : repartition.entrySet()) {
                    String categorie = entry.getKey();
                    Integer nombrePlaces = entry.getValue();
                    
                    if (nombrePlaces != null && nombrePlaces > 0) {
                        placesParCategorie.put(categorie, nombrePlaces.longValue());
                        
                        // Récupérer le tarif pour cette catégorie
                        List<Tarif> tarifs = depotTarif.findByTypeAndActifTrue(categorie);
                        
                        if (!tarifs.isEmpty()) {
                            // Utiliser le tarif le plus élevé pour cette catégorie
                            BigDecimal prix = tarifs.stream()
                                    .map(Tarif::getPrix)
                                    .max(BigDecimal::compareTo)
                                    .orElse(BigDecimal.ZERO);
                            
                            BigDecimal revenuCategorie = prix.multiply(BigDecimal.valueOf(nombrePlaces));
                            revenuMax = revenuMax.add(revenuCategorie);
                            
                            System.out.println("Salle " + salle.getNom() + " - " + categorie + 
                                ": " + nombrePlaces + " places × " + prix + "€ = " + revenuCategorie + "€");
                        }
                    }
                }
                
                // Créer l'entrée pour cette salle
                Map<String, Object> salleData = new HashMap<>();
                salleData.put("salleId", salle.getId());
                salleData.put("salleNom", salle.getNom());
                salleData.put("capacite", salle.getCapacite());
                salleData.put("type", salle.getType());
                salleData.put("revenuMaximum", revenuMax);
                salleData.put("totalPlaces", repartition.values().stream().mapToInt(Integer::intValue).sum());
                salleData.put("placesParCategorie", placesParCategorie);
                
                result.add(salleData);
                
                System.out.println("Salle " + salle.getNom() + " (ID: " + salle.getId() + 
                        ") - Configuration: " + repartition + 
                        " - Revenu max: " + revenuMax + "€");
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors du calcul du revenu max par salle: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getChiffreAffairesParFilm(String dateDebut, String dateFin, String periode, Long filmId) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            // Convertir les dates si fournies
            LocalDate debut = dateDebut != null ? LocalDate.parse(dateDebut) : null;
            LocalDate fin = dateFin != null ? LocalDate.parse(dateFin) : null;
            
            // Si pas de période, utiliser le mois en cours
            if (periode == null && debut == null && fin == null) {
                debut = LocalDate.now().withDayOfMonth(1);
                fin = LocalDate.now();
            }
            
            // Récupérer les chiffres d'affaires existants
            List<ChiffreAffaireFilm> caFilms;
            
            if (filmId != null) {
                // Filtrer par film spécifique
                caFilms = depotChiffreAffaireFilm.findByFilmIdAndDateCalculBetweenAndTypePeriode(filmId, debut, fin, "FILM");
            } else {
                // Tous les films
                caFilms = depotChiffreAffaireFilm.findByDateCalculBetweenAndTypePeriode(debut, fin, "FILM");
            }
            
            // Grouper par film
            Map<Long, Map<String, Object>> filmsGroupes = new HashMap<>();
            
            for (ChiffreAffaireFilm caf : caFilms) {
                Long currentFilmId = caf.getFilmId();
                
                if (!filmsGroupes.containsKey(currentFilmId)) {
                    Map<String, Object> filmData = new HashMap<>();
                    filmData.put("id", currentFilmId);
                    filmData.put("filmTitre", "Film " + currentFilmId); // On pourrait récupérer le vrai nom du film
                    filmData.put("dateCalcul", caf.getDateCalcul().toString());
                    filmData.put("typePeriode", "FILM");
                    filmData.put("chiffreAffaire", BigDecimal.ZERO);
                    filmData.put("nombreEntrees", 0);
                    filmData.put("tarifMoyen", BigDecimal.ZERO);
                    filmData.put("salleNom", "");
                    filmData.put("capacite", 0);
                    filmData.put("type", "");
                    filmData.put("placesParCategorie", new HashMap<>());
                    filmsGroupes.put(currentFilmId, filmData);
                }
                
                // Ajouter les chiffres d'affaires
                Map<String, Object> filmData = filmsGroupes.get(currentFilmId);
                BigDecimal caActuel = (BigDecimal) filmData.get("chiffreAffaire");
                filmData.put("chiffreAffaire", caActuel.add(caf.getChiffreAffaire()));
                
                Integer entreesActuelles = (Integer) filmData.get("nombreEntrees");
                filmData.put("nombreEntrees", entreesActuelles + caf.getNombreEntrees());
                
                // Calculer le tarif moyen
                Integer totalEntrees = (Integer) filmData.get("nombreEntrees");
                BigDecimal totalCA = (BigDecimal) filmData.get("chiffreAffaire");
                if (totalEntrees > 0) {
                    filmData.put("tarifMoyen", totalCA.divide(BigDecimal.valueOf(totalEntrees), RoundingMode.HALF_UP));
                }
            }
            
            result = new ArrayList<>(filmsGroupes.values());
            
        } catch (Exception e) {
            System.err.println("Erreur lors du calcul du chiffre d'affaires par film: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }

    @Override
    public List<ChiffreAffaireDTO> calculerChiffreAffaireParFilmEtSeance(Long filmId, LocalDate mois) {
        List<ChiffreAffaireDTO> result = new ArrayList<>();
        String filmTitre = depotFilm.findById(filmId)
            .map(Film::getTitre)
            .orElse("Film #" + filmId);

        // Récupérer les diffusions pour le film et le mois donné
        int month = mois.getMonthValue();
        List<Diffusionpub> diffusions = depotChiffreAffaireFilm.findByFilmIdAndMois(filmId, month);

        for (Diffusionpub diffusion : diffusions) {
            BigDecimal prixUnitaire = BigDecimal.valueOf(diffusion.getPrixUnitaire());
            BigDecimal totalPaye = depotChiffreAffaireFilm.calculerTotalPayePourDiffusion(diffusion.getId());
            if (totalPaye == null) {
                totalPaye = BigDecimal.ZERO;
            }
            BigDecimal resteAPayer = prixUnitaire.subtract(totalPaye);

            ChiffreAffaireDTO dto = new ChiffreAffaireDTO();
            dto.setFilmId(filmId);
            dto.setFilmTitre(filmTitre);
            dto.setDateCalcul(mois);
            dto.setChiffreAffaire(prixUnitaire);
            dto.setDejaPaye(totalPaye);
            dto.setResteAPayer(resteAPayer);

            result.add(dto);
        }

        return result;
    }
}
