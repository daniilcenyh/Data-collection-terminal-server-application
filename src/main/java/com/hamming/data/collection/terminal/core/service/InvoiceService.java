package com.hamming.data.collection.terminal.core.service;

import com.hamming.data.collection.terminal.core.dto.request.AddNewProductItemRequest;
import com.hamming.data.collection.terminal.core.dto.response.InvoiceResponse;
import com.hamming.data.collection.terminal.core.model.Invoice;

import java.util.List;
import java.util.UUID;

//public interface InvoiceService {
//
//    // product item service
//    void addNewProductItemInInvoice(String invoiceNumber, UUID invoiceId, AddNewProductItemRequest productItem);
////    void updateInvoice(String invoiceNumber, UUID invoiceId)
//    void deleteProductItemFromInvoice(String invoiceNumber, UUID invoiceId);
//
//
//    // invoice service
//    void createNewInvoice(String invoiceNumber);
//    void deleteInvoice(String invoiceNumber);
//    void sendCompleteInvoice(String invoiceNumber);
//}

public interface InvoiceService {
    InvoiceResponse createNewInvoice();
    InvoiceResponse getInvoiceByNumber(Long invoiceNumber);
    List<InvoiceResponse> getAllInvoices();
    InvoiceResponse sendInvoiceTo1C(Long invoiceNumber);
    void deleteInvoice(Long invoiceNumber);
    Invoice findByInvoiceNumberOrThrow(Long invoiceNumber);
}