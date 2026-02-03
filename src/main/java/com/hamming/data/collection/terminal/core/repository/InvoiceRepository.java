package com.hamming.data.collection.terminal.core.repository;

import com.hamming.data.collection.terminal.core.model.Invoice;
import com.hamming.data.collection.terminal.core.model.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Optional<Invoice> findByInvoiceNumber(Long invoiceNumber);

    List<Invoice> findByStatus(InvoiceStatus status);

    @Query("SELECT i FROM Invoice i LEFT JOIN FETCH i.productItems WHERE i.invoiceNumber = :invoiceNumber")
    Optional<Invoice> findByInvoiceNumberWithItems(Long invoiceNumber);

    boolean existsByInvoiceNumber(Long invoiceNumber);

    void deleteByInvoiceNumber(Long invoiceNumber);
}