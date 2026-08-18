package com.anoop.videoStream.service.authService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import org.springframework.stereotype.Service;

import com.anoop.videoStream.Model.RefreshSession;
import com.anoop.videoStream.Model.User;
import com.anoop.videoStream.repository.RefreshSessionRepository;

@Service
public class RefreshTokenService {

    private final RefreshSessionRepository repository;

    private final SecureRandom secureRandom =
            new SecureRandom();

    private static final Duration INACTIVITY_LIMIT =
            Duration.ofDays(7);

    private static final Duration ABSOLUTE_LIMIT =
            Duration.ofDays(30);

    public RefreshTokenService(
            RefreshSessionRepository repository) {

        this.repository = repository;
    }

    public String createSession(User user) {

        byte[] randomBytes = new byte[64];

        secureRandom.nextBytes(randomBytes);

        String rawToken =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(randomBytes);

        Instant now = Instant.now();

        RefreshSession session =
                new RefreshSession();

        session.setUser(user);
        session.setTokenHash(hash(rawToken));
        session.setCreatedAt(now);
        session.setLastUsedAt(now);
        session.setExpiresAt(
                now.plus(ABSOLUTE_LIMIT)
        );

        repository.save(session);

        return rawToken;
    }

    public User validateAndUse(String rawToken) {

        String hash = hash(rawToken);

        RefreshSession session =
                repository
                        .findByTokenHashAndRevokedFalse(hash)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Invalid refresh token"
                                )
                        );

        Instant now = Instant.now();

        // Absolute expiry
        if (session.getExpiresAt().isBefore(now)) {

            session.setRevoked(true);
            repository.save(session);

            throw new RuntimeException(
                    "Refresh session expired"
            );
        }

        // Inactivity expiry
        if (
            session.getLastUsedAt()
                    .plus(INACTIVITY_LIMIT)
                    .isBefore(now)
        ) {

            session.setRevoked(true);
            repository.save(session);

            throw new RuntimeException(
                    "Refresh session inactive"
            );
        }

        // User visited again
        session.setLastUsedAt(now);

        repository.save(session);

        return session.getUser();
    }

    public void revoke(String rawToken) {

        String hash = hash(rawToken);

        repository
                .findByTokenHashAndRevokedFalse(hash)
                .ifPresent(session -> {

                    session.setRevoked(true);

                    repository.save(session);
                });
    }

    private String hash(String token) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            token.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return Base64.getEncoder()
                    .encodeToString(hash);

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }
}