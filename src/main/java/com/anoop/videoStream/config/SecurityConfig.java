package com.anoop.videoStream.config;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http

            // =========================
            // CSRF
            // =========================

            // We are using JWT + cookies
            .csrf(csrf -> csrf.disable())


            // =========================
            // CORS
            // =========================

            // Use CorsConfig.java
            .cors(cors -> {})


            // =========================
            // AUTHORIZATION
            // =========================

            .authorizeHttpRequests(auth -> auth

                // -------------------------
                // PUBLIC ENDPOINTS
                // -------------------------

                .requestMatchers(
                    "/auth/google",
                    "/auth/refresh",
                    "/auth/logout"
                ).permitAll()


                // -------------------------
                // AUTHENTICATED ENDPOINTS
                // -------------------------

                .requestMatchers(
                    "/auth/me",
                    "/uploadvideo",
                    "/uploadtrail"
                ).authenticated()


                // -------------------------
                // EVERYTHING ELSE
                // -------------------------

                .anyRequest()
                .authenticated()
            )


            // =========================
            // ACCESS DENIED HANDLER
            // =========================

            .exceptionHandling(exception ->

                exception.accessDeniedHandler(
                    (request, response, accessDeniedException) -> {

                        response.setStatus(
                            HttpServletResponse.SC_FORBIDDEN
                        );

                        response.setContentType(
                            "application/json"
                        );

                        response.getWriter().write("""
                            {
                                "error": "SESSION_EXPIRED",
                                "message": "Your session has expired. Please refresh your session."
                            }
                            """);
                    }
                )
            )


            // =========================
            // JWT FILTER
            // =========================

            // JWT filter must run before
            // Spring's username/password filter
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );


        return http.build();
    }
}