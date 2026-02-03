package com.hamming.data.collection.terminal.sync.ware.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public record WareDto(
        @JsonProperty("ware_title")
        String title,

        @JsonProperty("ware_1c_code")
        String code1c,

        @JsonProperty("ware_is_weighty")
        boolean weighty,

        @JsonProperty("ware_price")
        Integer  price
) {
}
