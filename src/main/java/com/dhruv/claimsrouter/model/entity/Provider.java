package com.dhruv.claimsrouter.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "providers",
        indexes = {
                @Index(name = "idx_provider_npi", columnList = "npi", unique = true),
                @Index(name = "idx_provider_region", columnList = "region"),
                @Index(name = "idx_provider_active", columnList = "active")
        }
)
public class Provider extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "npi", nullable = false, unique = true, length = 10)
    private String npi;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "region", length = 64)
    private String region;

    @Column(name = "specialty", length = 128)
    private String specialty;

    @Column(name = "active", nullable = false)
    private boolean active;
}
