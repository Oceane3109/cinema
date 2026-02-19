package com.cinema.repository;

import com.cinema.model.ClientTarif;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepotClientTarif extends JpaRepository<ClientTarif, Long> {
    List<ClientTarif> findByReservationId(Long reservationId);
    List<ClientTarif> findByTarifId(Long tarifId);
    List<ClientTarif> findByGenreApplique(String genreApplique);
    void deleteByReservationId(Long reservationId);
}
