package com.cbcportal.repositories;

import com.cbcportal.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Custom method to find a user by their email address during login
    Optional<User> findByEmail(String email);
}