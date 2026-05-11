package com.dhruv.claimsrouter.model.dto;

import java.util.Map;

public record ClaimStatsResponse(
        long totalClaims,
        Map<String, Long> countsByStatus,
        Map<String, Long> countsByClaimType,
        Map<String, Long> countsByDestination
) {
}
