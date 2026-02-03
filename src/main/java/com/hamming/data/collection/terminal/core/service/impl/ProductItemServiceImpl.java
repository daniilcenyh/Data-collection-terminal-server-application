package com.hamming.data.collection.terminal.core.service.impl;

import com.hamming.data.collection.terminal.core.dto.request.AddNewProductItemRequest;
import com.hamming.data.collection.terminal.core.dto.request.UpdateProductItemRequestOnInvoice;
import com.hamming.data.collection.terminal.core.dto.response.ProductItemResponse;
import com.hamming.data.collection.terminal.core.model.Invoice;
import com.hamming.data.collection.terminal.core.model.ProductItem;
import com.hamming.data.collection.terminal.core.model.enums.InvoiceStatus;
import com.hamming.data.collection.terminal.core.repository.ProductItemRepository;
import com.hamming.data.collection.terminal.core.service.InvoiceService;
import com.hamming.data.collection.terminal.core.service.ProductItemService;
import com.hamming.data.collection.terminal.exception.BarcodeNotFoundException;
import com.hamming.data.collection.terminal.exception.ProductItemNotFoundException;
import com.hamming.data.collection.terminal.sync.barcode.model.Barcode;
import com.hamming.data.collection.terminal.sync.barcode.repository.BarcodeRepository;
import com.hamming.data.collection.terminal.sync.barcode.service.BarcodeService;
import com.hamming.data.collection.terminal.sync.ware.model.Ware;
import com.hamming.data.collection.terminal.sync.ware.repository.WareRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductItemServiceImpl implements ProductItemService {

    private final ProductItemRepository productItemRepository;
    private final BarcodeRepository barcodeRepository;
    private final BarcodeService barcodeService;
    private final WareRepository wareRepository;
    private final InvoiceService invoiceService;

    @Override
    @Transactional
    public ProductItemResponse addProductItem(Long invoiceNumber, AddNewProductItemRequest request) {
        log.info("Добавление позиции в накладную {}: штрихкод={}, количество={}",
                invoiceNumber, request.barcode(), request.quantity());

        Invoice invoice = invoiceService.findByInvoiceNumberOrThrow(invoiceNumber);

        if (invoice.getStatus() == InvoiceStatus.SENT) {
            throw new IllegalStateException("Нельзя изменять отправленную накладную");
        }

        // Найти штрихкод
        Barcode barcode = barcodeService.findByBarcode(request.barcode())
                .orElseThrow(() -> new BarcodeNotFoundException(
                        "Штрихкод " + request.barcode() + " не найден в базе"
                ));

        // Найти товар по коду 1С из штрихкода
        Ware ware = wareRepository.findByCode1c(barcode.getWare1cCode())
                .orElseThrow(() -> new RuntimeException(
                        "Товар с кодом 1С " + barcode.getWare1cCode() + " не найден"
                ));

        // Проверить, есть ли уже такой товар в накладной
        var existingItem = productItemRepository.findByInvoiceIdAndBarcodeId(
                invoice.getId(), barcode.getId()
        );

        ProductItem productItem;

        if (existingItem.isPresent()) {
            // Если товар уже есть, увеличиваем количество
            productItem = existingItem.get();
            productItem.setQuantity(productItem.getQuantity() + request.quantity());
            productItem.setLastUpdated(LocalDateTime.now());

            log.info("Товар {} уже есть в накладной, увеличено количество до {}",
                    ware.getCode1c(), productItem.getQuantity());
        } else {
            // Создаем новую позицию
            productItem = new ProductItem();
            productItem.setInvoiceId(invoice.getId());
            productItem.setWareId(ware.getId());
            productItem.setBarcodeId(barcode.getId());
            productItem.setQuantity(request.quantity());
            productItem.setUnitPrice(ware.getPrice());
            productItem.setLastUpdated(LocalDateTime.now());

            log.info("Создана новая позиция для товара {}", ware.getCode1c());
        }

        ProductItem saved = productItemRepository.save(productItem);

        log.info("Позиция успешно добавлена в накладную {}", invoiceNumber);

        return mapToResponse(saved, invoice);
    }

    @Override
    @Transactional
    public ProductItemResponse updateProductItem(Long invoiceNumber, UpdateProductItemRequestOnInvoice request) {
        log.info("Обновление позиции {} в накладной {}", request.productItemId(), invoiceNumber);

        Invoice invoice = invoiceService.findByInvoiceNumberOrThrow(invoiceNumber);

        if (invoice.getStatus() == InvoiceStatus.SENT) {
            throw new IllegalStateException("Нельзя изменять отправленную накладную");
        }

        ProductItem productItem = productItemRepository.findById(request.productItemId())
                .orElseThrow(() -> new ProductItemNotFoundException(
                        "Позиция с ID " + request.productItemId() + " не найдена"
                ));

        if (!productItem.getInvoiceId().equals(invoice.getId())) {
            throw new IllegalArgumentException("Позиция не принадлежит указанной накладной");
        }

        productItem.setQuantity(request.quantity());
        productItem.setLastUpdated(LocalDateTime.now());

        ProductItem updated = productItemRepository.save(productItem);

        log.info("Позиция {} успешно обновлена", request.productItemId());

        return mapToResponse(updated, invoice);
    }

    @Override
    @Transactional
    public void deleteProductItem(Long invoiceNumber, UUID productItemId) {
        log.info("Удаление позиции {} из накладной {}", productItemId, invoiceNumber);

        Invoice invoice = invoiceService.findByInvoiceNumberOrThrow(invoiceNumber);

        if (invoice.getStatus() == InvoiceStatus.SENT) {
            throw new IllegalStateException("Нельзя изменять отправленную накладную");
        }

        ProductItem productItem = productItemRepository.findById(productItemId)
                .orElseThrow(() -> new ProductItemNotFoundException(
                        "Позиция с ID " + productItemId + " не найдена"
                ));

        if (!productItem.getInvoiceId().equals(invoice.getId())) {
            throw new IllegalArgumentException("Позиция не принадлежит указанной накладной");
        }

        productItemRepository.delete(productItem);

        log.info("Позиция {} успешно удалена", productItemId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductItemResponse> getAllProductItemsByInvoiceNumber(Long invoiceNumber) {
        log.debug("Получение всех позиций накладной {}", invoiceNumber);

        Invoice invoice = invoiceService.findByInvoiceNumberOrThrow(invoiceNumber);

        List<ProductItem> items = productItemRepository.findByInvoiceIdWithDetails(invoice.getId());

        return items.stream()
                .map(item -> mapToResponse(item, invoice))
                .toList();
    }

    private ProductItemResponse mapToResponse(ProductItem item, Invoice invoice) {
        return new ProductItemResponse(
                item.getId(),
                item.getInvoiceId(),
                invoice.getInvoiceNumber(),
                item.getBarcode() != null ? item.getBarcode().getBarcode() : null,
                item.getWare() != null ? item.getWare().getTitle() : null,
                item.getWare() != null ? item.getWare().getCode1c() : null,
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice(),
                item.getWare() != null ? item.getWare().getIsWeighty() : null
        );
    }
}