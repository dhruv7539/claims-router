package com.dhruv.claimsrouter.model.entity;

import com.dhruv.claimsrouter.model.enums.ClaimStatus;
import com.dhruv.claimsrouter.model.enums.ClaimType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "claims",
        indexes = {
                @Index(name = "idx_claim_number", columnList = "claim_number", unique = true),
                @Index(name = "idx_claim_patient", columnList = "patient_id"),
                @Index(name = "idx_claim_provider", columnList = "provider_id"),
                @Index(name = "idx_claim_status", columnList = "status"),
                @Index(name = "idx_claim_status_type", columnList = "status,claim_type")
        }
)
public class Claim extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "claim_number", nullable = false, unique = true, length = 64)
    private String claimNumber;

    @Column(name = "patient_id", nullable = false, length = 64)
    private String patientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "claim_type", nullable = false, length = 32)
    private ClaimType claimType;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "service_date", nullable = false)
    private LocalDate serviceDate;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ClaimStatus status;

    @Column(name = "routing_destination", length = 128)
    private String routingDestination;

    @Column(name = "raw_payload", nullable = false, columnDefinition = "TEXT")
    private String rawPayload;

    @Column(name = "rejection_reason", length = 512)
    private String rejectionReason;

    @PrePersist
    void prePersist() {
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = ClaimStatus.NEW;
        }
    }
}
