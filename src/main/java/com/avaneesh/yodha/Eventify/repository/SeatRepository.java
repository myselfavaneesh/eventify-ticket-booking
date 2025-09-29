package com.avaneesh.yodha.Eventify.repository;

import com.avaneesh.yodha.Eventify.entities.Events;
import com.avaneesh.yodha.Eventify.entities.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatRepository extends JpaRepository<Seat,Long> {
    @Modifying
    @Query("DELETE FROM Seat s WHERE s.event = :event")
    void deleteByEvent(Events event);
}
