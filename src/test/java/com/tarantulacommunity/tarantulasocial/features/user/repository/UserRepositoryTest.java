package com.tarantulacommunity.tarantulasocial.features.user.repository;

import com.tarantulacommunity.tarantulasocial.TestUtilities;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserRepositoryTest {
    @Autowired
    private TestEntityManager em;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsername_returnsCorrectUser() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));

        Optional<UserEntity> result = userRepository.findByUsername("test1");

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void findByUsername_returnsEmpty_whenNoMatch() {
        Optional<UserEntity> result = userRepository.findByUsername("nonexistent");

        assertTrue(result.isEmpty());
    }

    @Test
    void existsByEmail_returnsTrue_whenExists() {
        em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        assertTrue(userRepository.existsByEmail("test1@example.com"));
    }

    @Test
    void existsByEmail_returnsFalse_whenNoMatch() {
        assertFalse(userRepository.existsByEmail("nonexistent@example.com"));
    }

    @Test
    void existsByUsername_returnsTrue_whenExists() {
        em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        assertTrue(userRepository.existsByUsername("test1"));
    }

    @Test
    void existsByUsername_returnsFalse_whenNoMatch() {
        assertFalse(userRepository.existsByUsername("nonexistent"));
    }

    @Test
    void existsByIdAndProfilePicKey_returnsTrue_whenKeyMatchesCurrentRow() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        user.setProfilePicKey("profile-pictures/current.jpg");
        em.persistAndFlush(user);

        assertTrue(userRepository.existsByIdAndProfilePicKey(user.getId(), "profile-pictures/current.jpg"));
    }

    @Test
    void existsByIdAndProfilePicKey_returnsFalse_whenKeyIsStale() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        user.setProfilePicKey("profile-pictures/current.jpg");
        em.persistAndFlush(user);

        assertFalse(userRepository.existsByIdAndProfilePicKey(user.getId(), "profile-pictures/stale.jpg"));
    }

    @Test
    void existsByIdAndFavoriteImageKey_returnsTrue_whenKeyMatchesCurrentRow() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        user.setFavoriteImageKey("favorite-images/current.jpg");
        em.persistAndFlush(user);

        assertTrue(userRepository.existsByIdAndFavoriteImageKey(user.getId(), "favorite-images/current.jpg"));
    }

    @Test
    void existsByIdAndFavoriteImageKey_returnsFalse_whenKeyIsStale() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        user.setFavoriteImageKey("favorite-images/current.jpg");
        em.persistAndFlush(user);

        assertFalse(userRepository.existsByIdAndFavoriteImageKey(user.getId(), "favorite-images/stale.jpg"));
    }

    // ---- new: @Modifying query methods ----

    @Test
    void updateBio_updatesOnlyBioAndUpdatedAt_leavesVersionUnchanged() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        Long originalVersion = user.getVersion();

        LocalDateTime now = LocalDateTime.now();
        int rowsAffected = userRepository.updateBio(user.getId(), "New bio", now);
        em.clear();

        assertEquals(1, rowsAffected);
        UserEntity reloaded = em.find(UserEntity.class, user.getId());
        assertEquals("New bio", reloaded.getBio());
        assertEquals(originalVersion, reloaded.getVersion());
    }

    @Test
    void updateBio_returnsZero_whenUserDoesNotExist() {
        int rowsAffected = userRepository.updateBio(999L, "New bio", LocalDateTime.now());
        assertEquals(0, rowsAffected);
    }

    @Test
    void updateFavoriteCaption_updatesOnlyCaption_leavesOtherFieldsUnchanged() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        user.setFavoriteImageKey("favorite-images/existing.jpg");
        em.persistAndFlush(user);
        Long originalVersion = user.getVersion();

        int rowsAffected = userRepository.updateFavoriteCaption(user.getId(), "New caption", LocalDateTime.now());
        em.clear();

        assertEquals(1, rowsAffected);
        UserEntity reloaded = em.find(UserEntity.class, user.getId());
        assertEquals("New caption", reloaded.getFavoriteCaption());
        assertEquals("favorite-images/existing.jpg", reloaded.getFavoriteImageKey());
        assertEquals(originalVersion, reloaded.getVersion());
    }

    @Test
    void updateProfilePicKey_updatesOnlyProfilePicKey_leavesVersionUnchanged() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        Long originalVersion = user.getVersion();

        int rowsAffected = userRepository.updateProfilePicKey(user.getId(), "profile-pictures/new.jpg", LocalDateTime.now());
        em.clear();

        assertEquals(1, rowsAffected);
        UserEntity reloaded = em.find(UserEntity.class, user.getId());
        assertEquals("profile-pictures/new.jpg", reloaded.getProfilePicKey());
        assertEquals(originalVersion, reloaded.getVersion());
    }

    @Test
    void updateFavoriteImageKey_updatesOnlyImageKey_leavesCaptionUnchanged() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        user.setFavoriteCaption("Existing caption");
        em.persistAndFlush(user);
        Long originalVersion = user.getVersion();

        int rowsAffected = userRepository.updateFavoriteImageKey(user.getId(), "favorite-images/new.jpg", LocalDateTime.now());
        em.clear();

        assertEquals(1, rowsAffected);
        UserEntity reloaded = em.find(UserEntity.class, user.getId());
        assertEquals("favorite-images/new.jpg", reloaded.getFavoriteImageKey());
        assertEquals("Existing caption", reloaded.getFavoriteCaption());
        assertEquals(originalVersion, reloaded.getVersion());
    }

    private UserEntity persistUnverifiedUserWithCreatedAt(String suffix, LocalDateTime createdAt) {
        UserEntity user = TestUtilities.unverifiedUser(suffix, suffix);
        em.persistAndFlush(user);

        em.getEntityManager()
                .createNativeQuery("""
                UPDATE users
                SET created_at = :createdAt
                WHERE id = :id
                """)
                .setParameter("createdAt", createdAt)
                .setParameter("id", user.getId())
                .executeUpdate();

        em.clear();

        return em.find(UserEntity.class, user.getId());
    }

    @Test
    void findByEmailVerifiedFalseAndCreatedAtBefore_excludesVerifiedUsers() {
        UserEntity verifiedOld = TestUtilities.verifiedUser("verifiedold", "verifiedold");
        em.persist(verifiedOld);
        em.flush();
        ReflectionTestUtils.setField(verifiedOld, "createdAt", LocalDateTime.now().minusDays(8));
        em.persistFlushFind(verifiedOld);

        List<UserEntity> result = userRepository.findByEmailVerifiedFalseAndCreatedAtBefore(
                LocalDateTime.now().minusDays(7));

        assertThat(result).isEmpty();
    }

    @Test
    void findByEmailVerifiedFalseAndCreatedAtBetween_returnsUsersInWindow() {
        LocalDateTime now = LocalDateTime.now();

        persistUnverifiedUserWithCreatedAt("day6", now.minusDays(6).minusHours(1));
        persistUnverifiedUserWithCreatedAt("day3", now.minusDays(3));
        persistUnverifiedUserWithCreatedAt("day8", now.minusDays(8));

        List<UserEntity> result =
                userRepository.findByEmailVerifiedFalseAndCreatedAtBetween(
                        now.minusDays(7),
                        now.minusDays(6)
                );

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUsername()).isEqualTo("testday6");
    }
}