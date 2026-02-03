package com.hamming.data.collection.terminal.core.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DeleteProductItemOnInvoiceRequest(
        @NotNull(message = "ID позиции обязателен")
        UUID productItemId
) {
}
