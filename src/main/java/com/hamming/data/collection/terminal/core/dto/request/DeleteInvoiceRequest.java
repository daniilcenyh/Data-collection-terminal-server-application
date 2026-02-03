package com.hamming.data.collection.terminal.core.dto.request;

import jakarta.validation.constraints.NotNull;


public record DeleteInvoiceRequest(
        @NotNull(message = "Номер накладной обязателен")
        Long invoiceNumber
) {
}
