package com.tarantulacommunity.tarantulasocial.features.user.repository;

import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByUsername(String username);

    List<UserEntity> findByEmailVerifiedFalseAndCreatedAtBefore(LocalDateTime cutoff);

    List<UserEntity> findByEmailVerifiedFalseAndCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByIdAndProfilePicKey(Long id, String profilePicKey);

    boolean existsByIdAndFavoriteImageKey(Long id, String favoriteImageKey);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE UserEntity u SET u.bio = :bio, u.updatedAt = :now WHERE u.id = :id")
    int updateBio(@Param("id") Long id, @Param("bio") String bio, @Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE UserEntity u SET u.favoriteCaption = :caption, u.updatedAt = :now WHERE u.id = :id")
    int updateFavoriteCaption(@Param("id") Long id, @Param("caption") String caption, @Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE UserEntity u SET u.profilePicKey = :key, u.updatedAt = :now WHERE u.id = :id")
    int updateProfilePicKey(@Param("id") Long id, @Param("key") String key, @Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE UserEntity u SET u.favoriteImageKey = :key, u.updatedAt = :now WHERE u.id = :id")
    int updateFavoriteImageKey(@Param("id") Long id, @Param("key") String key, @Param("now") LocalDateTime now);
}

