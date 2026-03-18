package com.bepro.MiniOrderSys.dto.response;

public record ActiveTableResponse(
    String tableNumber,
    Long activeOrderCount) {
}
