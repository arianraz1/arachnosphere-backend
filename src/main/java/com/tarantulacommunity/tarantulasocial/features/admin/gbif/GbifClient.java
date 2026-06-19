package com.tarantulacommunity.tarantulasocial.features.admin.gbif;

import com.tarantulacommunity.tarantulasocial.features.admin.gbif.data.GbifMatchResponse;
import com.tarantulacommunity.tarantulasocial.features.admin.gbif.data.GbifSearchResponse;
import com.tarantulacommunity.tarantulasocial.features.admin.gbif.data.GbifTaxon;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class GbifClient {

    private static final String GBIF_API_URL = "https://api.gbif.org/v1";
    private static final int PAGE_SIZE = 300;

    private final RestClient restClient;

    public GbifClient() {
        this.restClient = RestClient.builder()
                .baseUrl(GBIF_API_URL)
                .defaultHeader("Accept", "application/json")
                .defaultHeader("User-Agent", "TarantulaSocial/1.0")
                .build();
    }

    /**
     * Resolves the GBIF usageKey for a family name e.g. "Theraphosidae"
     */
    public Optional<Long> getFamilyKey(String familyName) {
        try {
            GbifMatchResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/species/match")
                            .queryParam("name", familyName)
                            .queryParam("rank", "FAMILY")
                            .queryParam("verbose", false)
                            .build())
                    .retrieve()
                    .body(GbifMatchResponse.class);

            if (response == null || response.getUsageKey() == null
                    || "NONE".equals(response.getMatchType())) {
                log.warn("No GBIF match found for family: {}", familyName);
                return Optional.empty();
            }

            log.info("Resolved family key {} for '{}'", response.getUsageKey(), familyName);
            return Optional.of(response.getUsageKey());

        } catch (Exception e) {
            log.error("Failed to resolve family key for '{}': {}", familyName, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Paginates through all accepted species under a family key.
     * Uses the search endpoint directly — no per-species detail calls needed.
     */
    public List<GbifTaxon> fetchAllSpeciesByFamily(Long familyKey) {
        List<GbifTaxon> all = new ArrayList<>();
        int offset = 0;

        while (true) {
            int currentOffset = offset;
            GbifSearchResponse response;
            try {
                response = restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/species/search")
                                .queryParam("higherTaxonKey", familyKey)
                                .queryParam("rank", "SPECIES")
                                .queryParam("status", "ACCEPTED")
                                .queryParam("limit", PAGE_SIZE)
                                .queryParam("offset", currentOffset)
                                .build())
                        .retrieve()
                        .body(GbifSearchResponse.class);
            } catch (Exception e) {
                log.error("GBIF request failed at offset {}: {}", offset, e.getMessage());
                break;
            }

            if (response == null || response.getResults() == null
                    || response.getResults().isEmpty()) {
                break;
            }

            all.addAll(response.getResults());
            log.info("Fetched {} species (offset: {}, total so far: {})",
                    response.getResults().size(), offset, all.size());

            if (response.isEndOfRecords()) break;

            offset += PAGE_SIZE;
        }

        return all;
    }
}