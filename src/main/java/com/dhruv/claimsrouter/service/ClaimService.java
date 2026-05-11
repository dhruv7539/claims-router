package com.dhruv.claimsrouter.service;

import com.dhruv.claimsrouter.model.dto.ClaimRequest;
import com.dhruv.claimsrouter.model.dto.ClaimResponse;
import com.dhruv.claimsrouter.model.dto.ClaimStatsResponse;
import com.dhruv.claimsrouter.model.enums.ClaimStatus;
import com.dhruv.claimsrouter.model.enums.ClaimType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ClaimService {

    /** Creates and persists a new claim, then runs the routing engine on it. */
    ClaimResponse submit(ClaimRequest request);

    /** Returns the claim with the given id, or throws if not found. */
    ClaimResponse get(UUID id);

    /** Lists claims with optional filters. */
    Page<ClaimResponse> list(ClaimStatus status, ClaimType claimType, String patientId, Pageable pageable);

    /** Re-runs the routing engine for an existing claim. */
    ClaimResponse reroute(UUID id);

    /** Aggregated counts across the claim table. */
    ClaimStatsResponse stats();
}
