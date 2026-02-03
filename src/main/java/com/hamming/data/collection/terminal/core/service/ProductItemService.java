package com.hamming.data.collection.terminal.core.service;

import com.hamming.data.collection.terminal.core.dto.request.AddNewProductItemRequest;
import com.hamming.data.collection.terminal.core.dto.request.UpdateProductItemRequestOnInvoice;
import com.hamming.data.collection.terminal.core.dto.response.ProductItemResponse;

import java.util.List;
import java.util.UUID;

public interface ProductItemService {
    ProductItemResponse addProductItem(Long invoiceNumber, AddNewProductItemRequest request);
    ProductItemResponse updateProductItem(Long invoiceNumber, UpdateProductItemRequestOnInvoice request);
    void deleteProductItem(Long invoiceNumber, UUID productItemId);
    List<ProductItemResponse> getAllProductItemsByInvoiceNumber(Long invoiceNumber);
}
