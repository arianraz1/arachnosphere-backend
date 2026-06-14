package com.tarantulacommunity.tarantulasocial.repositories;

import com.tarantulacommunity.tarantulasocial.domain.entities.MessageEntity;
import com.tarantulacommunity.tarantulasocial.domain.entities.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    Page<MessageEntity> findByRecipientAndDeletedByRecipientFalseOrderByCreatedAtDesc(
            UserEntity recipient, Pageable pageable);

    Page<MessageEntity> findBySenderAndDeletedBySenderFalseOrderByCreatedAtDesc(
            UserEntity sender, Pageable pageable);
}
