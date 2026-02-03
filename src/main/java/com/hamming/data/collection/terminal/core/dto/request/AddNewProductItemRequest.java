package com.hamming.data.collection.terminal.core.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AddNewProductItemRequest(
        @NotBlank(message = "Штрихкод обязателен")
        String barcode,

        @Min(value = 1, message = "Количество должно быть больше 0")
        Integer quantity
) {
}
