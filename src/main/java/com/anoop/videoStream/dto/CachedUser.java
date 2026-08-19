package com.anoop.videoStream.dto;


public record CachedUser(
        Long userId,
        String role,
        long expiresAt
) {
}