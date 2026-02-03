package com.hamming.data.collection.terminal.sync.barcode.repository;

import com.hamming.data.collection.terminal.sync.barcode.model.Barcode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BarcodeRepository extends JpaRepository<Barcode, Long> {

    Optional<Barcode> findByBarcode(String barcode);

    List<Barcode> findByWare1cCode(String ware1cCode);

    boolean existsByBarcode(String barcode);

    boolean existsByWare1cCode(String ware1cCode);

    @Modifying
    @Query("DELETE FROM Barcode b WHERE b.ware1cCode = :ware1cCode")
    void deleteByWare1cCode(@Param("ware1cCode") String ware1cCode);

    long countByWare1cCode(String ware1cCode);
}