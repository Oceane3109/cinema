package com.cinema.repository;

import com.cinema.model.ParametreReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepotParametreReservation extends JpaRepository<ParametreReservation, Long> {
    
    Optional<ParametreReservation> findByNomParametre(String nomParametre);
}
