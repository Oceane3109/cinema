package com.cinema.repository;

import com.cinema.model.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepotPlace extends JpaRepository<Place, Long> {

    List<Place> findBySalleId(Long salleId);

    @Query("SELECT p FROM Place p WHERE p.salle.id = :salleId ORDER BY p.numeroRangee, p.numeroPlace")
    List<Place> findBySalleIdOrderByRangeeAndPlace(@Param("salleId") Long salleId);

    @Query("SELECT p FROM Place p WHERE p.id IN :placeIds")
    List<Place> findByIds(@Param("placeIds") List<Long> placeIds);

    @Query("SELECT p FROM Place p WHERE p.salle.id = :salleId AND p.numeroRangee = :rangee AND p.numeroPlace = :place")
    Place findBySalleIdAndRangeeAndPlace(@Param("salleId") Long salleId, @Param("rangee") Integer rangee, @Param("place") Integer place);

    void deleteBySalleId(Long salleId);
}