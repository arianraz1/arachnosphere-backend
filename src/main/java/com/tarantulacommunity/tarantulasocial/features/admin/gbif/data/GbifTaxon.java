package com.tarantulacommunity.tarantulasocial.features.admin.gbif.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GbifTaxon {
    private Long key;
    private String scientificName;
    private String vernacularName;
    private List<GbifDescription> descriptions;
    private List<GbifMedia> media;
}