package com.dhruv.claimsrouter.service;

import com.dhruv.claimsrouter.exception.InvalidClaimException;
import com.dhruv.claimsrouter.mapper.RoutingRuleMapper;
import com.dhruv.claimsrouter.model.dto.RoutingRuleRequest;
import com.dhruv.claimsrouter.model.dto.RoutingRuleResponse;
import com.dhruv.claimsrouter.model.entity.RoutingRule;
import com.dhruv.claimsrouter.repository.RoutingRuleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
class RoutingRuleServiceImplTest {

    @Mock
    private RoutingRuleRepository ruleRepository;

    @Mock
    private RoutingRuleMapper ruleMapper;

    @Mock
    private RoutingService routingService;

    @InjectMocks
    private RoutingRuleServiceImpl service;

    @Test
    @DisplayName("create rule persists, evicts cache, and returns response")
    void createRule() {
        RoutingRuleRequest request = new RoutingRuleRequest(
                "test", null, null, null, null, "queue.test", 10, true);
        RoutingRule entity = RoutingRule.builder().build();
        when(ruleMapper.toEntity(request)).thenReturn(entity);
        when(ruleRepository.save(any(RoutingRule.class))).thenAnswer(inv -> inv.getArgument(0));
        RoutingRuleResponse mapped = sampleResponse();
        when(ruleMapper.toResponse(any(RoutingRule.class))).thenReturn(mapped);

        RoutingRuleResponse result = service.create(request);

        assertThat(result).isSameAs(mapped);
        verify(routingService).invalidateRuleCache();
    }

    @Test
    @DisplayName("create rule rejects invalid amount range")
    void createRuleInvalidAmountRange() {
        RoutingRuleRequest request = new RoutingRuleRequest(
                "bad", null, new BigDecimal("100"), new BigDecimal("10"), null, "queue.bad", 5, true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(InvalidClaimException.class);
    }

    @Test
    @DisplayName("deactivate sets active=false and evicts cache")
    void deactivate() {
        UUID id = UUID.randomUUID();
        RoutingRule rule = RoutingRule.builder().id(id).active(true).build();
        when(ruleRepository.findById(id)).thenReturn(Optional.of(rule));
        when(ruleRepository.save(rule)).thenReturn(rule);
        when(ruleMapper.toResponse(rule)).thenReturn(sampleResponse());

        service.deactivate(id);

        assertThat(rule.isActive()).isFalse();
        verify(routingService).invalidateRuleCache();
    }

    @Test
    @DisplayName("deactivate of missing rule throws")
    void deactivateMissing() {
        UUID id = UUID.randomUUID();
        when(ruleRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deactivate(id))
                .isInstanceOf(InvalidClaimException.class);
    }

    @Test
    @DisplayName("listAllByPriority returns mapped list")
    void listAll() {
        RoutingRule rule = RoutingRule.builder().id(UUID.randomUUID()).build();
        when(ruleRepository.findAllByOrderByPriorityAsc()).thenReturn(List.of(rule));
        when(ruleMapper.toResponse(rule)).thenReturn(sampleResponse());

        assertThat(service.listAllByPriority()).hasSize(1);
    }

    private RoutingRuleResponse sampleResponse() {
        return new RoutingRuleResponse(
                UUID.randomUUID(),
                "test",
                null,
                null,
                null,
                null,
                "queue.test",
                10,
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
