package com.bepro.MiniOrderSys.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.bepro.MiniOrderSys.entity.AppUser;
import com.bepro.MiniOrderSys.entity.UserVoucher;
import com.bepro.MiniOrderSys.entity.Voucher;
import com.bepro.MiniOrderSys.repository.UserRepository;
import com.bepro.MiniOrderSys.repository.UserVoucherRepository;
import com.bepro.MiniOrderSys.repository.VoucherRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VoucherService {

  private final VoucherRepository voucherRepo;
  private final UserVoucherRepository userVoucherRepo;
  private final UserRepository userRepo;

  @Transactional
  public Map<String, Object> drawVoucher(String username) {

    // Tim user
    AppUser user = userRepo.findByUsername(username)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    // Predicate - kiem tra user da rut chua
    Predicate<AppUser> alreadyDrawn = u -> userVoucherRepo.existsByUser(u);

    if (alreadyDrawn.test(user)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Á à! M có voucher rồi còn muốn lấy thêm à? Cút ngayyy!");
    }

    // Supplier - lay danh sach voucher kha dung
    Supplier<List<Voucher>> getVouchers = () -> voucherRepo.findByActiveTrue();

    List<Voucher> vouchers = getVouchers.get();
    if (vouchers.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong co voucher nao kha dung!");
    }

    // Function - random chon 1 voucher
    Function<List<Voucher>, Voucher> randomPick = list -> list.get(new Random().nextInt(list.size()));

    Voucher picked = randomPick.apply(vouchers);

    // Consumer - luu ket qua vao DB
    Consumer<UserVoucher> saveResult = uv -> userVoucherRepo.save(uv);

    UserVoucher userVoucher = UserVoucher.builder()
        .user(user)
        .voucher(picked)
        .assignedAt(LocalDateTime.now())
        .used(false)
        .build();

    saveResult.accept(userVoucher);

    // Tra ket qua
    return Map.of(
        "message", "Chuc mung! Ban da rut duoc voucher!",
        "code", picked.getCode(),
        "name", picked.getName(),
        "discountPercent", picked.getDiscountPercent() + "%"
    );
  }
}
