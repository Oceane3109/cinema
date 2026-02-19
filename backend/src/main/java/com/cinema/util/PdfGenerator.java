package com.cinema.util;

import com.cinema.model.Billet;
import com.cinema.model.Facture;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

public class PdfGenerator {

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 18, Font.BOLD);
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 14, Font.BOLD);
    private static final Font NORMAL_FONT = new Font(Font.HELVETICA, 12, Font.NORMAL);
    private static final Font BOLD_FONT = new Font(Font.HELVETICA, 12, Font.BOLD);

    public static byte[] genererPdfBillet(Billet billet) throws Exception {
        if (billet == null) {
            throw new IllegalArgumentException("Le billet ne peut pas être null");
        }

        Document document = new Document(PageSize.A6, 20, 20, 20, 20);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // En-tête stylisé
            Paragraph header = new Paragraph("🎬 CINÉMA ROYAL 🎬", TITLE_FONT);
            header.setAlignment(Element.ALIGN_CENTER);
            header.setSpacingAfter(10);
            document.add(header);

            // Ligne décorative
            Paragraph deco = new Paragraph("═══════════════════════════════════════════", NORMAL_FONT);
            deco.setAlignment(Element.ALIGN_CENTER);
            deco.setSpacingAfter(15);
            document.add(deco);

            // Titre du film en gros
            Font filmFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Paragraph filmTitle = new Paragraph(safeString(getFilmTitre(billet)), filmFont);
            filmTitle.setAlignment(Element.ALIGN_CENTER);
            filmTitle.setSpacingAfter(10);
            document.add(filmTitle);

            // Informations principales dans un cadre
            addInfoSection(document, "📅 Date & Heure",
                formatDate(getSeanceDate(billet)));
            addInfoSection(document, "🏛️ Salle",
                safeString(getSalleNom(billet)));
            addInfoSection(document, "💺 Siège",
                "Rangée " + getRangee(billet) + " - Place " + getPlace(billet));

            // Prix en évidence
            Paragraph priceSection = new Paragraph();
            priceSection.setSpacingBefore(15);
            priceSection.setSpacingAfter(15);

            Chunk priceLabel = new Chunk("💰 Prix: ", BOLD_FONT);
            Chunk priceValue = new Chunk(formatPrix(billet.getPrixUnitaire()), new Font(Font.HELVETICA, 14, Font.BOLD));

            priceSection.add(priceLabel);
            priceSection.add(priceValue);
            priceSection.setAlignment(Element.ALIGN_CENTER);
            document.add(priceSection);

            // QR Code section
            Paragraph qrTitle = new Paragraph("📱 Code d'accès", HEADER_FONT);
            qrTitle.setAlignment(Element.ALIGN_CENTER);
            qrTitle.setSpacingBefore(10);
            qrTitle.setSpacingAfter(5);
            document.add(qrTitle);

            // Simuler un QR code avec du texte stylisé
            Paragraph qrCode = new Paragraph();
            qrCode.setAlignment(Element.ALIGN_CENTER);
            qrCode.setSpacingAfter(10);

            String qrText = safeString(billet.getCodeQr());
            if (qrText.length() > 20) {
                qrText = qrText.substring(0, 20) + "...";
            }

            qrCode.add(new Chunk("█▀▀▀▀▀█\n", new Font(Font.COURIER, 8)));
            qrCode.add(new Chunk("█     █\n", new Font(Font.COURIER, 8)));
            qrCode.add(new Chunk("█  " + qrText + "  █\n", new Font(Font.COURIER, 8)));
            qrCode.add(new Chunk("█     █\n", new Font(Font.COURIER, 8)));
            qrCode.add(new Chunk("█▄▄▄▄▄█", new Font(Font.COURIER, 8)));

            document.add(qrCode);

            // Référence du billet
            Paragraph refSection = new Paragraph();
            refSection.setAlignment(Element.ALIGN_CENTER);
            refSection.setSpacingBefore(10);
            refSection.setSpacingAfter(10);

            refSection.add(new Chunk("Référence: ", BOLD_FONT));
            refSection.add(new Chunk(safeString(billet.getReferenceBillet()), NORMAL_FONT));
            document.add(refSection);

            // Statut
            Paragraph status = new Paragraph();
            status.setAlignment(Element.ALIGN_CENTER);
            status.setSpacingAfter(15);

            String statutText = safeString(billet.getStatut().toString());

            status.add(new Chunk("Statut: ", BOLD_FONT));
            status.add(new Chunk(statutText, new Font(Font.HELVETICA, 12, Font.BOLD)));
            document.add(status);

            // Ligne de séparation finale
            document.add(deco);

            // Message de remerciement
            Paragraph thanks = new Paragraph("🎭 Merci pour votre visite ! 🎭", NORMAL_FONT);
            thanks.setAlignment(Element.ALIGN_CENTER);
            thanks.setSpacingBefore(10);
            document.add(thanks);

            // Conditions
            Paragraph conditions = new Paragraph(
                "• Présentez ce billet à l'entrée\n• Arrivez 15 min avant la séance\n• Billets non remboursables",
                new Font(Font.HELVETICA, 8, Font.NORMAL));
            conditions.setAlignment(Element.ALIGN_CENTER);
            conditions.setSpacingBefore(10);
            document.add(conditions);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            document.close();
            throw new RuntimeException("Erreur lors de la génération du PDF billet: " + e.getMessage(), e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    public static byte[] genererPdfFacture(Facture facture) throws Exception {
        if (facture == null) {
            throw new IllegalArgumentException("La facture ne peut pas être null");
        }

        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // Titre
            Paragraph title = new Paragraph("CINEMA - FACTURE", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Ligne de séparation
            document.add(new Paragraph("========================================", NORMAL_FONT));
            document.add(new Paragraph(" ", NORMAL_FONT));

            // Informations de la facture
            document.add(createParagraph("Numéro de facture:", safeString(facture.getNumeroFacture())));
            document.add(createParagraph("Date d'émission:", formatDate(facture.getDateEmission())));

            document.add(new Paragraph(" ", NORMAL_FONT));

            // Informations client
            Paragraph clientTitle = new Paragraph("CLIENT", HEADER_FONT);
            clientTitle.setSpacingAfter(10);
            document.add(clientTitle);

            document.add(createParagraph("Nom:", safeString(facture.getNomClient())));
            document.add(createParagraph("Email:", safeString(facture.getEmailClient())));
            document.add(createParagraph("Téléphone:", safeString(facture.getTelephoneClient())));

            document.add(new Paragraph(" ", NORMAL_FONT));

            // Détails de la réservation
            Paragraph reservationTitle = new Paragraph("DÉTAIL DE LA RÉSERVATION", HEADER_FONT);
            reservationTitle.setSpacingAfter(10);
            document.add(reservationTitle);

            document.add(createParagraph("Film:", safeString(getFilmTitreFacture(facture))));
            document.add(createParagraph("Date:", formatDate(getSeanceDateFacture(facture))));
            document.add(createParagraph("Salle:", safeString(getSalleNomFacture(facture))));
            document.add(createParagraph("Nombre de places:", String.valueOf(getNombrePlaces(facture))));

            document.add(new Paragraph(" ", NORMAL_FONT));

            // Montants
            Paragraph amountTitle = new Paragraph("MONTANT", HEADER_FONT);
            amountTitle.setSpacingAfter(10);
            document.add(amountTitle);

            document.add(createParagraph("Montant HT:", formatPrix(facture.getMontantHt())));
            document.add(createParagraph("TVA (" + (facture.getTvaTaux() != null ? facture.getTvaTaux() : 0) + "%):",
                formatPrix(facture.getMontantTva())));
            document.add(createParagraph("Montant TTC:", formatPrix(facture.getMontantTtc())));

            document.add(new Paragraph(" ", NORMAL_FONT));
            document.add(createParagraph("Statut:", safeString(facture.getStatut().toString())));
            if (facture.getDatePaiement() != null) {
                document.add(createParagraph("Date de paiement:", formatDate(facture.getDatePaiement())));
            }

            document.add(new Paragraph(" ", NORMAL_FONT));
            document.add(new Paragraph("========================================", NORMAL_FONT));

            // Message de remerciement
            Paragraph thanks = new Paragraph("Merci pour votre visite !", NORMAL_FONT);
            thanks.setAlignment(Element.ALIGN_CENTER);
            thanks.setSpacingBefore(20);
            document.add(thanks);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            if (document.isOpen()) {
                document.close();
            }
            throw new RuntimeException("Erreur lors de la génération du PDF facture: " + e.getMessage(), e);
        }
    }

    private static String safeString(String value) {
        return value != null ? value : "N/A";
    }

    private static String getFilmTitre(Billet billet) {
        try {
            if (billet.getReservation() != null &&
                billet.getReservation().getSeance() != null &&
                billet.getReservation().getSeance().getFilm() != null) {
                return billet.getReservation().getSeance().getFilm().getTitre();
            }
        } catch (Exception e) {
            // Ignore
        }
        return "N/A";
    }

    private static java.time.LocalDateTime getSeanceDate(Billet billet) {
        try {
            if (billet.getReservation() != null &&
                billet.getReservation().getSeance() != null) {
                return billet.getReservation().getSeance().getDateHeure();
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }

    private static String getSalleNom(Billet billet) {
        try {
            if (billet.getReservation() != null &&
                billet.getReservation().getSeance() != null &&
                billet.getReservation().getSeance().getSalle() != null) {
                return billet.getReservation().getSeance().getSalle().getNom();
            }
        } catch (Exception e) {
            // Ignore
        }
        return "N/A";
    }

    private static int getRangee(Billet billet) {
        try {
            if (billet.getPlace() != null) {
                return billet.getPlace().getNumeroRangee();
            }
        } catch (Exception e) {
            // Ignore
        }
        return 0;
    }

    private static int getPlace(Billet billet) {
        try {
            if (billet.getPlace() != null) {
                return billet.getPlace().getNumeroPlace();
            }
        } catch (Exception e) {
            // Ignore
        }
        return 0;
    }

    private static String formatPrix(java.math.BigDecimal prix) {
        return prix != null ? prix + " €" : "0.00 €";
    }

    private static String getFilmTitreFacture(Facture facture) {
        try {
            if (facture.getReservation() != null &&
                facture.getReservation().getSeance() != null &&
                facture.getReservation().getSeance().getFilm() != null) {
                return facture.getReservation().getSeance().getFilm().getTitre();
            }
        } catch (Exception e) {
            // Ignore
        }
        return "N/A";
    }

    private static java.time.LocalDateTime getSeanceDateFacture(Facture facture) {
        try {
            if (facture.getReservation() != null &&
                facture.getReservation().getSeance() != null) {
                return facture.getReservation().getSeance().getDateHeure();
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }

    private static String getSalleNomFacture(Facture facture) {
        try {
            if (facture.getReservation() != null &&
                facture.getReservation().getSeance() != null &&
                facture.getReservation().getSeance().getSalle() != null) {
                return facture.getReservation().getSeance().getSalle().getNom();
            }
        } catch (Exception e) {
            // Ignore
        }
        return "N/A";
    }

    private static int getNombrePlaces(Facture facture) {
        try {
            if (facture.getReservation() != null &&
                facture.getReservation().getReservationPlaces() != null) {
                return facture.getReservation().getReservationPlaces().size();
            }
        } catch (Exception e) {
            // Ignore
        }
        return 0;
    }

    private static String formatDate(java.time.LocalDateTime dateTime) {
        if (dateTime != null) {
            try {
                return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            } catch (Exception e) {
                return "N/A";
            }
        }
        return "N/A";
    }

    private static void addInfoSection(Document document, String iconLabel, String value) throws DocumentException {
        Paragraph section = new Paragraph();
        section.setSpacingAfter(8);

        Chunk icon = new Chunk(iconLabel + " ", HEADER_FONT);
        Chunk val = new Chunk(value, NORMAL_FONT);

        section.add(icon);
        section.add(val);
        section.setAlignment(Element.ALIGN_CENTER);

        document.add(section);
    }

    private static Paragraph createParagraph(String label, String value) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + " ", BOLD_FONT));
        p.add(new Chunk(value, NORMAL_FONT));
        p.setSpacingAfter(5);
        return p;
    }
}