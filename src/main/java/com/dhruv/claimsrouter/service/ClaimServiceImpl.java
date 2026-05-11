package com.dhruv.claimsrouter.service;

import com.dhruv.claimsrouter.exception.ClaimNotFoundException;
import com.dhruv.claimsrouter.exception.InvalidClaimException;
import com.dhruv.claimsrouter.exception.ProviderNotFoundException;
import com.dhruv.claimsrouter.mapper.ClaimMapper;
import com.dhruv.claimsrouter.model.dto.ClaimRequest;
import com.dhruv.claimsrouter.model.dto.ClaimResponse;
import com.dhruv.claimsrouter.model.dto.ClaimStatsResponse;
import com.dhruv.claimsrouter.model.entity.Claim;
import com.dhruv.claimsrouter.model.entity.Provider;
import com.dhruv.claimsrouter.model.enums.ClaimStatus;
import com.dhruv.claimsrouter.model.enums.ClaimType;
import com.dhruv.claimsrouter.repository.ClaimRepository;
import com.dhruv.claimsrouter.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimServiceImpl implements ClaimService {

    private final ClaimRepository claimRepository;
    private final ProviderRepository providerRepository;
    private final RoutingService routingService;
    private final ClaimMapper claimMapper;

    @Override
    @Transactional
    public ClaimResponse submit(ClaimRequest request) {
        log.info("Submitting claim number={}", request.claimNumber());

        claimRepository.findByClaimNumber(request.claimNumber()).ifPresent(c -> {
            throw new InvalidClaimException("Claim number already exists: " + request.claimNumber());
        });

        Provider provider = providerRepository.findByNpi(request.providerNpi())
                .orElseThrow(() -> new ProviderNotFoundException(
                        "Provider with NPI " + request.providerNpi() + " not found"));

        if (!provider.isActive()) {
            throw new InvalidClaimException("Provider " + provider.getNpi() + " is not active");
        }

        Claim claim = Claim.builder()
                .id(UUID.randomUUID())
                .claimNumber(request.claimNumber())
                .patientId(request.patientId())
                .provider(provider)
                .claimType(request.claimType())
                .amount(request.amount())
                .serviceDate(request.serviceDate())
                .rawPayload(request.rawPayload())
                .status(ClaimStatus.NEW)
                .build();

        Claim saved = claimRepository.save(claim);

        // Mark validated, then route. Routing service may transition to ROUTED
        // (matched) or leave as VALIDATED (no-match).
        saved.setStatus(ClaimStatus.VALIDATED);
        Claim routed = routingService.route(saved);

        Claim persisted = claimRepository.save(routed);
        return claimMapper.toResponse(persisted);
    }

    @Override
    @Transactional(readOnly = true)
    public ClaimResponse get(UUID id) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new ClaimNotFoundException(id));
        return claimMapper.toResponse(claim);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClaimResponse> list(ClaimStatus status, ClaimType claimType, String patientId, Pageable pageable) {
        Page<Claim> page;
        if (status != null && claimType != null) {
            page = claimRepository.findByStatusAndClaimType(status, claimType, pageable);
        } else if (status != null) {
            page = claimRepository.findByStatus(status, pageable);
        } else if (claimType != null) {
            page = claimRepository.findByClaimType(claimType, pageable);
        } else if (patientId != null && !patientId.isBlank()) {
            page = claimRepository.findByPatientId(patientId, pageable);
        } else {
            page = claimRepository.findAll(pageable);
        }
        return page.map(claimMapper::toResponse);
    }

    @Override
    @Transactional
    public ClaimResponse reroute(UUID id) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new ClaimNotFoundException(id));
        log.info("Re-routing claim id={} (current status={}, destination={})",
                id, claim.getStatus(), claim.getRoutingDestination());

        // Reset destination so a no-match outcome correctly clears it.
        claim.setRoutingDestination(null);
        Claim routed = routingService.route(claim);
        return claimMapper.toResponse(claimRepository.save(routed));
    }

    @Override
    @Transactional(readOnly = true)
    public ClaimStatsResponse stats() {
        long total = claimRepository.count();
        return new ClaimStatsResponse(
                total,
                toMap(claimRepository.countByStatus()),
                toMap(claimRepository.countByClaimType()),
                toMap(claimRepository.countByDestination())
        );
    }

    private Map<String, Long> toMap(List<Object[]> rows) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            Object key = row[0];
            Long count = ((Number) row[1]).longValue();
            result.put(key == null ? "UNROUTED" : key.toString(), count);
        }
        return result;
    }
}
