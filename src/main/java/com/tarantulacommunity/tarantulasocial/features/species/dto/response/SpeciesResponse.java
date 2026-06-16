package com.tarantulacommunity.tarantulasocial.features.species.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpeciesResponse {

    private Long id;
    private Long gbifNubKey;
    private String scientificName;
    private String commonName;
    private String imageUrl;
    private String description;
}