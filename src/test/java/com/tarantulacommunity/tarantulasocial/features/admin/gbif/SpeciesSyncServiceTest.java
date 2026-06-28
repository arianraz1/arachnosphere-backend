package com.tarantulacommunity.tarantulasocial.features.admin.gbif;

import com.tarantulacommunity.tarantulasocial.features.admin.gbif.data.GbifDescription;
import com.tarantulacommunity.tarantulasocial.features.admin.gbif.data.GbifMedia;
import com.tarantulacommunity.tarantulasocial.features.admin.gbif.data.GbifTaxon;
import com.tarantulacommunity.tarantulasocial.features.species.entity.SpeciesEntity;
import com.tarantulacommunity.tarantulasocial.features.species.repository.SpeciesRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpeciesSyncServiceTest {

    @Mock
    private GbifClient gbifClient;

    @Mock
    private SpeciesRepository speciesRepository;

    @InjectMocks
    private SpeciesSyncService speciesSyncService;

    @Test
    void syncAllTarantulas_shouldReturnCount_whenSuccessful() {
        GbifTaxon taxon = new GbifTaxon();
        taxon.setKey(1L);
        taxon.setScientificName("Chromatopelma cyaneopubescens");
        taxon.setVernacularName("Greenbottle Blue");

        when(gbifClient.fetchAllSpeciesByFamily(5650L)).thenReturn(List.of(taxon));
        when(speciesRepository.findByGbifNubKey(1L)).thenReturn(Optional.empty());
        when(speciesRepository.save(any(SpeciesEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        int result = speciesSyncService.syncAllTarantulas();

        assertThat(result).isEqualTo(1);
        verify(speciesRepository).save(any(SpeciesEntity.class));
    }


    @Test
    void syncAllTarantulas_shouldUpdateExistingSpecies_whenAlreadyExists() {
        when(gbifClient.getFamilyKey("Theraphosidae")).thenReturn(Optional.of(5650L));

        GbifTaxon taxon = new GbifTaxon();
        taxon.setKey(1L);
        taxon.setScientificName("Chromatopelma cyaneopubescens");
        taxon.setVernacularName("Greenbottle Blue");
        taxon.setDescriptions(List.of());
        taxon.setMedia(List.of());

        when(gbifClient.fetchAllSpeciesByFamily(5650L)).thenReturn(List.of(taxon));

        SpeciesEntity existingEntity = new SpeciesEntity();
        existingEntity.setId(1L);
        existingEntity.setGbifNubKey(1L);
        existingEntity.setScientificName("Old Name");
        existingEntity.setCommonName("Old Common");
        existingEntity.setImageUrl("old-image.jpg");
        existingEntity.setDescription("Old description");

        when(speciesRepository.findByGbifNubKey(1L)).thenReturn(Optional.of(existingEntity));
        when(speciesRepository.save(any(SpeciesEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        int result = speciesSyncService.syncAllTarantulas();

        assertThat(result).isEqualTo(1);
        verify(speciesRepository).save(any(SpeciesEntity.class));
    }

    @Test
    void syncAllTarantulas_shouldPreserveExistingImageUrl_whenAlreadySet() {
        when(gbifClient.getFamilyKey("Theraphosidae")).thenReturn(Optional.of(5650L));

        GbifTaxon taxon = new GbifTaxon();
        taxon.setKey(1L);
        taxon.setScientificName("Chromatopelma cyaneopubescens");
        taxon.setVernacularName("Greenbottle Blue");
        taxon.setDescriptions(List.of());
        taxon.setMedia(List.of());

        when(gbifClient.fetchAllSpeciesByFamily(5650L)).thenReturn(List.of(taxon));

        SpeciesEntity existingEntity = new SpeciesEntity();
        existingEntity.setId(1L);
        existingEntity.setGbifNubKey(1L);
        existingEntity.setScientificName("Old Name");
        existingEntity.setImageUrl("custom-image.jpg");
        existingEntity.setDescription("Old description");

        when(speciesRepository.findByGbifNubKey(1L)).thenReturn(Optional.of(existingEntity));
        when(speciesRepository.save(any(SpeciesEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        int result = speciesSyncService.syncAllTarantulas();

        assertThat(result).isEqualTo(1);
        verify(speciesRepository).save(any(SpeciesEntity.class));
    }

    @Test
    void syncAllTarantulas_shouldPreserveExistingDescription_whenAlreadySet() {
        when(gbifClient.getFamilyKey("Theraphosidae")).thenReturn(Optional.of(5650L));

        GbifTaxon taxon = new GbifTaxon();
        taxon.setKey(1L);
        taxon.setScientificName("Chromatopelma cyaneopubescens");
        taxon.setVernacularName("Greenbottle Blue");
        taxon.setDescriptions(List.of());
        taxon.setMedia(List.of());

        when(gbifClient.fetchAllSpeciesByFamily(5650L)).thenReturn(List.of(taxon));

        SpeciesEntity existingEntity = new SpeciesEntity();
        existingEntity.setId(1L);
        existingEntity.setGbifNubKey(1L);
        existingEntity.setScientificName("Old Name");
        existingEntity.setImageUrl("old-image.jpg");
        existingEntity.setDescription("Custom description");

        when(speciesRepository.findByGbifNubKey(1L)).thenReturn(Optional.of(existingEntity));
        when(speciesRepository.save(any(SpeciesEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        int result = speciesSyncService.syncAllTarantulas();

        assertThat(result).isEqualTo(1);
        verify(speciesRepository).save(any(SpeciesEntity.class));
    }

    @Test
    void syncAllTarantulas_shouldExtractEnglishDescription_whenAvailable() {
        when(gbifClient.getFamilyKey("Theraphosidae")).thenReturn(Optional.of(5650L));

        GbifDescription englishDesc = new GbifDescription();
        englishDesc.setLanguage("eng");
        englishDesc.setDescription("English description");

        GbifTaxon taxon = new GbifTaxon();
        taxon.setKey(1L);
        taxon.setScientificName("Chromatopelma cyaneopubescens");
        taxon.setVernacularName("Greenbottle Blue");
        taxon.setDescriptions(List.of(englishDesc));
        taxon.setMedia(List.of());

        when(gbifClient.fetchAllSpeciesByFamily(5650L)).thenReturn(List.of(taxon));

        when(speciesRepository.findByGbifNubKey(1L)).thenReturn(Optional.empty());
        when(speciesRepository.save(any(SpeciesEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        int result = speciesSyncService.syncAllTarantulas();

        assertThat(result).isEqualTo(1);
        verify(speciesRepository).save(any(SpeciesEntity.class));
    }

    @Test
    void syncAllTarantulas_shouldExtractStillImage_whenAvailable() {
        when(gbifClient.getFamilyKey("Theraphosidae")).thenReturn(Optional.of(5650L));

        GbifMedia image = new GbifMedia();
        image.setType("StillImage");
        image.setIdentifier("https://example.com/image.jpg");

        GbifTaxon taxon = new GbifTaxon();
        taxon.setKey(1L);
        taxon.setScientificName("Chromatopelma cyaneopubescens");
        taxon.setVernacularName("Greenbottle Blue");
        taxon.setDescriptions(List.of());
        taxon.setMedia(List.of(image));

        when(gbifClient.fetchAllSpeciesByFamily(5650L)).thenReturn(List.of(taxon));

        when(speciesRepository.findByGbifNubKey(1L)).thenReturn(Optional.empty());
        when(speciesRepository.save(any(SpeciesEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        int result = speciesSyncService.syncAllTarantulas();

        assertThat(result).isEqualTo(1);
        verify(speciesRepository).save(any(SpeciesEntity.class));
    }

    @Test
    void syncAllTarantulas_shouldSkipTaxonWithNullKey() {
        when(gbifClient.getFamilyKey("Theraphosidae")).thenReturn(Optional.of(5650L));

        GbifTaxon taxon = new GbifTaxon();
        taxon.setKey(null);
        taxon.setScientificName("Invalid Species");

        when(gbifClient.fetchAllSpeciesByFamily(5650L)).thenReturn(List.of(taxon));

        int result = speciesSyncService.syncAllTarantulas();

        assertThat(result).isEqualTo(0);
    }

    @Test
    void syncAllTarantulas_shouldSkipTaxonWithNullScientificName() {
        when(gbifClient.getFamilyKey("Theraphosidae")).thenReturn(Optional.of(5650L));

        GbifTaxon taxon = new GbifTaxon();
        taxon.setKey(1L);
        taxon.setScientificName(null);

        when(gbifClient.fetchAllSpeciesByFamily(5650L)).thenReturn(List.of(taxon));

        int result = speciesSyncService.syncAllTarantulas();

        assertThat(result).isEqualTo(0);
    }


    @Test
    void syncAllTarantulas_shouldProcessMultipleTaxa() {
        when(gbifClient.getFamilyKey("Theraphosidae")).thenReturn(Optional.of(5650L));

        GbifTaxon taxon1 = new GbifTaxon();
        taxon1.setKey(1L);
        taxon1.setScientificName("Species 1");

        GbifTaxon taxon2 = new GbifTaxon();
        taxon2.setKey(2L);
        taxon2.setScientificName("Species 2");

        when(gbifClient.fetchAllSpeciesByFamily(5650L)).thenReturn(List.of(taxon1, taxon2));

        when(speciesRepository.findByGbifNubKey(anyLong())).thenReturn(Optional.empty());
        when(speciesRepository.save(any(SpeciesEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        int result = speciesSyncService.syncAllTarantulas();

        assertThat(result).isEqualTo(2);
        verify(speciesRepository, times(2)).save(any(SpeciesEntity.class));
    }

    @Test
    void syncAllTarantulas_shouldUseNonEnglishDescription_whenEnglishNotAvailable() {
        when(gbifClient.getFamilyKey("Theraphosidae")).thenReturn(Optional.of(5650L));

        GbifDescription spanishDesc = new GbifDescription();
        spanishDesc.setLanguage("spa");
        spanishDesc.setDescription("Descripción en español");

        GbifTaxon taxon = new GbifTaxon();
        taxon.setKey(1L);
        taxon.setScientificName("Chromatopelma cyaneopubescens");
        taxon.setDescriptions(List.of(spanishDesc));
        taxon.setMedia(List.of());

        when(gbifClient.fetchAllSpeciesByFamily(5650L)).thenReturn(List.of(taxon));
        when(speciesRepository.findByGbifNubKey(1L)).thenReturn(Optional.empty());

        // Capture the saved entity to verify description
        ArgumentCaptor<SpeciesEntity> captor = ArgumentCaptor.forClass(SpeciesEntity.class);
        when(speciesRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        speciesSyncService.syncAllTarantulas();

        SpeciesEntity saved = captor.getValue();
        assertThat(saved.getDescription()).isNull();
    }

    @Test
    void syncAllTarantulas_shouldReturnNullImage_whenOnlyNonStillImageMedia() {
        when(gbifClient.getFamilyKey("Theraphosidae")).thenReturn(Optional.of(5650L));

        GbifMedia sound = new GbifMedia();
        sound.setType("Sound");
        sound.setIdentifier("https://example.com/sound.mp3");

        GbifTaxon taxon = new GbifTaxon();
        taxon.setKey(1L);
        taxon.setScientificName("Chromatopelma cyaneopubescens");
        taxon.setMedia(List.of(sound));

        when(gbifClient.fetchAllSpeciesByFamily(5650L)).thenReturn(List.of(taxon));
        when(speciesRepository.findByGbifNubKey(1L)).thenReturn(Optional.empty());

        ArgumentCaptor<SpeciesEntity> captor = ArgumentCaptor.forClass(SpeciesEntity.class);
        when(speciesRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        speciesSyncService.syncAllTarantulas();

        SpeciesEntity saved = captor.getValue();
        assertThat(saved.getImageUrl()).isNull();
    }

    @Test
    void syncAllTarantulas_shouldPreserveExistingImageUrl() {
        when(gbifClient.getFamilyKey("Theraphosidae")).thenReturn(Optional.of(5650L));

        GbifTaxon taxon = new GbifTaxon();
        taxon.setKey(1L);
        taxon.setScientificName("Chromatopelma cyaneopubescens");

        when(gbifClient.fetchAllSpeciesByFamily(5650L)).thenReturn(List.of(taxon));

        SpeciesEntity existing = new SpeciesEntity();
        existing.setId(1L);
        existing.setGbifNubKey(1L);
        existing.setScientificName("Old Name");
        existing.setImageUrl("custom-image.jpg");
        existing.setDescription("Old description");

        when(speciesRepository.findByGbifNubKey(1L)).thenReturn(Optional.of(existing));

        ArgumentCaptor<SpeciesEntity> captor = ArgumentCaptor.forClass(SpeciesEntity.class);
        when(speciesRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        speciesSyncService.syncAllTarantulas();

        SpeciesEntity saved = captor.getValue();
        assertThat(saved.getImageUrl()).isEqualTo("custom-image.jpg");
        assertThat(saved.getScientificName()).isEqualTo("Chromatopelma cyaneopubescens");
        assertThat(saved.getDescription()).isEqualTo("Old description");
    }

    @Test
    void syncAllTarantulas_shouldReturnCorrectCount_whenSomeTaxaAreSkipped() {
        when(gbifClient.getFamilyKey("Theraphosidae")).thenReturn(Optional.of(5650L));

        GbifTaxon valid = new GbifTaxon();
        valid.setKey(1L);
        valid.setScientificName("Valid Species");

        GbifTaxon invalid = new GbifTaxon();
        invalid.setKey(2L);
        invalid.setScientificName(null); // Skipped

        when(gbifClient.fetchAllSpeciesByFamily(5650L)).thenReturn(List.of(valid, invalid));
        when(speciesRepository.findByGbifNubKey(anyLong())).thenReturn(Optional.empty());
        when(speciesRepository.save(any(SpeciesEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        int result = speciesSyncService.syncAllTarantulas();

        assertThat(result).isEqualTo(1);
    }

    @Test
    void extractBareName_stripsAuthorAndYear() {
        GbifTaxon taxon = new GbifTaxon();
        taxon.setKey(1L);
        taxon.setScientificName("Ischnocolus rubropilosus Keyserling, 1891");

        when(gbifClient.fetchAllSpeciesByFamily(any())).thenReturn(List.of(taxon));
        when(gbifClient.getFamilyKey(any())).thenReturn(Optional.of(5650L));
        when(gbifClient.getVernacularNames(any())).thenReturn(List.of());
        when(speciesRepository.findByGbifNubKey(any())).thenReturn(Optional.empty());
        when(speciesRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        speciesSyncService.syncAllTarantulas();

        ArgumentCaptor<SpeciesEntity> captor = ArgumentCaptor.forClass(SpeciesEntity.class);
        verify(speciesRepository).save(captor.capture());
        assertThat(captor.getValue().getScientificName()).isEqualTo("Ischnocolus rubropilosus");
    }

    @Test
    void extractBareName_handlesSingleWordName() {
        GbifTaxon taxon = new GbifTaxon();
        taxon.setKey(1L);
        taxon.setScientificName("Theraphosidae");

        when(gbifClient.fetchAllSpeciesByFamily(any())).thenReturn(List.of(taxon));
        when(gbifClient.getFamilyKey(any())).thenReturn(Optional.of(5650L));
        when(gbifClient.getVernacularNames(any())).thenReturn(List.of());
        when(speciesRepository.findByGbifNubKey(any())).thenReturn(Optional.empty());
        when(speciesRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        speciesSyncService.syncAllTarantulas();

        ArgumentCaptor<SpeciesEntity> captor = ArgumentCaptor.forClass(SpeciesEntity.class);
        verify(speciesRepository).save(captor.capture());
        assertThat(captor.getValue().getScientificName()).isEqualTo("Theraphosidae");
    }

    @Test
    void syncAllTarantulas_onlySetsEnglishCommonName() {
        GbifTaxon taxon = new GbifTaxon();
        taxon.setKey(1L);
        taxon.setScientificName("Brachypelma hamorii");

        when(gbifClient.fetchAllSpeciesByFamily(any())).thenReturn(List.of(taxon));
        when(gbifClient.getFamilyKey(any())).thenReturn(Optional.of(5650L));
        when(gbifClient.getVernacularNames(any())).thenReturn(List.of("Mexican Red Knee"));
        when(speciesRepository.findByGbifNubKey(any())).thenReturn(Optional.empty());
        when(speciesRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        speciesSyncService.syncAllTarantulas();

        ArgumentCaptor<SpeciesEntity> captor = ArgumentCaptor.forClass(SpeciesEntity.class);
        verify(speciesRepository).save(captor.capture());
        assertThat(captor.getValue().getCommonName()).isEqualTo("Mexican Red Knee");
    }

    @Test
    void syncAllTarantulas_skipsNullKey() {
        GbifTaxon taxon = new GbifTaxon();
        taxon.setKey(null);
        taxon.setScientificName("Brachypelma hamorii");

        when(gbifClient.fetchAllSpeciesByFamily(any())).thenReturn(List.of(taxon));
        when(gbifClient.getFamilyKey(any())).thenReturn(Optional.of(5650L));

        int result = speciesSyncService.syncAllTarantulas();

        verify(speciesRepository, never()).save(any());
        assertThat(result).isZero();
    }

    @Test
    void syncAllTarantulas_DoesNotOverrideInatCommonNameWithGbifName() {
        GbifTaxon taxon = new GbifTaxon();
        taxon.setKey(1L);
        taxon.setScientificName("Brachypelma hamorii");

        SpeciesEntity existing = new SpeciesEntity();
        existing.setGbifNubKey(1L);
        existing.setScientificName("Brachypelma hamorii");
        existing.setCommonName("Mexican Red Knee"); // already set by iNat

        when(gbifClient.getFamilyKey(any())).thenReturn(Optional.of(5650L));
        when(gbifClient.fetchAllSpeciesByFamily(any())).thenReturn(List.of(taxon));
        when(gbifClient.getVernacularNames(1L)).thenReturn(List.of("Some GBIF Name"));
        when(speciesRepository.findByGbifNubKey(1L)).thenReturn(Optional.of(existing));
        when(speciesRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        speciesSyncService.syncAllTarantulas();

        ArgumentCaptor<SpeciesEntity> captor = ArgumentCaptor.forClass(SpeciesEntity.class);
        verify(speciesRepository).save(captor.capture());
        assertThat(captor.getValue().getCommonName()).isEqualTo("Mexican Red Knee");
    }

    @Test
    void syncAllTarantulas_excludesGenericFamilyCommonNames() {
        GbifTaxon taxon = new GbifTaxon();
        taxon.setKey(1L);
        taxon.setScientificName("Brachypelma hamorii");

        when(gbifClient.getFamilyKey(any())).thenReturn(Optional.of(5650L));
        when(gbifClient.fetchAllSpeciesByFamily(any())).thenReturn(List.of(taxon));
        when(gbifClient.getVernacularNames(1L)).thenReturn(List.of("tarantula spiders"));
        when(speciesRepository.findByGbifNubKey(1L)).thenReturn(Optional.empty());
        when(speciesRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        speciesSyncService.syncAllTarantulas();

        ArgumentCaptor<SpeciesEntity> captor = ArgumentCaptor.forClass(SpeciesEntity.class);
        verify(speciesRepository).save(captor.capture());
        assertThat(captor.getValue().getCommonName()).isNull();
    }

    @Test
    void syncAllTarantulas_excludesTarantulasCommonName() {
        GbifTaxon taxon = new GbifTaxon();
        taxon.setKey(1L);
        taxon.setScientificName("Brachypelma hamorii");

        when(gbifClient.getFamilyKey(any())).thenReturn(Optional.of(5650L));
        when(gbifClient.fetchAllSpeciesByFamily(any())).thenReturn(List.of(taxon));
        when(gbifClient.getVernacularNames(1L)).thenReturn(List.of("tarantulas"));
        when(speciesRepository.findByGbifNubKey(1L)).thenReturn(Optional.empty());
        when(speciesRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        speciesSyncService.syncAllTarantulas();

        ArgumentCaptor<SpeciesEntity> captor = ArgumentCaptor.forClass(SpeciesEntity.class);
        verify(speciesRepository).save(captor.capture());
        assertThat(captor.getValue().getCommonName()).isNull();
    }

    @Test
    void syncAllTarantulas_excludesTarantulaCommonName() {
        GbifTaxon taxon = new GbifTaxon();
        taxon.setKey(1L);
        taxon.setScientificName("Brachypelma hamorii");

        when(gbifClient.getFamilyKey(any())).thenReturn(Optional.of(5650L));
        when(gbifClient.fetchAllSpeciesByFamily(any())).thenReturn(List.of(taxon));
        when(gbifClient.getVernacularNames(1L)).thenReturn(List.of("tarantula"));
        when(speciesRepository.findByGbifNubKey(1L)).thenReturn(Optional.empty());
        when(speciesRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        speciesSyncService.syncAllTarantulas();

        ArgumentCaptor<SpeciesEntity> captor = ArgumentCaptor.forClass(SpeciesEntity.class);
        verify(speciesRepository).save(captor.capture());
        assertThat(captor.getValue().getCommonName()).isNull();
    }

    @Test
    void syncAllTarantulas_excludesIsCaseInsensitive() {
        GbifTaxon taxon = new GbifTaxon();
        taxon.setKey(1L);
        taxon.setScientificName("Brachypelma hamorii");

        when(gbifClient.getFamilyKey(any())).thenReturn(Optional.of(5650L));
        when(gbifClient.fetchAllSpeciesByFamily(any())).thenReturn(List.of(taxon));
        when(gbifClient.getVernacularNames(1L)).thenReturn(List.of("Tarantula Spiders"));
        when(speciesRepository.findByGbifNubKey(1L)).thenReturn(Optional.empty());
        when(speciesRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        speciesSyncService.syncAllTarantulas();

        ArgumentCaptor<SpeciesEntity> captor = ArgumentCaptor.forClass(SpeciesEntity.class);
        verify(speciesRepository).save(captor.capture());
        assertThat(captor.getValue().getCommonName()).isNull();
    }

    @Test
    void syncAllTarantulas_allowsLegitimateCommonName() {
        GbifTaxon taxon = new GbifTaxon();
        taxon.setKey(1L);
        taxon.setScientificName("Brachypelma hamorii");

        when(gbifClient.getFamilyKey(any())).thenReturn(Optional.of(5650L));
        when(gbifClient.fetchAllSpeciesByFamily(any())).thenReturn(List.of(taxon));
        when(gbifClient.getVernacularNames(1L)).thenReturn(List.of("Mexican Red Knee"));
        when(speciesRepository.findByGbifNubKey(1L)).thenReturn(Optional.empty());
        when(speciesRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        speciesSyncService.syncAllTarantulas();

        ArgumentCaptor<SpeciesEntity> captor = ArgumentCaptor.forClass(SpeciesEntity.class);
        verify(speciesRepository).save(captor.capture());
        assertThat(captor.getValue().getCommonName()).isEqualTo("Mexican Red Knee");
    }
}
