package com.dhruv.claimsrouter.service;

import com.dhruv.claimsrouter.model.dto.RoutingRuleRequest;
import com.dhruv.claimsrouter.model.dto.RoutingRuleResponse;

import java.util.List;
import java.util.UUID;

public interface RoutingRuleService {

    RoutingRuleResponse create(RoutingRuleRequest request);

    List<RoutingRuleResponse> listAllByPriority();

    RoutingRuleResponse deactivate(UUID id);
}
