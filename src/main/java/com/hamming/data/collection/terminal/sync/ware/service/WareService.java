package com.hamming.data.collection.terminal.sync.ware.service;

import com.hamming.data.collection.terminal.sync.ware.dto.WareDto;

import java.util.List;

public interface WareService {
    List<WareDto> findWaresByBarcode(String barcode);
}
