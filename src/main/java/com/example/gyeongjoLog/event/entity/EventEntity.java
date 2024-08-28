package com.example.gyeongjoLog.event.entity;

import com.example.gyeongjoLog.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phoneNumber;
    private String eventType;
    private String date;
    private String relationship;
    private int amount;

    @Column(nullable = true)
    private String memo;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}
