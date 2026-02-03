package com.hamming.data.collection.terminal.core.dto.response;

import com.hamming.data.collection.terminal.core.model.enums.InvoiceStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        Long invoiceNumber,
        InvoiceStatus status,
        Integer totalAmount,
        Integer itemsCount,
        LocalDateTime sentAt,
        LocalDateTime createdAt,
        List<ProductItemResponse> items
) {
}
