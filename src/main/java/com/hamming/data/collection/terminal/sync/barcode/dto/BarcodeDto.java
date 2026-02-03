package com.hamming.data.collection.terminal.sync.barcode.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BarcodeDto(
    @JsonProperty("ware_1c_code")
    String code1c,
    @JsonProperty("ware_barcode")
    String barcode
) {
}
