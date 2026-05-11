package com.dhruv.claimsrouter.controller;

import com.dhruv.claimsrouter.model.dto.ClaimRequest;
import com.dhruv.claimsrouter.model.dto.ClaimResponse;
import com.dhruv.claimsrouter.model.enums.ClaimStatus;
import com.dhruv.claimsrouter.model.enums.ClaimType;
import com.dhruv.claimsrouter.service.ClaimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@Tag(name = "Claims", description = "Submit, query, and re-route healthcare claims")
@RestController
@RequestMapping("/api/v1/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    @Operation(summary = "Submit a new claim and run the routing engine")
    @PostMapping
    public ResponseEntity<ClaimResponse> submit(@Valid @RequestBody ClaimRequest request) {
        ClaimResponse response = claimService.submit(request);
        return ResponseEntity.created(URI.create("/api/v1/claims/" + response.id())).body(response);
    }

    @Operation(summary = "Get a claim by id")
    @GetMapping("/{id}")
    public ResponseEntity<ClaimResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(claimService.get(id));
    }

    @Operation(summary = "List claims with optional filters")
    @GetMapping
    public ResponseEntity<Page<ClaimResponse>> list(
            @RequestParam(required = false) ClaimStatus status,
            @RequestParam(required = false) ClaimType claimType,
            @RequestParam(required = false) String patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "submittedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDirection
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(claimService.list(status, claimType, patientId, pageable));
    }

    @Operation(summary = "Re-run the routing engine for an existing claim")
    @PatchMapping("/{id}/route")
    public ResponseEntity<ClaimResponse> reroute(@PathVariable UUID id) {
        return ResponseEntity.ok(claimService.reroute(id));
    }
}
