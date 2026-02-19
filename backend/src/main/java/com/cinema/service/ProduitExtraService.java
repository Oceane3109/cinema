package com.cinema.service;

import com.cinema.model.ProduitExtra;
import com.cinema.repository.ProduitExtraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProduitExtraService {
    
    @Autowired
    private ProduitExtraRepository produitExtraRepository;
    
    public List<ProduitExtra> getAllProduits() {
        return produitExtraRepository.findByOrderByNomAsc();
    }
    
    public Optional<ProduitExtra> getProduitById(Integer id) {
        return produitExtraRepository.findById(id);
    }
    
    public Optional<ProduitExtra> getProduitByNom(String nom) {
        return produitExtraRepository.findByNom(nom);
    }
    
    public ProduitExtra createProduit(ProduitExtra produit) {
        // Vérifier que le nom n'existe pas déjà
        if (produitExtraRepository.existsByNom(produit.getNom())) {
            throw new RuntimeException("Un produit avec ce nom existe déjà");
        }
        return produitExtraRepository.save(produit);
    }
    
    public ProduitExtra updateProduit(Integer id, ProduitExtra produit) {
        Optional<ProduitExtra> existingProduit = produitExtraRepository.findById(id);
        if (existingProduit.isEmpty()) {
            throw new RuntimeException("Produit non trouvé");
        }
        
        ProduitExtra produitToUpdate = existingProduit.get();
        
        // Vérifier que le nouveau nom n'existe pas déjà (si différent)
        if (!produitToUpdate.getNom().equals(produit.getNom()) && 
            produitExtraRepository.existsByNom(produit.getNom())) {
            throw new RuntimeException("Un produit avec ce nom existe déjà");
        }
        
        produitToUpdate.setNom(produit.getNom());
        produitToUpdate.setPrixUnitaire(produit.getPrixUnitaire());
        produitToUpdate.setDescription(produit.getDescription());
        
        return produitExtraRepository.save(produitToUpdate);
    }
    
    public void deleteProduit(Integer id) {
        if (!produitExtraRepository.existsById(id)) {
            throw new RuntimeException("Produit non trouvé");
        }
        produitExtraRepository.deleteById(id);
    }
}
