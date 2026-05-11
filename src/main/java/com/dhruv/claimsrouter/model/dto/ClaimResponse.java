package com.dhruv.claimsrouter.model.dto;

import com.dhruv.claimsrouter.model.enums.ClaimStatus;
import com.dhruv.claimsrouter.model.enums.ClaimType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ClaimResponse(
        UUID id,
        String claimNumber,
        String patientId,
        UUID providerId,
        String providerNpi,
        String providerName,
        ClaimType claimType,
        BigDecimal amount,
        LocalDate serviceDate,
        LocalDateTime submittedAt,
        ClaimStatus status,
        String routingDestination,
        String rejectionReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
