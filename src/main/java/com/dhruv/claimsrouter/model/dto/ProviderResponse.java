package com.dhruv.claimsrouter.model.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProviderResponse(
        UUID id,
        String npi,
        String name,
        String region,
        String specialty,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
