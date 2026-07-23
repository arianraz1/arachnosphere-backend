package com.tarantulacommunity.tarantulasocial.features.collection_entry.service;

import com.tarantulacommunity.tarantulasocial.TestUtilities;
import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.response.CollectionImageResponse;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.entity.CollectionEntryEntity;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.entity.CollectionImageEntity;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.repository.CollectionEntryRepository;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.repository.CollectionImageRepository;
import com.tarantulacommunity.tarantulasocial.features.species.entity.SpeciesEntity;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.s3.S3StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionImageServiceTest {

    @Mock
    private S3StorageService s3StorageService;

    @Mock
    private CollectionImageRepository collectionImageRepository;

    @Mock
    private CollectionEntryRepository collectionEntryRepository;

    @InjectMocks
    private CollectionImageService collectionImageService;

    private static final Long ENTRY_ID = 1L;
    private static final Long USER_ID = 1L;

    private UserEntity user;
    private SpeciesEntity species;
    private CollectionEntryEntity entry;

    @BeforeEach
    void setUp() {
        user = TestUtilities.verifiedUser("1", "1");
        user.setId(USER_ID);

        species = TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens");
        species.setId(1L);

        entry = TestUtilities.validCollectionEntry(user, species);
        entry.setId(ENTRY_ID);
    }

    // ---------- uploadImage ----------

    @Test
    void uploadImage_savesAndReturnsResponse_whenWithinLimit() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "data".getBytes());

        when(collectionEntryRepository.existsByIdAndUserId(ENTRY_ID, USER_ID)).thenReturn(true);
        when(collectionImageRepository.countByCollectionId(ENTRY_ID)).thenReturn(2L);
        when(s3StorageService.uploadImage(eq(file), anyString())).thenReturn("collection-images/1/abc.jpg");
        when(collectionEntryRepository.getReferenceById(ENTRY_ID)).thenReturn(entry);

        CollectionImageEntity saved = CollectionImageEntity.builder()
                .id(50L)
                .collection(entry)
                .s3Key("collection-images/1/abc.jpg")
                .displayOrder(2)
                .build();
        when(collectionImageRepository.save(any(CollectionImageEntity.class))).thenReturn(saved);
        when(s3StorageService.getImageUrl("collection-images/1/abc.jpg")).thenReturn("https://cdn/abc.jpg");

        CollectionImageResponse response = collectionImageService.uploadImage(USER_ID, ENTRY_ID, file, "caption");

        assertEquals(50L, response.getId());
        assertEquals("https://cdn/abc.jpg", response.getUrl());
    }

    @Test
    void uploadImage_throwsImageLimitExceededException_whenAtLimit() {
        MultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "data".getBytes());

        when(collectionEntryRepository.existsByIdAndUserId(ENTRY_ID, USER_ID)).thenReturn(true);
        when(collectionImageRepository.countByCollectionId(ENTRY_ID)).thenReturn(6L);

        assertThrows(GlobalExceptionHandler.ImageLimitExceededException.class, () ->
                collectionImageService.uploadImage(USER_ID, ENTRY_ID, file, "caption"));

        verifyNoInteractions(s3StorageService);
    }

    @Test
    void uploadImage_throwsAccessDeniedException_whenUserDoesNotOwnEntry() {
        MultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "data".getBytes());

        when(collectionEntryRepository.existsByIdAndUserId(ENTRY_ID, 99L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () ->
                collectionImageService.uploadImage(99L, ENTRY_ID, file, "caption"));

        verifyNoInteractions(s3StorageService);
    }

    @Test
    void uploadImage_deletesS3ObjectWhenDatabaseSaveFails() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "data".getBytes());

        when(collectionEntryRepository.existsByIdAndUserId(ENTRY_ID, USER_ID)).thenReturn(true);
        when(collectionImageRepository.countByCollectionId(ENTRY_ID)).thenReturn(0L);
        when(s3StorageService.uploadImage(eq(file), anyString())).thenReturn("collection-images/1/abc.jpg");
        when(collectionEntryRepository.getReferenceById(ENTRY_ID)).thenReturn(entry);
        when(collectionImageRepository.save(any(CollectionImageEntity.class)))
                .thenThrow(new RuntimeException("db down"));

        assertThrows(RuntimeException.class, () ->
                collectionImageService.uploadImage(USER_ID, ENTRY_ID, file, "caption"));

        verify(s3StorageService).deleteImage("collection-images/1/abc.jpg");
    }

    @Test
    void uploadImage_throwsImageUploadException_whenFileReadFails() throws IOException {
        MultipartFile file = mock(MultipartFile.class);

        when(collectionEntryRepository.existsByIdAndUserId(ENTRY_ID, USER_ID)).thenReturn(true);
        when(collectionImageRepository.countByCollectionId(ENTRY_ID)).thenReturn(0L);
        when(s3StorageService.uploadImage(eq(file), anyString())).thenThrow(new IOException("read failed"));

        assertThrows(GlobalExceptionHandler.ImageUploadException.class, () ->
                collectionImageService.uploadImage(USER_ID, ENTRY_ID, file, "caption"));
    }

    // ---------- deleteImage ----------

    @Test
    void deleteImage_deletesFromDbAndS3AndResequencesRemainingImages() {
        CollectionImageEntity toDelete = CollectionImageEntity.builder()
                .id(50L).collection(entry).s3Key("key-1").displayOrder(1).build();
        CollectionImageEntity remaining = CollectionImageEntity.builder()
                .id(51L).collection(entry).s3Key("key-2").displayOrder(2).build();

        when(collectionEntryRepository.existsByIdAndUserId(ENTRY_ID, USER_ID)).thenReturn(true);
        when(collectionImageRepository.findByIdAndCollectionId(50L, ENTRY_ID)).thenReturn(Optional.of(toDelete));
        when(collectionImageRepository.findByCollectionIdOrderByDisplayOrderAsc(ENTRY_ID))
                .thenReturn(List.of(remaining));

        collectionImageService.deleteImage(USER_ID, ENTRY_ID, 50L);

        verify(collectionImageRepository).delete(toDelete);
        verify(s3StorageService).deleteImage("key-1");

        ArgumentCaptor<List<CollectionImageEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(collectionImageRepository).saveAll(captor.capture());
        assertEquals(0, captor.getValue().getFirst().getDisplayOrder());
    }

    @Test
    void deleteImage_throwsAccessDeniedException_whenUserDoesNotOwnEntry() {
        when(collectionEntryRepository.existsByIdAndUserId(ENTRY_ID, 99L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () ->
                collectionImageService.deleteImage(99L, ENTRY_ID, 50L));

        verifyNoInteractions(s3StorageService);
    }

    @Test
    void deleteImage_throwsImageNotFoundException_whenImageDoesNotExist() {
        when(collectionEntryRepository.existsByIdAndUserId(ENTRY_ID, USER_ID)).thenReturn(true);
        when(collectionImageRepository.findByIdAndCollectionId(99L, ENTRY_ID)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.ImageNotFoundException.class, () ->
                collectionImageService.deleteImage(USER_ID, ENTRY_ID, 99L));
    }

    // ---------- listImages ----------

    @Test
    void listImages_returnsImagesInDisplayOrder() {
        CollectionImageEntity img = CollectionImageEntity.builder()
                .id(50L).collection(entry).s3Key("key-1").caption("c").featured(true).displayOrder(0).build();

        when(collectionImageRepository.findByCollectionIdOrderByDisplayOrderAsc(ENTRY_ID)).thenReturn(List.of(img));
        when(s3StorageService.getImageUrl("key-1")).thenReturn("https://cdn/key-1");

        List<CollectionImageResponse> result = collectionImageService.listImages(ENTRY_ID);

        assertEquals(1, result.size());
        assertEquals("https://cdn/key-1", result.getFirst().getUrl());
        assertTrue(result.getFirst().isFeatured());
    }

    @Test
    void listImages_returnsEmpty_whenNoImages() {
        when(collectionImageRepository.findByCollectionIdOrderByDisplayOrderAsc(ENTRY_ID)).thenReturn(List.of());

        List<CollectionImageResponse> result = collectionImageService.listImages(ENTRY_ID);

        assertTrue(result.isEmpty());
    }

    // ---------- listImagesByCollectionIds (bulk lookup) ----------

    @Test
    void listImagesByCollectionIds_groupsImagesByCollectionId() {
        CollectionEntryEntity entry2 = TestUtilities.validCollectionEntry(user, species);
        entry2.setId(2L);

        CollectionImageEntity img1 = CollectionImageEntity.builder()
                .id(50L).collection(entry).s3Key("key-1").displayOrder(0).build();
        CollectionImageEntity img2 = CollectionImageEntity.builder()
                .id(51L).collection(entry2).s3Key("key-2").displayOrder(0).build();

        when(collectionImageRepository.findByCollectionIdInOrderByDisplayOrderAsc(List.of(ENTRY_ID, 2L)))
                .thenReturn(List.of(img1, img2));
        when(s3StorageService.getImageUrl(anyString())).thenReturn("https://cdn/x");

        Map<Long, List<CollectionImageResponse>> result =
                collectionImageService.listImagesByCollectionIds(List.of(ENTRY_ID, 2L));

        assertEquals(2, result.size());
        assertEquals(1, result.get(ENTRY_ID).size());
        assertEquals(1, result.get(2L).size());
    }

    @Test
    void listImagesByCollectionIds_returnsEmptyMapAndSkipsQuery_whenInputEmpty() {
        Map<Long, List<CollectionImageResponse>> result =
                collectionImageService.listImagesByCollectionIds(List.of());

        assertTrue(result.isEmpty());
        verifyNoInteractions(collectionImageRepository);
    }

    @Test
    void listImagesByCollectionIds_returnsEmptyMap_whenInputNull() {
        Map<Long, List<CollectionImageResponse>> result =
                collectionImageService.listImagesByCollectionIds(null);

        assertTrue(result.isEmpty());
        verifyNoInteractions(collectionImageRepository);
    }

    // ---------- setFeatured ----------

    @Test
    void setFeatured_unfeaturesPreviousAndFeaturesTarget() {
        CollectionImageEntity previouslyFeatured = CollectionImageEntity.builder()
                .id(50L).collection(entry).s3Key("key-1").featured(true).build();
        CollectionImageEntity target = CollectionImageEntity.builder()
                .id(51L).collection(entry).s3Key("key-2").featured(false).build();

        when(collectionEntryRepository.existsByIdAndUserId(ENTRY_ID, USER_ID)).thenReturn(true);
        when(collectionImageRepository.findByCollectionIdAndFeaturedTrue(ENTRY_ID))
                .thenReturn(List.of(previouslyFeatured));
        when(collectionImageRepository.findByIdAndCollectionId(51L, ENTRY_ID)).thenReturn(Optional.of(target));
        when(collectionImageRepository.save(target)).thenReturn(target);
        when(s3StorageService.getImageUrl("key-2")).thenReturn("https://cdn/key-2");

        CollectionImageResponse response = collectionImageService.setFeatured(USER_ID, ENTRY_ID, 51L);

        assertFalse(previouslyFeatured.isFeatured());
        assertTrue(target.isFeatured());
        assertTrue(response.isFeatured());
    }

    @Test
    void setFeatured_throwsAccessDeniedException_whenUserDoesNotOwnEntry() {
        when(collectionEntryRepository.existsByIdAndUserId(ENTRY_ID, 99L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () ->
                collectionImageService.setFeatured(99L, ENTRY_ID, 51L));
    }

    // ---------- updateCaption ----------

    @Test
    void updateCaption_updatesAndReturnsResponse() {
        CollectionImageEntity image = CollectionImageEntity.builder()
                .id(50L).collection(entry).s3Key("key-1").caption("old caption").featured(false).displayOrder(0).build();

        when(collectionEntryRepository.existsByIdAndUserId(ENTRY_ID, USER_ID)).thenReturn(true);
        when(collectionImageRepository.findByIdAndCollectionId(50L, ENTRY_ID)).thenReturn(Optional.of(image));
        when(collectionImageRepository.save(any(CollectionImageEntity.class))).thenReturn(image);
        when(s3StorageService.getImageUrl("key-1")).thenReturn("https://cdn/key-1");

        CollectionImageResponse response = collectionImageService.updateCaption(USER_ID, ENTRY_ID, 50L, "new caption");

        assertEquals("new caption", response.getCaption());
        assertEquals("new caption", image.getCaption());
    }

    @Test
    void updateCaption_throwsAccessDeniedException_whenUserDoesNotOwnEntry() {
        when(collectionEntryRepository.existsByIdAndUserId(ENTRY_ID, 99L)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> collectionImageService.updateCaption(99L, ENTRY_ID, 50L, "new caption"));
    }

    @Test
    void updateCaption_throwsImageNotFoundException_whenImageDoesNotExist() {
        when(collectionEntryRepository.existsByIdAndUserId(ENTRY_ID, USER_ID)).thenReturn(true);
        when(collectionImageRepository.findByIdAndCollectionId(99L, ENTRY_ID)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.ImageNotFoundException.class,
                () -> collectionImageService.updateCaption(USER_ID, ENTRY_ID, 99L, "new caption"));
    }
}
