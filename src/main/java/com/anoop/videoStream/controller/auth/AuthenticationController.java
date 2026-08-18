package com.anoop.videoStream.controller.auth;

import java.time.Duration;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.anoop.videoStream.Model.User;
import com.anoop.videoStream.dto.GoogleLoginRequest;
import com.anoop.videoStream.service.authService.AuthenticationService;
import com.anoop.videoStream.service.authService.JwtService;
import com.anoop.videoStream.service.authService.RefreshTokenService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins ="*")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    public AuthenticationController(
            AuthenticationService authenticationService,
            RefreshTokenService refreshTokenService,
            JwtService jwtService) {

        this.authenticationService = authenticationService;
        this.refreshTokenService = refreshTokenService;
        this.jwtService = jwtService;
    }

    // =========================================================
    // GOOGLE LOGIN
    // =========================================================

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(
            @RequestBody GoogleLoginRequest request,
            HttpServletResponse response)
            throws Exception {

        authenticationService.loginWithGoogle(
                request.getCredential(),
                response
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Login successful"
                )
        );
    }


    // =========================================================
    // CURRENT USER
    // =========================================================

    @GetMapping("/me")
    public ResponseEntity<?> me(
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        return ResponseEntity.ok(
                Map.of(
                        "username",
                        authentication.getName()
                )
        );
    }


    // =========================================================
    // REFRESH ACCESS TOKEN
    // =========================================================

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(
                    name = "refresh_token",
                    required = false
            )
            String refreshToken) {

        /*
         * No refresh token
         */
        if (refreshToken == null ||
                refreshToken.isBlank()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        try {

            /*
             * Validate refresh token
             */
            User user =
                    refreshTokenService
                            .validateAndUse(
                                    refreshToken
                            );

            /*
             * Generate NEW access token
             */
            String accessToken =
                    jwtService.generateToken(user);

            /*
             * Store access token
             * inside HttpOnly cookie.
             *
             * JavaScript cannot read this cookie.
             */
            ResponseCookie accessCookie =
                    ResponseCookie
                            .from(
                                    "access_token",
                                    accessToken
                            )
                            .httpOnly(true)

                            // localhost testing
                            .secure(false)

                            .sameSite("Lax")

                            .path("/")

                            // 15 minutes
                            .maxAge(
                                    Duration.ofMinutes(15)
                            )

                            .build();

            return ResponseEntity
                    .ok()
                    .header(
                            HttpHeaders.SET_COOKIE,
                            accessCookie.toString()
                    )
                    .body(
                            Map.of(
                                    "message",
                                    "Token refreshed"
                            )
                    );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }
    }


    // =========================================================
    // LOGOUT
    // =========================================================

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(name = "refresh_token",required = false) String refreshToken,
                                                         HttpServletResponse response) {

        /*
         * Delete access token cookie
         */

        refreshTokenService.revoke(refreshToken);
        ResponseCookie accessCookie =
                ResponseCookie
                        .from(
                                "access_token",
                                ""
                        )
                        .httpOnly(true)
                        .secure(false)
                        .sameSite("Lax")
                        .path("/")
                        .maxAge(0)
                        .build();

        /*
         * Delete refresh token cookie
         */
        ResponseCookie refreshCookie =
                ResponseCookie
                        .from(
                                "refresh_token",
                                ""
                        )
                        .httpOnly(true)
                        .secure(false)
                        .sameSite("Lax")
                        .path("/")
                        .maxAge(0)
                        .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                accessCookie.toString()
        );

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                refreshCookie.toString()
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Logout successful"
                )
        );
    }
}