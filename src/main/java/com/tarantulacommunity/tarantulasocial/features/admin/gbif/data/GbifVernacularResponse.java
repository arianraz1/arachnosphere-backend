package com.tarantulacommunity.tarantulasocial.features.admin.gbif.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GbifVernacularResponse {
    private List<GbifVernacular> results;
    private boolean endOfRecords;
    private int limit;
    private int offset;
}