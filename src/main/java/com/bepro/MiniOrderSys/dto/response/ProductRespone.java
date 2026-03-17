package com.bepro.MiniOrderSys.dto.response;

public record ProductRespone(
    Long id,
    String name,
    String description,
    String price,
    Boolean active) {
}
