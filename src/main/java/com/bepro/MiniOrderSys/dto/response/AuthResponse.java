package com.bepro.MiniOrderSys.dto.response;

public record AuthResponse(
        String accessToken,
        String tokenType,
        String username,
        String role) {
}
