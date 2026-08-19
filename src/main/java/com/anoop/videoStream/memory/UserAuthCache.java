package com.anoop.videoStream.memory;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.anoop.videoStream.dto.CachedUser;

@Component
public class UserAuthCache {

    private final ConcurrentHashMap<Long, CachedUser> cache = new ConcurrentHashMap<>();

    public CachedUser get(Long userId) {

        CachedUser cached = cache.get(userId);

        if (cached == null) {
            return null;
        }

        if (cached.expiresAt() <= System.currentTimeMillis()) {

            cache.remove(userId, cached);

            return null;
        }

        return cached;
    }

    public void put(Long userId, String role, long ttlMillis) {
        cache.put(userId, new CachedUser(userId, role, System.currentTimeMillis() + ttlMillis));
    }

    public void remove(Long userId) {
        cache.remove(userId);
    }

    public void clear() {
        cache.clear();
    }

    public int size() {
        return cache.size();
    }
}