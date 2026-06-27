package com.tarantulacommunity.tarantulasocial.features.admin.gbif.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GbifVernacular {
    private String vernacularName;
    private String language;
    private Boolean preferred;
}
