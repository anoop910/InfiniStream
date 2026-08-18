package com.anoop.videoStream.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.anoop.videoStream.Model.User;

public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByGoogleId(String googleId);

    Optional<User> findByEmail(String email);
}