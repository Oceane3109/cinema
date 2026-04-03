package com.cinema.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import javax.sql.DataSource;
import java.util.*;
import java.sql.*;

@RestController
@RequestMapping("/api/pub")
@CrossOrigin(origins = "http://localhost:5173")
public class ControlleurPub {
    
    @Autowired
    private DataSource dataSource;
    
    // DTO simple pour les résultats
    public static class StatsSociete {
        public String societe;
        public int nombreDiffusions;
        public double montantTotal;
        public double dejaPayer;
        public double resteAPayer;
        
        public StatsSociete(String societe, int nombreDiffusions, double montantTotal, double dejaPayer) {
            this.societe = societe;
            this.nombreDiffusions = nombreDiffusions;
            this.montantTotal = montantTotal;
            this.dejaPayer = dejaPayer;
            this.resteAPayer = montantTotal - dejaPayer;
        }
    }
    
    // DTO pour le total général
    public static class TotalGeneral {
        public int totalDiffusions;
        public double montantTotal;
        
        public TotalGeneral(int totalDiffusions, double montantTotal) {
            this.totalDiffusions = totalDiffusions;
            this.montantTotal = montantTotal;
        }
    }
    
    @GetMapping("/fix-diffusion-sequence")
    public ResponseEntity<String> fixDiffusionSequence() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Récupérer le max ID actuel
            ResultSet rs = stmt.executeQuery("SELECT MAX(id) FROM diffusion_pub");
            long maxId = 0;
            if (rs.next()) {
                maxId = rs.getLong(1);
            }
            
            // Mettre à jour la séquence
            stmt.executeUpdate("ALTER SEQUENCE diffusion_pub_id_seq RESTART WITH " + (maxId + 1));
            
