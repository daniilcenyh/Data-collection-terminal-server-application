package com.hamming.data.collection.terminal.sync.barcode.web;

import com.hamming.data.collection.terminal.sync.barcode.dto.BarcodeDto;
import com.hamming.data.collection.terminal.sync.barcode.service.BarcodeService;
import com.hamming.data.collection.terminal.sync.ware.dto.WareDto;
import com.hamming.data.collection.terminal.sync.ware.service.WareService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
public class BarcodeControllerV1 {

    private final BarcodeService barcodeService;
    private final WareService wareService;

    @GetMapping("/{barcode}")
    public ResponseEntity<BarcodeDto> findByBarcode(
            @PathVariable(name = "barcode") String barcode
    ) {
        var barcodeEntity = this.barcodeService.findByBarcode(barcode).get();
        var response = new BarcodeDto(barcodeEntity.getWare1cCode(), barcodeEntity.getBarcode());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/wares/{barcode}")
    public ResponseEntity<List<WareDto>> findWareByBarcode(
            @PathVariable(name = "barcode") String barcode
    ) {
        return ResponseEntity.ok(this.wareService.findWaresByBarcode(barcode));
    }
}
