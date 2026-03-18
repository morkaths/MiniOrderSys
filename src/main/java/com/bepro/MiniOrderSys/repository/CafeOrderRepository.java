package com.bepro.MiniOrderSys.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bepro.MiniOrderSys.entity.CafeOrder;
import com.bepro.MiniOrderSys.entity.enums.OrderStatus;

public interface CafeOrderRepository extends JpaRepository<CafeOrder, Long> {

  List<CafeOrder> findAllByOrderByCreatedAt();

  List<CafeOrder> findByStatusOrderByCreatedAtDesc(OrderStatus status);

  long countByTableNumberAndStatus(String tableNumber, OrderStatus status);
}
