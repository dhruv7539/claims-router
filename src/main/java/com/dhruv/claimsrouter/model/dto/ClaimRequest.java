package com.dhruv.claimsrouter.model.dto;

import com.dhruv.claimsrouter.model.enums.ClaimType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ClaimRequest(

        @NotBlank
        @Size(max = 64)
        String claimNumber,

        @NotBlank
        String patientId,

        @NotBlank
        @Pattern(regexp = "\\d{10}", message = "providerNpi must be a 10-digit NPI")
        String providerNpi,

        @NotNull
        ClaimType claimType,

        @NotNull
        @Positive
        @DecimalMax(value = "9999999.99")
        BigDecimal amount,

        @NotNull
        @PastOrPresent
        LocalDate serviceDate,

        @NotBlank
        String rawPayload
) {
}
