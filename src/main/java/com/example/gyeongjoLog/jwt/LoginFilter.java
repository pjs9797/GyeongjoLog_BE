package com.example.gyeongjoLog.jwt;

import com.example.gyeongjoLog.common.APIResponse;
import com.example.gyeongjoLog.user.dto.CustomUserDetails;
import com.example.gyeongjoLog.user.repository.RefreshTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    private final long refreshExpirationTime;
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil, long refreshExpirationTime) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshExpirationTime = refreshExpirationTime;

        setFilterProcessesUrl("/user/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            // JSON 형식으로 전송된 요청 본문에서 이메일과 비밀번호를 읽어옴
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> credentials = objectMapper.readValue(request.getInputStream(), Map.class);

            String email = credentials.get("email");  // JSON의 "email" 필드에서 값을 가져옴
            String password = credentials.get("password");  // JSON의 "password" 필드에서 값을 가져옴

            log.info("LOGIN FILTER ::: attemptAuthentication with email: {}", email);

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password, null);

            return authenticationManager.authenticate(authToken);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse authentication request body", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException {
        // 인증 성공 후 JWT 토큰 생성
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        String email = customUserDetails.getUsername();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String role = authorities.iterator().next().getAuthority();

        String accessToken = jwtUtil.createAccessToken(email, role);
        String refreshToken = jwtUtil.createRefreshToken(email, role);
        log.info("리프레시 토큰",refreshExpirationTime);
        // Refresh Token을 Redis에 저장
        jwtUtil.saveRefreshTokenAtRedis(email, refreshToken, refreshExpirationTime);

        // Access Token과 Refresh Token을 헤더에 추가
        response.addHeader("Authorization", "Bearer " + accessToken);
        response.addHeader("Authorization-Refresh", "Bearer " + refreshToken);

        APIResponse apiResponse = APIResponse.builder().resultCode("200").resultMessage("로그인 성공").build();

        // 응답을 JSON 형태로 변환하여 바디에 추가
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(new ObjectMapper().writeValueAsString(apiResponse));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
