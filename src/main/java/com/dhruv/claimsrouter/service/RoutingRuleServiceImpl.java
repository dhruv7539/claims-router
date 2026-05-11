package com.dhruv.claimsrouter.service;

import com.dhruv.claimsrouter.exception.InvalidClaimException;
import com.dhruv.claimsrouter.mapper.RoutingRuleMapper;
import com.dhruv.claimsrouter.model.dto.RoutingRuleRequest;
import com.dhruv.claimsrouter.model.dto.RoutingRuleResponse;
import com.dhruv.claimsrouter.model.entity.RoutingRule;
import com.dhruv.claimsrouter.repository.RoutingRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutingRuleServiceImpl implements RoutingRuleService {

    private final RoutingRuleRepository ruleRepository;
    private final RoutingRuleMapper ruleMapper;
    private final RoutingService routingService;

    @Override
    @Transactional
    public RoutingRuleResponse create(RoutingRuleRequest request) {
        validateAmountRange(request.minAmount(), request.maxAmount());

        RoutingRule entity = ruleMapper.toEntity(request);
        entity.setId(UUID.randomUUID());
        entity.setActive(Boolean.TRUE.equals(request.active()));
        RoutingRule saved = ruleRepository.save(entity);
        routingService.invalidateRuleCache();

        log.info("Created routing rule id={} name={} priority={}",
                saved.getId(), saved.getName(), saved.getPriority());
        return ruleMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoutingRuleResponse> listAllByPriority() {
        return ruleRepository.findAllByOrderByPriorityAsc().stream()
                .map(ruleMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public RoutingRuleResponse deactivate(UUID id) {
        RoutingRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new InvalidClaimException("Routing rule not found: " + id));
        rule.setActive(false);
        RoutingRule saved = ruleRepository.save(rule);
        routingService.invalidateRuleCache();
        log.info("Deactivated routing rule id={} name={}", saved.getId(), saved.getName());
        return ruleMapper.toResponse(saved);
    }

    private void validateAmountRange(BigDecimal min, BigDecimal max) {
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new InvalidClaimException("minAmount must not exceed maxAmount");
        }
    }
}
