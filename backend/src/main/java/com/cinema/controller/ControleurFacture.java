package com.cinema.controller;

import com.cinema.model.Facture;
import com.cinema.repository.DepotFacture;
import com.cinema.util.PdfGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/factures")
@CrossOrigin(origins = "*")
public class ControleurFacture {

    @Autowired
    private DepotFacture depotFacture;

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<Facture> getFactureByReservation(@PathVariable Long reservationId) {
        Optional<Facture> facture = depotFacture.findByReservationId(reservationId);
        return facture.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Facture> getFactureById(@PathVariable Long id) {
        Optional<Facture> facture = depotFacture.findById(id);
        return facture.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/numero/{numero}")
    public ResponseEntity<Facture> getFactureByNumero(@PathVariable String numero) {
        Optional<Facture> facture = depotFacture.findByNumeroFacture(numero);
        return facture.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/client/{email}")
    public ResponseEntity<List<Facture>> getFacturesByClient(@PathVariable String email) {
        List<Facture> factures = depotFacture.findByEmailClient(email);
        return ResponseEntity.ok(factures);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> genererPdfFacture(@PathVariable Long id) {
        try {
            Optional<Facture> factureOpt = depotFacture.findByIdWithAllData(id);
            if (factureOpt.isEmpty()) {
                System.err.println("Facture non trouvée avec ID: " + id);
                return ResponseEntity.notFound().build();
            }

            Facture facture = factureOpt.get();
            System.out.println("Génération PDF pour facture: " + facture.getNumeroFacture());

            // Vérifier les données essentielles
            if (facture.getReservation() == null) {
                System.err.println("Réservation null pour facture: " + facture.getNumeroFacture());
                return ResponseEntity.internalServerError().build();
            }

            byte[] pdfBytes = PdfGenerator.genererPdfFacture(facture);

            // Vérifier que le PDF n'est pas vide
            if (pdfBytes == null || pdfBytes.length == 0) {
                System.err.println("PDF généré est vide pour facture: " + facture.getNumeroFacture());
                return ResponseEntity.internalServerError().build();
            }

            System.out.println("PDF généré avec succès, taille: " + pdfBytes.length + " bytes");

            // Vérifier que c'est bien du PDF (commence par %PDF-)
            if (pdfBytes.length < 4 || pdfBytes[0] != '%' || pdfBytes[1] != 'P' ||
                pdfBytes[2] != 'D' || pdfBytes[3] != 'F') {
                System.err.println("PDF généré n'est pas valide pour facture: " + facture.getNumeroFacture());
                System.err.println("Premiers bytes: " + (pdfBytes.length > 10 ? new String(pdfBytes, 0, 10) : "trop court"));
                return ResponseEntity.internalServerError().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "facture-" + facture.getNumeroFacture() + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            System.err.println("Erreur lors de la génération du PDF facture " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/revenus")
    public ResponseEntity<java.math.BigDecimal> getTotalRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        java.math.BigDecimal total = depotFacture.calculateTotalRevenue(startDate, endDate);
        return ResponseEntity.ok(total != null ? total : java.math.BigDecimal.ZERO);
    }

}