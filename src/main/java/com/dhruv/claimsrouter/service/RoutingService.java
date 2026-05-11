package com.dhruv.claimsrouter.service;

import com.dhruv.claimsrouter.model.entity.Claim;

public interface RoutingService {

    /**
     * Runs the routing engine for a claim. Updates the claim's status and
     * destination, persists a {@link com.dhruv.claimsrouter.model.entity.RoutingDecision}
     * audit record, and returns the (possibly updated) claim.
     */
    Claim route(Claim claim);

    /** Invalidates the in-memory cache of active routing rules. */
    void invalidateRuleCache();
}
