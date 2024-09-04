package com.example.gyeongjoLog.jwt;

import com.example.gyeongjoLog.user.repository.RefreshTokenRepository;
import com.example.gyeongjoLog.user.service.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private final CustomUserDetailsService customUserDetailsService;
    private final SecretKey secretKey;
    private final RedisTemplate<String, String> redisTemplate;

    public JWTUtil(@Value("${spring.jwt.secret}") String secret, RedisTemplate<String, String> redisTemplate, RefreshTokenRepository refreshTokenRepository, CustomUserDetailsService customUserDetailsService) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes()); // SecretKey 생성
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
        Claims claims = parseClaims(token);
        return claims.getExpiration().before(new Date());
    }

    public Boolean isRefreshTokenExpired(String token) {
        String email = getEmail(token);
        Long expireTime = redisTemplate.getExpire(email, TimeUnit.MILLISECONDS);
        return expireTime != null && expireTime <= 0;
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

//    public String createAccessToken(String email, String role) {
//        ZonedDateTime nowInKST = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
//        Date expirationDate = Date.from(nowInKST.plusSeconds(accessExpirationTime).toInstant());
//
//        return Jwts.builder()
//                .claim("email", email)
//                .claim("role", role)
//                .setIssuedAt(Date.from(nowInKST.toInstant()))
//                .setExpiration(expirationDate)
//                .signWith(secretKey)
//                .compact();
//    }
//
//    public String createRefreshToken(String email, String role) {
//        ZonedDateTime nowInKST = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
//        Date expirationDate = Date.from(nowInKST.plusSeconds(refreshExpirationTime).toInstant());
//
//        return Jwts.builder()
//                .claim("email", email)
//                .claim("role", role)
//                .setIssuedAt(Date.from(nowInKST.toInstant()))
//                .setExpiration(expirationDate)
//                .signWith(secretKey)
//                .compact();
//    }

    public void saveRefreshTokenAtRedis(String email, String token, Long expiredMs) {

        log.info("Saving refresh token with expiration time: {}", expiredMs);

        if (expiredMs == null || expiredMs <= 0) {
            throw new IllegalArgumentException("Expiration time must be greater than 0");
        }
        redisTemplate.opsForValue().set(email, token, expiredMs,TimeUnit.MILLISECONDS);
    }

    public void saveLogoutToken(String token, Long expiredMs) {
        redisTemplate.opsForValue().set(token, "logout", expiredMs, TimeUnit.MILLISECONDS);
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

}
