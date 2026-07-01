package com.tarantulacommunity.tarantulasocial.features.message.repository;

import com.tarantulacommunity.tarantulasocial.features.message.entity.MessageEntity;
import com.tarantulacommunity.tarantulasocial.features.message.entity.ThreadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    List<MessageEntity> findByThreadIdOrderByCreatedAtAsc(Long threadId);

    @Query(value = "SELECT DISTINCT m.thread FROM MessageEntity m " +
            "WHERE m.sender.id = :userId OR m.recipient.id = :userId " +
            "ORDER BY m.thread.updatedAt DESC",
            countQuery = "SELECT COUNT(DISTINCT m.thread) FROM MessageEntity m " +
                    "WHERE m.sender.id = :userId OR m.recipient.id = :userId")
    Page<ThreadEntity> findThreadsByUserId(@Param("userId") Long userId, Pageable pageable);

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN TRUE ELSE FALSE END FROM MessageEntity m " +
            "WHERE m.thread.id = :threadId AND (m.sender.id = :userId OR m.recipient.id = :userId)")
    boolean existsByThreadIdAndUserId(@Param("threadId") Long threadId, @Param("userId") Long userId);

    @Query("SELECT m FROM MessageEntity m WHERE m.id IN (" +
            "SELECT MAX(m2.id) FROM MessageEntity m2 WHERE m2.thread.id IN :threadIds GROUP BY m2.thread.id)")
    List<MessageEntity> findLatestMessagePerThread(@Param("threadIds") List<Long> threadIds);
}