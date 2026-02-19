package com.cinema.repository;

import com.cinema.model.Tarif;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepotTarif extends JpaRepository<Tarif, Long> {
    List<Tarif> findByActifTrue();
    List<Tarif> findByTypeAndActifTrue(String type);
    List<Tarif> findByGenreAndActifTrue(String genre);
    Optional<Tarif> findByTypeAndGenreAndActifTrue(String type, String genre);
    List<Tarif> findByTypeContainingIgnoreCase(String type);
    List<Tarif> findByGenreContainingIgnoreCase(String genre);
    boolean existsByNomAndGenre(String nom, String genre);
}
