package com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CollectionImageResponse {

    private Long id;
    private String url;
    private String caption;
    private boolean featured;
    private int displayOrder;
}