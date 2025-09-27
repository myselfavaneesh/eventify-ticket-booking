package com.avaneesh.yodha.Eventify.security;

import com.avaneesh.yodha.Eventify.utils.JWTUtility;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtility jwtUtil;
    private final UserDetailServiceImp userDetailsService;

    @Autowired
    public JWTFilter(JWTUtility jwtUtil, UserDetailServiceImp userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        extractTokenFromRequest(request)
                .filter(jwtUtil::validateToken)
                .ifPresent(token -> {
                    String username = jwtUtil.extractUsername(token);
                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        setupSpringAuthentication(token, userDetails, request);
                    }
                });

        chain.doFilter(request, response);
    }

    private void setupSpringAuthentication(String token, UserDetails userDetails, HttpServletRequest request) {
        List<String> roles = jwtUtil.extractRoles(token);
        List<SimpleGrantedAuthority> authorities;

        if (roles != null && !roles.isEmpty()) {
            authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        } else {
            // Fallback to UserDetails authorities if token has no roles
            authorities = userDetails.getAuthorities().stream()
                    .map(auth -> new SimpleGrantedAuthority(auth.getAuthority()))
                    .collect(Collectors.toList());
        }

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private Optional<String> extractTokenFromRequest(HttpServletRequest request) {
        // Prefer the "Authorization" header for the token
        Optional<String> tokenFromHeader = extractTokenFromHeader(request);
        if (tokenFromHeader.isPresent()) {
            return tokenFromHeader;
        }

        // Fallback to checking the cookie
        return extractTokenFromCookie(request);
    }

    private Optional<String> extractTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return Optional.of(bearerToken.substring(7));
        }
        return Optional.empty();
    }

    private Optional<String> extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> "JWT-TOKEN".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}
