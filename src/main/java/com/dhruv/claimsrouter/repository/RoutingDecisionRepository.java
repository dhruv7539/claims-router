package com.dhruv.claimsrouter.repository;

import com.dhruv.claimsrouter.model.entity.RoutingDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoutingDecisionRepository extends JpaRepository<RoutingDecision, UUID> {

    List<RoutingDecision> findByClaimIdOrderByDecisionAtDesc(UUID claimId);
}
