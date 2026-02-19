package com.cinema.repository;

import com.cinema.model.ProduitExtra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProduitExtraRepository extends JpaRepository<ProduitExtra, Integer> {
    
    List<ProduitExtra> findByOrderByNomAsc();
    
    Optional<ProduitExtra> findByNom(String nom);
    
    boolean existsByNom(String nom);
}
