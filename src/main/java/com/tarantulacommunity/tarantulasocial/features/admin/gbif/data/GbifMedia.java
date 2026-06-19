package com.tarantulacommunity.tarantulasocial.features.admin.gbif.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GbifMedia {
    private String type;       // "StillImage"
    private String identifier; // image URL
}