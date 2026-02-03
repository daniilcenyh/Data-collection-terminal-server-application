package com.hamming.data.collection.terminal.core.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Invoice1CRequest(
        @JsonProperty("invoice_number")
        Long invoiceNumber,

        @JsonProperty("total_amount")
        Integer totalAmount,

        @JsonProperty("items_count")
        Integer itemsCount,

        @JsonProperty("items")
        List<ProductItem1C> items
) {}