package com.tarantulacommunity.tarantulasocial.security.repository;

import com.tarantulacommunity.tarantulasocial.security.entity.EmailVerificationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationTokenEntity, Long> {

    Optional<EmailVerificationTokenEntity> findByToken(String token);

    Optional<EmailVerificationTokenEntity> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
