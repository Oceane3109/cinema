package com.cinema.controller;

import com.cinema.dto.TarifDTO;
import com.cinema.service.TarifService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tarifs")
@CrossOrigin(origins = "http://localhost:5173")
public class ControleurTarif {

    @Autowired
    private TarifService tarifService;

    @GetMapping
    public List<TarifDTO> getTousLesTarifs() {
        return tarifService.getTousLesTarifs();
    }

    @GetMapping("/actifs")
    public List<TarifDTO> getTarifsActifs() {
        return tarifService.getTarifsActifs();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TarifDTO> getTarifParId(@PathVariable Long id) {
        TarifDTO tarif = tarifService.getTarifParId(id);
        if (tarif != null) {
            return ResponseEntity.ok(tarif);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/categorie/{categorie}")
    public List<TarifDTO> getTarifsParCategorie(@PathVariable String categorie) {
        return tarifService.getTarifsParCategorie(categorie);
    }

    @GetMapping("/genre/{genre}")
    public List<TarifDTO> getTarifsParGenre(@PathVariable String genre) {
        return tarifService.getTarifsParGenre(genre);
    }

    @GetMapping("/categorie/{categorie}/genre/{genre}")
    public ResponseEntity<TarifDTO> getTarifParCategorieEtGenre(
            @PathVariable String categorie, 
            @PathVariable String genre) {
        TarifDTO tarif = tarifService.getTarifParCategorieEtGenre(categorie, genre);
        if (tarif != null) {
            return ResponseEntity.ok(tarif);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public TarifDTO creerTarif(@RequestBody TarifDTO tarifDTO) {
        return tarifService.creerTarif(tarifDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TarifDTO> modifierTarif(
            @PathVariable Long id, 
            @RequestBody TarifDTO tarifDTO) {
        TarifDTO tarifModifie = tarifService.modifierTarif(id, tarifDTO);
        if (tarifModifie != null) {
            return ResponseEntity.ok(tarifModifie);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerTarif(@PathVariable Long id) {
        if (tarifService.supprimerTarif(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/activer")
    public ResponseEntity<Void> activerTarif(@PathVariable Long id) {
        if (tarifService.changerStatutTarif(id, true)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/desactiver")
    public ResponseEntity<Void> desactiverTarif(@PathVariable Long id) {
        if (tarifService.changerStatutTarif(id, false)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/statistiques")
    public Map<String, Object> getStatistiquesTarifs() {
        return tarifService.getStatistiquesTarifs();
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exporterTarifsCSV() {
        byte[] csvData = tarifService.exporterTarifsCSV();
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=\"tarifs.csv\"")
                .body(csvData);
    }

    @PostMapping("/appliquer")
    public ResponseEntity<Map<String, Object>> appliquerTarifReservation(
            @RequestBody Map<String, Object> request) {
        Long reservationId = Long.valueOf(request.get("reservationId").toString());
        Long tarifId = Long.valueOf(request.get("tarifId").toString());
        String genre = request.get("genre").toString();
        
        Map<String, Object> result = tarifService.appliquerTarifReservation(reservationId, tarifId, genre);
        
        if (result.containsKey("success") && (Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/client/{reservationId}")
    public ResponseEntity<List<Map<String, Object>>> getTarifsClient(@PathVariable Long reservationId) {
        List<Map<String, Object>> tarifsClient = tarifService.getTarifsClient(reservationId);
        return ResponseEntity.ok(tarifsClient);
    }
}
