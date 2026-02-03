package com.hamming.data.collection.terminal.sync.barcode.service.impl;


import com.hamming.data.collection.terminal.sync.barcode.model.Barcode;
import com.hamming.data.collection.terminal.sync.barcode.repository.BarcodeRepository;
import com.hamming.data.collection.terminal.sync.barcode.service.BarcodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BarcodeServiceImpl implements BarcodeService {

    private final BarcodeRepository barcodeRepository;

    @Override
    public Optional<Barcode> findByBarcode(String barcode) {
        log.debug("Поиск штрихкода: {}", barcode);
        String firstSeven = barcode.substring(0, 7);
        if (firstSeven.startsWith("21")) {
            log.info("Штрихкод для весового товара: {}", barcode);
            String fiveDigits = firstSeven.substring(2);

            String weightyBarcode = removeLeadingZeros(fiveDigits);

            return barcodeRepository.findByBarcode(weightyBarcode);
        }
        return barcodeRepository.findByBarcode(barcode);
    }

    private static String removeLeadingZeros(String str) {
        // Находим первый символ, не равный '0'
        int i = 0;
        while (i < str.length() && str.charAt(i) == '0') {
            i++;
        }

        // Возвращаем подстроку начиная с этого символа
        // Если i == str.length(), значит все символы были '0'
        return str.substring(i);
    }

    @Override
    public List<Barcode> findByWareCode1c(String wareCode1c) {
        log.debug("Поиск штрихкодов для товара с кодом 1C: {}", wareCode1c);
        return barcodeRepository.findByWare1cCode(wareCode1c);
    }

    @Override
    public long countByWareCode1c(String wareCode1c) {
        log.debug("Подсчет штрихкодов для товара с кодом 1C: {}", wareCode1c);
        return barcodeRepository.countByWare1cCode(wareCode1c);
    }
}