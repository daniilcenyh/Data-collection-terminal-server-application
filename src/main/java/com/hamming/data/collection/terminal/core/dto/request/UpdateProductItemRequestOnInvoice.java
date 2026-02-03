package com.hamming.data.collection.terminal.core.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateProductItemRequestOnInvoice(
        @NotNull(message = "ID позиции обязателен")
        UUID productItemId,

        @Min(value = 1, message = "Количество должно быть больше 0")
        Integer quantity
) {
}
