package com.cinema.repository;

import com.cinema.model.Film;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepotFilm extends JpaRepository<Film, Long> {
}




