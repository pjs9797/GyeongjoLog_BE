package com.example.gyeongjoLog.user.login;

import com.example.gyeongjoLog.common.APIResponse;
import com.example.gyeongjoLog.jwt.JWTUtil;
import com.example.gyeongjoLog.user.dto.CustomUserDetails;
import com.example.gyeongjoLog.user.repository.RefreshTokenRepository;
import com.example.gyeongjoLog.user.service.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        CustomUserDetails customUserDetails = (CustomUserDetails) authResult.getPrincipal();
        String email = customUserDetails.getUsername();
        String role = customUserDetails.getAuthorities().iterator().next().getAuthority();

        String accessToken = jwtUtil.createAccessToken(email, role);
        String refreshToken = jwtUtil.createRefreshToken(email, role);

        jwtUtil.saveRefreshTokenAtRedis(email, refreshToken, refreshExpirationTime);

        response.addHeader("Authorization", "Bearer " + accessToken);
        response.addHeader("Authorization-Refresh", "Bearer " + refreshToken);

        APIResponse apiResponse = APIResponse.builder()
                .resultCode("200")
                .resultMessage("로그인 성공")
                .build();

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(new ObjectMapper().writeValueAsString(apiResponse));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);

        APIResponse apiResponse = APIResponse.builder()
                .resultCode("403")
                .resultMessage("로그인 정보를 찾을 수 없습니다")
                .build();

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(new ObjectMapper().writeValueAsString(apiResponse));
    }
}
