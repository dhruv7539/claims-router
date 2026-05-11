package com.dhruv.claimsrouter.mapper;

import com.dhruv.claimsrouter.model.dto.RoutingRuleRequest;
import com.dhruv.claimsrouter.model.dto.RoutingRuleResponse;
import com.dhruv.claimsrouter.model.entity.RoutingRule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoutingRuleMapper {

    RoutingRuleResponse toResponse(RoutingRule rule);

    @Mapping(target = "id", ignore = true)
    RoutingRule toEntity(RoutingRuleRequest request);
}
