package com.hamming.data.collection.terminal.core.repository;

import com.hamming.data.collection.terminal.core.model.ProductItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductItemRepository extends JpaRepository<ProductItem, UUID> {

    List<ProductItem> findByInvoiceId(UUID invoiceId);

    @Query("SELECT pi FROM ProductItem pi JOIN FETCH pi.ware JOIN FETCH pi.barcode WHERE pi.invoiceId = :invoiceId")
    List<ProductItem> findByInvoiceIdWithDetails(UUID invoiceId);

    Optional<ProductItem> findByInvoiceIdAndBarcodeId(UUID invoiceId, Long barcodeId);

    void deleteByInvoiceId(UUID invoiceId);
}
