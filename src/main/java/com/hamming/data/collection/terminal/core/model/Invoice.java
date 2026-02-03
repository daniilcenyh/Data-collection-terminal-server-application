package com.hamming.data.collection.terminal.core.model;

import com.hamming.data.collection.terminal.core.model.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "invoice_number", unique = true, nullable = false)
    private Long invoiceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(name = "total_amount")
    private Integer totalAmount = 0;

    @Column(name = "items_count")
    private Integer itemsCount = 0;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductItem> productItems = new ArrayList<>();

    // Вспомогательные методы
    public void addProductItem(ProductItem item) {
        productItems.add(item);
        item.setInvoice(this);
    }

    public void removeProductItem(ProductItem item) {
        productItems.remove(item);
        item.setInvoice(null);
    }
}
