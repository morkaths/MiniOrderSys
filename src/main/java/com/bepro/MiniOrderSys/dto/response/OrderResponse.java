package com.bepro.MiniOrderSys.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
    Long orderId,
    String tableNumber,
    String status,
    String orderedBy,
    BigDecimal totalAmount,
    LocalDateTime createdAt,
    List<OrderItemResponse> items) {
}
