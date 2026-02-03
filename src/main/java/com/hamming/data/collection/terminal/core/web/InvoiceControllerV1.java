package com.hamming.data.collection.terminal.core.web;

import com.hamming.data.collection.terminal.core.dto.request.AddNewProductItemRequest;
import com.hamming.data.collection.terminal.core.dto.request.CreateNewInvoiceRequest;
import com.hamming.data.collection.terminal.core.dto.request.DeleteProductItemOnInvoiceRequest;
import com.hamming.data.collection.terminal.core.dto.request.UpdateProductItemRequestOnInvoice;
import com.hamming.data.collection.terminal.core.dto.response.InvoiceResponse;
import com.hamming.data.collection.terminal.core.dto.response.ProductItemResponse;
import com.hamming.data.collection.terminal.core.service.InvoiceService;
import com.hamming.data.collection.terminal.core.service.ProductItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/invoices")
@Tag(name = "Накладные", description = "API для работы с накладными")
public class InvoiceControllerV1 {

    private final InvoiceService invoiceService;
    private final ProductItemService productItemService;

    // ==================== INVOICE ENDPOINTS ====================

    @PostMapping
    @Operation(summary = "Создать новую накладную")
    public ResponseEntity<InvoiceResponse> createNewInvoice(
//            @RequestBody CreateNewInvoiceRequest request
    ) {
        log.info("REST: Запрос на создание новой накладной");
        InvoiceResponse response = invoiceService.createNewInvoice();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{invoiceNumber}")
    @Operation(summary = "Получить накладную по номеру")
    public ResponseEntity<InvoiceResponse> getInvoiceByNumber(
            @PathVariable Long invoiceNumber
    ) {
        log.info("REST: Запрос накладной {}", invoiceNumber);
        InvoiceResponse response = invoiceService.getInvoiceByNumber(invoiceNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Получить все накладные")
    public ResponseEntity<List<InvoiceResponse>> getAllInvoices() {
        log.info("REST: Запрос всех накладных");
        List<InvoiceResponse> response = invoiceService.getAllInvoices();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{invoiceNumber}/send")
    @Operation(summary = "Отправить накладную в 1С")
    public ResponseEntity<InvoiceResponse> sendInvoiceTo1C(
            @PathVariable Long invoiceNumber
    ) {
        log.info("REST: Запрос на отправку накладной {} в 1С", invoiceNumber);
        InvoiceResponse response = invoiceService.sendInvoiceTo1C(invoiceNumber);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{invoiceNumber}")
    @Operation(summary = "Удалить накладную")
    public ResponseEntity<Void> deleteInvoice(
            @PathVariable Long invoiceNumber
    ) {
        log.info("REST: Запрос на удаление накладной {}", invoiceNumber);
        invoiceService.deleteInvoice(invoiceNumber);
        return ResponseEntity.noContent().build();
    }

    // ==================== PRODUCT ITEM ENDPOINTS ====================

    @PostMapping("/{invoiceNumber}/items")
    @Operation(summary = "Добавить позицию в накладную")
    public ResponseEntity<ProductItemResponse> addProductItem(
            @PathVariable Long invoiceNumber,
            @Valid @RequestBody AddNewProductItemRequest request
    ) {
        log.info("REST: Добавление позиции в накладную {}: {}", invoiceNumber, request);
        ProductItemResponse response = productItemService.addProductItem(invoiceNumber, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

//    @PostMapping("/{invoiceId}/items")
//    @Operation(summary = "Добавить позицию в накладную")
//    public ResponseEntity<ProductItemResponse> addProduct(
//            @PathVariable Long invoiceNumber,
//            @Valid @RequestBody AddNewProductItemRequest request
//    ) {
//        log.info("REST: Добавление позиции в накладную {}: {}", invoiceNumber, request);
//        ProductItemResponse response = productItemService.addProductItem(invoiceNumber, request);
//        return ResponseEntity.status(HttpStatus.CREATED).body(response);
//    }

    @GetMapping("/{invoiceNumber}/items")
    @Operation(summary = "Получить все позиции накладной")
    public ResponseEntity<List<ProductItemResponse>> getAllProductItems(
            @PathVariable Long invoiceNumber
    ) {
        log.info("REST: Запрос всех позиций накладной {}", invoiceNumber);
        List<ProductItemResponse> response = productItemService.getAllProductItemsByInvoiceNumber(invoiceNumber);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{invoiceNumber}/items")
    @Operation(summary = "Изменить позицию в накладной")
    public ResponseEntity<ProductItemResponse> updateProductItem(
            @PathVariable Long invoiceNumber,
            @Valid @RequestBody UpdateProductItemRequestOnInvoice request
    ) {
        log.info("REST: Обновление позиции в накладной {}: {}", invoiceNumber, request);
        ProductItemResponse response = productItemService.updateProductItem(invoiceNumber, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{invoiceNumber}/items")
    @Operation(summary = "Удалить позицию из накладной")
    public ResponseEntity<Void> deleteProductItem(
            @PathVariable Long invoiceNumber,
            @Valid @RequestBody DeleteProductItemOnInvoiceRequest request
    ) {
        log.info("REST: Удаление позиции {} из накладной {}", request.productItemId(), invoiceNumber);
        productItemService.deleteProductItem(invoiceNumber, request.productItemId());
        return ResponseEntity.noContent().build();
    }
}
