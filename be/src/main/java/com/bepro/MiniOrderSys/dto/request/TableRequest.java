package com.bepro.MiniOrderSys.dto.request;

import com.bepro.MiniOrderSys.entity.enums.TableStatus;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record TableRequest(
        @Size(max = 20, message = "Table number cannot exceed 20 characters")
        String tableNumber,

        @Min(value = 1, message = "Capacity must be at least 1 person")
        Integer capacity,

        TableStatus status) {

    public TableRequest {
        if (status == null) {
            status = TableStatus.AVAILABLE;
        }
    }
}

