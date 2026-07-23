package com.tarantulacommunity.tarantulasocial.features.message.repository;

import com.tarantulacommunity.tarantulasocial.features.message.entity.ThreadParticipantEntity;
import com.tarantulacommunity.tarantulasocial.features.message.entity.ThreadParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ThreadParticipantRepository extends JpaRepository<ThreadParticipantEntity, ThreadParticipantId> {

    Optional<ThreadParticipantEntity> findByThread_IdAndUser_Id(Long threadId, Long userId);

    boolean existsByThread_IdAndUser_Id(Long threadId, Long userId);

    @Modifying
    @Query("UPDATE ThreadParticipantEntity tp SET tp.lastReadAt = :readAt " +
            "WHERE tp.thread.id = :threadId AND tp.user.id = :userId")
    int markRead(@Param("threadId") Long threadId, @Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);

    @Query("SELECT tp.thread.id AS threadId, tp.lastReadAt AS lastReadAt FROM ThreadParticipantEntity tp " +
            "WHERE tp.thread.id IN :threadIds AND tp.user.id = :userId")
    List<ThreadReadState> findReadStatesForThreads(@Param("threadIds") List<Long> threadIds, @Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(tp) > 0 THEN TRUE ELSE FALSE END FROM ThreadParticipantEntity tp " +
            "WHERE tp.user.id = :userId " +
            "AND EXISTS (SELECT 1 FROM MessageEntity m " +
            "  WHERE m.thread.id = tp.thread.id " +
            "  AND m.sender.id != :userId " +
            "  AND m.deletedByRecipient = false " +
            "  AND (tp.lastReadAt IS NULL OR m.createdAt > tp.lastReadAt))")
    boolean existsUnreadForUser(@Param("userId") Long userId);
}
