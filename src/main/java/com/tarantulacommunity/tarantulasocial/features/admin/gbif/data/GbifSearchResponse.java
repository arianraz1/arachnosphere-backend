package com.tarantulacommunity.tarantulasocial.features.admin.gbif.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GbifSearchResponse {
    private List<GbifTaxon> results;
    private long count;
    private int limit;
    private int offset;
    private boolean endOfRecords;
}