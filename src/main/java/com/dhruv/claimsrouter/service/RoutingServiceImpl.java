package com.dhruv.claimsrouter.service;

import com.dhruv.claimsrouter.model.entity.Claim;
import com.dhruv.claimsrouter.model.entity.RoutingDecision;
import com.dhruv.claimsrouter.model.entity.RoutingRule;
import com.dhruv.claimsrouter.model.enums.ClaimStatus;
import com.dhruv.claimsrouter.model.enums.DecisionOutcome;
import com.dhruv.claimsrouter.repository.RoutingDecisionRepository;
import com.dhruv.claimsrouter.repository.RoutingRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutingServiceImpl implements RoutingService {

    public static final String ACTIVE_RULES_CACHE = "activeRoutingRules";

    private final RoutingRuleRepository ruleRepository;
    private final RoutingDecisionRepository decisionRepository;

    /**
     * Routes a claim by walking active rules in priority order. The first
     * matching rule wins. A {@link RoutingDecision} is always persisted —
     * even on no-match or error — so we have a complete audit trail.
     */
    @Override
    @Transactional
    public Claim route(Claim claim) {
        log.info("Routing claim id={} number={}", claim.getId(), claim.getClaimNumber());

        try {
            List<RoutingRule> rules = loadActiveRules();
            RoutingRule match = findFirstMatch(claim, rules);

            if (match == null) {
                log.info("No rule matched claim id={}", claim.getId());
                recordDecision(claim, null, null, DecisionOutcome.NO_MATCH,
                        "No active rule matched claim attributes");
                if (claim.getStatus() == ClaimStatus.NEW) {
                    claim.setStatus(ClaimStatus.VALIDATED);
                }
                return claim;
            }

            log.info("Claim id={} matched rule '{}' (priority={}) -> destination={}",
                    claim.getId(), match.getName(), match.getPriority(), match.getDestination());

            claim.setRoutingDestination(match.getDestination());
            claim.setStatus(ClaimStatus.ROUTED);
            recordDecision(claim, match, match.getDestination(), DecisionOutcome.ROUTED,
                    "Matched rule: " + match.getName());
            return claim;

        } catch (RuntimeException ex) {
            log.error("Routing failed for claim id={}: {}", claim.getId(), ex.getMessage(), ex);
            try {
                recordDecision(claim, null, null, DecisionOutcome.ERROR,
                        "Routing error: " + ex.getMessage());
            } catch (RuntimeException nested) {
                log.error("Failed to persist ERROR decision for claim id={}: {}",
                        claim.getId(), nested.getMessage(), nested);
            }
            throw ex;
        }
    }

    @Override
    @CacheEvict(value = ACTIVE_RULES_CACHE, allEntries = true)
    public void invalidateRuleCache() {
        log.debug("Routing rule cache invalidated");
    }

    /**
     * Cached lookup of active rules. Cache is small (low cardinality) and
     * refreshed any time a rule is created or deactivated.
     */
    @Cacheable(ACTIVE_RULES_CACHE)
    public List<RoutingRule> loadActiveRules() {
        log.debug("Loading active routing rules from database");
        return ruleRepository.findAllByActiveTrueOrderByPriorityAsc();
    }

    private RoutingRule findFirstMatch(Claim claim, List<RoutingRule> rules) {
        for (RoutingRule rule : rules) {
            if (matches(claim, rule)) {
                return rule;
            }
        }
        return null;
    }

    private boolean matches(Claim claim, RoutingRule rule) {
        if (!rule.isActive()) {
            return false;
        }
        if (rule.getClaimType() != null && rule.getClaimType() != claim.getClaimType()) {
            return false;
        }
        BigDecimal amount = claim.getAmount();
        if (rule.getMinAmount() != null && amount.compareTo(rule.getMinAmount()) < 0) {
            return false;
        }
        if (rule.getMaxAmount() != null && amount.compareTo(rule.getMaxAmount()) > 0) {
            return false;
        }
        if (rule.getRegion() != null && !rule.getRegion().isBlank()) {
            String providerRegion = claim.getProvider() != null ? claim.getProvider().getRegion() : null;
            if (!rule.getRegion().equalsIgnoreCase(providerRegion)) {
                return false;
            }
        }
        return true;
    }

    private void recordDecision(Claim claim,
                                RoutingRule rule,
                                String destination,
                                DecisionOutcome outcome,
                                String notes) {
        RoutingDecision decision = RoutingDecision.builder()
                .id(UUID.randomUUID())
                .claim(claim)
                .matchedRule(rule)
                .destination(destination)
                .decisionAt(LocalDateTime.now())
                .outcome(outcome)
                .notes(notes)
                .build();
        decisionRepository.save(decision);
    }
}
