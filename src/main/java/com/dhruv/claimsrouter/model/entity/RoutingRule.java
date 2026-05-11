package com.dhruv.claimsrouter.model.entity;

import com.dhruv.claimsrouter.model.enums.ClaimType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "routing_rules",
        indexes = {
                @Index(name = "idx_rule_priority", columnList = "priority"),
                @Index(name = "idx_rule_active", columnList = "active")
        }
)
public class RoutingRule extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "claim_type", length = 32)
    private ClaimType claimType;

    @Column(name = "min_amount", precision = 12, scale = 2)
    private BigDecimal minAmount;

    @Column(name = "max_amount", precision = 12, scale = 2)
    private BigDecimal maxAmount;

    @Column(name = "region", length = 64)
    private String region;

    @Column(name = "destination", nullable = false, length = 128)
    private String destination;

    @Column(name = "priority", nullable = false)
    private int priority;

    @Column(name = "active", nullable = false)
    private boolean active;
}
