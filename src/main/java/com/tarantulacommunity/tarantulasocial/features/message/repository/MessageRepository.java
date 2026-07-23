package com.tarantulacommunity.tarantulasocial.features.message.repository;

import com.tarantulacommunity.tarantulasocial.features.message.entity.MessageEntity;
import com.tarantulacommunity.tarantulasocial.features.message.entity.ThreadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    @Query("SELECT m FROM MessageEntity m WHERE m.thread.id = :threadId " +
            "AND NOT (m.sender.id = :userId AND m.deletedBySender = true) " +
            "AND NOT (m.recipient.id = :userId AND m.deletedByRecipient = true) " +
            "ORDER BY m.createdAt DESC")
    Page<MessageEntity> findVisibleMessagesByThreadId(
            @Param("threadId") Long threadId,
            @Param("userId") Long userId,
            Pageable pageable);

    Page<MessageEntity> findByThreadIdOrderByCreatedAtDesc(Long threadId, Pageable pageable);

    @Query(value = "SELECT DISTINCT m.thread FROM MessageEntity m " +
            "WHERE (m.sender.id = :userId AND m.deletedBySender = false) " +
            "OR (m.recipient.id = :userId AND m.deletedByRecipient = false) " +
            "ORDER BY m.thread.updatedAt DESC",
            countQuery = "SELECT COUNT(DISTINCT m.thread) FROM MessageEntity m " +
                    "WHERE (m.sender.id = :userId AND m.deletedBySender = false) " +
                    "OR (m.recipient.id = :userId AND m.deletedByRecipient = false)")
    Page<ThreadEntity> findThreadsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN TRUE ELSE FALSE END FROM MessageEntity m " +
            "WHERE m.thread.id = :threadId " +
            "AND ((m.sender.id = :userId AND m.deletedBySender = false) " +
            "OR (m.recipient.id = :userId AND m.deletedByRecipient = false))")
    boolean hasVisibleMessageInThread(@Param("threadId") Long threadId, @Param("userId") Long userId);

    @Query("SELECT m FROM MessageEntity m WHERE m.id IN (" +
            "SELECT MAX(m2.id) FROM MessageEntity m2 " +
            "WHERE m2.thread.id IN :threadIds " +
            "AND ((m2.sender.id = :userId AND m2.deletedBySender = false) " +
            "OR (m2.recipient.id = :userId AND m2.deletedByRecipient = false)) " +
            "GROUP BY m2.thread.id)")
  List<MessageEntity> findLatestVisibleMessagePerThread(@Param("threadIds") List<Long> threadIds, @Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN TRUE ELSE FALSE END FROM MessageEntity m " +
            "WHERE m.thread.id = :threadId AND (m.sender.id = :userId OR m.recipient.id = :userId)")
    boolean isUserParticipantInThread(@Param("threadId") Long threadId, @Param("userId") Long userId);

    // Derived query to check if any messages still exist for a thread
    boolean existsByThreadId(Long threadId);

    @Modifying
    @Query("UPDATE MessageEntity m SET m.deletedBySender = true " +
           "WHERE m.thread.id = :threadId AND m.sender.id = :userId AND m.deletedBySender = false")
    int markDeletedBySenderForThread(@Param("threadId") Long threadId, @Param("userId") Long userId);

    @Modifying
    @Query("UPDATE MessageEntity m SET m.deletedByRecipient = true " +
           "WHERE m.thread.id = :threadId AND m.recipient.id = :userId AND m.deletedByRecipient = false")
    int markDeletedByRecipientForThread(@Param("threadId") Long threadId, @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM MessageEntity m WHERE m.thread.id = :threadId " +
           "AND m.deletedBySender = true AND m.deletedByRecipient = true")
    int deleteFullyDeletedMessagesInThread(@Param("threadId") Long threadId);

    @Query("SELECT MAX(m.createdAt) FROM MessageEntity m WHERE m.thread.id = :threadId " +
            "AND NOT (m.sender.id = :userId AND m.deletedBySender = true) " +
            "AND NOT (m.recipient.id = :userId AND m.deletedByRecipient = true)")
    LocalDateTime findLatestMessageTimestamp(@Param("threadId") Long threadId, @Param("userId") Long userId);
}