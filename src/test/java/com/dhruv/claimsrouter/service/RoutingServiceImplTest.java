package com.dhruv.claimsrouter.service;

import com.dhruv.claimsrouter.model.entity.Claim;
import com.dhruv.claimsrouter.model.entity.Provider;
import com.dhruv.claimsrouter.model.entity.RoutingDecision;
import com.dhruv.claimsrouter.model.entity.RoutingRule;
import com.dhruv.claimsrouter.model.enums.ClaimStatus;
import com.dhruv.claimsrouter.model.enums.ClaimType;
import com.dhruv.claimsrouter.model.enums.DecisionOutcome;
import com.dhruv.claimsrouter.repository.RoutingDecisionRepository;
import com.dhruv.claimsrouter.repository.RoutingRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoutingServiceImplTest {

    @Mock
    private RoutingRuleRepository ruleRepository;

    @Mock
    private RoutingDecisionRepository decisionRepository;

    @InjectMocks
    private RoutingServiceImpl service;

    private Claim baseClaim;

    @BeforeEach
    void setUp() {
        Provider provider = Provider.builder()
                .id(UUID.randomUUID())
                .npi("1000000001")
                .name("Test Provider")
                .region("WEST")
                .active(true)
                .build();

        baseClaim = Claim.builder()
                .id(UUID.randomUUID())
                .claimNumber("CLM-1")
                .patientId("PAT-1")
                .provider(provider)
                .claimType(ClaimType.MEDICAL)
                .amount(new BigDecimal("250.00"))
                .status(ClaimStatus.VALIDATED)
                .rawPayload("{}")
                .build();
    }

    @Test
    @DisplayName("matches by claim type and routes to destination")
    void matchesByClaimType() {
        RoutingRule rule = rule("medical", ClaimType.MEDICAL, null, null, null, "queue.medical", 10);
        when(ruleRepository.findAllByActiveTrueOrderByPriorityAsc()).thenReturn(List.of(rule));

        Claim result = service.route(baseClaim);

        assertThat(result.getStatus()).isEqualTo(ClaimStatus.ROUTED);
        assertThat(result.getRoutingDestination()).isEqualTo("queue.medical");
        verifyDecisionPersisted(DecisionOutcome.ROUTED);
    }

    @Test
    @DisplayName("matches by amount range")
    void matchesByAmount() {
        RoutingRule rule = rule("high-value", null,
                new BigDecimal("100"), new BigDecimal("1000"), null, "queue.high-value", 10);
        when(ruleRepository.findAllByActiveTrueOrderByPriorityAsc()).thenReturn(List.of(rule));

        Claim result = service.route(baseClaim);

        assertThat(result.getRoutingDestination()).isEqualTo("queue.high-value");
        assertThat(result.getStatus()).isEqualTo(ClaimStatus.ROUTED);
    }

    @Test
    @DisplayName("amount below minAmount is not a match")
    void belowMinAmountNoMatch() {
        RoutingRule rule = rule("only-big", null,
                new BigDecimal("10000"), null, null, "queue.big", 10);
        when(ruleRepository.findAllByActiveTrueOrderByPriorityAsc()).thenReturn(List.of(rule));

        Claim result = service.route(baseClaim);

        assertThat(result.getRoutingDestination()).isNull();
        assertThat(result.getStatus()).isEqualTo(ClaimStatus.VALIDATED);
        verifyDecisionPersisted(DecisionOutcome.NO_MATCH);
    }

    @Test
    @DisplayName("matches by region against the provider's region")
    void matchesByRegion() {
        RoutingRule rule = rule("west-region", null, null, null, "WEST", "queue.west", 10);
        when(ruleRepository.findAllByActiveTrueOrderByPriorityAsc()).thenReturn(List.of(rule));

        Claim result = service.route(baseClaim);

        assertThat(result.getRoutingDestination()).isEqualTo("queue.west");
    }

    @Test
    @DisplayName("region mismatch causes no match")
    void regionMismatchNoMatch() {
        RoutingRule rule = rule("east-only", null, null, null, "EAST", "queue.east", 10);
        when(ruleRepository.findAllByActiveTrueOrderByPriorityAsc()).thenReturn(List.of(rule));

        Claim result = service.route(baseClaim);

        assertThat(result.getRoutingDestination()).isNull();
        verifyDecisionPersisted(DecisionOutcome.NO_MATCH);
    }

    @Test
    @DisplayName("no active rules -> NO_MATCH and status stays VALIDATED")
    void noRulesNoMatch() {
        when(ruleRepository.findAllByActiveTrueOrderByPriorityAsc()).thenReturn(List.of());

        Claim result = service.route(baseClaim);

        assertThat(result.getRoutingDestination()).isNull();
        assertThat(result.getStatus()).isEqualTo(ClaimStatus.VALIDATED);
        verifyDecisionPersisted(DecisionOutcome.NO_MATCH);
    }

    @Test
    @DisplayName("priority ordering: lower priority number wins")
    void priorityOrdering() {
        RoutingRule low = rule("default-medical", ClaimType.MEDICAL, null, null, null, "queue.default", 100);
        RoutingRule high = rule("priority-medical", ClaimType.MEDICAL, null, null, null, "queue.priority", 10);
        // Repository contract is "ordered by priority asc"; mimic that.
        when(ruleRepository.findAllByActiveTrueOrderByPriorityAsc()).thenReturn(List.of(high, low));

        Claim result = service.route(baseClaim);

        assertThat(result.getRoutingDestination()).isEqualTo("queue.priority");
    }

    @Test
    @DisplayName("inactive rules are ignored even if returned")
    void inactiveRulesIgnored() {
        RoutingRule inactive = rule("disabled", ClaimType.MEDICAL, null, null, null, "queue.disabled", 1);
        inactive.setActive(false);
        RoutingRule active = rule("enabled", ClaimType.MEDICAL, null, null, null, "queue.enabled", 10);
        when(ruleRepository.findAllByActiveTrueOrderByPriorityAsc())
                .thenReturn(List.of(inactive, active));

        Claim result = service.route(baseClaim);

        assertThat(result.getRoutingDestination()).isEqualTo("queue.enabled");
    }

    @Test
    @DisplayName("exception during routing logs ERROR decision and rethrows")
    void exceptionLogsErrorDecision() {
        when(ruleRepository.findAllByActiveTrueOrderByPriorityAsc())
                .thenThrow(new RuntimeException("db down"));

        assertThatThrownBy(() -> service.route(baseClaim))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("db down");

        ArgumentCaptor<RoutingDecision> captor = ArgumentCaptor.forClass(RoutingDecision.class);
        verify(decisionRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getOutcome()).isEqualTo(DecisionOutcome.ERROR);
    }

    private RoutingRule rule(String name, ClaimType type, BigDecimal min, BigDecimal max,
                             String region, String destination, int priority) {
        return RoutingRule.builder()
                .id(UUID.randomUUID())
                .name(name)
                .claimType(type)
                .minAmount(min)
                .maxAmount(max)
                .region(region)
                .destination(destination)
                .priority(priority)
                .active(true)
                .build();
    }

    private void verifyDecisionPersisted(DecisionOutcome outcome) {
        ArgumentCaptor<RoutingDecision> captor = ArgumentCaptor.forClass(RoutingDecision.class);
        verify(decisionRepository).save(captor.capture());
        assertThat(captor.getValue().getOutcome()).isEqualTo(outcome);
    }
}
