package com.anoop.videoStream.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.anoop.videoStream.Model.RefreshSession;

public interface RefreshSessionRepository
        extends JpaRepository<RefreshSession, Long> {

    Optional<RefreshSession>
    findByTokenHashAndRevokedFalse(String tokenHash);
}