package com.cinema.controller;

import com.cinema.model.ProduitExtra;
import com.cinema.service.ProduitExtraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produits-extras")
@CrossOrigin(origins = "*")
public class ProduitExtraController {
    
    @Autowired
    private ProduitExtraService produitExtraService;
    
    @GetMapping
    public ResponseEntity<List<ProduitExtra>> getAllProduits() {
        try {
            List<ProduitExtra> produits = produitExtraService.getAllProduits();
            return ResponseEntity.ok(produits);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProduitExtra> getProduitById(@PathVariable Integer id) {
        try {
            return produitExtraService.getProduitById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createProduit(@RequestBody ProduitExtra produit) {
        try {
            ProduitExtra newProduit = produitExtraService.createProduit(produit);
            return ResponseEntity.ok(newProduit);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erreur lors de la création du produit");
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduit(@PathVariable Integer id, @RequestBody ProduitExtra produit) {
        try {
            ProduitExtra updatedProduit = produitExtraService.updateProduit(id, produit);
            return ResponseEntity.ok(updatedProduit);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erreur lors de la mise à jour du produit");
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduit(@PathVariable Integer id) {
        try {
            produitExtraService.deleteProduit(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erreur lors de la suppression du produit");
        }
    }
}
