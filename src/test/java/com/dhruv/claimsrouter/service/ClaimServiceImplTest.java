package com.dhruv.claimsrouter.service;

import com.dhruv.claimsrouter.exception.ClaimNotFoundException;
import com.dhruv.claimsrouter.exception.InvalidClaimException;
import com.dhruv.claimsrouter.exception.ProviderNotFoundException;
import com.dhruv.claimsrouter.mapper.ClaimMapper;
import com.dhruv.claimsrouter.model.dto.ClaimRequest;
import com.dhruv.claimsrouter.model.dto.ClaimResponse;
import com.dhruv.claimsrouter.model.entity.Claim;
import com.dhruv.claimsrouter.model.entity.Provider;
import com.dhruv.claimsrouter.model.enums.ClaimStatus;
import com.dhruv.claimsrouter.model.enums.ClaimType;
import com.dhruv.claimsrouter.repository.ClaimRepository;
import com.dhruv.claimsrouter.repository.ProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimServiceImplTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private ProviderRepository providerRepository;

    @Mock
    private RoutingService routingService;

    @Mock
    private ClaimMapper claimMapper;

    @InjectMocks
    private ClaimServiceImpl service;

    private Provider provider;
    private ClaimRequest request;

    @BeforeEach
    void setUp() {
        provider = Provider.builder()
                .id(UUID.randomUUID())
                .npi("1000000001")
                .name("Test")
                .region("WEST")
                .active(true)
                .build();

        request = new ClaimRequest(
                "CLM-1",
                "PAT-1",
                "1000000001",
                ClaimType.MEDICAL,
                new BigDecimal("250.00"),
                LocalDate.now().minusDays(1),
                "{\"src\":\"test\"}"
        );
    }

    @Test
    @DisplayName("submit persists claim, runs routing, and returns mapped response")
    void submitHappyPath() {
        when(claimRepository.findByClaimNumber("CLM-1")).thenReturn(Optional.empty());
        when(providerRepository.findByNpi("1000000001")).thenReturn(Optional.of(provider));
        when(claimRepository.save(any(Claim.class))).thenAnswer(inv -> inv.getArgument(0));
        when(routingService.route(any(Claim.class))).thenAnswer(inv -> {
            Claim c = inv.getArgument(0);
            c.setStatus(ClaimStatus.ROUTED);
            c.setRoutingDestination("queue.medical");
            return c;
        });
        ClaimResponse mapped = sampleResponse();
        when(claimMapper.toResponse(any(Claim.class))).thenReturn(mapped);

        ClaimResponse result = service.submit(request);

        assertThat(result).isSameAs(mapped);
        verify(routingService).route(any(Claim.class));
    }

    @Test
    @DisplayName("submit fails when claim number already exists")
    void submitDuplicateClaimNumber() {
        when(claimRepository.findByClaimNumber("CLM-1"))
                .thenReturn(Optional.of(Claim.builder().id(UUID.randomUUID()).build()));

        assertThatThrownBy(() -> service.submit(request))
                .isInstanceOf(InvalidClaimException.class);
    }

    @Test
    @DisplayName("submit fails when provider does not exist")
    void submitMissingProvider() {
        when(claimRepository.findByClaimNumber("CLM-1")).thenReturn(Optional.empty());
        when(providerRepository.findByNpi("1000000001")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.submit(request))
                .isInstanceOf(ProviderNotFoundException.class);
    }

    @Test
    @DisplayName("submit fails when provider is inactive")
    void submitInactiveProvider() {
        provider.setActive(false);
        when(claimRepository.findByClaimNumber("CLM-1")).thenReturn(Optional.empty());
        when(providerRepository.findByNpi("1000000001")).thenReturn(Optional.of(provider));

        assertThatThrownBy(() -> service.submit(request))
                .isInstanceOf(InvalidClaimException.class);
    }

    @Test
    @DisplayName("get returns mapped response when claim exists")
    void getExisting() {
        UUID id = UUID.randomUUID();
        Claim claim = Claim.builder().id(id).build();
        when(claimRepository.findById(id)).thenReturn(Optional.of(claim));
        ClaimResponse mapped = sampleResponse();
        when(claimMapper.toResponse(claim)).thenReturn(mapped);

        assertThat(service.get(id)).isSameAs(mapped);
    }

    @Test
    @DisplayName("get throws ClaimNotFoundException when missing")
    void getMissing() {
        UUID id = UUID.randomUUID();
        when(claimRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(id))
                .isInstanceOf(ClaimNotFoundException.class);
    }

    @Test
    @DisplayName("list with no filters falls back to findAll")
    void listFindAll() {
        Pageable pageable = Pageable.ofSize(10);
        Page<Claim> page = new PageImpl<>(List.of(Claim.builder().id(UUID.randomUUID()).build()));
        when(claimRepository.findAll(pageable)).thenReturn(page);
        when(claimMapper.toResponse(any(Claim.class))).thenReturn(sampleResponse());

        Page<ClaimResponse> result = service.list(null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("reroute runs the routing service for an existing claim")
    void rerouteExisting() {
        UUID id = UUID.randomUUID();
        Claim claim = Claim.builder().id(id).status(ClaimStatus.VALIDATED).build();
        when(claimRepository.findById(id)).thenReturn(Optional.of(claim));
        when(routingService.route(claim)).thenReturn(claim);
        when(claimRepository.save(claim)).thenReturn(claim);
        when(claimMapper.toResponse(claim)).thenReturn(sampleResponse());

        service.reroute(id);

        verify(routingService).route(claim);
    }

    private ClaimResponse sampleResponse() {
        return new ClaimResponse(
                UUID.randomUUID(),
                "CLM-1",
                "PAT-1",
                provider.getId(),
                provider.getNpi(),
                provider.getName(),
                ClaimType.MEDICAL,
                new BigDecimal("250.00"),
                LocalDate.now(),
                LocalDateTime.now(),
                ClaimStatus.ROUTED,
                "queue.medical",
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
