package com.bepro.MiniOrderSys.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bepro.MiniOrderSys.entity.AppUser;
import com.bepro.MiniOrderSys.entity.UserVoucher;
import com.bepro.MiniOrderSys.entity.Voucher;

public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {

  boolean existsByUser(AppUser user);

  Optional<UserVoucher> findByUserAndVoucher(AppUser user, Voucher voucher);
}
