package com.dhruv.claimsrouter.controller;

import com.dhruv.claimsrouter.model.dto.ClaimStatsResponse;
import com.dhruv.claimsrouter.service.ClaimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Stats", description = "Aggregated statistics over claims")
@RestController
@RequestMapping("/api/v1/claims/stats")
@RequiredArgsConstructor
public class StatsController {

    private final ClaimService claimService;

    @Operation(summary = "Aggregated counts by status, claim type, and routing destination")
    @GetMapping
    public ResponseEntity<ClaimStatsResponse> stats() {
        return ResponseEntity.ok(claimService.stats());
    }
}
