package com.bepro.MiniOrderSys.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVoucher {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private AppUser user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "voucher_id", nullable = false)
  private Voucher voucher;

  @Column(nullable = false)
  private LocalDateTime assignedAt;

  @Column(nullable = false)
  @Builder.Default
  private Boolean used = false;

  @Column
  private LocalDateTime usedAt;

  @PrePersist
  void prePersist() {
    if (assignedAt == null) {
      assignedAt = LocalDateTime.now();
    }
    if (used == null) {
      used = false;
    }
  }
}
