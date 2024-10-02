package com.example.gyeongjoLog.jwt;

import com.example.gyeongjoLog.user.dto.CustomUserDetails;
import com.example.gyeongjoLog.user.entity.UserEntity;
import com.example.gyeongjoLog.user.repository.RefreshTokenRepository;
import com.example.gyeongjoLog.user.service.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;


    public JWTFilter(JWTUtil jwtUtil, RedisTemplate<String, String> redisTemplate) {
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = jwtUtil.getAccessToken(request);
        String refreshToken = jwtUtil.getRefreshToken(request);

        if (accessToken != null) {
            if (jwtUtil.isExpired(accessToken)) {
                if (refreshToken != null) {
                    try {
                        String email = jwtUtil.getEmail(refreshToken);
                        String storedRefreshToken = jwtUtil.getRefreshTokenFromRedis(email);

                        if (refreshToken.equals(storedRefreshToken)) {
                            String newAccessToken = jwtUtil.createAccessToken(email, jwtUtil.getRole(refreshToken));
                            response.setHeader("Authorization", "Bearer " + newAccessToken);

                            Authentication authentication = jwtUtil.getAuthentication(email);
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        } else {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid refresh token");
                            return;
                        }
                    } catch (ExpiredJwtException e) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Refresh token expired");
                        return;
                    }
                } else {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access token expired");
                    return;
                }
            } else {
                Authentication authentication = jwtUtil.getAuthentication(jwtUtil.getEmail(accessToken));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
