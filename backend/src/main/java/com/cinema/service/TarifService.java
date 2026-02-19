package com.cinema.service;

import com.cinema.dto.TarifDTO;
import java.util.List;
import java.util.Map;

public interface TarifService {
    List<TarifDTO> getTousLesTarifs();
    List<TarifDTO> getTarifsActifs();
    TarifDTO getTarifParId(Long id);
    List<TarifDTO> getTarifsParCategorie(String categorie);
    List<TarifDTO> getTarifsParGenre(String genre);
    TarifDTO getTarifParCategorieEtGenre(String categorie, String genre);
    TarifDTO creerTarif(TarifDTO tarifDTO);
    TarifDTO modifierTarif(Long id, TarifDTO tarifDTO);
    boolean supprimerTarif(Long id);
    boolean changerStatutTarif(Long id, boolean actif);
    Map<String, Object> getStatistiquesTarifs();
    byte[] exporterTarifsCSV();
    Map<String, Object> appliquerTarifReservation(Long reservationId, Long tarifId, String genre);
    List<Map<String, Object>> getTarifsClient(Long reservationId);
}
