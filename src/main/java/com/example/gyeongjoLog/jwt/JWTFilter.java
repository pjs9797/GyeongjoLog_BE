package com.example.gyeongjoLog.jwt;

import com.example.gyeongjoLog.user.dto.CustomUserDetails;
import com.example.gyeongjoLog.user.entity.UserEntity;
import com.example.gyeongjoLog.user.repository.RefreshTokenRepository;
import com.example.gyeongjoLog.user.service.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
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
        String authorization = request.getHeader("Authorization");
        String refreshAuthorization = request.getHeader("Authorization-Refresh");

        if (authorization == null || !authorization.startsWith("Bearer ") || refreshAuthorization == null || !refreshAuthorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = authorization.split(" ")[1];
        String refreshToken = refreshAuthorization.split(" ")[1];

        // Access Token이 만료되었는지 확인
        if (jwtUtil.isExpired(accessToken)) {
            if (refreshToken != null) {
                boolean isRefreshTokenValid = !jwtUtil.isExpired(refreshToken);
                boolean isExistRefreshTokenInRedis = jwtUtil.isExistRefreshTokenInRedis(refreshToken);

                if (isRefreshTokenValid && isExistRefreshTokenInRedis) {
                    String email = jwtUtil.parseClaims(refreshToken).getSubject();
                    Authentication authentication = jwtUtil.getAuthentication(email);
                    // Refresh Token이 유효하다면 새로운 Access Token을 발급
                    String role = jwtUtil.getRole(refreshToken);
                    String newAccessToken = jwtUtil.createAccessToken(email, role);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    response.addHeader("Authorization", "Bearer " + newAccessToken);

                    // 필터 체인을 종료하고 응답을 반환
                    filterChain.doFilter(request, response);
                    return;
                } else {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;  // Refresh Token이 유효하지 않다면 필터 체인을 중단
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;  // Refresh Token이 없으면 필터 체인을 중단
            }
        }

        // Redis에서 로그아웃 여부 확인
        String isLogout = redisTemplate.opsForValue().get(accessToken);
        if (isLogout != null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String email = jwtUtil.getEmail(accessToken);
        String role = jwtUtil.getRole(accessToken);

        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(email);
        userEntity.setPassword("temppassword"); // 임시 비밀번호, 사용되지 않음
        userEntity.setRole(role);

        CustomUserDetails customUserDetails = new CustomUserDetails(userEntity);
        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authToken);
        filterChain.doFilter(request, response);
    }
}
