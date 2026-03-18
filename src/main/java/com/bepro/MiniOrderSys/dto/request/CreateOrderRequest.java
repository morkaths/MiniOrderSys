package com.bepro.MiniOrderSys.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record CreateOrderRequest(
    @NotBlank(message = "Table number is required") String tableNumber,

    @NotEmpty(message = "Order items cannot be empty") List<@Valid OrderItemRequest> items) {
}
