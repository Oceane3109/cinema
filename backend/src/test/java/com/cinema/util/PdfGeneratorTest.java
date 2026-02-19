package com.cinema.util;

import com.cinema.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PdfGeneratorTest {

    @Test
    void testGenererPdfBillet() throws Exception {
        // Créer des objets mock pour le test
        Film film = new Film();
        film.setTitre("Test Film");

        Salle salle = new Salle();
        salle.setNom("Salle Test");

        Seance seance = new Seance();
        seance.setFilm(film);
        seance.setSalle(salle);
        seance.setDateHeure(LocalDateTime.now().plusHours(2));

        Reservation reservation = new Reservation();
        reservation.setSeance(seance);

        Place place = new Place();
        place.setNumeroRangee(5);
        place.setNumeroPlace(10);

        Billet billet = new Billet();
        billet.setReferenceBillet("TEST-123");
        billet.setReservation(reservation);
        billet.setPlace(place);
        billet.setPrixUnitaire(new BigDecimal("12.50"));
        billet.setStatut(Billet.StatutBillet.ACTIF);
        billet.setCodeQr("QR123456");

        // Générer le PDF
        byte[] pdfBytes = PdfGenerator.genererPdfBillet(billet);

        // Vérifications
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        // Vérifier que c'est un PDF valide
        assertEquals('%', (char) pdfBytes[0]);
        assertEquals('P', (char) pdfBytes[1]);
        assertEquals('D', (char) pdfBytes[2]);
        assertEquals('F', (char) pdfBytes[3]);
    }

    @Test
    void testGenererPdfFacture() throws Exception {
        // Créer des objets mock pour le test
        Film film = new Film();
        film.setTitre("Test Film");

        Salle salle = new Salle();
        salle.setNom("Salle Test");

        Seance seance = new Seance();
        seance.setFilm(film);
        seance.setSalle(salle);
        seance.setDateHeure(LocalDateTime.now().plusHours(2));

        Reservation reservation = new Reservation();
        reservation.setSeance(seance);

        Facture facture = new Facture();
        facture.setNumeroFacture("FACT-2024-001");
        facture.setReservation(reservation);
        facture.setNomClient("Test Client");
        facture.setEmailClient("test@example.com");
        facture.setTelephoneClient("0123456789");
        facture.setMontantHt(new BigDecimal("25.00"));
        facture.setMontantTva(new BigDecimal("5.00"));
        facture.setMontantTtc(new BigDecimal("30.00"));
        facture.setTvaTaux(new BigDecimal("20.00"));
        facture.setDateEmission(LocalDateTime.now());
        facture.setStatut(Facture.StatutFacture.PAYE);

        // Générer le PDF
        byte[] pdfBytes = PdfGenerator.genererPdfFacture(facture);

        // Vérifications
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        // Vérifier que c'est un PDF valide
        assertEquals('%', (char) pdfBytes[0]);
        assertEquals('P', (char) pdfBytes[1]);
        assertEquals('D', (char) pdfBytes[2]);
        assertEquals('F', (char) pdfBytes[3]);
    }

    @Test
    void testNullObjects() {
        assertThrows(IllegalArgumentException.class, () -> PdfGenerator.genererPdfBillet(null));
        assertThrows(IllegalArgumentException.class, () -> PdfGenerator.genererPdfFacture(null));
    }
}