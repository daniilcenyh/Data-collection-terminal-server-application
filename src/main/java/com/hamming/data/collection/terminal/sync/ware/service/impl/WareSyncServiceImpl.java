package com.hamming.data.collection.terminal.sync.ware.service.impl;

import com.hamming.data.collection.terminal.sync.configuration.ExternalApiProperties;
import com.hamming.data.collection.terminal.sync.ware.dto.WareDto;
import com.hamming.data.collection.terminal.sync.ware.model.Ware;
import com.hamming.data.collection.terminal.sync.ware.repository.WareRepository;
import com.hamming.data.collection.terminal.sync.ware.service.WareSyncService;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WareSyncServiceImpl implements WareSyncService {

    private final WareRepository wareRepository;

    @Qualifier("externalApiRestTemplate")
    private final RestTemplate restTemplate;

    private final ExternalApiProperties apiProperties;

    @Override
    public void syncWares() {
        log.info("Начало синхронизации данных с внешним API: {}", apiProperties.getWaresUrl());

        try {
            List<WareDto> wareDTOs = fetchWaresFromApi();

            if (wareDTOs == null || wareDTOs.isEmpty()) {
                log.warn("Получен пустой список товаров");
                return;
            }

            int updated = 0;
            int created = 0;

            for (WareDto dto : wareDTOs) {
                boolean isNew = saveOrUpdateWare(dto);
                if (isNew) {
                    created++;
                } else {
                    updated++;
                }
            }

            log.info("Синхронизация завершена. Создано: {}, Обновлено: {}", created, updated);

        } catch (Exception e) {
            log.error("Ошибка при синхронизации данных: {}", e.getMessage(), e);
        }
    }

    private List<WareDto> fetchWaresFromApi() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(
                    MediaType.APPLICATION_JSON,
                    MediaType.APPLICATION_OCTET_STREAM,
                    MediaType.ALL
            ));
            headers.set("Accept-Charset", "UTF-8");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            log.debug("Отправка запроса к API: {}", apiProperties.getWaresUrl());

            // Получаем как byte array для правильной обработки BOM
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    apiProperties.getWaresUrl(),
                    HttpMethod.GET,
                    entity,
                    byte[].class
            );

            log.debug("Получен ответ. Статус: {}, Content-Type: {}",
                    response.getStatusCode(),
                    response.getHeaders().getContentType());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // Конвертируем byte[] в String с правильной кодировкой и удаляем BOM
                String jsonString = new String(response.getBody(), StandardCharsets.UTF_8);

                // Удаляем BOM если он есть (UTF-8 BOM = EF BB BF = \uFEFF)
                if (jsonString.startsWith("\uFEFF")) {
                    jsonString = jsonString.substring(1);
                    log.debug("BOM удален из ответа");
                }

                // Также удаляем другие возможные невидимые символы в начале
                jsonString = jsonString.trim();

                log.debug("Тело ответа (первые 500 символов): {}",
                        jsonString.substring(0, Math.min(500, jsonString.length())));

                // Парсим JSON
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                List<WareDto> wares = objectMapper.readValue(
                        jsonString,
                        new TypeReference<List<WareDto>>() {}
                );

                log.info("Получено {} товаров из внешнего API", wares.size());
                return wares;
            }

            log.warn("Получен неожиданный статус код: {}", response.getStatusCode());
            return List.of();

        } catch (HttpClientErrorException e) {
            log.error("HTTP ошибка при запросе к API. Статус: {}, Сообщение: {}",
                    e.getStatusCode(), e.getMessage());
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                log.error("Ошибка аутентификации! Проверьте правильность логина и пароля в application.yaml");
            }
            return List.of();
        } catch (HttpServerErrorException e) {
            log.error("Ошибка сервера при запросе к API. Статус: {}, Тело ответа: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            return List.of();
        } catch (Exception e) {
            log.error("Ошибка при получении данных из API: {}", e.getMessage(), e);
            return List.of();
        }
    }

    private boolean saveOrUpdateWare(WareDto dto) {
        var existingWare = wareRepository.findByCode1c(dto.code1c());

        Ware ware;
        boolean isNew;

        if (existingWare.isPresent()) {
            ware = existingWare.get();
            isNew = false;
            log.debug("Обновление товара: {} - {}", dto.code1c(), dto.title());
        } else {
            ware = new Ware();
            isNew = true;
            log.debug("Создание нового товара: {} - {}", dto.code1c(), dto.title());
        }

        ware.setTitle(dto.title());
        ware.setCode1c(dto.code1c());
        ware.setIsWeighty(dto.weighty());
        ware.setPrice(dto.price());
        ware.setLastUpdated(LocalDateTime.now());

        wareRepository.save(ware);

        return isNew;
    }
}