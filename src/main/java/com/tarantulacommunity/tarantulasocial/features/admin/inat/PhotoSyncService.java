package com.tarantulacommunity.tarantulasocial.features.admin.inat;

import com.tarantulacommunity.tarantulasocial.features.admin.inat.data.InatTaxon;
import com.tarantulacommunity.tarantulasocial.features.species.entity.SpeciesEntity;
import com.tarantulacommunity.tarantulasocial.features.species.repository.SpeciesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhotoSyncService {

    private final InatClient inatClient;
    private final SpeciesRepository speciesRepository;

    /**
     * For every species without a photo, attempts to fetch one from iNaturalist.
     * Safe to re-run — skips species that already have an imageUrl set.
     */
    @Transactional
    public void syncPhotos() {
        syncPhotos(speciesRepository.findAllByImageUrlIsNull());
    }

    /**
     * Re-syncs photos for all species, including those that already have one.
     * Use to pick up improved default photos from iNaturalist.
     */
    @Transactional
    public void syncAllPhotos() {
        syncPhotos(speciesRepository.findAll());
    }

    private void syncPhotos(List<SpeciesEntity> species) {
        log.info("Syncing photos for {} species", species.size());

        int photosUpdated = 0;
        int namesUpdated = 0;
        int notFound = 0;
        long sleepMs = 1000;

        for (SpeciesEntity s : species) {
            try {
                Optional<InatTaxon> taxon = inatClient.findByScientificName(s.getScientificName());

                if (taxon.isPresent()) {
                    InatTaxon inat = taxon.get();

                    if (inat.getCommonName() != null && !inat.getCommonName().isBlank()) {
                        s.setCommonName(inat.getCommonName());
                        namesUpdated++;
                    }

                    if (inat.getDefaultPhoto() != null) {
                        String mediumUrl = inat.getDefaultPhoto().getMediumUrl();
                        if (mediumUrl != null) {
                            s.setImageUrl(mediumUrl.replace("/medium.", "/original."));
                            s.setImageAttribution(inat.getDefaultPhoto().getAttribution());
                            photosUpdated++;
                        }
                    }

                    speciesRepository.save(s);
                } else {
                    notFound++;
                    log.debug("No iNat data found for: {}", s.getScientificName());
                }

                sleepMs = 1000;

            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().value() == 429) {
                    sleepMs = Math.min(sleepMs * 2, 30000);
                    log.warn("Rate limited by iNaturalist, backing off to {}ms", sleepMs);
                } else {
                    log.error("HTTP error for '{}': {}", s.getScientificName(), e.getMessage());
                }
            }

            try {
                Thread.sleep(sleepMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        log.info("Photo sync complete — photos: {}, names: {}, not found: {}", photosUpdated, namesUpdated, notFound);
    }
}