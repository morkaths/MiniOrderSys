package com.bepro.MiniOrderSys.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bepro.MiniOrderSys.entity.CafeTable;

public interface CafeTableRepository extends JpaRepository<CafeTable, Long> {

  Optional<CafeTable> findByTableNumberIgnoreCase(String tableNumber);
}
