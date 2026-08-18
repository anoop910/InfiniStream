package com.anoop.videoStream.service.authService;

import java.time.Duration;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import com.anoop.videoStream.Model.User;
import com.anoop.videoStream.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuthenticationService {

        @Value("${google.client-id}")
        private String googleClientId;

        private final UserRepository userRepository;
        private final JwtService jwtService;
        private final RefreshTokenService refreshTokenService;

        public AuthenticationService(
                        UserRepository userRepository,
                        JwtService jwtService,
                        RefreshTokenService refreshTokenService) {

                this.userRepository = userRepository;
                this.jwtService = jwtService;
                this.refreshTokenService = refreshTokenService;
        }

        public void loginWithGoogle(
                        String credential,
                        HttpServletResponse response)
                        throws Exception {

                /*
                 * STEP 1
                 * Verify Google ID token
                 */

                GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                                new NetHttpTransport(),
                                GsonFactory.getDefaultInstance())
                                .setAudience(
                                                Collections.singletonList(
                                                                googleClientId))
                                .build();

                GoogleIdToken idToken = verifier.verify(credential);

                if (idToken == null) {

                        throw new RuntimeException(
                                        "Invalid Google ID token");
                }

                /*
                 * STEP 2
                 * Get verified Google information
                 */

                GoogleIdToken.Payload payload = idToken.getPayload();

                String googleId = payload.getSubject();

                String email = payload.getEmail();

                String name = (String) payload.get("name");

                String picture = (String) payload.get("picture");

                /*
                 * STEP 3
                 * Find existing user
                 */

                User user = userRepository
                                .findByGoogleId(googleId)
                                .orElse(null);

                /*
                 * STEP 4
                 * If Google user doesn't exist,
                 * check email.
                 */

                if (user == null) {

                        user = userRepository
                                        .findByEmail(email)
                                        .orElse(null);
                }

                /*
                 * STEP 5
                 * Create user if first login
                 */

                if (user == null) {

                        user = User.builder()
                                        .googleId(googleId)
                                        .email(email)
                                        .name(name)
                                        .pictureUrl(picture)
                                        .role("USER")
                                        .build();

                        user = userRepository.save(user);

                } else {

                        /*
                         * Update Google information
                         */

                        user.setGoogleId(googleId);
                        user.setName(name);
                        user.setPictureUrl(picture);

                        user = userRepository.save(user);
                }

                /*
                 * STEP 6
                 * Generate YOUR application's JWT
                 */

                String jwt = jwtService.generateToken(user);
                String refreshToken = refreshTokenService.createSession(user);

                /*
                 * STEP 7
                 * Put JWT inside HttpOnly cookie
                 */
                ResponseCookie accessCookie = ResponseCookie
                                .from("access_token", jwt)
                                .httpOnly(true)
                                .secure(true) // localhost
                                .sameSite("Lax")
                                .path("/")
                                .maxAge(Duration.ofMinutes(15))
                                .build();

                ResponseCookie refreshCookie = ResponseCookie
                                .from("refresh_token", refreshToken)
                                .httpOnly(true)
                                .secure(true) // localhost
                                .sameSite("Lax")
                                .path("/")
                                .maxAge(Duration.ofDays(7))
                                .build();

                response.addHeader(
                                HttpHeaders.SET_COOKIE,
                                accessCookie.toString());

                response.addHeader(
                                HttpHeaders.SET_COOKIE,
                                refreshCookie.toString());
        }
}