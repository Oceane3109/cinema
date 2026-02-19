package com.cinema.repository;

import com.cinema.model.ReservationPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepotReservationPlace extends JpaRepository<ReservationPlace, Long> {

    List<ReservationPlace> findByReservationId(Long reservationId);

    @Query("SELECT rp.place.id FROM ReservationPlace rp WHERE rp.reservation.seance.id = :seanceId")
    List<Long> findPlaceIdsBySeanceId(@Param("seanceId") Long seanceId);

    @Query("SELECT rp FROM ReservationPlace rp JOIN FETCH rp.place WHERE rp.reservation.id = :reservationId")
    List<ReservationPlace> findByReservationIdWithPlaces(@Param("reservationId") Long reservationId);

    void deleteByReservationId(Long reservationId);
}