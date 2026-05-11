package com.dhruv.claimsrouter.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProviderRequest(

        @NotBlank
        @Pattern(regexp = "\\d{10}", message = "npi must be a 10-digit NPI")
        String npi,

        @NotBlank
        @Size(max = 200)
        String name,

        @Size(max = 64)
        String region,

        @Size(max = 128)
        String specialty,

        @NotNull
        Boolean active
) {
}
