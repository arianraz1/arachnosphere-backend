package com.tarantulacommunity.tarantulasocial.features.species.service;

import com.tarantulacommunity.tarantulasocial.exception.SpeciesNotFoundException;
import com.tarantulacommunity.tarantulasocial.features.species.dto.response.SpeciesResponse;
import com.tarantulacommunity.tarantulasocial.features.species.dto.response.SpeciesSummaryResponse;
import com.tarantulacommunity.tarantulasocial.features.species.entity.SpeciesEntity;
import com.tarantulacommunity.tarantulasocial.features.species.mapper.SpeciesMapper;
import com.tarantulacommunity.tarantulasocial.features.species.repository.SpeciesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpeciesService {

    private final SpeciesRepository speciesRepository;
    private final SpeciesMapper speciesMapper;

    public SpeciesResponse getSpeciesById(Long id) {
        return speciesRepository.findById(id)
                .map(speciesMapper::toResponse)
                .orElseThrow(() -> new SpeciesNotFoundException(id));
    }

    public SpeciesResponse getSpeciesByGbifNubKey(Long gbifKey) {
        return speciesRepository.findByGbifNubKey(gbifKey)
                .map(speciesMapper::toResponse)
                .orElseThrow(() -> new SpeciesNotFoundException(gbifKey));
    }

    public Page<SpeciesSummaryResponse> searchOwnedSpecies(String query, Pageable pageable) {
        return speciesRepository.findOwnedSpeciesByQuery(query, pageable)
                .map(speciesMapper::toSummaryResponse);
    }

    public Page<SpeciesSummaryResponse> searchUnownedSpecies(String query, Pageable pageable) {
        return speciesRepository.findUnownedSpeciesByQuery(query, pageable)
                .map(speciesMapper::toSummaryResponse);
    }

    @Transactional
    public SpeciesResponse saveFromGbif(Long gbifNubKey, String scientificName,
                                        String commonName, String imageUrl, String description) {
        return speciesRepository.findByGbifNubKey(gbifNubKey)
                .map(speciesMapper::toResponse)
                .orElseGet(() -> {
                    SpeciesEntity species = new SpeciesEntity();
                    species.setGbifNubKey(gbifNubKey);
                    species.setScientificName(scientificName);
                    species.setCommonName(commonName);
                    species.setImageUrl(imageUrl);
                    species.setDescription(description);

                    return speciesMapper.toResponse(speciesRepository.save(species));
                });
    }
}
