package com.example.gyeongjoLog.user.service;

import com.example.gyeongjoLog.common.APIResponse;
import com.example.gyeongjoLog.jwt.JWTUtil;
import com.example.gyeongjoLog.user.dto.UserDTO;
import com.example.gyeongjoLog.user.entity.UserEntity;
import com.example.gyeongjoLog.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public APIResponse join(UserDTO user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            return APIResponse.createWithoutData("201", "존재하는 이메일");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        UserEntity userEntity = UserEntity.builder()
                .email(user.getEmail())
                .password(user.getPassword())
                .build();

        userRepository.save(userEntity);
        return APIResponse.createWithoutData("200", "회원가입 성공");
    }

    public APIResponse checkEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            return APIResponse.createWithoutData("201", "존재하는 이메일");
        }
        return APIResponse.createWithoutData("200", "중복 체크 성공");
    }

}
