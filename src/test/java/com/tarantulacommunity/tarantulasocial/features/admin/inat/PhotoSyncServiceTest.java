package com.tarantulacommunity.tarantulasocial.features.admin.inat;

import com.tarantulacommunity.tarantulasocial.features.admin.inat.data.InatTaxon;
import com.tarantulacommunity.tarantulasocial.features.species.entity.SpeciesEntity;
import com.tarantulacommunity.tarantulasocial.features.species.repository.SpeciesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhotoSyncServiceTest {

    @Mock
    private InatClient inatClient;

    @Mock
    private SpeciesRepository speciesRepository;

    @InjectMocks
    private PhotoSyncService photoSyncService;

    @Test
    void syncPhotos_WhenSpeciesWithoutPhotos_CallsRepository() {
        SpeciesEntity species = new SpeciesEntity();
        species.setScientificName("Brachypelma hamorii");
        species.setImageUrl(null);

        when(speciesRepository.findAllByImageUrlIsNull()).thenReturn(List.of(species));
        when(inatClient.findByScientificName(any())).thenReturn(Optional.empty());

        photoSyncService.syncPhotos();

        verify(speciesRepository).findAllByImageUrlIsNull();
    }

    @Test
    void syncPhotos_WhenTaxonFound_UpdatesPhotoAndCommonName() {
        SpeciesEntity species = new SpeciesEntity();
        species.setScientificName("Brachypelma hamorii");
        species.setImageUrl(null);
        species.setCommonName(null);

        InatTaxon taxon = new InatTaxon();
        taxon.setScientificName("Brachypelma hamorii");
        taxon.setCommonName("Mexican Red Knee");

        InatTaxon.InatPhoto photo = new InatTaxon.InatPhoto();
        photo.setMediumUrl("https://example.com/medium.jpg");
        taxon.setDefaultPhoto(photo);

        when(speciesRepository.findAllByImageUrlIsNull()).thenReturn(List.of(species));
        when(inatClient.findByScientificName("Brachypelma hamorii")).thenReturn(Optional.of(taxon));
        when(speciesRepository.save(any(SpeciesEntity.class))).thenReturn(species);

        photoSyncService.syncPhotos();

        verify(speciesRepository).save(any(SpeciesEntity.class));
    }

    @Test
    void syncPhotos_WhenTaxonFoundWithPhotoOnly_UpdatesPhoto() {
        SpeciesEntity species = new SpeciesEntity();
        species.setScientificName("Brachypelma hamorii");
        species.setImageUrl(null);

        InatTaxon taxon = new InatTaxon();
        taxon.setScientificName("Brachypelma hamorii");
        taxon.setCommonName(null);

        InatTaxon.InatPhoto photo = new InatTaxon.InatPhoto();
        photo.setMediumUrl("https://example.com/medium.jpg");
        taxon.setDefaultPhoto(photo);

        when(speciesRepository.findAllByImageUrlIsNull()).thenReturn(List.of(species));
        when(inatClient.findByScientificName("Brachypelma hamorii")).thenReturn(Optional.of(taxon));
        when(speciesRepository.save(any(SpeciesEntity.class))).thenReturn(species);

        photoSyncService.syncPhotos();

        verify(speciesRepository).save(any(SpeciesEntity.class));
    }

    @Test
    void syncPhotos_WhenTaxonFoundWithCommonNameOnly_UpdatesCommonName() {
        SpeciesEntity species = new SpeciesEntity();
        species.setScientificName("Brachypelma hamorii");
        species.setImageUrl(null);
        species.setCommonName(null);

        InatTaxon taxon = new InatTaxon();
        taxon.setScientificName("Brachypelma hamorii");
        taxon.setCommonName("Mexican Red Knee");
        taxon.setDefaultPhoto(null);

        when(speciesRepository.findAllByImageUrlIsNull()).thenReturn(List.of(species));
        when(inatClient.findByScientificName("Brachypelma hamorii")).thenReturn(Optional.of(taxon));
        when(speciesRepository.save(any(SpeciesEntity.class))).thenReturn(species);

        photoSyncService.syncPhotos();

        verify(speciesRepository).save(any(SpeciesEntity.class));
    }

    @Test
    void syncPhotos_WhenTaxonNotFound_SkipsUpdate() {
        SpeciesEntity species = new SpeciesEntity();
        species.setScientificName("Unknown Species");
        species.setImageUrl(null);

        when(speciesRepository.findAllByImageUrlIsNull()).thenReturn(List.of(species));
        when(inatClient.findByScientificName("Unknown Species")).thenReturn(Optional.empty());

        photoSyncService.syncPhotos();

        verify(speciesRepository, never()).save(any(SpeciesEntity.class));
    }

    @Test
    void syncAllPhotos_CallsFindAllInsteadOfFindByImageUrlIsNull() {
        SpeciesEntity species = new SpeciesEntity();
        species.setScientificName("Brachypelma hamorii");
        species.setImageUrl("https://example.com/existing.jpg");

        when(speciesRepository.findAll()).thenReturn(List.of(species));
        when(inatClient.findByScientificName(any())).thenReturn(Optional.empty());

        photoSyncService.syncAllPhotos();

        verify(speciesRepository).findAll();
        verify(speciesRepository, never()).findAllByImageUrlIsNull();
    }

    @Test
    void syncPhotos_WhenMultipleSpecies_ProcessesAll() {
        SpeciesEntity species1 = new SpeciesEntity();
        species1.setScientificName("Brachypelma hamorii");
        species1.setImageUrl(null);

        SpeciesEntity species2 = new SpeciesEntity();
        species2.setScientificName("Brachypelma smithi");
        species2.setImageUrl(null);

        when(speciesRepository.findAllByImageUrlIsNull()).thenReturn(List.of(species1, species2));
        when(inatClient.findByScientificName(any())).thenReturn(Optional.empty());

        photoSyncService.syncPhotos();

        verify(inatClient, times(2)).findByScientificName(any());
    }

    @Test
    void syncPhotos_WhenEmptyList_DoesNothing() {
        when(speciesRepository.findAllByImageUrlIsNull()).thenReturn(List.of());

        photoSyncService.syncPhotos();

        verify(inatClient, never()).findByScientificName(any());
        verify(speciesRepository, never()).save(any(SpeciesEntity.class));
    }

    @Test
    void syncPhotos_ReplacesmediumWithOriginalInUrl() {
        SpeciesEntity species = new SpeciesEntity();
        species.setScientificName("Brachypelma hamorii");

        InatTaxon taxon = new InatTaxon();
        InatTaxon.InatPhoto photo = new InatTaxon.InatPhoto();
        photo.setMediumUrl("https://inaturalist.org/photos/1/medium.jpeg");
        taxon.setDefaultPhoto(photo);

        when(speciesRepository.findAllByImageUrlIsNull()).thenReturn(List.of(species));
        when(inatClient.findByScientificName(any())).thenReturn(Optional.of(taxon));
        when(speciesRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        photoSyncService.syncPhotos();

        assertThat(species.getImageUrl()).isEqualTo("https://inaturalist.org/photos/1/original.jpeg");
    }

    @Test
    void syncPhotos_DoesNotOverrideExistingCommonNameWhenInatHasNone() {
        SpeciesEntity species = new SpeciesEntity();
        species.setScientificName("Brachypelma hamorii");
        species.setCommonName("Existing GBIF Name");

        InatTaxon taxon = new InatTaxon();
        taxon.setCommonName(null);
        InatTaxon.InatPhoto photo = new InatTaxon.InatPhoto();
        photo.setMediumUrl("https://inaturalist.org/photos/1/medium.jpg");
        taxon.setDefaultPhoto(photo);

        when(speciesRepository.findAllByImageUrlIsNull()).thenReturn(List.of(species));
        when(inatClient.findByScientificName(any())).thenReturn(Optional.of(taxon));
        when(speciesRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        photoSyncService.syncPhotos();

        assertThat(species.getCommonName()).isEqualTo("Existing GBIF Name");
    }

    @Test
    void syncPhotos_DoesNotOverrideExistingCommonNameWhenInatHasBlank() {
        SpeciesEntity species = new SpeciesEntity();
        species.setScientificName("Brachypelma hamorii");
        species.setCommonName("Existing GBIF Name");

        InatTaxon taxon = new InatTaxon();
        taxon.setCommonName("   ");
        InatTaxon.InatPhoto photo = new InatTaxon.InatPhoto();
        photo.setMediumUrl("https://inaturalist.org/photos/1/medium.jpg");
        taxon.setDefaultPhoto(photo);

        when(speciesRepository.findAllByImageUrlIsNull()).thenReturn(List.of(species));
        when(inatClient.findByScientificName(any())).thenReturn(Optional.of(taxon));
        when(speciesRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        photoSyncService.syncPhotos();

        assertThat(species.getCommonName()).isEqualTo("Existing GBIF Name");
    }

    @Test
    void syncPhotos_StoresImageAttribution() {
        SpeciesEntity species = new SpeciesEntity();
        species.setScientificName("Brachypelma hamorii");

        InatTaxon taxon = new InatTaxon();
        InatTaxon.InatPhoto photo = new InatTaxon.InatPhoto();
        photo.setMediumUrl("https://inaturalist.org/photos/1/medium.jpg");
        photo.setAttribution("(c) John Smith, some rights reserved (CC BY-NC)");
        taxon.setDefaultPhoto(photo);

        when(speciesRepository.findAllByImageUrlIsNull()).thenReturn(List.of(species));
        when(inatClient.findByScientificName(any())).thenReturn(Optional.of(taxon));
        when(speciesRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        photoSyncService.syncPhotos();

        assertThat(species.getImageAttribution()).isEqualTo("(c) John Smith, some rights reserved (CC BY-NC)");
    }

    @Test
    void syncPhotos_SetsNullAttributionWhenPhotoHasNone() {
        SpeciesEntity species = new SpeciesEntity();
        species.setScientificName("Brachypelma hamorii");

        InatTaxon taxon = new InatTaxon();
        InatTaxon.InatPhoto photo = new InatTaxon.InatPhoto();
        photo.setMediumUrl("https://inaturalist.org/photos/1/medium.jpg");
        photo.setAttribution(null);
        taxon.setDefaultPhoto(photo);

        when(speciesRepository.findAllByImageUrlIsNull()).thenReturn(List.of(species));
        when(inatClient.findByScientificName(any())).thenReturn(Optional.of(taxon));
        when(speciesRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        photoSyncService.syncPhotos();

        assertThat(species.getImageAttribution()).isNull();
    }

    @Test
    void syncPhotos_DoesNotSetAttributionWhenNoPhoto() {
        SpeciesEntity species = new SpeciesEntity();
        species.setScientificName("Brachypelma hamorii");
        species.setImageAttribution("existing attribution");

        InatTaxon taxon = new InatTaxon();
        taxon.setCommonName("Mexican Red Knee");
        taxon.setDefaultPhoto(null);

        when(speciesRepository.findAllByImageUrlIsNull()).thenReturn(List.of(species));
        when(inatClient.findByScientificName(any())).thenReturn(Optional.of(taxon));
        when(speciesRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        photoSyncService.syncPhotos();

        assertThat(species.getImageAttribution()).isEqualTo("existing attribution");
    }
}
