package com.tarantulacommunity.tarantulasocial.features.user.service;

import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.features.user.event.FavoriteImageReplacedEvent;
import com.tarantulacommunity.tarantulasocial.features.user.event.ProfileImageReplacedEvent;
import com.tarantulacommunity.tarantulasocial.features.user.repository.UserRepository;
import com.tarantulacommunity.tarantulasocial.s3.S3StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
public class UserImageService {

    private final S3StorageService s3StorageService;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void uploadProfilePicture(Long userId, MultipartFile file) {
        UserEntity user = findUserOrThrow(userId);
        String oldKey = user.getProfilePicKey();

        String newKey = uploadToS3(file, "profile-pictures");
        user.setProfilePicKey(newKey);
        userRepository.save(user);

        eventPublisher.publishEvent(new ProfileImageReplacedEvent(userId, oldKey, newKey));
    }

    @Transactional
    public void uploadFavoriteImage(Long userId, MultipartFile file) {
        UserEntity user = findUserOrThrow(userId);
        String oldKey = user.getFavoriteImageKey();

        String newKey = uploadToS3(file, "favorite-images");
        user.setFavoriteImageKey(newKey);
        userRepository.save(user);

        eventPublisher.publishEvent(new FavoriteImageReplacedEvent(userId, oldKey, newKey));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProfileImageReplaced(ProfileImageReplacedEvent event) {
        if (event.oldKey() == null || event.oldKey().isBlank()) {
            return;
        }
        // Only delete the old object if the new key is confirmed persisted —
        // guards against deleting oldKey after a concurrent update already moved past us.
        boolean committed = userRepository.existsByIdAndProfilePicKey(event.userId(), event.newKey());
        if (committed) {
            s3StorageService.deleteImage(event.oldKey());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFavoriteImageReplaced(FavoriteImageReplacedEvent event) {
        if (event.oldKey() == null || event.oldKey().isBlank()) {
            return;
        }
        boolean committed = userRepository.existsByIdAndFavoriteImageKey(event.userId(), event.newKey());
        if (committed) {
            s3StorageService.deleteImage(event.oldKey());
        }
    }

    private String uploadToS3(MultipartFile file, String folder) {
        try {
            return s3StorageService.uploadImage(file, folder);
        } catch (java.io.IOException e) {
            throw new GlobalExceptionHandler.ImageUploadException("Failed to read uploaded file", e);
        }
    }

    private UserEntity findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }
}