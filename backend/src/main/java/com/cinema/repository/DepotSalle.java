package com.cinema.repository;

import com.cinema.model.Salle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepotSalle extends JpaRepository<Salle, Long> {

    Optional<Salle> findByNom(String nom);

    List<Salle> findByType(String type);

    @Query("SELECT s FROM Salle s WHERE s.capacite >= :capacite")
    List<Salle> findSallesAvecCapaciteMinimale(@Param("capacite") Integer capacite);
}
