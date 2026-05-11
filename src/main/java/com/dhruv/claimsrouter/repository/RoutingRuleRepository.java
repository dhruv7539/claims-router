package com.dhruv.claimsrouter.repository;

import com.dhruv.claimsrouter.model.entity.RoutingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoutingRuleRepository extends JpaRepository<RoutingRule, UUID> {

    List<RoutingRule> findAllByActiveTrueOrderByPriorityAsc();

    List<RoutingRule> findAllByOrderByPriorityAsc();
}
