package com.tarantulacommunity.tarantulasocial.features.admin.inat.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InatSearchResponse {
    private int total_results;
    private List<InatTaxon> results;
}