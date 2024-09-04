package com.example.gyeongjoLog.user.service;

import com.example.gyeongjoLog.common.APIResponse;
import com.example.gyeongjoLog.jwt.JWTUtil;
import com.example.gyeongjoLog.user.dto.UserDTO;
import com.example.gyeongjoLog.user.entity.UserEntity;
import com.example.gyeongjoLog.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JWTUtil jwtUtil;

    public APIResponse join(UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            return APIResponse.builder().resultCode("201").resultMessage("가입된 이메일").build();
        }
        log.info("UserDTO ::: {}",userDTO);
        userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        UserEntity userEntity = UserEntity.builder()
                .email(userDTO.getEmail())
                .password(userDTO.getPassword())
                .build();

        userRepository.save(userEntity);
        return APIResponse.builder().resultCode("200").resultMessage("회원가입 성공").build();
    }

    public APIResponse login(UserDTO userDTO) {
        if (!userRepository.existsByEmail(userDTO.getEmail())) {
            return APIResponse.builder().resultCode("202").resultMessage("가입되지 않은 이메일").build();
        }
        // 이메일로 사용자 정보 가져오기
        UserEntity userEntity = userRepository.findByEmail(userDTO.getEmail());

        // 비밀번호 비교를 위한 BCryptPasswordEncoder 인스턴스 생성
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        // 비밀번호가 일치하지 않는 경우
        if (!passwordEncoder.matches(userDTO.getPassword(), userEntity.getPassword())) {
            return APIResponse.builder().resultCode("203").resultMessage("비밀번호가 일치하지 않습니다.").build();
        }

        return APIResponse.builder().resultCode("200").resultMessage("로그인 성공").build();
    }

    public APIResponse checkEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            return APIResponse.builder().resultCode("201").resultMessage("가입된 이메일").build();
        }
        return APIResponse.builder().resultCode("200").resultMessage("이메일 중복 체크 성공").build();
    }
}
