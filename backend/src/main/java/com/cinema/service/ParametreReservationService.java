package com.cinema.service;

import com.cinema.model.ParametreReservation;
import com.cinema.repository.DepotParametreReservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ParametreReservationService {
    
    @Autowired
    private DepotParametreReservation depotParametreReservation;
    
    @PostConstruct
    public void initParametres() {
        // Créer les paramètres par défaut s'ils n'existent pas
        creerParametreSiNexistePas("REMISE_PLACES_MIN", "5", "Nombre minimum de places pour obtenir une remise", "INTEGER");
        creerParametreSiNexistePas("REMISE_POURCENTAGE", "10", "Pourcentage de remise pour les réservations groupées", "DECIMAL");
    }
    
    private void creerParametreSiNexistePas(String nom, String valeur, String description, String type) {
        if (!depotParametreReservation.findByNomParametre(nom).isPresent()) {
            ParametreReservation parametre = new ParametreReservation(nom, valeur, description, type);
            depotParametreReservation.save(parametre);
            System.out.println("Paramètre créé: " + nom + " = " + valeur);
        }
    }
    
    public List<ParametreReservation> getAllParametres() {
        return depotParametreReservation.findAll();
    }
    
    public Optional<ParametreReservation> getParametre(String nomParametre) {
        return depotParametreReservation.findByNomParametre(nomParametre);
    }
    
    public ParametreReservation saveOrUpdateParametre(ParametreReservation parametre) {
        Optional<ParametreReservation> existing = depotParametreReservation.findByNomParametre(parametre.getNomParametre());
        if (existing.isPresent()) {
            ParametreReservation existingParam = existing.get();
            existingParam.setValeur(parametre.getValeur());
            existingParam.setDescription(parametre.getDescription());
            existingParam.setTypeValeur(parametre.getTypeValeur());
            return depotParametreReservation.save(existingParam);
        } else {
            return depotParametreReservation.save(parametre);
        }
    }
    
    public void deleteParametre(Long id) {
        depotParametreReservation.deleteById(id);
    }
    
    // Méthodes spécifiques pour les paramètres de remise
    public int getNombrePlacesMinimumPourRemise() {
        Optional<ParametreReservation> param = getParametre("REMISE_PLACES_MIN");
        return param.map(ParametreReservation::getValeurAsInteger).orElse(5);
    }
    
    public BigDecimal getPourcentageRemise() {
        Optional<ParametreReservation> param = getParametre("REMISE_POURCENTAGE");
        return param.map(ParametreReservation::getValeurAsBigDecimal).orElse(new BigDecimal("10"));
    }
    
    public boolean appliquerRemise(int nombrePlaces) {
        return nombrePlaces >= getNombrePlacesMinimumPourRemise();
    }
    
    public BigDecimal calculerRemise(BigDecimal totalHT, int nombrePlaces) {
        if (appliquerRemise(nombrePlaces)) {
            BigDecimal pourcentageRemise = getPourcentageRemise();
            return totalHT.multiply(pourcentageRemise).divide(new BigDecimal("100"));
        }
        return BigDecimal.ZERO;
    }
    
    public BigDecimal calculerTotalAvecRemise(BigDecimal totalHT, int nombrePlaces) {
        BigDecimal remise = calculerRemise(totalHT, nombrePlaces);
        return totalHT.subtract(remise);
    }
}
