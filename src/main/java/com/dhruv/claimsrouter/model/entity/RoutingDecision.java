package com.dhruv.claimsrouter.model.entity;

import com.dhruv.claimsrouter.model.enums.DecisionOutcome;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable audit record of a routing attempt for a claim.
 * Each decision is appended; existing decisions are never modified.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "routing_decisions",
        indexes = {
                @Index(name = "idx_decision_claim", columnList = "claim_id"),
                @Index(name = "idx_decision_rule", columnList = "matched_rule_id"),
                @Index(name = "idx_decision_claim_time", columnList = "claim_id,decision_at")
        }
)
public class RoutingDecision {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matched_rule_id")
    private RoutingRule matchedRule;

    @Column(name = "destination", length = 128)
    private String destination;

    @Column(name = "decision_at", nullable = false)
    private LocalDateTime decisionAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false, length = 16)
    private DecisionOutcome outcome;

    @Column(name = "notes", length = 1024)
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
