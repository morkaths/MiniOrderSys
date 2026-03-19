package com.bepro.MiniOrderSys.dto.response;

public record TableResponse(
        Long id,
        String tableNumber,
        Integer capacity,
        String status) {
}
