package com.hamming.data.collection.terminal.sync.ware.web;

import com.hamming.data.collection.terminal.sync.barcode.service.BarcodeSyncService;
import com.hamming.data.collection.terminal.sync.ware.model.Ware;
import com.hamming.data.collection.terminal.sync.ware.repository.WareRepository;
import com.hamming.data.collection.terminal.sync.ware.service.WareSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sync")
public class HandleSyncWaresControllerV1 {

    private final WareSyncService wareSyncService;
    private final BarcodeSyncService barcodeSyncService;
    private final WareRepository wareRepository;

    @PostMapping("/all")
    public ResponseEntity<String> syncWares() {
        wareSyncService.syncWares();
        barcodeSyncService.syncBarcodes();
        return ResponseEntity.ok("Синхронизация запущена");
    }

    @GetMapping("/all-wares")
    public ResponseEntity<List<Ware>> getAll() {
        return ResponseEntity.ok(this.wareRepository.findAll());
    }
}
