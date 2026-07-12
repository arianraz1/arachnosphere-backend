package com.tarantulacommunity.tarantulasocial.features.user.service;

import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.features.user.event.FavoriteImageReplacedEvent;
import com.tarantulacommunity.tarantulasocial.features.user.event.ProfileImageReplacedEvent;
import com.tarantulacommunity.tarantulasocial.features.user.repository.UserRepository;
import com.tarantulacommunity.tarantulasocial.s3.S3StorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserImageServiceTest {

    @Mock
    private S3StorageService s3StorageService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserImageService userImageService;

    private MultipartFile fakeFile() {
        return new MockMultipartFile("file", "tarantula.jpg", "image/jpeg", "fake-bytes".getBytes());
    }

    // ---- uploadProfilePicture ----

    @Test
    void uploadProfilePicture_savesNewKeyAndPublishesEvent() throws IOException {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setProfilePicKey("old-key.jpg");
        MultipartFile file = fakeFile();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(s3StorageService.uploadImage(file, "profile-pictures")).thenReturn("new-key.jpg");

        userImageService.uploadProfilePicture(1L, file);

        assertEquals("new-key.jpg", user.getProfilePicKey());
        verify(userRepository).save(user);

        ArgumentCaptor<ProfileImageReplacedEvent> captor = ArgumentCaptor.forClass(ProfileImageReplacedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals(1L, captor.getValue().userId());
        assertEquals("old-key.jpg", captor.getValue().oldKey());
        assertEquals("new-key.jpg", captor.getValue().newKey());
    }

    @Test
    void uploadProfilePicture_wrapsIOExceptionAsImageUploadException() throws IOException {
        UserEntity user = new UserEntity();
        user.setId(1L);
        MultipartFile file = fakeFile();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(s3StorageService.uploadImage(file, "profile-pictures")).thenThrow(new IOException("boom"));

        assertThrows(GlobalExceptionHandler.ImageUploadException.class,
                () -> userImageService.uploadProfilePicture(1L, file));

        verify(userRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void uploadProfilePicture_throwsWhenUserNotFound() {
        MultipartFile file = fakeFile();
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userImageService.uploadProfilePicture(99L, file));

        verifyNoInteractions(s3StorageService);
        verifyNoInteractions(eventPublisher);
    }

    // ---- uploadFavoriteImage ----

    @Test
    void uploadFavoriteImage_savesNewKeyAndPublishesEvent() throws IOException {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setFavoriteImageKey("old-fav-key.jpg");
        MultipartFile file = fakeFile();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(s3StorageService.uploadImage(file, "favorite-images")).thenReturn("new-fav-key.jpg");

        userImageService.uploadFavoriteImage(1L, file);

        assertEquals("new-fav-key.jpg", user.getFavoriteImageKey());
        verify(userRepository).save(user);

        ArgumentCaptor<FavoriteImageReplacedEvent> captor = ArgumentCaptor.forClass(FavoriteImageReplacedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals(1L, captor.getValue().userId());
        assertEquals("old-fav-key.jpg", captor.getValue().oldKey());
        assertEquals("new-fav-key.jpg", captor.getValue().newKey());
    }

    @Test
    void uploadFavoriteImage_wrapsIOExceptionAsImageUploadException() throws IOException {
        UserEntity user = new UserEntity();
        user.setId(1L);
        MultipartFile file = fakeFile();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(s3StorageService.uploadImage(file, "favorite-images")).thenThrow(new IOException("boom"));

        assertThrows(GlobalExceptionHandler.ImageUploadException.class,
                () -> userImageService.uploadFavoriteImage(1L, file));

        verify(userRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void uploadFavoriteImage_throwsWhenUserNotFound() {
        MultipartFile file = fakeFile();
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userImageService.uploadFavoriteImage(99L, file));

        verifyNoInteractions(s3StorageService);
        verifyNoInteractions(eventPublisher);
    }

    // ---- onProfileImageReplaced (AFTER_COMMIT listener, invoked directly) ----

    @Test
    void onProfileImageReplaced_deletesOldKeyWhenNewKeyIsCommitted() {
        ProfileImageReplacedEvent event = new ProfileImageReplacedEvent(1L, "old-key.jpg", "new-key.jpg");
        when(userRepository.existsByIdAndProfilePicKey(1L, "new-key.jpg")).thenReturn(true);

        userImageService.onProfileImageReplaced(event);

        verify(s3StorageService).deleteImage("old-key.jpg");
    }

    @Test
    void onProfileImageReplaced_skipsDeleteWhenNewKeyWasNotCommitted() {
        // simulates a concurrent update overwriting this upload before we could confirm the commit
        ProfileImageReplacedEvent event = new ProfileImageReplacedEvent(1L, "old-key.jpg", "new-key.jpg");
        when(userRepository.existsByIdAndProfilePicKey(1L, "new-key.jpg")).thenReturn(false);

        userImageService.onProfileImageReplaced(event);

        verify(s3StorageService, never()).deleteImage(any());
    }

    @Test
    void onProfileImageReplaced_doesNothingWhenOldKeyIsNull() {
        ProfileImageReplacedEvent event = new ProfileImageReplacedEvent(1L, null, "new-key.jpg");

        userImageService.onProfileImageReplaced(event);

        verifyNoInteractions(s3StorageService);
        verify(userRepository, never()).existsByIdAndProfilePicKey(any(), any());
    }

    @Test
    void onProfileImageReplaced_doesNothingWhenOldKeyIsBlank() {
        ProfileImageReplacedEvent event = new ProfileImageReplacedEvent(1L, "   ", "new-key.jpg");

        userImageService.onProfileImageReplaced(event);

        verifyNoInteractions(s3StorageService);
    }

    // ---- onFavoriteImageReplaced (mirror) ----

    @Test
    void onFavoriteImageReplaced_deletesOldKeyWhenNewKeyIsCommitted() {
        FavoriteImageReplacedEvent event = new FavoriteImageReplacedEvent(1L, "old-fav.jpg", "new-fav.jpg");
        when(userRepository.existsByIdAndFavoriteImageKey(1L, "new-fav.jpg")).thenReturn(true);

        userImageService.onFavoriteImageReplaced(event);

        verify(s3StorageService).deleteImage("old-fav.jpg");
    }

    @Test
    void onFavoriteImageReplaced_skipsDeleteWhenNewKeyWasNotCommitted() {
        FavoriteImageReplacedEvent event = new FavoriteImageReplacedEvent(1L, "old-fav.jpg", "new-fav.jpg");
        when(userRepository.existsByIdAndFavoriteImageKey(1L, "new-fav.jpg")).thenReturn(false);

        userImageService.onFavoriteImageReplaced(event);

        verify(s3StorageService, never()).deleteImage(any());
    }

    @Test
    void onFavoriteImageReplaced_doesNothingWhenOldKeyIsNull() {
        FavoriteImageReplacedEvent event = new FavoriteImageReplacedEvent(1L, null, "new-fav.jpg");

        userImageService.onFavoriteImageReplaced(event);

        verifyNoInteractions(s3StorageService);
    }
}
