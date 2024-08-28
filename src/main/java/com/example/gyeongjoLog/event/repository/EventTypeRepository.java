package com.example.gyeongjoLog.event.repository;

import com.example.gyeongjoLog.event.entity.EventTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EventTypeRepository extends JpaRepository<EventTypeEntity, Long> {
    @Query("SELECT e FROM EventTypeEntity e WHERE e.user.id = :userId ORDER BY e.id ASC")
    List<EventTypeEntity> findByUserId(Long userId);
}
