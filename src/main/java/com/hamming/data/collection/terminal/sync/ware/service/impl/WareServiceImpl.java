package com.hamming.data.collection.terminal.sync.ware.service.impl;

import com.hamming.data.collection.terminal.sync.barcode.service.BarcodeService;
import com.hamming.data.collection.terminal.sync.ware.dto.WareDto;
import com.hamming.data.collection.terminal.sync.ware.model.Ware;
import com.hamming.data.collection.terminal.sync.ware.repository.WareRepository;
import com.hamming.data.collection.terminal.sync.ware.service.WareService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WareServiceImpl implements WareService {

    private final BarcodeService barcodeService;
    private final WareRepository wareRepository;

    @Override
    public List<WareDto> findWaresByBarcode(String barcode) {
        var barcodeEntity = this.barcodeService.findByBarcode(barcode)
                .orElseThrow(() -> new RuntimeException("Barcode with code={%s} not found.".formatted(barcode)));

        Ware ware = this.wareRepository.findByCode1c(barcodeEntity.getWare1cCode())
                .orElseThrow(() -> new RuntimeException("Ware with 1C-code={%s} not foud".formatted(barcodeEntity.getWare1cCode())));

        WareDto wareDto = new WareDto(ware.getTitle(), ware.getCode1c(), ware.getIsWeighty(), ware.getPrice());
        return List.of(wareDto);
    }
}
