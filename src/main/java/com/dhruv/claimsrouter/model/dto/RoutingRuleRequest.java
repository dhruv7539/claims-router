package com.dhruv.claimsrouter.model.dto;

import com.dhruv.claimsrouter.model.enums.ClaimType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RoutingRuleRequest(

        @NotBlank
        @Size(max = 128)
        String name,

        ClaimType claimType,

        @PositiveOrZero
        BigDecimal minAmount,

        @Positive
        BigDecimal maxAmount,

        @Size(max = 64)
        String region,

        @NotBlank
        @Size(max = 128)
        String destination,

        @NotNull
        @PositiveOrZero
        Integer priority,

        @NotNull
        Boolean active
) {
}
