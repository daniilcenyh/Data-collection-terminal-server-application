package com.hamming.data.collection.terminal.sync.scheduler;

import com.hamming.data.collection.terminal.sync.barcode.service.BarcodeSyncService;
import com.hamming.data.collection.terminal.sync.ware.service.WareSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncScheduler {

    private final WareSyncService wareSyncService;
    private final BarcodeSyncService barcodeSyncService;

    // Запуск каждый день в 2:00 ночи
    @Scheduled(cron = "0 0 2 * * *")
    public void scheduleDailySync() {
        log.info("Запуск запланированной синхронизации");

        // Сначала синхронизируем товары
        wareSyncService.syncWares();

        // Затем синхронизируем штрихкоды
        barcodeSyncService.syncBarcodes();

        log.info("Запланированная синхронизация завершена");
    }
}