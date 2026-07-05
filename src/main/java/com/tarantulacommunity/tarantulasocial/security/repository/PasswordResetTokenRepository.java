package com.tarantulacommunity.tarantulasocial.security.repository;

import com.tarantulacommunity.tarantulasocial.security.entity.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {

    Optional<PasswordResetTokenEntity> findByToken(String token);

    void deleteByUserId(Long userId);

    Optional<PasswordResetTokenEntity> findByUserId(Long userId);
}
