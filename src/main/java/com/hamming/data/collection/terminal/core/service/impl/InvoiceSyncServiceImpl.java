package com.hamming.data.collection.terminal.core.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hamming.data.collection.terminal.core.dto.request.Invoice1CRequest;
import com.hamming.data.collection.terminal.core.dto.request.ProductItem1C;
import com.hamming.data.collection.terminal.core.model.Invoice;
import com.hamming.data.collection.terminal.core.model.ProductItem;
import com.hamming.data.collection.terminal.core.service.InvoiceSyncService;
import com.hamming.data.collection.terminal.sync.configuration.ExternalApiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceSyncServiceImpl implements InvoiceSyncService {

    @Qualifier("externalApiRestTemplate")
    private final RestTemplate restTemplate;

    private final ExternalApiProperties apiProperties;

    @Override
    public void sendInvoiceTo1C(Invoice invoice) {
        log.info("Отправка накладной {} в 1С", invoice.getInvoiceNumber());

        try {
            // Формируем JSON для 1С
            Invoice1CRequest request = buildInvoice1CRequest(invoice);

            // Логируем JSON
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            String jsonRequest = mapper.writeValueAsString(request);
            log.debug("JSON для отправки в 1С: {}", jsonRequest);

            // Отправляем в 1С
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.ALL));

            HttpEntity<Invoice1CRequest> entity = new HttpEntity<>(request, headers);

            String url = apiProperties.getInvoicesUrl(); // Добавим в конфигурацию

            log.debug("Отправка запроса на URL: {}", url);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Накладная {} успешно отправлена в 1С. Ответ: {}",
                        invoice.getInvoiceNumber(), response.getBody());
            } else {
                log.error("Ошибка при отправке накладной {}. Статус: {}, Ответ: {}",
                        invoice.getInvoiceNumber(), response.getStatusCode(), response.getBody());
                throw new RuntimeException("Ошибка при отправке в 1С: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Ошибка при отправке накладной {} в 1С: {}",
                    invoice.getInvoiceNumber(), e.getMessage(), e);
            throw new RuntimeException("Ошибка при отправке накладной в 1С", e);
        }
    }

    private Invoice1CRequest buildInvoice1CRequest(Invoice invoice) {
        List<ProductItem1C> items = invoice.getProductItems().stream()
                .map(this::mapToProductItem1C)
                .toList();

        return new Invoice1CRequest(
                invoice.getInvoiceNumber(),
                invoice.getTotalAmount(),
                invoice.getItemsCount(),
                items
        );
    }

    private ProductItem1C mapToProductItem1C(ProductItem item) {
        return new ProductItem1C(
                item.getWare().getCode1c(),
                item.getBarcode().getBarcode(),
                item.getWare().getTitle(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice(),
                item.getWare().getIsWeighty()
        );
    }
}
