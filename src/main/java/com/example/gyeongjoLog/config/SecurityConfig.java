package com.example.gyeongjoLog.config;

import com.example.gyeongjoLog.jwt.JWTFilter;
import com.example.gyeongjoLog.jwt.JWTUtil;
import com.example.gyeongjoLog.user.login.LoginFilter;
import com.example.gyeongjoLog.user.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${spring.jwt.token.refresh.expiration.time}")
    private long refreshExpirationTime;

    private final AuthenticationConfiguration authenticationConfiguration;

    private final JWTUtil jwtUtil;

    private final RedisTemplate<String, String> redisTemplate;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // CSRF 비활성화
        http.csrf(AbstractHttpConfigurer::disable);

        // Form 로그인 방식 비활성화
        http.formLogin(AbstractHttpConfigurer::disable);

        // HTTP Basic 인증 방식 비활성화
        http.httpBasic(AbstractHttpConfigurer::disable);

        // URL 별 권한 설정
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/user/**").permitAll()
                .anyRequest().authenticated());

        // JWT 필터 설정
        http.addFilterBefore(new JWTFilter(jwtUtil, redisTemplate), UsernamePasswordAuthenticationFilter.class);

        // 로그인 필터 설정
        http.addFilterBefore(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, refreshExpirationTime), UsernamePasswordAuthenticationFilter.class);

        // 세션 설정 (Stateless로 설정하여 세션을 사용하지 않음)
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
