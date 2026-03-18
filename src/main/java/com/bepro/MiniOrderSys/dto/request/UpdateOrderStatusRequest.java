package com.bepro.MiniOrderSys.dto.request;

import com.bepro.MiniOrderSys.entity.enums.OrderStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
    @NotNull(message = "Status is required") OrderStatus status) {
}
