package com.cinema.service;

import com.cinema.dto.TarifDTO;
import com.cinema.repository.DepotTarif;
import com.cinema.repository.DepotClientTarif;
import com.cinema.model.Tarif;
import com.cinema.model.ClientTarif;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TarifServiceImpl implements TarifService {

    @Autowired
    private DepotTarif depotTarif;

    @Autowired
    private DepotClientTarif depotClientTarif;

    @Override
    public List<TarifDTO> getTousLesTarifs() {
        return depotTarif.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TarifDTO> getTarifsActifs() {
        return depotTarif.findByActifTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TarifDTO getTarifParId(Long id) {
        Optional<Tarif> tarif = depotTarif.findById(id);
        return tarif.map(this::convertToDTO).orElse(null);
    }

    @Override
    public List<TarifDTO> getTarifsParCategorie(String categorie) {
        return depotTarif.findByTypeAndActifTrue(categorie).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TarifDTO> getTarifsParGenre(String genre) {
        return depotTarif.findByGenreAndActifTrue(genre).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TarifDTO getTarifParCategorieEtGenre(String categorie, String genre) {
        Optional<Tarif> tarif = depotTarif.findByTypeAndGenreAndActifTrue(categorie, genre);
        return tarif.map(this::convertToDTO).orElse(null);
    }

    @Override
    public TarifDTO creerTarif(TarifDTO tarifDTO) {
        Tarif tarif = convertToEntity(tarifDTO);
        tarif.setCreatedAt(LocalDateTime.now());
        tarif.setUpdatedAt(LocalDateTime.now());
        Tarif savedTarif = depotTarif.save(tarif);
        return convertToDTO(savedTarif);
    }

    @Override
    public TarifDTO modifierTarif(Long id, TarifDTO tarifDTO) {
        Optional<Tarif> existingTarif = depotTarif.findById(id);
        if (existingTarif.isPresent()) {
            Tarif tarif = existingTarif.get();
            tarif.setNom(tarifDTO.getNom());
            tarif.setPrix(tarifDTO.getPrix());
            tarif.setDescription(tarifDTO.getDescription());
            tarif.setType(tarifDTO.getType());
            tarif.setGenre(tarifDTO.getGenre());
            tarif.setAgeMin(tarifDTO.getAgeMin());
            tarif.setAgeMax(tarifDTO.getAgeMax());
            tarif.setDescriptionComplementaire(tarifDTO.getDescriptionComplementaire());
            tarif.setActif(tarifDTO.getActif());
            tarif.setUpdatedAt(LocalDateTime.now());
            Tarif savedTarif = depotTarif.save(tarif);
            return convertToDTO(savedTarif);
        }
        return null;
    }

    @Override
    public boolean supprimerTarif(Long id) {
        if (depotTarif.existsById(id)) {
            depotTarif.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean changerStatutTarif(Long id, boolean actif) {
        Optional<Tarif> tarif = depotTarif.findById(id);
        if (tarif.isPresent()) {
            Tarif t = tarif.get();
            t.setActif(actif);
            t.setUpdatedAt(LocalDateTime.now());
            depotTarif.save(t);
            return true;
        }
        return false;
    }

    @Override
    public Map<String, Object> getStatistiquesTarifs() {
        Map<String, Object> stats = new HashMap<>();
        
        List<Tarif> tousTarifs = depotTarif.findAll();
        stats.put("totalTarifs", tousTarifs.size());
        
        long tarifsActifs = tousTarifs.stream().filter(Tarif::getActif).count();
        stats.put("tarifsActifs", tarifsActifs);
        
        Map<String, Long> statsParType = tousTarifs.stream()
                .collect(Collectors.groupingBy(Tarif::getType, Collectors.counting()));
        stats.put("statsParType", statsParType);
        
        Map<String, Long> statsParGenre = tousTarifs.stream()
                .collect(Collectors.groupingBy(Tarif::getGenre, Collectors.counting()));
        stats.put("statsParGenre", statsParGenre);
        
        BigDecimal prixMoyen = tousTarifs.stream()
                .filter(Tarif::getActif)
                .map(Tarif::getPrix)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(tarifsActifs), 2, BigDecimal.ROUND_HALF_UP);
        stats.put("prixMoyen", prixMoyen);
        
        return stats;
    }

    @Override
    public byte[] exporterTarifsCSV() {
        List<TarifDTO> tarifs = getTousLesTarifs();
        StringBuilder csv = new StringBuilder();
        
        csv.append("ID,Nom,Prix,Description,Type,Genre,Age Min,Age Max,Actif,Créé le\n");
        
        for (TarifDTO tarif : tarifs) {
            csv.append(tarif.getId()).append(",");
            csv.append("\"").append(tarif.getNom()).append("\",");
            csv.append(tarif.getPrix()).append(",");
            csv.append("\"").append(tarif.getDescription() != null ? tarif.getDescription() : "").append("\",");
            csv.append(tarif.getType()).append(",");
            csv.append(tarif.getGenre()).append(",");
            csv.append(tarif.getAgeMin() != null ? tarif.getAgeMin() : "").append(",");
            csv.append(tarif.getAgeMax() != null ? tarif.getAgeMax() : "").append(",");
            csv.append(tarif.getActif() ? "Oui" : "Non").append(",");
            csv.append(tarif.getCreatedAt()).append("\n");
        }
        
        return csv.toString().getBytes();
    }

    @Override
    public Map<String, Object> appliquerTarifReservation(Long reservationId, Long tarifId, String genre) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Optional<Tarif> tarifOpt = depotTarif.findById(tarifId);
            if (!tarifOpt.isPresent()) {
                result.put("success", false);
                result.put("message", "Tarif non trouvé");
                return result;
            }
            
            Tarif tarif = tarifOpt.get();
            if (!tarif.getActif()) {
                result.put("success", false);
                result.put("message", "Tarif inactif");
                return result;
            }
            
            ClientTarif clientTarif = new ClientTarif();
            clientTarif.setReservationId(reservationId);
            clientTarif.setTarifId(tarifId);
            clientTarif.setGenreApplique(genre);
            clientTarif.setTarifApplique(tarif.getPrix());
            clientTarif.setCreatedAt(LocalDateTime.now());
            clientTarif.setUpdatedAt(LocalDateTime.now());
            
            depotClientTarif.save(clientTarif);
            
            result.put("success", true);
            result.put("message", "Tarif appliqué avec succès");
            result.put("tarifApplique", tarif.getPrix());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Erreur lors de l'application du tarif: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getTarifsClient(Long reservationId) {
        List<ClientTarif> clientTarifs = depotClientTarif.findByReservationId(reservationId);
        
        return clientTarifs.stream().map(ct -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", ct.getId());
            map.put("tarifId", ct.getTarifId());
            map.put("genreApplique", ct.getGenreApplique());
            map.put("tarifApplique", ct.getTarifApplique());
            
            Optional<Tarif> tarif = depotTarif.findById(ct.getTarifId());
            if (tarif.isPresent()) {
                map.put("nomTarif", tarif.get().getNom());
                map.put("typeTarif", tarif.get().getType());
            }
            
            return map;
        }).collect(Collectors.toList());
    }

    private TarifDTO convertToDTO(Tarif tarif) {
        return new TarifDTO(
            tarif.getId(),
            tarif.getNom(),
            tarif.getPrix(),
            tarif.getDescription(),
            tarif.getType(),
            tarif.getGenre(),
            tarif.getAgeMin(),
            tarif.getAgeMax(),
            tarif.getDescriptionComplementaire(),
            tarif.getActif(),
            tarif.getCreatedAt(),
            tarif.getUpdatedAt()
        );
    }

    private Tarif convertToEntity(TarifDTO dto) {
        Tarif tarif = new Tarif();
        tarif.setId(dto.getId());
        tarif.setNom(dto.getNom());
        tarif.setPrix(dto.getPrix());
        tarif.setDescription(dto.getDescription());
        tarif.setType(dto.getType());
        tarif.setGenre(dto.getGenre());
        tarif.setAgeMin(dto.getAgeMin());
        tarif.setAgeMax(dto.getAgeMax());
        tarif.setDescriptionComplementaire(dto.getDescriptionComplementaire());
        tarif.setActif(dto.getActif());
        return tarif;
    }
}
