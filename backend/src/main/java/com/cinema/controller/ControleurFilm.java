package com.cinema.controller;

import com.cinema.model.Film;
import com.cinema.repository.DepotFilm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/films")
@CrossOrigin(origins = "*")
public class ControleurFilm {

    @Autowired
    private DepotFilm depotFilm;

    @GetMapping
    public List<Film> getTousLesFilms() {
        return depotFilm.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Film> getFilmParId(@PathVariable Long id) {
        Optional<Film> film = depotFilm.findById(id);
        if (film.isPresent()) {
            return ResponseEntity.ok(film.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public Film creerFilm(@RequestBody Film film) {
        film.setCreatedAt(LocalDateTime.now());
        film.setUpdatedAt(LocalDateTime.now());
        return depotFilm.save(film);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Film> modifierFilm(@PathVariable Long id, @RequestBody Film detailsFilm) {
        Optional<Film> filmOptional = depotFilm.findById(id);
        if (filmOptional.isPresent()) {
            Film film = filmOptional.get();
            film.setTitre(detailsFilm.getTitre());
            film.setDescription(detailsFilm.getDescription());
            film.setDureeMinutes(detailsFilm.getDureeMinutes());
            film.setDateSortie(detailsFilm.getDateSortie());
            film.setGenre(detailsFilm.getGenre());
            film.setRealisateur(detailsFilm.getRealisateur());
            film.setActeurs(detailsFilm.getActeurs());
            film.setNote(detailsFilm.getNote());
            film.setLangue(detailsFilm.getLangue());
            film.setPays(detailsFilm.getPays());
            film.setImageUrl(detailsFilm.getImageUrl());
            film.setUpdatedAt(LocalDateTime.now());
            return ResponseEntity.ok(depotFilm.save(film));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerFilm(@PathVariable Long id) {
        if (depotFilm.existsById(id)) {
            depotFilm.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}



