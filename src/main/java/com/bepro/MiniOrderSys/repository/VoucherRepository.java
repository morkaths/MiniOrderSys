package com.bepro.MiniOrderSys.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bepro.MiniOrderSys.entity.Voucher;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {

  List<Voucher> findByActiveTrue();

  Optional<Voucher> findByCode(String code);
}
