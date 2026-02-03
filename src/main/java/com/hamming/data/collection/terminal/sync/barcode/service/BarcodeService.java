package com.hamming.data.collection.terminal.sync.barcode.service;


import com.hamming.data.collection.terminal.sync.barcode.model.Barcode;

import java.util.List;
import java.util.Optional;

public interface BarcodeService {
    Optional<Barcode> findByBarcode(String barcode);
    List<Barcode> findByWareCode1c(String wareCode1c);
    long countByWareCode1c(String wareCode1c);
}