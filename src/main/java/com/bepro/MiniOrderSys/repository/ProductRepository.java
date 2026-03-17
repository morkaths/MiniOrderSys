package com.bepro.MiniOrderSys.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bepro.MiniOrderSys.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

  List<Product> findByActiveTrueOrderByNameAsc();
}
