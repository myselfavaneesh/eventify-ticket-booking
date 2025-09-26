package com.avaneesh.yodha.Eventify.repository;

import com.avaneesh.yodha.Eventify.entities.Booking;
import com.avaneesh.yodha.Eventify.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByUser(Users user);
}
