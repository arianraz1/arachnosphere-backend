package com.tarantulacommunity.tarantulasocial.features.collection_entry.repository;

import com.tarantulacommunity.tarantulasocial.features.collection_entry.entity.CollectionImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CollectionImageRepository extends JpaRepository<CollectionImageEntity, Long> {

    List<CollectionImageEntity> findByCollectionIdOrderByDisplayOrderAsc(Long collectionId);

    List<CollectionImageEntity> findByCollectionIdInOrderByDisplayOrderAsc(List<Long> collectionIds);

    Optional<CollectionImageEntity> findByIdAndCollectionId(Long imageId, Long collectionId);

    long countByCollectionId(Long collectionId);

    List<CollectionImageEntity> findByCollectionIdAndFeaturedTrue(Long collectionId);
}