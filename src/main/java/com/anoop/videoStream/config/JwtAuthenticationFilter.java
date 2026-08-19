package com.anoop.videoStream.config;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.anoop.videoStream.dto.CachedUser;
import com.anoop.videoStream.service.authService.JwtService;
import com.anoop.videoStream.service.authService.UserAuthenticationService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserAuthenticationService userAuthenticationService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserAuthenticationService userAuthenticationService) {

        this.jwtService = jwtService;
        this.userAuthenticationService =
                userAuthenticationService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("JWT AUTH: "+ request.getRequestURI());
        String token = getAccessToken(request);


        if (token != null) {

            try {

                /*
                 * Validate JWT
                 */
                Long userId =
                        jwtService.getUserId(token);

                /*
                 * Get user from memory cache.
                 *
                 * DB is accessed only when
                 * cache doesn't contain user.
                 */
                CachedUser user =
                        userAuthenticationService
                                .getUser(userId);

                if (user != null) {

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user.userId(),
                                    null,
                                    List.of(
                                            new SimpleGrantedAuthority(
                                                    "ROLE_" +
                                                    user.role()
                                            )
                                    )
                            );

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(
                                    authentication
                            );
                }

            } catch (Exception e) {

                SecurityContextHolder
                        .clearContext();
            }
        }

        filterChain.doFilter(
                request,
                response
        );
    }

    private String getAccessToken(
            HttpServletRequest request) {

        Cookie[] cookies =
                request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {

            if ("access_token"
                    .equals(cookie.getName())) {

                return cookie.getValue();
            }
        }

        return null;
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }
}