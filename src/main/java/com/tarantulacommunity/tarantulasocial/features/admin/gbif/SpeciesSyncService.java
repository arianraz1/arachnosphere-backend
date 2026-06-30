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
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpeciesSyncService {

    private static final String FAMILY_NAME = "Theraphosidae";
    private static final Long FALLBACK_FAMILY_KEY = 5650L; // Stable fallback

    private final GbifClient gbifClient;
    private final SpeciesRepository speciesRepository;

    private static final Set<String> EXCLUDED_COMMON_NAMES = Set.of(
            "tarantula spiders", "tarantulas", "tarantula", "brushed trapdoor spiders", "fossorial tarantula"
    );

    /**
     * Fetches all accepted Theraphosidae species from GBIF and upserts them.
     * Resolves the family key dynamically, with a fallback to the hardcoded key.
     */
    @Transactional
    public int syncAllTarantulas() {
        Long familyKey = resolveFamilyKey()
                .orElseGet(() -> {
                    log.warn("Falling back to hardcoded family key: {}", FALLBACK_FAMILY_KEY);
                    return FALLBACK_FAMILY_KEY;
                });

        log.info("Syncing species for family: {} (key: {})", FAMILY_NAME, familyKey);

        List<GbifTaxon> taxa = gbifClient.fetchAllSpeciesByFamily(familyKey);
        log.info("Fetched {} taxa from GBIF", taxa.size());

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
            entity.setScientificName(extractBareName(taxon.getScientificName()));

            List<String> names = gbifClient.getVernacularNames(taxon.getKey());
            if (!names.isEmpty() && entity.getCommonName() == null) {
                String name = names.getFirst();
                if (!EXCLUDED_COMMON_NAMES.contains(name.toLowerCase())) {
                    entity.setCommonName(name);
                }
            }

            speciesRepository.save(entity);
            saved++;
        }

        log.info("Sync complete — saved: {}, skipped: {}", saved, skipped);
        return saved;
    }

    /**
     * Resolves the GBIF usageKey for Theraphosidae.
     * Returns empty if the API call fails or returns no key.
     */
    private Optional<Long> resolveFamilyKey() {
        try {
            return gbifClient.getFamilyKey(FAMILY_NAME);
        } catch (Exception e) {
            log.error("Exception while resolving family key: {}", e.getMessage());
            return Optional.empty();
        }
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

    private String extractBareName(String scientificName) {
        String[] parts = scientificName.split("\\s+");
        if (parts.length < 2) return scientificName;
        return parts[0] + " " + parts[1];
    }
}