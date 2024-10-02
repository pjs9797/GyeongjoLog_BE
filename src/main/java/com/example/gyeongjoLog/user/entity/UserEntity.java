package com.example.gyeongjoLog.user.entity;

import com.example.gyeongjoLog.event.entity.EventEntity;
import com.example.gyeongjoLog.event.entity.EventTypeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;


@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String password;
    @Builder.Default
    private String role = "USER";

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventEntity> events;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventTypeEntity> eventTypes;
}
