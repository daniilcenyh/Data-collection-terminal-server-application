package com.hamming.data.collection.terminal.sync.ware.repository;


import com.hamming.data.collection.terminal.sync.ware.model.Ware;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WareRepository extends JpaRepository<Ware, Long> {
    Optional<Ware> findByCode1c(String code);
}
