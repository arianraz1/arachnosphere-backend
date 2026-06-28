package com.tarantulacommunity.tarantulasocial.features.admin.inat.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InatTaxon {

    private Long id;

    @JsonProperty("name")
    private String scientificName;

    @JsonProperty("preferred_common_name")
    private String commonName;

    @JsonProperty("default_photo")
    private InatPhoto defaultPhoto;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InatPhoto {
        @JsonProperty("medium_url")
        private String mediumUrl;

        @JsonProperty("attribution")
        private String attribution;
    }
}