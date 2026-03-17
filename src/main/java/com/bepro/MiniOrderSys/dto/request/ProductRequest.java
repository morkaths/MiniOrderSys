package com.bepro.MiniOrderSys.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductRequest(
    @NotBlank(message = "Product name is required") @Size(max = 100, message = "Product name must be less than 100 characters") String name,

    @Size(max = 500, message = "Product description must be less than 500 characters") String description,

    @NotNull(message = "Product price is required") @DecimalMin(value = "0.01", message = "Product price must be greater than 0") BigDecimal price,

    Boolean active) {
}
