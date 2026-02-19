package com.cinema.model;

import lombok.Data;

import java.util.List;

@Data
public class DemandeReservation {
    private Long seanceId;
    private String nomClient;
    private String emailClient;
    private String telephoneClient;
    private String typeClient = "adulte"; // "adulte", "enfant", "ado", "senior"
    private List<Long> placeIds; // Liste des IDs des places sélectionnées
}