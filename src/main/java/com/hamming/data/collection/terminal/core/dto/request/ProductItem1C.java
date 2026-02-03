package com.hamming.data.collection.terminal.core.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductItem1C(
        @JsonProperty("ware_1c_code")
        String wareCode1c,

        @JsonProperty("barcode")
        String barcode,

        @JsonProperty("ware_title")
        String wareTitle,

        @JsonProperty("quantity")
        Integer quantity,

        @JsonProperty("unit_price")
        Integer unitPrice,

        @JsonProperty("total_price")
        Integer totalPrice,

        @JsonProperty("is_weighty")
        Boolean isWeighty
) {}