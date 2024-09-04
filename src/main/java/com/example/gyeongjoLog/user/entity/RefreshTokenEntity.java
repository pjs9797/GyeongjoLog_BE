package com.example.gyeongjoLog.user.entity;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@AllArgsConstructor
@Getter
@RedisHash(value = "jwtToken", timeToLive = 60*60*24*7)
public class RefreshTokenEntity {
    @Id
    private String id;

    @Indexed
    private String refeshToken;
}
