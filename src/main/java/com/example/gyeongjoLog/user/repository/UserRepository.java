package com.example.gyeongjoLog.user.repository;

import com.example.gyeongjoLog.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity,Integer> {
    Boolean existsByEmail(String email);

    UserEntity findByEmail(String email);
}
