package com.anoop.videoStream.config;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.anoop.videoStream.Model.User;
import com.anoop.videoStream.repository.UserRepository;
import com.anoop.videoStream.service.authService.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository) {

        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println(
                "JWT FILTER -> "
                + request.getMethod()
                + " "
                + request.getRequestURI()
        );

        String token = getAccessToken(request);

        if (token != null) {

            try {

                /*
                 * Get user ID from JWT
                 */
                Long userId =
                        jwtService.getUserId(token);

                /*
                 * Find user in database
                 */
                User user =
                        userRepository
                                .findById(userId)
                                .orElse(null);

                if (user != null) {

                    /*
                     * Create Authentication
                     */
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userId,
                                    null,
                                    List.of(
                                            new SimpleGrantedAuthority(
                                                    "ROLE_" + user.getRole()
                                            )
                                    )
                            );

                    /*
                     * Put Authentication
                     * into SecurityContext
                     */
                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authentication);

                    System.out.println(
                            "JWT AUTHENTICATED USER ID : "
                            + userId
                    );
                }

            } catch (Exception e) {

                /*
                 * Token invalid or expired.
                 *
                 * Do NOT throw exception here.
                 * Let Spring Security return 401/403.
                 */
                System.out.println(
                        "JWT INVALID OR EXPIRED"
                );

                SecurityContextHolder
                        .clearContext();
            }
        }

        /*
         * Continue request
         */
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