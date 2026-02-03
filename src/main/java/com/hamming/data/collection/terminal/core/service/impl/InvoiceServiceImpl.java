package com.hamming.data.collection.terminal.core.service.impl;

import com.hamming.data.collection.terminal.core.dto.response.InvoiceResponse;
import com.hamming.data.collection.terminal.core.dto.response.ProductItemResponse;
import com.hamming.data.collection.terminal.core.model.Invoice;
import com.hamming.data.collection.terminal.core.model.enums.InvoiceStatus;
import com.hamming.data.collection.terminal.core.repository.InvoiceRepository;
import com.hamming.data.collection.terminal.core.service.InvoiceService;
import com.hamming.data.collection.terminal.core.service.InvoiceSyncService;
import com.hamming.data.collection.terminal.exception.InvoiceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceSyncService invoiceSyncService;

    @Override
    @Transactional
    public InvoiceResponse createNewInvoice() {
        log.info("Создание новой накладной");

        Invoice invoice = new Invoice();
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setInvoiceNumber(Math.abs(ThreadLocalRandom.current().nextLong()));
        invoice.setLastUpdated(LocalDateTime.now());

        Invoice saved = invoiceRepository.save(invoice);

        log.info("Создана накладная с номером: {}", saved.getInvoiceNumber());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByNumber(Long invoiceNumber) {
        log.debug("Получение накладной по номеру: {}", invoiceNumber);

        Invoice invoice = invoiceRepository.findByInvoiceNumberWithItems(invoiceNumber)
                .orElseThrow(() -> new InvoiceNotFoundException(
                        "Накладная с номером " + invoiceNumber + " не найдена"
                ));

        return mapToResponseWithItems(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAllInvoices() {
        log.debug("Получение всех накладных");

        return invoiceRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public InvoiceResponse sendInvoiceTo1C(Long invoiceNumber) {
        log.info("Отправка накладной {} в 1С", invoiceNumber);

        Invoice invoice = findByInvoiceNumberOrThrow(invoiceNumber);

        if (invoice.getStatus() == InvoiceStatus.SENT) {
            log.warn("Накладная {} уже была отправлена", invoiceNumber);
            throw new IllegalStateException("Накладная уже была отправлена");
        }

        if (invoice.getProductItems().isEmpty()) {
            log.warn("Попытка отправки пустой накладной {}", invoiceNumber);
            throw new IllegalStateException("Невозможно отправить пустую накладную");
        }

        try {
            // Отправка в 1С
            invoiceSyncService.sendInvoiceTo1C(invoice);

            invoice.setStatus(InvoiceStatus.SENT);
            invoice.setSentAt(LocalDateTime.now());
            invoice.setLastUpdated(LocalDateTime.now());

            Invoice updated = invoiceRepository.save(invoice);

            log.info("Накладная {} успешно отправлена в 1С", invoiceNumber);

            return mapToResponseWithItems(updated);

        } catch (Exception e) {
            log.error("Ошибка при отправке накладной {} в 1С: {}", invoiceNumber, e.getMessage(), e);

            invoice.setStatus(InvoiceStatus.ERROR);
            invoice.setLastUpdated(LocalDateTime.now());
            invoiceRepository.save(invoice);

            throw new RuntimeException("Ошибка при отправке накладной в 1С: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deleteInvoice(Long invoiceNumber) {
        log.info("Удаление накладной: {}", invoiceNumber);

        Invoice invoice = findByInvoiceNumberOrThrow(invoiceNumber);

        if (invoice.getStatus() == InvoiceStatus.SENT) {
            log.warn("Попытка удаления отправленной накладной {}", invoiceNumber);
            throw new IllegalStateException("Нельзя удалить отправленную накладную");
        }

        invoiceRepository.delete(invoice);

        log.info("Накладная {} успешно удалена", invoiceNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public Invoice findByInvoiceNumberOrThrow(Long invoiceNumber) {
        return invoiceRepository.findByInvoiceNumberWithItems(invoiceNumber)
                .orElseThrow(() -> new InvoiceNotFoundException(
                        "Накладная с номером " + invoiceNumber + " не найдена"
                ));
    }

    private InvoiceResponse mapToResponse(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getStatus(),
                invoice.getTotalAmount(),
                invoice.getItemsCount(),
                invoice.getSentAt(),
                invoice.getCreatedAt(),
                List.of()
        );
    }

    private InvoiceResponse mapToResponseWithItems(Invoice invoice) {
        List<ProductItemResponse> items = invoice.getProductItems().stream()
                .map(item -> new ProductItemResponse(
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
                ))
                .toList();

        return new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getStatus(),
                invoice.getTotalAmount(),
                invoice.getItemsCount(),
                invoice.getSentAt(),
                invoice.getCreatedAt(),
                items
        );
    }
}