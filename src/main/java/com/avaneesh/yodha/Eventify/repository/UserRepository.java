package com.avaneesh.yodha.Eventify.repository;

import com.avaneesh.yodha.Eventify.entities.Users;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> getUsersByEmail(@NotBlank(message = "Email cannot be blank.") @Email(message = "Invalid email format.") String email);

    Optional<Object> findByEmail(String email);
}