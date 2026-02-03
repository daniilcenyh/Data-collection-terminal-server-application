package com.hamming.data.collection.terminal.sync.barcode.model;

import com.hamming.data.collection.terminal.core.model.ProductItem;
import com.hamming.data.collection.terminal.sync.ware.model.Ware;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "barcodes")
public class Barcode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ware_1c_code", nullable = false, length = 100)
    private String ware1cCode;

    @Column(name = "barcode", unique = true, nullable = false, length = 100)
    private String barcode;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Связь с товаром через ware_1c_code (не через ID!)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ware_1c_code", referencedColumnName = "code_1c", insertable = false, updatable = false)
    private Ware ware;

    @OneToMany(mappedBy = "barcode", fetch = FetchType.LAZY)
    private List<ProductItem> productItems = new ArrayList<>();
}
