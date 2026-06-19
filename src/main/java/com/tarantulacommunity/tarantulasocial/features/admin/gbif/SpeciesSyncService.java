package com.tarantulacommunity.tarantulasocial.features.admin.gbif;

import com.tarantulacommunity.tarantulasocial.features.admin.gbif.data.GbifDescription;
import com.tarantulacommunity.tarantulasocial.features.admin.gbif.data.GbifMedia;
import com.tarantulacommunity.tarantulasocial.features.admin.gbif.data.GbifTaxon;
import com.tarantulacommunity.tarantulasocial.features.species.entity.SpeciesEntity;
import com.tarantulacommunity.tarantulasocial.features.species.repository.SpeciesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpeciesSyncService {

    private static final String FAMILY_NAME = "Theraphosidae";

    private final GbifClient gbifClient;
    private final SpeciesRepository speciesRepository;

    /**
     * Fetches all accepted Theraphosidae species from GBIF and upserts them
     * into the local species_entries table.
     *
     * Safe to run multiple times — existing records are updated, not duplicated.
     * Triggered manually via the admin endpoint, not on a schedule.
     */
    @Transactional
    public int syncAllTarantulas() {
        Long familyKey = gbifClient.getFamilyKey(FAMILY_NAME)
                .orElseThrow(() -> new IllegalStateException(
                        "Could not resolve GBIF family key for: " + FAMILY_NAME));

        List<GbifTaxon> taxa = gbifClient.fetchAllSpeciesByFamily(familyKey);
        log.info("Fetched {} taxa from GBIF for syncing", taxa.size());

        int saved = 0;
        int skipped = 0;

        for (GbifTaxon taxon : taxa) {
            if (taxon.getKey() == null || taxon.getScientificName() == null) {
                skipped++;
                continue;
            }

            SpeciesEntity entity = speciesRepository
                    .findByGbifNubKey(taxon.getKey())
                    .orElseGet(SpeciesEntity::new);

            entity.setGbifNubKey(taxon.getKey());
            entity.setScientificName(taxon.getScientificName());
            entity.setCommonName(taxon.getVernacularName());

            // Only overwrite imageUrl if we don't already have one —
            // lets you manually set better images without them being clobbered on re-sync
            if (entity.getImageUrl() == null) {
                entity.setImageUrl(extractImageUrl(taxon));
            }

            // Only overwrite description if we don't already have one —
            // preserves any custom descriptions you've written for notable species
            if (entity.getDescription() == null) {
                entity.setDescription(extractDescription(taxon));
            }

            speciesRepository.save(entity);
            saved++;
        }

        log.info("Sync complete — saved: {}, skipped: {}", saved, skipped);
        return saved;
    }

    private String extractDescription(GbifTaxon taxon) {
        if (taxon.getDescriptions() == null) return null;
        return taxon.getDescriptions().stream()
                .filter(d -> "eng".equals(d.getLanguage()))
                .map(GbifDescription::getDescription)
                .findFirst()
                .orElse(null);
    }

    private String extractImageUrl(GbifTaxon taxon) {
        if (taxon.getMedia() == null) return null;
        return taxon.getMedia().stream()
                .filter(m -> "StillImage".equals(m.getType()))
                .map(GbifMedia::getIdentifier)
                .findFirst()
                .orElse(null);
    }
}