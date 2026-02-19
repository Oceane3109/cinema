package com.cinema.repository;

import com.cinema.model.VenteProduit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VenteProduitRepository extends JpaRepository<VenteProduit, Integer> {
    
    List<VenteProduit> findBySeanceIdOrderByDateVenteDesc(Integer idSeance);
    
    List<VenteProduit> findByDateVenteBetweenOrderByDateVenteDesc(LocalDateTime debut, LocalDateTime fin);
    
    @Query("SELECT COALESCE(SUM(v.quantite * v.prixUnitaireVente), 0) FROM VenteProduit v WHERE v.seance.id = :idSeance")
    BigDecimal getMontantTotalBySeance(@Param("idSeance") Integer idSeance);
    
    @Query("SELECT v FROM VenteProduit v WHERE v.seance.id = :idSeance ORDER BY v.dateVente DESC")
    List<VenteProduit> getVentesBySeance(@Param("idSeance") Integer idSeance);
    
    @Query("SELECT v FROM VenteProduit v WHERE DATE(v.dateVente) = :date ORDER BY v.dateVente DESC")
    List<VenteProduit> getVentesByDate(@Param("date") LocalDate date);
}