            return ResponseEntity.ok("Séquence diffusion_pub corrigée. Prochain ID: " + (maxId + 1));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur: " + e.getMessage());
        }
    }

    @PostMapping("/reset-deja-payer")
    public ResponseEntity<String> resetDejaPayer(@RequestBody Map<String, Object> data) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE societe SET deja_payer = 0 WHERE nom = ?"
             )) {
            
            stmt.setString(1, (String) data.get("nom"));
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                return ResponseEntity.ok("deja_payer réinitialisé pour " + data.get("nom"));
            } else {
                return ResponseEntity.status(404).body("Société non trouvée");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur serveur: " + e.getMessage());
        }
    }

    @PostMapping("/creer-reservations-titanic")
    public ResponseEntity<String> creerReservationsTitanic() {
        try (Connection conn = dataSource.getConnection()) {
            
            // Supprimer les réservations Titanic existantes
            Statement stmtDelete = conn.createStatement();
            stmtDelete.executeUpdate("DELETE FROM reservation_places WHERE reservation_id IN (SELECT id FROM reservations WHERE nom_client = 'Client Titanic')");
            stmtDelete.executeUpdate("DELETE FROM reservations WHERE nom_client = 'Client Titanic'");
            
            // Récupérer le tarif pour les places standard adulte
            double tarifStandardAdulte = getTarifStandardAdulte(conn);
            
            // Séance 34 - 40 billets
            creerReservationsPourSeance(conn, 34, 40, tarifStandardAdulte);
            
            // Séance 35 - 30 billets  
            creerReservationsPourSeance(conn, 35, 30, tarifStandardAdulte);
            
            // Séance 36 - 50 billets
            creerReservationsPourSeance(conn, 36, 50, tarifStandardAdulte);
            
            return ResponseEntity.ok("Réservations Titanic créées avec succès - Tarif standard adulte: " + tarifStandardAdulte + " Ar");
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur: " + e.getMessage());
        }
    }
    
    private double getTarifStandardAdulte(Connection conn) throws Exception {
        // Récupérer le tarif standard adulte
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT t.prix FROM tarifs t " +
            "WHERE t.nom = 'Standard' AND t.genre = 'TOUS' AND t.type = 'STANDARD' AND t.actif = true " +
            "ORDER BY t.prix DESC LIMIT 1"
        );
        
        if (rs.next()) {
            return rs.getDouble("prix");
        } else {
            // Si aucun tarif trouvé, utiliser 20,000 Ar par défaut
            return 20000.0;
        }
    }
    
    private void creerReservationsPourSeance(Connection conn, int seanceId, int nombreBillets, double prixUnitaire) throws Exception {
        // Récupérer les places standard disponibles pour la séance
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT p.id FROM places p WHERE p.salle_id = 1 AND p.categorie = 'STANDARD' LIMIT " + nombreBillets
        );
        
        List<Integer> placeIds = new ArrayList<>();
        while (rs.next() && placeIds.size() < nombreBillets) {
            placeIds.add(rs.getInt("id"));
        }
        
        if (placeIds.size() < nombreBillets) {
            throw new Exception("Pas assez de places standard disponibles pour la séance " + seanceId);
        }
        
        // Créer les réservations
        for (int placeId : placeIds) {
            // Créer la réservation principale - version ultra simplifiée
            try {
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO reservations (seance_id, nom_client, montant_total, statut, date_reservation) VALUES (?, ?, ?, ?, ?)"
                );
                
                ps.setInt(1, seanceId);
                ps.setString(2, "Client Titanic");
                ps.setDouble(3, prixUnitaire);
                ps.setString(4, "CONFIRMEE");
                ps.setTimestamp(5, new java.sql.Timestamp(System.currentTimeMillis()));
                
                ps.executeUpdate();
                
                // Créer le lien réservation-place si la réservation a réussi
                try {
                    PreparedStatement psLink = conn.prepareStatement(
                        "INSERT INTO reservation_places (reservation_id, place_id) VALUES ((SELECT MAX(id) FROM reservations), ?)"
                    );
                    psLink.setInt(1, placeId);
                    psLink.executeUpdate();
                } catch (Exception e) {
                    // Si le lien échoue, continuer quand même
                    System.err.println("Erreur lien réservation-place: " + e.getMessage());
                }
                
            } catch (Exception e) {
                System.err.println("Erreur création réservation: " + e.getMessage());
            }
        }
    }

    @PostMapping("/societe")
    public ResponseEntity<String> ajouterSociete(@RequestBody Map<String, Object> societeData) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO societe (nom, deja_payer) VALUES (?, ?)"
             )) {
            
            stmt.setString(1, (String) societeData.get("nom"));
            
            Object dejaPayerObj = societeData.get("deja_payer");
            double dejaPayer = 0.0;
            if (dejaPayerObj instanceof Number) {
                dejaPayer = ((Number) dejaPayerObj).doubleValue();
            } else if (dejaPayerObj instanceof String) {
                dejaPayer = Double.parseDouble((String) dejaPayerObj);
            }
            
            stmt.setDouble(2, dejaPayer);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                return ResponseEntity.ok("Société ajoutée avec succès");
            } else {
                return ResponseEntity.status(500).body("Erreur lors de l'ajout de la société");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur serveur: " + e.getMessage());
        }
    }

    @GetMapping("/societes")
    public ResponseEntity<List<Map<String, Object>>> getSocietes() {
        List<Map<String, Object>> societes = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, nom FROM societe ORDER BY nom")) {
            
            while (rs.next()) {
                Map<String, Object> societe = new HashMap<>();
                societe.put("id", rs.getInt("id"));
                societe.put("nom", rs.getString("nom"));
                societes.add(societe);
            }
            
            return ResponseEntity.ok(societes);
            
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
    
    @GetMapping("/total-par-societe-separe")
    public ResponseEntity<List<Map<String, Object>>> getTotalParSocieteSepare() {
        List<Map<String, Object>> stats = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Récupérer toutes les sociétés
            ResultSet rsSocietes = stmt.executeQuery("SELECT id, nom, COALESCE(deja_payer, 0) as deja_payer FROM societe ORDER BY nom");
            
            while (rsSocietes.next()) {
                int societeId = rsSocietes.getInt("id");
                String societeNom = rsSocietes.getString("nom");
                double dejaPayer = rsSocietes.getDouble("deja_payer");
                
                int nbPasses = 0;
                double montantPasses = 0.0;
                int nbPresents = 0;
                double montantPresents = 0.0;

                // Utiliser une requete preparee separee pour ne pas invalider rsSocietes
                try (PreparedStatement diffStmt = conn.prepareStatement(
                        "SELECT dp.prix_unitaire, TO_CHAR(sc.date_heure, 'YYYY-MM-DD') as date_seance " +
                        "FROM DIFFUSION_PUB dp " +
                        "LEFT JOIN seances sc ON dp.id_sceance = sc.id " +
                        "WHERE dp.id_societe = ?"
                )) {
                    diffStmt.setInt(1, societeId);
                    try (ResultSet rsAllDiffusions = diffStmt.executeQuery()) {
                        while (rsAllDiffusions.next()) {
                            String dateSeance = rsAllDiffusions.getString("date_seance");
                            double prix = rsAllDiffusions.getDouble("prix_unitaire");

                            if (dateSeance != null && dateSeance.startsWith("2026")) {
                                nbPresents++;
                                montantPresents += prix;
                            } else {
                                nbPasses++;
                                montantPasses += prix;
                            }
                        }
                    }
                }
                
                // Ajouter les stats passées si > 0
                if (nbPasses > 0) {
                    Map<String, Object> stat = new HashMap<>();
                    stat.put("societe", societeNom);
                    stat.put("periode", "passé");
                    stat.put("nombreDiffusions", nbPasses);
                    stat.put("montantTotal", montantPasses);
                    stat.put("dejaPayer", dejaPayer);
                    stat.put("resteAPayer", montantPasses - dejaPayer);
                    stats.add(stat);
                }
                
                // Toujours ajouter les stats présentes (même si 0)
                Map<String, Object> stat = new HashMap<>();
                stat.put("societe", societeNom);
                stat.put("periode", "présent");
                stat.put("nombreDiffusions", nbPresents);
                stat.put("montantTotal", montantPresents);
                stat.put("dejaPayer", dejaPayer);
                stat.put("resteAPayer", montantPresents - dejaPayer);
                stats.add(stat);
            }
            
            return ResponseEntity.ok(stats);
            
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    @GetMapping("/total-par-societe")
    public ResponseEntity<List<StatsSociete>> getTotalParSociete() {
        List<StatsSociete> stats = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT s.nom as societe, " +
                 "COUNT(dp.id) as nb_diffusions, " +
                 "COALESCE(SUM(dp.prix_unitaire), 0) as montant_total, " +
                 "COALESCE(s.deja_payer, 0) as deja_payer " +
                 "FROM societe s " +
                 "LEFT JOIN DIFFUSION_PUB dp ON s.id = dp.id_societe " +
                 "GROUP BY s.id, s.nom, s.deja_payer " +
                 "ORDER BY montant_total DESC"
             )) {
            
            while (rs.next()) {
                String societeNom = rs.getString("societe");
                double dejaPayer = rs.getDouble("deja_payer");
                
                // Gérer le cas où deja_payer est NULL
                if (rs.wasNull()) {
                    dejaPayer = 0.0;
                }
                
                stats.add(new StatsSociete(
                    societeNom,
                    rs.getInt("nb_diffusions"),
                    rs.getDouble("montant_total"),
                    dejaPayer
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // En cas d'erreur, retourner les données de test
            stats.add(new StatsSociete("Vaniala", 10, 2000000.0, 0.0));
            stats.add(new StatsSociete("Lewis", 10, 2000000.0, 0.0));
            stats.add(new StatsSociete("VANIALA JANVIER", 5, 1000000.0, 0.0));
        }
        
        return ResponseEntity.ok(stats);
    }
    
    // DTO pour les paiements
    public static class PaiementInfo {
        public int id;
        public String societe;
        public double montant;
        public String datePaiement;
        public String mois;
        public String description;
        
        public PaiementInfo(int id, String societe, double montant, String datePaiement, String mois, String description) {
            this.id = id;
            this.societe = societe;
            this.montant = montant;
            this.datePaiement = datePaiement;
            this.mois = mois;
            this.description = description;
        }
    }
    
    @GetMapping("/paiements")
    public ResponseEntity<List<PaiementInfo>> getPaiements() {
        List<PaiementInfo> paiements = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT p.id, s.nom as societe, p.montant, " +
                 "TO_CHAR(p.date_paiement, 'DD/MM/YYYY HH24:MI') as date_paiement, " +
                 "TO_CHAR(p.date_paiement, 'Month YYYY') as mois, " +
                 "p.description " +
                 "FROM paiement p " +
                 "JOIN societe s ON p.id_societe = s.id " +
                 "ORDER BY p.date_paiement DESC"
             )) {
            
            while (rs.next()) {
                paiements.add(new PaiementInfo(
                    rs.getInt("id"),
                    rs.getString("societe"),
                    rs.getDouble("montant"),
                    rs.getString("date_paiement"),
                    rs.getString("mois"),
                    rs.getString("description")
                ));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(paiements);
    }
    
    @PostMapping("/diffusion")
    public ResponseEntity<String> ajouterDiffusion(@RequestBody Map<String, Object> diffusionData) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO DIFFUSION_PUB (id_societe, id_sceance, date_heure_diffusion, prix_unitaire, statut) VALUES (?, ?, ?, ?, 'PROGRAMMEE')"
             )) {
            
            // Gérer id_societe qui peut être String ou Integer
            Object societeObj = diffusionData.get("id_societe");
            int idSociete;
            if (societeObj instanceof String) {
                idSociete = Integer.parseInt((String) societeObj);
            } else if (societeObj instanceof Integer) {
                idSociete = (Integer) societeObj;
            } else {
                throw new IllegalArgumentException("Type de id_societe invalide: " + societeObj.getClass().getName());
            }
            stmt.setInt(1, idSociete);
            
            // Gérer id_sceance qui peut être String ou Integer
            Object sceanceObj = diffusionData.get("id_sceance");
            int idSceance;
            if (sceanceObj instanceof String) {
                idSceance = Integer.parseInt((String) sceanceObj);
            } else if (sceanceObj instanceof Integer) {
                idSceance = (Integer) sceanceObj;
            } else {
                throw new IllegalArgumentException("Type de id_sceance invalide: " + sceanceObj.getClass().getName());
            }
            stmt.setInt(2, idSceance);
            
            // Gérer la date (peut être String ou Timestamp)
            Object dateObj = diffusionData.get("date_heure_diffusion");
            if (dateObj instanceof String) {
                String dateStr = (String) dateObj;
                // Convertir de format datetime-local (YYYY-MM-DDTHH:MM) vers SQL timestamp
                if (dateStr.contains("T")) {
                    dateStr = dateStr.replace("T", " ");
                    if (dateStr.length() == 16) { // Format YYYY-MM-DD HH:MM
                        dateStr += ":00";
                    }
                }
                System.out.println("Date convertie: " + dateStr);
                stmt.setTimestamp(3, java.sql.Timestamp.valueOf(dateStr));
            } else {
                stmt.setTimestamp(3, (java.sql.Timestamp) dateObj);
            }
            
            // Gérer le prix
            Object prixObj = diffusionData.get("prix_unitaire");
            double prix;
            if (prixObj instanceof String) {
                prix = Double.parseDouble((String) prixObj);
            } else if (prixObj instanceof Number) {
                prix = ((Number) prixObj).doubleValue();
            } else {
                throw new IllegalArgumentException("Type de prix invalide: " + prixObj.getClass().getName());
            }
            stmt.setDouble(4, prix);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                return ResponseEntity.ok("Diffusion ajoutée avec succès");
            } else {
                return ResponseEntity.status(500).body("Erreur lors de l'ajout de la diffusion");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur de base de données: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur serveur: " + e.getMessage());
        }
    }
    
    @PutMapping("/diffusion/{id}")
    public ResponseEntity<String> modifierDiffusion(@PathVariable int id, @RequestBody Map<String, Object> diffusionData) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE DIFFUSION_PUB SET prix_unitaire = ? WHERE id = ?"
             )) {
            
            // Gérer le prix
            Object prixObj = diffusionData.get("prix_unitaire");
            double prix;
            if (prixObj instanceof String) {
                prix = Double.parseDouble((String) prixObj);
            } else if (prixObj instanceof Number) {
                prix = ((Number) prixObj).doubleValue();
            } else {
                throw new IllegalArgumentException("Type de prix_unitaire invalide: " + prixObj.getClass().getName());
            }
            
            stmt.setDouble(1, prix);
            stmt.setInt(2, id);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                return ResponseEntity.ok("Diffusion mise à jour avec succès");
            } else {
                return ResponseEntity.status(404).body("Diffusion non trouvée");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur serveur: " + e.getMessage());
        }
    }

    @DeleteMapping("/diffusion/{id}")
    public ResponseEntity<String> supprimerDiffusion(@PathVariable int id) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "DELETE FROM DIFFUSION_PUB WHERE id = ?"
             )) {
            
            stmt.setInt(1, id);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                return ResponseEntity.ok("Diffusion supprimée avec succès");
            } else {
                return ResponseEntity.status(404).body("Diffusion non trouvée");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur serveur: " + e.getMessage());
        }
    }

    @GetMapping("/diffusions")
    public ResponseEntity<List<Map<String, Object>>> getDiffusions() {
        List<Map<String, Object>> diffusions = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT dp.id, dp.id_societe, dp.id_sceance, dp.date_heure_diffusion, dp.prix_unitaire, dp.statut, " +
                 "s.nom as societe, COALESCE(f.titre, 'Séance #' || dp.id_sceance) as film " +
                 "FROM diffusion_pub dp " +
                 "JOIN societe s ON dp.id_societe = s.id " +
                 "LEFT JOIN seances sc ON dp.id_sceance = sc.id " +
                 "LEFT JOIN films f ON sc.film_id = f.id " +
                 "ORDER BY dp.date_heure_diffusion DESC"
             )) {
            
            while (rs.next()) {
                Map<String, Object> diffusion = new HashMap<>();
                diffusion.put("id", rs.getInt("id"));
                diffusion.put("id_societe", rs.getInt("id_societe"));
                diffusion.put("id_sceance", rs.getInt("id_sceance"));
                diffusion.put("societe", rs.getString("societe"));
                diffusion.put("film", rs.getString("film"));
                diffusion.put("date_heure_diffusion", rs.getTimestamp("date_heure_diffusion"));
                diffusion.put("prix_unitaire", rs.getDouble("prix_unitaire"));
                diffusion.put("statut", rs.getString("statut"));
                diffusions.add(diffusion);
            }
            
            return ResponseEntity.ok(diffusions);
            
        } catch (SQLException e) {
            e.printStackTrace();
            // En cas d'erreur (table vide ou autre), retourner une liste vide
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
    
    @PostMapping("/paiement")
    public ResponseEntity<String> ajouterPaiement(@RequestBody Map<String, Object> paiementData) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO paiement (id_societe, montant, description, date_paiement) VALUES (?, ?, ?, ?)"
             )) {
            
            Object idSocieteObj = paiementData.get("id_societe");
            if (idSocieteObj == null) {
                return ResponseEntity.status(400).body("id_societe est requis");
            }
            
            int idSociete;
            if (idSocieteObj instanceof String) {
                idSociete = Integer.parseInt((String) idSocieteObj);
            } else if (idSocieteObj instanceof Integer) {
                idSociete = (Integer) idSocieteObj;
            } else {
                return ResponseEntity.status(400).body("Type de id_societe invalide");
            }
            
            stmt.setInt(1, idSociete);
            
            // Gérer la date de paiement
            Object dateObj = paiementData.get("date_paiement");
            java.sql.Timestamp datePaiement;
            if (dateObj instanceof String) {
                String dateStr = (String) dateObj;
                // Convertir de format datetime-local vers SQL timestamp
                if (dateStr.contains("T")) {
                    dateStr = dateStr.replace("T", " ");
                    if (dateStr.length() == 16) { // Format YYYY-MM-DD HH:MM
                        dateStr += ":00";
                    }
                }
                datePaiement = java.sql.Timestamp.valueOf(dateStr);
            } else {
                datePaiement = new java.sql.Timestamp(System.currentTimeMillis());
            }
            
            // Gérer le montant qui peut être String, Number ou pourcentage
            Object montantObj = paiementData.get("montant");
            double montant;
            if (montantObj instanceof String) {
                String montantStr = (String) montantObj;
                if (montantStr.endsWith("%")) {
                    // Gérer le pourcentage
                    double pourcentage = Double.parseDouble(montantStr.replace("%", ""));
                    
                    // Récupérer le montant total dû par cette société
                    try (PreparedStatement totalStmt = conn.prepareStatement(
                        "SELECT COALESCE(SUM(dp.prix_unitaire), 0) as total_du " +
                        "FROM diffusion_pub dp " +
                        "WHERE dp.id_societe = ?"
                    )) {
                        totalStmt.setInt(1, idSociete);
                        ResultSet totalRs = totalStmt.executeQuery();
                        if (totalRs.next()) {
                            double totalDu = totalRs.getDouble("total_du");
                            montant = totalDu * (pourcentage / 100.0);
                        } else {
                            montant = 0.0;
                        }
                    }
                } else {
                    montant = Double.parseDouble(montantStr);
                }
            } else if (montantObj instanceof Number) {
                montant = ((Number) montantObj).doubleValue();
            } else {
                throw new IllegalArgumentException("Type de montant invalide: " + montantObj.getClass().getName());
            }
            stmt.setDouble(2, montant);
            
            stmt.setString(3, (String) paiementData.get("description"));
            stmt.setTimestamp(4, datePaiement);
            
            int rowsAffected = stmt.executeUpdate();
            
            // Mettre à jour le champ deja_payer dans societe
            if (rowsAffected > 0) {
                try (PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE societe SET deja_payer = COALESCE(deja_payer, 0) + ? WHERE id = ?"
                )) {
                    updateStmt.setDouble(1, montant);
                    updateStmt.setInt(2, (Integer) paiementData.get("id_societe"));
                    updateStmt.executeUpdate();
                }
            }
            
            if (rowsAffected > 0) {
                return ResponseEntity.ok("Paiement ajouté avec succès");
            } else {
                return ResponseEntity.status(500).body("Erreur lors de l'ajout du paiement");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur de base de données: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur serveur: " + e.getMessage());
        }
    }
    
    @GetMapping("/montant-total")
    public ResponseEntity<TotalGeneral> getMontantTotal() {
        TotalGeneral total = new TotalGeneral(0, 0.0);
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT COUNT(*) as total_diffusions, SUM(prix_unitaire) as montant_total " +
                 "FROM DIFFUSION_PUB"
             )) {
            
            if (rs.next()) {
                total = new TotalGeneral(
                    rs.getInt("total_diffusions"),
                    rs.getDouble("montant_total")
                );
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            total = new TotalGeneral(0, 0.0);
        }
        
        return ResponseEntity.ok(total);
    }
}
