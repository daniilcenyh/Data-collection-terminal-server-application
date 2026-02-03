package com.hamming.data.collection.terminal.core.service;

import com.hamming.data.collection.terminal.core.model.Invoice;

public interface InvoiceSyncService {
    void sendInvoiceTo1C(Invoice invoice);
}