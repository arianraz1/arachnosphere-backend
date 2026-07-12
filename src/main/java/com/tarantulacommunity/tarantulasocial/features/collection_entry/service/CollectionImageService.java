package com.tarantulacommunity.tarantulasocial.features.collection_entry.service;

import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.response.CollectionImageResponse;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.entity.CollectionEntryEntity;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.entity.CollectionImageEntity;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.repository.CollectionEntryRepository;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.repository.CollectionImageRepository;
import com.tarantulacommunity.tarantulasocial.s3.S3StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CollectionImageService {

    private static final int MAX_IMAGES_PER_ENTRY = 5;

    private final S3StorageService s3StorageService;
    private final CollectionImageRepository collectionImageRepository;
    private final CollectionEntryRepository collectionEntryRepository;

    public CollectionImageResponse uploadImage(Long userId, Long entryId, MultipartFile file, String caption) {
        assertOwnsEntry(userId, entryId);

        long currentCount = collectionImageRepository.countByCollectionId(entryId);
        if (currentCount >= MAX_IMAGES_PER_ENTRY) {
            throw new GlobalExceptionHandler.ImageLimitExceededException(
                    "Maximum of " + MAX_IMAGES_PER_ENTRY + " images per collection entry reached");
        }

        String key = uploadToS3(file, "collection-images/" + entryId);

        CollectionEntryEntity entryRef = collectionEntryRepository.getReferenceById(entryId);

        CollectionImageEntity image = CollectionImageEntity.builder()
                .collection(entryRef)
                .s3Key(key)
                .caption(caption)
                .displayOrder((int) currentCount)
                .build();

        try {
            return toResponse(collectionImageRepository.save(image));
        } catch (RuntimeException e) {
            // Avoid leaving an orphaned object in S3 if the DB write fails
            // (constraint violation, optimistic lock, connection drop, etc.)
            s3StorageService.deleteImage(key);
            throw e;
        }
    }

    public void deleteImage(Long userId, Long entryId, Long imageId) {
        assertOwnsEntry(userId, entryId);

        CollectionImageEntity image = findImageOrThrow(entryId, imageId);

        collectionImageRepository.delete(image);
        s3StorageService.deleteImage(image.getS3Key());

        resequenceDisplayOrder(entryId);
    }

    @Transactional(readOnly = true)
    public List<CollectionImageResponse> listImages(Long entryId) {
        return collectionImageRepository.findByCollectionIdOrderByDisplayOrderAsc(entryId).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Bulk-fetches images for many collection entries in a single query and
     * groups them by collection id. Use this instead of calling listImages()
     * in a loop when rendering a list of entries — avoids N+1 queries.
     *
     * Entries with no images will simply be absent from the returned map;
     * callers should default to an empty list when looking up a missing key.
     */
    @Transactional(readOnly = true)
    public Map<Long, List<CollectionImageResponse>> listImagesByCollectionIds(List<Long> collectionIds) {
        if (collectionIds == null || collectionIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return collectionImageRepository.findByCollectionIdInOrderByDisplayOrderAsc(collectionIds).stream()
                .map(this::toResponseWithCollectionId)
                .collect(Collectors.groupingBy(
                        Entry::collectionId,
                        Collectors.mapping(Entry::response, Collectors.toList())
                ));
    }

    public CollectionImageResponse setFeatured(Long userId, Long entryId, Long imageId) {
        assertOwnsEntry(userId, entryId);

        List<CollectionImageEntity> currentlyFeatured = collectionImageRepository.findByCollectionIdAndFeaturedTrue(entryId);
        currentlyFeatured.forEach(img -> img.setFeatured(false));
        collectionImageRepository.saveAll(currentlyFeatured);

        CollectionImageEntity target = findImageOrThrow(entryId, imageId);
        target.setFeatured(true);

        return toResponse(collectionImageRepository.save(target));
    }

    private String uploadToS3(MultipartFile file, String folder) {
        try {
            return s3StorageService.uploadImage(file, folder);
        } catch (IOException e) {
            throw new GlobalExceptionHandler.ImageUploadException("Failed to read uploaded file", e);
        }
    }

    private CollectionImageEntity findImageOrThrow(Long entryId, Long imageId) {
        return collectionImageRepository.findByIdAndCollectionId(imageId, entryId)
                .orElseThrow(() -> new GlobalExceptionHandler.ImageNotFoundException("Image not found in this collection entry"));
    }

    /**
     * Keeps displayOrder contiguous (0..n-1) after a delete so the frontend
     * can rely on gap-free ordering for things like drag-to-reorder.
     */
    private void resequenceDisplayOrder(Long entryId) {
        List<CollectionImageEntity> remaining =
                collectionImageRepository.findByCollectionIdOrderByDisplayOrderAsc(entryId);

        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setDisplayOrder(i);
        }
        collectionImageRepository.saveAll(remaining);
    }

    private CollectionImageResponse toResponse(CollectionImageEntity entity) {
        return new CollectionImageResponse(
                entity.getId(),
                s3StorageService.getImageUrl(entity.getS3Key()),
                entity.getCaption(),
                entity.isFeatured(),
                entity.getDisplayOrder()
        );
    }

    private Entry toResponseWithCollectionId(CollectionImageEntity entity) {
        return new Entry(entity.getCollection().getId(), toResponse(entity));
    }

    private void assertOwnsEntry(Long userId, Long entryId) {
        if (!collectionEntryRepository.existsByIdAndUserId(entryId, userId)) {
            throw new AccessDeniedException("You do not have permission to modify this collection entry");
        }
    }

    private record Entry(Long collectionId, CollectionImageResponse response) {}
}