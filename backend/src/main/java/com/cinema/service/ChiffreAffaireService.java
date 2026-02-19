package com.cinema.service;

import com.cinema.dto.ChiffreAffaireDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ChiffreAffaireService {
    List<ChiffreAffaireDTO> getChiffreAffaireParFilm(Long filmId, String periode, LocalDate dateDebut, LocalDate dateFin);
    List<ChiffreAffaireDTO> getChiffreAffaireTousFilms(String periode, LocalDate dateDebut, LocalDate dateFin);
    Map<String, Object> getStatistiquesGenerales(String periode, LocalDate dateDebut, LocalDate dateFin);
    List<Map<String, Object>> getTopFilms(String periode, LocalDate dateDebut, LocalDate dateFin, int limite);
    List<Map<String, Object>> getChiffreAffaireParCategorie(String periode, LocalDate dateDebut, LocalDate dateFin);
    List<Map<String, Object>> getChiffreAffaireParGenre(String periode, LocalDate dateDebut, LocalDate dateFin);
    List<Map<String, Object>> getEvolutionTemporelle(Long filmId, String periode, LocalDate dateDebut, LocalDate dateFin);
    Map<String, Object> recalculerChiffreAffaire(LocalDate dateDebut, LocalDate dateFin);
    byte[] exporterChiffreAffairePDF(String periode, LocalDate dateDebut, LocalDate dateFin, Long filmId);
    byte[] exporterChiffreAffaireExcel(String periode, LocalDate dateDebut, LocalDate dateFin, Long filmId);
    Map<String, Object> getDashboardData(String periode, LocalDate dateDebut, LocalDate dateFin);
    List<Map<String, Object>> getRevenuMaxParSeance();
    List<Map<String, Object>> getRevenuReelParSeance();
    List<Map<String, Object>> getRevenuMaxParSalle();
    List<Map<String, Object>> getChiffreAffairesParFilm(String dateDebut, String dateFin, String periode, Long filmId);
    List<ChiffreAffaireDTO> calculerChiffreAffaireParFilmEtSeance(Long filmId, LocalDate mois);
}
