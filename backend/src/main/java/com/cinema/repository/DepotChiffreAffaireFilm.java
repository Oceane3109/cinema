package com.cinema.repository;

import com.cinema.model.ChiffreAffaireFilm;
import com.cinema.model.Diffusionpub;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DepotChiffreAffaireFilm extends JpaRepository<ChiffreAffaireFilm, Long> {
    
    List<ChiffreAffaireFilm> findByFilmIdAndDateCalculBetweenAndTypePeriode(
            Long filmId, LocalDate dateDebut, LocalDate dateFin, String typePeriode);
    
    List<ChiffreAffaireFilm> findByDateCalculBetweenAndTypePeriode(
            LocalDate dateDebut, LocalDate dateFin, String typePeriode);
    
    List<ChiffreAffaireFilm> findByFilmId(Long filmId);
    
    List<ChiffreAffaireFilm> findByTypePeriode(String typePeriode);
    
    @Query("SELECT ca FROM ChiffreAffaireFilm ca WHERE ca.dateCalcul = :date AND ca.typePeriode = :periode")
    List<ChiffreAffaireFilm> findByDateAndPeriode(@Param("date") LocalDate date, @Param("periode") String periode);
    
    void deleteByFilmId(Long filmId);

    @Query("SELECT d FROM Diffusionpub d WHERE d.filmId = :filmId AND EXTRACT(MONTH FROM d.dateHeureDiff) = :mois")
    List<Diffusionpub> findByFilmIdAndMois(@Param("filmId") Long filmId, @Param("mois") int mois);

    @Query("SELECT SUM(p.montant) FROM Paiement p WHERE p.diffusionId = :diffusionId")
    BigDecimal calculerTotalPayePourDiffusion(@Param("diffusionId") int diffusionId);
}
