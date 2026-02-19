package com.cinema.service;

import com.cinema.model.VenteProduit;
import com.cinema.repository.VenteProduitRepository;
import com.cinema.repository.DepotSeance;
import com.cinema.repository.ProduitExtraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VenteProduitService {
    
    @Autowired
    private VenteProduitRepository venteProduitRepository;
    
    @Autowired
    private DepotSeance seanceRepository;
    
    @Autowired
    private ProduitExtraRepository produitExtraRepository;
    
    public List<VenteProduit> getAllVentes() {
        return venteProduitRepository.findAll();
    }
    
    public List<VenteProduit> getVentesBySeance(Integer idSeance) {
        // Vérifier que la séance existe
        if (!seanceRepository.existsById(idSeance.longValue())) {
            throw new RuntimeException("Séance non trouvée");
        }
        return venteProduitRepository.getVentesBySeance(idSeance);
    }
    
    public List<VenteProduit> getVentesByDate(LocalDate date) {
        return venteProduitRepository.getVentesByDate(date);
    }
    
    public BigDecimal getMontantTotalBySeance(Integer idSeance) {
        // Vérifier que la séance existe
        if (!seanceRepository.existsById(idSeance.longValue())) {
            throw new RuntimeException("Séance non trouvée");
        }
        return venteProduitRepository.getMontantTotalBySeance(idSeance);
    }
    
    public VenteProduit createVente(VenteProduit vente) {
        // Validation
        if (vente.getSeance() == null || vente.getSeance().getId() == null) {
            throw new RuntimeException("Séance obligatoire");
        }
        
        if (vente.getProduit() == null || vente.getProduit().getId() == null) {
            throw new RuntimeException("Produit obligatoire");
        }
        
        if (vente.getQuantite() == null || vente.getQuantite() <= 0) {
            throw new RuntimeException("Quantité invalide");
        }
        
        if (vente.getPrixUnitaireVente() == null || vente.getPrixUnitaireVente().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Prix unitaire invalide");
        }
        
        // Vérifier que la séance existe
        if (!seanceRepository.existsById(vente.getSeance().getId().longValue())) {
            throw new RuntimeException("Séance non trouvée");
        }
        
        // Vérifier que le produit existe
        if (!produitExtraRepository.existsById(vente.getProduit().getId())) {
            throw new RuntimeException("Produit non trouvé");
        }
        
        // Récupérer les entités complètes
        vente.setSeance(seanceRepository.findById(vente.getSeance().getId().longValue()).get());
        vente.setProduit(produitExtraRepository.findById(vente.getProduit().getId()).get());
        
        return venteProduitRepository.save(vente);
    }
    
    public List<VenteProduit> getVentesDuJour() {
        return getVentesByDate(LocalDate.now());
    }
    
    public BigDecimal getChiffreAffairesDuJour() {
        return getVentesDuJour().stream()
                .map(VenteProduit::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
