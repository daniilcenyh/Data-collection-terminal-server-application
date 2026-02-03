package com.hamming.data.collection.terminal.sync.ware.model;

import com.hamming.data.collection.terminal.core.model.ProductItem;
import com.hamming.data.collection.terminal.sync.barcode.model.Barcode;
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
@Table(name = "wares")
public class Ware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "code_1c", unique = true, nullable = false, length = 100)
    private String code1c;

    @Column(name = "is_weighty")
    private Boolean isWeighty;

    @Column(name = "price")
    private Integer price;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Связь со штрихкодами через code_1c
    @OneToMany(mappedBy = "ware", fetch = FetchType.LAZY)
    private List<Barcode> barcodes = new ArrayList<>();

    @OneToMany(mappedBy = "ware", fetch = FetchType.LAZY)
    private List<ProductItem> productItems = new ArrayList<>();

//    @OneToMany(mappedBy = "")
}