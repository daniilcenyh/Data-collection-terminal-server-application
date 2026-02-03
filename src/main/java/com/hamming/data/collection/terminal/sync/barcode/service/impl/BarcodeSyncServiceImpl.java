package com.hamming.data.collection.terminal.sync.barcode.service.impl;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hamming.data.collection.terminal.sync.configuration.ExternalApiProperties;
import com.hamming.data.collection.terminal.sync.barcode.dto.BarcodeDto;
import com.hamming.data.collection.terminal.sync.barcode.model.Barcode;
import com.hamming.data.collection.terminal.sync.barcode.repository.BarcodeRepository;
import com.hamming.data.collection.terminal.sync.barcode.service.BarcodeSyncService;
import com.hamming.data.collection.terminal.sync.ware.repository.WareRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BarcodeSyncServiceImpl implements BarcodeSyncService {

    private final BarcodeRepository barcodeRepository;
    private final WareRepository wareRepository;

    @Qualifier("externalApiRestTemplate")
    private final RestTemplate restTemplate;

    private final ExternalApiProperties apiProperties;

    @Override
    @Transactional
    public void syncBarcodes() {
        log.info("Начало синхронизации штрихкодов с внешним API: {}", apiProperties.getBarcodesUrl());

        try {
            List<BarcodeDto> barcodeDTOs = fetchBarcodesFromApi();

            if (barcodeDTOs == null || barcodeDTOs.isEmpty()) {
                log.warn("Получен пустой список штрихкодов");
                return;
            }

            int updated = 0;
            int created = 0;
            int skipped = 0;

            for (BarcodeDto dto : barcodeDTOs) {
                SyncResult result = saveOrUpdateBarcode(dto);
                switch (result) {
                    case CREATED -> created++;
                    case UPDATED -> updated++;
                    case SKIPPED -> skipped++;
                }
            }

            log.info("Синхронизация штрихкодов завершена. Создано: {}, Обновлено: {}, Пропущено: {}",
                    created, updated, skipped);

        } catch (Exception e) {
            log.error("Ошибка при синхронизации штрихкодов: {}", e.getMessage(), e);
        }
    }

    private List<BarcodeDto> fetchBarcodesFromApi() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(
                    MediaType.APPLICATION_JSON,
                    MediaType.APPLICATION_OCTET_STREAM,
                    MediaType.ALL
            ));
            headers.set("Accept-Charset", "UTF-8");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            log.debug("Отправка запроса к API штрихкодов: {}", apiProperties.getBarcodesUrl());

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    apiProperties.getBarcodesUrl(),
                    HttpMethod.GET,
                    entity,
                    byte[].class
            );

            log.debug("Получен ответ. Статус: {}, Content-Type: {}",
                    response.getStatusCode(),
                    response.getHeaders().getContentType());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String jsonString = new String(response.getBody(), StandardCharsets.UTF_8);

                if (jsonString.startsWith("\uFEFF")) {
                    jsonString = jsonString.substring(1);
                    log.debug("BOM удален из ответа");
                }

                jsonString = jsonString.trim();

                log.debug("Тело ответа штрихкодов (первые 500 символов): {}",
                        jsonString.substring(0, Math.min(500, jsonString.length())));

                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                List<BarcodeDto> barcodes = objectMapper.readValue(
                        jsonString,
                        new TypeReference<List<BarcodeDto>>() {}
                );

                log.info("Получено {} штрихкодов из внешнего API", barcodes.size());
                return barcodes;
            }

            log.warn("Получен неожиданный статус код: {}", response.getStatusCode());
            return List.of();

        } catch (HttpClientErrorException e) {
            log.error("HTTP ошибка при запросе штрихкодов к API. Статус: {}, Сообщение: {}",
                    e.getStatusCode(), e.getMessage());
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                log.error("Ошибка аутентификации! Проверьте правильность логина и пароля в application.yaml");
            }
            return List.of();
        } catch (HttpServerErrorException e) {
            log.error("Ошибка сервера при запросе штрихкодов к API. Статус: {}, Тело ответа: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            return List.of();
        } catch (Exception e) {
            log.error("Ошибка при получении штрихкодов из API: {}", e.getMessage(), e);
            return List.of();
        }
    }

    private SyncResult saveOrUpdateBarcode(BarcodeDto dto) {
        // ВАЖНО: Проверяем существование товара по code_1c
        boolean wareExists = wareRepository.findByCode1c(dto.code1c()).isPresent();

        if (!wareExists) {
            log.warn("Товар с кодом 1C {} не найден. Штрихкод {} пропущен",
                    dto.code1c(), dto.barcode());
            return SyncResult.SKIPPED;
        }

        // Проверяем, существует ли штрихкод
        Optional<Barcode> existingBarcode = barcodeRepository.findByBarcode(dto.barcode());

        Barcode barcode;
        boolean isNew;

        if (existingBarcode.isPresent()) {
            barcode = existingBarcode.get();
            isNew = false;

            // Проверяем, не изменился ли товар для этого штрихкода
            if (!barcode.getWare1cCode().equals(dto.code1c())) {
                log.warn("Штрихкод {} переназначен с товара {} на товар {}",
                        dto.barcode(),
                        barcode.getWare1cCode(),
                        dto.code1c());
                barcode.setWare1cCode(dto.code1c());
            }

            log.debug("Обновление штрихкода: {} для товара: {}",
                    dto.barcode(), dto.code1c());
        } else {
            barcode = new Barcode();
            barcode.setWare1cCode(dto.code1c());
            barcode.setBarcode(dto.barcode());
            isNew = true;
            log.debug("Создание нового штрихкода: {} для товара: {}",
                    dto.barcode(), dto.code1c());
        }

        barcode.setLastUpdated(LocalDateTime.now());
        barcodeRepository.save(barcode);

        return isNew ? SyncResult.CREATED : SyncResult.UPDATED;
    }

    private enum SyncResult {
        CREATED, UPDATED, SKIPPED
    }
}