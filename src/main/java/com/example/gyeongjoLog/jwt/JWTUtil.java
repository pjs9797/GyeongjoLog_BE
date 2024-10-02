package com.example.gyeongjoLog.jwt;

import com.example.gyeongjoLog.user.repository.RefreshTokenRepository;
import com.example.gyeongjoLog.user.service.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Slf4j
@Component
public class JWTUtil {

    @Value("${spring.jwt.token.access-expiration-time}")
    private long accessExpirationTime;

    @Value("${spring.jwt.token.refresh.expiration.time}")
    private long refreshExpirationTime;


    private final RefreshTokenRepository refreshTokenRepository;
    @Lazy
    private final CustomUserDetailsService customUserDetailsService;
    private final SecretKey secretKey;
    private final RedisTemplate<String, String> redisTemplate;

    public JWTUtil(@Value("${spring.jwt.secret}") String secret, RedisTemplate<String, String> redisTemplate, RefreshTokenRepository refreshTokenRepository, CustomUserDetailsService customUserDetailsService) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.redisTemplate = redisTemplate;
        this.refreshTokenRepository = refreshTokenRepository;
        this.customUserDetailsService = customUserDetailsService;
    }

    public String getEmail(String token) {
        Claims claims = parseClaims(token);
        return claims.get("email", String.class);
    }

    public String getRole(String token) {
        Claims claims = parseClaims(token);
        return claims.get("role", String.class);
    }

    public Boolean isExpired(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            log.error("Error checking JWT expiration", e);
            return true;
        }
    }

    public String createAccessToken(String email, String role) {
        return Jwts.builder()
                .claim("email", email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpirationTime))
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken(String email, String role) {
        return Jwts.builder()
                .claim("email", email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationTime))
                .signWith(secretKey)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Authentication getAuthentication(String email) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public boolean isExistRefreshTokenInRedis(String token){
        return refreshTokenRepository.existByRefreshToken(token);
    }

    public void invalidateUserSession(String email) {
        redisTemplate.delete(email);
    }

    public void saveRefreshTokenAtRedis(String email, String token, Long expiredMs) {
        String key = "user:" + email + ":refresh_token";
        redisTemplate.opsForValue().set(key, token, expiredMs, TimeUnit.MILLISECONDS);
    }

    public String getRefreshTokenFromRedis(String email) {
        String key = "user:" + email + ":refresh_token";
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteRefreshTokenFromRedis(String email) {
        String key = "user:" + email + ":refresh_token";
        redisTemplate.delete(key);
    }

    public String getAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public String getRefreshToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization-Refresh");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
