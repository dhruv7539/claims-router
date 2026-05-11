package com.dhruv.claimsrouter.model.dto;

import com.dhruv.claimsrouter.model.enums.ClaimType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record RoutingRuleResponse(
        UUID id,
        String name,
        ClaimType claimType,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        String region,
        String destination,
        int priority,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
