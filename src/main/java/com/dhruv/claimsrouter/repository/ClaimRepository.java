package com.dhruv.claimsrouter.repository;

import com.dhruv.claimsrouter.model.entity.Claim;
import com.dhruv.claimsrouter.model.enums.ClaimStatus;
import com.dhruv.claimsrouter.model.enums.ClaimType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, UUID> {

    Optional<Claim> findByClaimNumber(String claimNumber);

    Page<Claim> findByStatus(ClaimStatus status, Pageable pageable);

    Page<Claim> findByClaimType(ClaimType claimType, Pageable pageable);

    Page<Claim> findByPatientId(String patientId, Pageable pageable);

    Page<Claim> findByStatusAndClaimType(ClaimStatus status, ClaimType claimType, Pageable pageable);

    @Query("SELECT c.status, COUNT(c) FROM Claim c GROUP BY c.status")
    List<Object[]> countByStatus();

    @Query("SELECT c.claimType, COUNT(c) FROM Claim c GROUP BY c.claimType")
    List<Object[]> countByClaimType();

    @Query("SELECT COALESCE(c.routingDestination, 'UNROUTED'), COUNT(c) " +
            "FROM Claim c GROUP BY c.routingDestination")
    List<Object[]> countByDestination();

    @Query("SELECT COUNT(c) FROM Claim c WHERE c.status = :status")
    long countByStatusValue(@Param("status") ClaimStatus status);
}
