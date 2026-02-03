package com.hamming.data.collection.terminal.core.dto.response;

import java.util.UUID;

public record ProductItemResponse(
        UUID id,
        UUID invoiceId,
        Long invoiceNumber,
        String barcode,
        String wareTitle,
        String wareCode1c,
        Integer quantity,
        Integer unitPrice,
        Integer totalPrice,
        Boolean isWeighty
) {
}
