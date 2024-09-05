package com.example.gyeongjoLog.event.repository;

import com.example.gyeongjoLog.event.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventRepository extends JpaRepository<EventEntity, Long> {
    @Query("SELECT e FROM EventEntity e WHERE e.user.id = :userId ORDER BY e.date DESC")
    List<EventEntity> findAllEvents(@Param("userId") Long userId);
    @Query("SELECT e FROM EventEntity e WHERE e.user.id = :userId AND e.amount > 0 ORDER BY e.date DESC")
    List<EventEntity> findMyEvents(@Param("userId") Long userId);

    @Query("SELECT e FROM EventEntity e WHERE e.user.id = :userId AND e.amount > 0 AND e.date = :date And e.eventType = :type ORDER BY e.date DESC")
    List<EventEntity> findMyEventSummaries(@Param("userId") Long userId, @Param("type") String type, @Param("date") String date);

    @Query("SELECT e FROM EventEntity e WHERE e.user.id = :userId AND e.amount < 0 ORDER BY e.date DESC")
    List<EventEntity> findOthersEventSummaries(@Param("userId") Long userId);

    @Query("SELECT e FROM EventEntity e WHERE e.user.id = :userId AND FUNCTION('DATE_FORMAT', e.date, '%Y-%m') = :yearMonth")
    List<EventEntity> findEventsByYearMonth(@Param("userId") Long userId, @Param("yearMonth") String yearMonth);
}
