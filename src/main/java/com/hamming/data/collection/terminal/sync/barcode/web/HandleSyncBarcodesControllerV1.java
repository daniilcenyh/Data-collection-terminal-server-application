package com.hamming.data.collection.terminal.sync.barcode.web;


import com.hamming.data.collection.terminal.sync.barcode.service.BarcodeSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sync-barcodes")
public class HandleSyncBarcodesControllerV1 {

    private final BarcodeSyncService barcodeSyncService;

    @PostMapping("/barcodes")
    public ResponseEntity<String> syncBarcodes() {
        barcodeSyncService.syncBarcodes();
        return ResponseEntity.ok("Синхронизация запущена");
    }
}
