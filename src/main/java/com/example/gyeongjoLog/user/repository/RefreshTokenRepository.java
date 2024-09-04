package com.example.gyeongjoLog.user.repository;

import com.example.gyeongjoLog.user.entity.RefreshTokenEntity;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshTokenEntity,String> {
    boolean existByRefreshToken(String token);
}
