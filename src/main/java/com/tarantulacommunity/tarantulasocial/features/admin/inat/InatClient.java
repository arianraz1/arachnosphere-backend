package com.tarantulacommunity.tarantulasocial.features.admin.inat;

import com.tarantulacommunity.tarantulasocial.features.admin.inat.data.InatSearchResponse;
import com.tarantulacommunity.tarantulasocial.features.admin.inat.data.InatTaxon;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
@Slf4j
public class InatClient {

    private static final String INAT_API_URL = "https://api.inaturalist.org/v1";

    private final RestClient restClient;

    public InatClient() {
        this.restClient = RestClient.builder()
                .baseUrl(INAT_API_URL)
                .defaultHeader("Accept", "application/json")
                .defaultHeader("User-Agent", "TarantulaSocial/1.0")
                .build();
    }

    /**
     * Looks up a species by exact scientific name and returns the best-matching taxon.
     * Filters to rank=species to avoid subfamily/genus noise.
     */
    public Optional<InatTaxon> findByScientificName(String scientificName) {
        try {
            InatSearchResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/taxa")
                            .queryParam("q", scientificName)
                            .queryParam("rank", "species")
                            .queryParam("per_page", 5)
                            .build())
                    .retrieve()
                    .body(InatSearchResponse.class);

            if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
                log.debug("No iNaturalist results for: {}", scientificName);
                return Optional.empty();
            }

            return response.getResults().stream()
                    .filter(t -> scientificName.equalsIgnoreCase(t.getScientificName()))
                    .findFirst()
                    .or(() -> Optional.of(response.getResults().getFirst()));

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 429) {
                throw e; // let caller handle backoff
            }
            log.error("iNaturalist lookup failed for '{}': {}", scientificName, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("iNaturalist lookup failed for '{}': {}", scientificName, e.getMessage());
            return Optional.empty();
        }
    }
}