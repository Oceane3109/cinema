package com.cinema.controller;

import com.cinema.model.Billet;
import com.cinema.repository.DepotBillet;
import com.cinema.util.PdfGenerator;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/billets")
@CrossOrigin(origins = "*")
public class ControleurBillet {

    @Autowired
    private DepotBillet depotBillet;

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<List<Billet>> getBilletsByReservation(@PathVariable Long reservationId) {
        List<Billet> billets = depotBillet.findByReservationId(reservationId);
        return ResponseEntity.ok(billets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Billet> getBilletById(@PathVariable Long id) {
        Optional<Billet> billet = depotBillet.findById(id);
        return billet.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/reference/{reference}")
    public ResponseEntity<Billet> getBilletByReference(@PathVariable String reference) {
        Optional<Billet> billet = depotBillet.findByReferenceBillet(reference);
        return billet.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/qr/{codeQr}")
    public ResponseEntity<Billet> getBilletByQrCode(@PathVariable String codeQr) {
        Optional<Billet> billet = depotBillet.findByCodeQr(codeQr);
        return billet.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/utiliser")
    public ResponseEntity<Billet> utiliserBillet(@PathVariable Long id) {
        Optional<Billet> billetOpt = depotBillet.findById(id);
        if (billetOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Billet billet = billetOpt.get();
        if (billet.getStatut() != Billet.StatutBillet.ACTIF) {
            return ResponseEntity.badRequest().build();
        }

        billet.setStatut(Billet.StatutBillet.UTILISE);
        billet.setDateUtilisation(java.time.LocalDateTime.now());
        depotBillet.save(billet);

        return ResponseEntity.ok(billet);
    }

    @GetMapping("/test-pdf")
    public ResponseEntity<byte[]> testPdf() {
        try {
            System.out.println("=== TEST PDF COMMENCÉ ===");

            // Créer un PDF minimal
            Document document = new Document(PageSize.A4);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            try {
                com.lowagie.text.pdf.PdfWriter.getInstance(document, baos);
                document.open();

                document.add(new Paragraph("TEST PDF - CINEMA"));
                document.add(new Paragraph("Si vous voyez ce texte, la génération PDF fonctionne !"));
                document.add(new Paragraph("Date: " + java.time.LocalDateTime.now()));

                document.close();
            } catch (Exception e) {
                document.close();
                throw e;
            }

            byte[] pdfBytes = baos.toByteArray();

            System.out.println("PDF test généré avec succès !");
            System.out.println("Taille: " + pdfBytes.length + " bytes");

            // Vérifier que c'est un PDF valide
            if (pdfBytes.length > 4) {
                System.out.println("Premiers bytes: " +
                    String.format("%02X %02X %02X %02X",
                        pdfBytes[0], pdfBytes[1], pdfBytes[2], pdfBytes[3]));
                System.out.println("Commence par %PDF?: " +
                    (pdfBytes[0] == '%' && pdfBytes[1] == 'P' &&
                     pdfBytes[2] == 'D' && pdfBytes[3] == 'F'));
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "test-cinema.pdf");
            headers.setContentLength(pdfBytes.length);

            System.out.println("=== TEST PDF TERMINÉ ===");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            System.err.println("=== ERREUR TEST PDF ===");
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            System.err.println("=== FIN ERREUR TEST PDF ===");
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/debug-data")
    public ResponseEntity<String> debugData() {
        try {
            System.out.println("=== DEBUG DATA COMMENCÉ ===");

            // Tester l'accès aux données
            List<Billet> billets = depotBillet.findAll();
            System.out.println("Nombre total de billets: " + billets.size());

            if (!billets.isEmpty()) {
                Billet premierBillet = billets.get(0);
                System.out.println("Premier billet ID: " + premierBillet.getId());
                System.out.println("Référence: " + premierBillet.getReferenceBillet());

                // Tester le chargement avec JOIN FETCH
                Optional<Billet> billetComplet = depotBillet.findByIdWithAllData(premierBillet.getId());
                if (billetComplet.isPresent()) {
                    Billet b = billetComplet.get();
                    System.out.println("Billet chargé avec JOIN FETCH:");
                    System.out.println("  - Réservation: " + (b.getReservation() != null ? "OK" : "NULL"));
                    System.out.println("  - Séance: " + (b.getReservation() != null && b.getReservation().getSeance() != null ? "OK" : "NULL"));
                    System.out.println("  - Film: " + (b.getReservation() != null && b.getReservation().getSeance() != null && b.getReservation().getSeance().getFilm() != null ? "OK" : "NULL"));
                    System.out.println("  - Salle: " + (b.getReservation() != null && b.getReservation().getSeance() != null && b.getReservation().getSeance().getSalle() != null ? "OK" : "NULL"));
                    System.out.println("  - Place: " + (b.getPlace() != null ? "OK" : "NULL"));
                }
            }

            System.out.println("=== DEBUG DATA TERMINÉ ===");
            return ResponseEntity.ok("Debug terminé - voir les logs du serveur");
        } catch (Exception e) {
            System.err.println("=== ERREUR DEBUG DATA ===");
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            System.err.println("=== FIN ERREUR DEBUG DATA ===");
            return ResponseEntity.internalServerError().body("Erreur: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> genererPdfBillet(@PathVariable Long id) {
        try {
            Optional<Billet> billetOpt = depotBillet.findByIdWithAllData(id);
            if (billetOpt.isEmpty()) {
                System.err.println("Billet non trouvé avec ID: " + id);
                return ResponseEntity.notFound().build();
            }

            Billet billet = billetOpt.get();
            System.out.println("Génération PDF pour billet: " + billet.getReferenceBillet());

            // Vérifier les données essentielles
            if (billet.getReservation() == null) {
                System.err.println("Réservation null pour billet: " + billet.getReferenceBillet());
                return ResponseEntity.internalServerError().build();
            }

            byte[] pdfBytes = PdfGenerator.genererPdfBillet(billet);

            // Vérifier que le PDF n'est pas vide
            if (pdfBytes == null || pdfBytes.length == 0) {
                System.err.println("PDF généré est vide pour billet: " + billet.getReferenceBillet());
                return ResponseEntity.internalServerError().build();
            }

            System.out.println("PDF généré avec succès, taille: " + pdfBytes.length + " bytes");

            // Vérifier que c'est bien du PDF (commence par %PDF-)
            if (pdfBytes.length < 4 || pdfBytes[0] != '%' || pdfBytes[1] != 'P' ||
                pdfBytes[2] != 'D' || pdfBytes[3] != 'F') {
                System.err.println("PDF généré n'est pas valide pour billet: " + billet.getReferenceBillet());
                System.err.println("Premiers bytes: " + (pdfBytes.length > 10 ? new String(pdfBytes, 0, 10) : "trop court"));
                return ResponseEntity.internalServerError().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "billet-" + billet.getReferenceBillet() + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            System.err.println("Erreur lors de la génération du PDF billet " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

}