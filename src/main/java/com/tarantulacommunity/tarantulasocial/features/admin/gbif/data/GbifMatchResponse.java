package com.tarantulacommunity.tarantulasocial.features.admin.gbif.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GbifMatchResponse {
    private Long usageKey;
    private String scientificName;
    private String rank;
    private String matchType; // "EXACT", "FUZZY", "NONE"
}