package com.anoop.videoStream.service.authService;


import org.springframework.stereotype.Service;

import com.anoop.videoStream.Model.User;
import com.anoop.videoStream.dto.CachedUser;
import com.anoop.videoStream.memory.UserAuthCache;
import com.anoop.videoStream.repository.UserRepository;

@Service
public class UserAuthenticationService {

    private static final long CACHE_TTL =
            5 * 60 * 1000; // 5 minutes

    private final UserRepository userRepository;
    private final UserAuthCache userAuthCache;

    public UserAuthenticationService(
            UserRepository userRepository,
            UserAuthCache userAuthCache) {

        this.userRepository = userRepository;
        this.userAuthCache = userAuthCache;
    }

    public CachedUser getUser(Long userId) {

        /*
         * 1. Check memory
         */
        CachedUser cached =
                userAuthCache.get(userId);

        if (cached != null) {
            return cached;
        }

        /*
         * 2. Cache miss → database
         */
        User user =
                userRepository
                        .findById(userId)
                        .orElse(null);

        if (user == null) {
            return null;
        }

        /*
         * 3. Store minimal information
         */
        userAuthCache.put(
                user.getId(),
                user.getRole(),
                CACHE_TTL
        );

        return userAuthCache.get(userId);
    }

    public void invalidate(Long userId) {
        userAuthCache.remove(userId);
    }

    public void clearCache() {
        userAuthCache.clear();
    }
}